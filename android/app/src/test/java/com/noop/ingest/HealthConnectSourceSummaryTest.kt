package com.noop.ingest

import org.junit.Assert.assertEquals
import org.junit.Test

class HealthConnectSourceSummaryTest {
    @Test
    fun emptyImportHasNoClaimedSources() {
        assertEquals(emptyList<String>(), HealthConnectSourceSummary.displayNames(emptySet()))
    }

    @Test
    fun identifiesWritingAppsWithoutClaimingAWatchModel() {
        assertEquals(
            listOf("Fitbit", "Garmin Connect", "Samsung Health"),
            HealthConnectSourceSummary.displayNames(
                listOf(
                    "com.sec.android.app.shealth",
                    "com.garmin.android.apps.connectmobile",
                    "com.fitbit.FitbitMobile",
                    "com.sec.android.app.shealth",
                ),
            ),
        )
    }

    @Test
    fun unknownAppsRemainIdentifiableAndResultsAreStable() {
        assertEquals(
            listOf("Samsung Health", "org.example.unknown"),
            HealthConnectSourceSummary.displayNames(
                listOf("org.example.unknown", "", "  ", " com.sec.android.app.shealth "),
            ),
        )
    }
}
