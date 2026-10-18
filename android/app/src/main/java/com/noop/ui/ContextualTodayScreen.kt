package com.noop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale
import kotlin.math.roundToInt

/**
 * An experimental, on-device presentation of NOOP's EXISTING measurements.
 * No new scoring, inference, sync, storage or network access. Only one contextual
 * action is surfaced at a time; dismissals are local to this composition, not stored.
 */
internal enum class ContextualPrompt {
    ACTIVE_SESSION, SCANNING, CONNECT_STRAP, WAITING_FOR_SCORE, REVIEW_DAY
}

/** Stable precedence: a running session cannot be displaced by a transient BLE disconnect. */
internal fun contextualPrompt(
    activeWorkout: Boolean,
    connected: Boolean,
    scanning: Boolean,
    hasCharge: Boolean,
): ContextualPrompt = when {
    activeWorkout -> ContextualPrompt.ACTIVE_SESSION
    scanning -> ContextualPrompt.SCANNING
    !connected -> ContextualPrompt.CONNECT_STRAP
    !hasCharge -> ContextualPrompt.WAITING_FOR_SCORE
    else -> ContextualPrompt.REVIEW_DAY
}

/** Never turn an unavailable or invalid recovery estimate into a reassuring number. */
internal fun visibleChargePercent(score: Double?): Int? =
    score?.takeIf { it.isFinite() && it in 0.0..100.0 }?.roundToInt()

private data class PromptCopy(val title: String, val detail: String, val action: String)

@Composable
internal fun ContextualTodayScreen(
    viewModel: AppViewModel,
    onOpenDevices: () -> Unit,
    onOpenSleep: () -> Unit,
    onOpenTrends: () -> Unit,
    onOpenLive: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenFullDashboard: () -> Unit,
    onResumeWorkout: () -> Unit,
) {
    val metric by viewModel.today.collectAsStateWithLifecycle()
    val live by viewModel.live.collectAsStateWithLifecycle()
    val workout by viewModel.activeWorkout.collectAsStateWithLifecycle()
    val charge = visibleChargePercent(metric?.recovery)
    val prompt = contextualPrompt(
        activeWorkout = workout != null,
        connected = live.connected,
        scanning = live.scanning,
        hasCharge = charge != null,
    )
    // Deliberately not rememberSaveable or SharedPreferences: context is fleeting.
    var dismissedPrompt by remember { mutableStateOf<ContextualPrompt?>(null) }
    val copy = when (prompt) {
        ContextualPrompt.ACTIVE_SESSION ->
            PromptCopy("Session in progress", "Your active session is ready to resume.", "Resume")
        ContextualPrompt.SCANNING ->
            PromptCopy("Finding your strap", "Connection is in progress.", "Devices")
        ContextualPrompt.CONNECT_STRAP ->
            PromptCopy("Strap offline", "Open devices to check your connection.", "Devices")
        ContextualPrompt.WAITING_FOR_SCORE ->
            PromptCopy("Awaiting a score", "Recorded sleep and recovery will appear after processing.", "Sleep")
        ContextualPrompt.REVIEW_DAY ->
            PromptCopy("Your readings are ready", "See how your recent days compare.", "Trends")
    }
    val openPrompt: () -> Unit = when (prompt) {
        ContextualPrompt.ACTIVE_SESSION -> onResumeWorkout
        ContextualPrompt.SCANNING, ContextualPrompt.CONNECT_STRAP -> onOpenDevices
        ContextualPrompt.WAITING_FOR_SCORE -> onOpenSleep
        ContextualPrompt.REVIEW_DAY -> onOpenTrends
    }
    val effort = metric?.strain?.takeIf { it.isFinite() && it >= 0.0 }
        ?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "—"
    val sleep = metric?.totalSleepMin?.takeIf { it.isFinite() && it > 0.0 }
        ?.let { String.format(Locale.getDefault(), "%.1f h", it / 60.0) } ?: "—"
    val latestHeartRate = live.heartRate?.takeIf { live.connected && it > 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.surfaceBase)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "N O O P",
                style = MaterialTheme.typography.labelMedium,
                color = Palette.textSecondary,
                letterSpacing = 2.sp,
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenDevices)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Box(
                    Modifier.size(6.dp).background(
                        if (live.connected) Palette.chargeColor else Palette.textTertiary,
                        CircleShape,
                    ),
                )
                Text(
                    if (live.connected) "Connected" else if (live.scanning) "Searching" else "Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = Palette.textSecondary,
                )
            }
        }

        Spacer(Modifier.height(52.dp))
        Text(
            "Today",
            style = MaterialTheme.typography.titleLarge,
            color = Palette.textSecondary,
        )
        Spacer(Modifier.height(9.dp))
        Text(
            charge?.let { it.toString() + "%" } ?: "—",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Light),
            color = Palette.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            when {
                charge != null -> "Charge · " + (metric?.day ?: "")
                metric != null -> "Charge not available · " + metric.day
                else -> "No score recorded yet"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Palette.textTertiary,
        )

        Spacer(Modifier.height(36.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ContextMetric(
                label = if (latestHeartRate != null) "Heart rate" else "Effort",
                value = latestHeartRate?.let { it.toString() + " bpm" } ?: effort,
                modifier = Modifier.weight(1f),
                onClick = if (latestHeartRate != null) onOpenLive else onOpenTrends,
            )
            ContextMetric(
                label = "Sleep",
                value = sleep,
                modifier = Modifier.weight(1f),
                onClick = onOpenSleep,
            )
        }

        Spacer(Modifier.height(44.dp))
        if (dismissedPrompt != prompt) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Palette.surfaceRaised,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "IN CONTEXT",
                            style = MaterialTheme.typography.labelSmall,
                            color = Palette.textTertiary,
                            letterSpacing = 1.sp,
                        )
                        IconButton(
                            onClick = { dismissedPrompt = prompt },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Dismiss this suggestion",
                                tint = Palette.textSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Text(
                        copy.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Palette.textPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        copy.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Palette.textSecondary,
                    )
                    Spacer(Modifier.height(6.dp))
                    TextButton(onClick = openPrompt) {
                        Text(copy.action)
                        Spacer(Modifier.width(2.dp))
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onOpenJournal) { Text("Journal") }
            TextButton(onClick = onOpenFullDashboard) { Text("Full dashboard") }
        }
    }
}

@Composable
private fun ContextMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Palette.textTertiary)
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = Palette.textPrimary)
    }
}
