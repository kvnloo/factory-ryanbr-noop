package com.noop.ingest

/**
 * Readable app provenance for Health Connect imports.
 *
 * A Health Connect DataOrigin names the APP that wrote a sample, not the sensor that
 * measured it. A Samsung Health row, for example, is not proof of a Galaxy Watch.
 * Keep unknown packages visible instead of guessing their device or manufacturer.
 */
internal object HealthConnectSourceSummary {
    private val appNames = mapOf(
        "com.sec.android.app.shealth" to "Samsung Health",
        "com.fitbit.FitbitMobile" to "Fitbit",
        "com.garmin.android.apps.connectmobile" to "Garmin Connect",
        "com.google.android.apps.fitness" to "Google Fit",
        "com.ouraring.oura" to "Oura",
        "com.strava" to "Strava",
    )

    fun displayNames(packages: Collection<String>): List<String> =
        packages.asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { appNames[it] ?: it }
            .distinct()
            .sorted()
            .toList()
}
