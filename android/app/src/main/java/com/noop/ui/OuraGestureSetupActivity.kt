package com.noop.ui

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.EditText
import android.text.InputType
import android.view.WindowManager
import com.noop.ble.gesture.OuraGestureBroker
import java.util.concurrent.Executors
import com.noop.ble.gesture.OuraTapDetector
import com.noop.NoopApplication
import com.noop.R
import com.noop.ble.OuraZ0LiveHandoff

/**
 * Opt-in bounded gesture onboarding on NOOP's already-connected Oura Gen 3 BLE source.
 * Launched by z0live, but no microphone/audio/network permission belongs to NOOP.
 * The app remains user-disarmable, and the ring stream always has a hard timeout.
 */
class OuraGestureSetupActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var progress: TextView
    private lateinit var arm: Button
    private lateinit var stop: Button
    private val broker by lazy { OuraGestureBroker(this) }
    private val export = Executors.newSingleThreadExecutor()
    private var claiming = false
    private var pairingInProgress = false
    // Read by the single-thread export worker; Stop invalidates queued events immediately.
    @Volatile private var claimGeneration = 0
    private var armed = false
    private var visible = false
    private var doubles = 0
    private var sequence = 0L
    private var capability: PendingIntent? = null
    private val handler = Handler(Looper.getMainLooper())
    private val endNotice = Runnable {
        disarm()
        status.text = getString(R.string.oura_broker_expired)
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        capability = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(EXTRA_CAPABILITY, PendingIntent::class.java)
        } else {
            @Suppress("DEPRECATION")
            (intent.getParcelableExtra(EXTRA_CAPABILITY) as? PendingIntent)
        }?.takeIf { it.creatorPackage == LAWNCHAIR_PACKAGE }
        // PendingIntent is process-local onboarding authority; never save or log it.
        intent.replaceExtras(Bundle())

        val scroll = ScrollView(this)
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
            setBackgroundColor(Color.rgb(14, 19, 22))
        }
        scroll.addView(column)
        fun label(value: String, size: Float): TextView =
            TextView(this).also {
                it.text = value
                it.textSize = size
                it.setTextColor(Color.WHITE)
                it.setPadding(0, dp(6), 0, dp(10))
                column.addView(it)
            }
        fun button(value: String, click: () -> Unit): Button =
            Button(this).also {
                it.text = value
                it.setOnClickListener { click() }
                column.addView(it, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        label("Oura ring → z0live", 24f)
        label(
            "1. In NOOP → Devices, connect your existing Oura Ring Gen 3. Do NOT reset or rekey it.\n" +
            "2. In Lawnchair z0live, pair and start voice yourself, then mute it.\n" +
            "3. Arm 2 minutes of experimental ring motion. Double tap the ring 3 times to verify recognition.\n" +
            "4. Another double tap toggles mute in that already-running voice session.\n" +
            "Stop anytime. Gesture settings are OFF by default and this never records microphone audio.",
            15f
        )
        status = label(
            if (capability == null) "z0live voice not armed: ring taps can only open visible controls."
            else "Session gesture capability received; starts only after you arm the ring.", 16f)
        progress = label("Recognized double taps: 0 / 3", 18f)
        arm = button("Arm ring for 2 minutes") { begin() }
        stop = button("Stop and turn gestures off") { disarm() }.apply { isEnabled = false }
        button("Open NOOP devices / connect ring") {
            disarm()
            startActivity(Intent(this, MainActivity::class.java))
        }
        val pairing = EditText(this).apply {
            hint = getString(R.string.oura_broker_pair_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }
        column.addView(pairing)
        button(getString(R.string.oura_broker_pair)) {
            disarm()
            pairingInProgress = true
            arm.isEnabled = false
            val link = pairing.text.toString()
            pairing.text.clear()
            val generation = claimGeneration
            // Disarm queues the old remote lease release. Pair on the SAME
            // worker after it, so rotating credentials cannot strand that lease
            // or race with an earlier gesture send. Keystore work also stays off UI.
            export.execute {
                if (generation != claimGeneration) return@execute
                val paired = runCatching {
                    broker.release()
                    broker.pair(link)
                }.isSuccess
                handler.post {
                    if (generation == claimGeneration && !isDestroyed) {
                        pairingInProgress = false
                        arm.isEnabled = true
                        status.text = getString(
                            if (paired) R.string.oura_broker_paired else R.string.oura_broker_failed
                        )
                    }
                }
            }
        }
        button(getString(R.string.oura_broker_forget)) { disarm(); export.execute { runCatching { broker.release() }; broker.forget() } }
        button("Open z0live voice controls") { openControls() }
        button("Close setup") { disarm(); finish() }
        setContentView(scroll)
    }

    private fun dp(n: Int): Int = (n * resources.displayMetrics.density + 0.5f).toInt()
    private val coordinator get() = (application as NoopApplication).sourceCoordinator

    private fun begin(leaseReady: Boolean = false) {
        // Never start a local-only gesture capture while the optional broker
        // credentials are still being replaced on the export worker.
        if (armed || claiming || pairingInProgress) return
        if (Build.VERSION.SDK_INT >= 31 &&
            (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)) {
            status.text = "Allow Bluetooth permissions for NOOP first, then arm the ring."
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN), BLUETOOTH_REQUEST)
            return
        }
        if (broker.configured && !leaseReady) {
            claiming = true
            stop.isEnabled = true
            val generation = ++claimGeneration
            export.execute {
                val ok = runCatching { broker.claim(120) }.isSuccess
                handler.post {
                    claiming = false
                    if (generation != claimGeneration || isDestroyed) {
                        if (!export.isShutdown) export.execute { runCatching { broker.release() } }
                    } else if (ok) begin(true)
                    else status.text = getString(R.string.oura_broker_failed)
                }
            }
            return
        }
        doubles = 0
        sequence = 0
        progress.text = "Recognized double taps: 0 / 3"
        val ok = coordinator.startActiveOuraGestureCapture(120) { gesture ->
            if (gesture == OuraTapDetector.Gesture.DOUBLE_TAP) {
                doubles++
                progress.text = "Recognized double taps: ${minOf(doubles, 3)} / 3"
                if (doubles <= 3) {
                    status.text = if (doubles < 3) "Tap the ring twice again (${3 - doubles} trial(s) remain)."
                        else "Three trials detected. Next double tap will invoke z0live."
                } else {
                    deliverGesture("double_tap")
                }
            } else if (gesture == OuraTapDetector.Gesture.TRIPLE_TAP && doubles >= 3) {
                deliverGesture("triple_tap")
            }
        }
        if (!ok) {
            export.execute { runCatching { broker.release() } }
            status.text = "NOOP needs an authenticated, actively connected Gen 3 ring. " +
                "Open NOOP → Devices, select Oura Ring 3 and reconnect, then retry."
            return
        }
        armed = true
        arm.isEnabled = false
        stop.isEnabled = true
        status.text = "Oura motion enabled for 120 seconds. Double tap the ring 3 times."
        handler.removeCallbacks(endNotice)
        handler.postDelayed(endNotice, 120_000)
    }

    private fun deliverGesture(gesture: String) {
        if (broker.configured) {
            val generation = claimGeneration
            export.execute {
                // Stop/re-pair increments generation on the UI thread. Queued
                // gestures from an expired or disarmed capture must not toggle
                // the remote voice session after the owner pressed Stop.
                if (generation != claimGeneration) return@execute
                val ok = runCatching { broker.send(gesture) }.getOrDefault(false)
                handler.post {
                    if (generation == claimGeneration && !isDestroyed) {
                        status.text = getString(if (ok) R.string.oura_broker_sent else R.string.oura_broker_failed)
                    }
                }
            }
            return // Never fire capability AND broker; an ambiguous request must not toggle twice.
        }
        if (gesture != "double_tap") return
        val token = capability
        if (token != null) {
            sequence++
            val data = Intent()
                .putExtra(EXTRA_GESTURE, "DOUBLE_TAP")
                .putExtra(EXTRA_SEQUENCE, sequence)
                .putExtra(EXTRA_TIMESTAMP, SystemClock.elapsedRealtime())
            try {
                token.send(this, 0, data)
                status.text = "Double tap delivered to z0live. Check its Mute status."
                return
            } catch (_: PendingIntent.CanceledException) {
                capability = null
            }
        }
        if (visible) {
            openControls() // The exported link opens controls only; never microphone.
        } else {
            notifyToOpenControls()
        }
    }

    private fun openControls() {
        runCatching { startActivity(OuraZ0LiveHandoff.visibleControlsIntent()) }
            .onFailure { status.text = "Install the matching experimental Lawnchair z0live APK first." }
    }

    private fun notifyToOpenControls() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val mgr = getSystemService(NotificationManager::class.java)
        val channel = "oura_z0live_setup"
        mgr.createNotificationChannel(NotificationChannel(channel, getString(R.string.oura_z0live_notification_channel), NotificationManager.IMPORTANCE_HIGH))
        val pending = PendingIntent.getActivity(this, 317,
            OuraZ0LiveHandoff.visibleControlsIntent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        mgr.notify(317, Notification.Builder(this, channel)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle(getString(R.string.oura_z0live_notification_title))
            .setContentText(getString(R.string.oura_z0live_notification_body))
            .setContentIntent(pending).setAutoCancel(true).build())
    }

    private fun disarm() {
        claimGeneration++
        claiming = false
        pairingInProgress = false
        export.execute { runCatching { broker.release() } }
        if (armed) coordinator.stopActiveOuraGestureCapture()
        armed = false
        handler.removeCallbacks(endNotice)
        if (::arm.isInitialized) arm.isEnabled = true
        if (::stop.isInitialized) stop.isEnabled = false
        if (::status.isInitialized) status.text = "Gesture capture OFF."
    }

    override fun onResume() { super.onResume(); visible = true }
    override fun onPause() { visible = false; super.onPause() }
    override fun onDestroy() {
        disarm()
        export.execute { runCatching { broker.release() }; export.shutdown() }
        super.onDestroy()
    }


    companion object {
        const val EXTRA_CAPABILITY = "app.lawnchair.zer0.voice.z0live.CAPABILITY"
        const val EXTRA_GESTURE = "app.lawnchair.zer0.voice.z0live.OURA_GESTURE"
        const val EXTRA_SEQUENCE = "app.lawnchair.zer0.voice.z0live.OURA_SEQUENCE"
        const val EXTRA_TIMESTAMP = "app.lawnchair.zer0.voice.z0live.OURA_TIMESTAMP"
        const val LAWNCHAIR_PACKAGE = "app.lawnchair.play.debug"
        const val BLUETOOTH_REQUEST = 714
    }
}
