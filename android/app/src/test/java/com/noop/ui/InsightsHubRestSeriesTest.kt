package com.noop.ui

import com.noop.data.DailyMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Fork-only regression for the Insights Hub "Rest" outcome.
 *
 * Rest is a stored / source-resolved composite on a 0..100 scale, not the
 * DailyMetric.efficiency fraction on a 0..1 scale. Test numbers are synthetic.
 */
class InsightsHubRestSeriesTest {
    @Test fun restUsesResolvedCompositeInsteadOfEfficiency() {
        val day = DailyMetric(
            deviceId = "whoop-test-noop",
            day = "2026-10-01",
            totalSleepMin = 420.0,
            efficiency = 0.94,
            recovery = 67.0,
            avgHrv = 42.0,
            restingHr = 51,
        )
        val actualRest = 72.0 // Representative, synthetic resolved sleep_performance.
        val series = insightOutcomeSeries(listOf(day), mapOf(day.day to actualRest))

        assertEquals("sleep_performance", InsightsOutcome.Sleep.key)
        assertNull("Rest must not pick a daily efficiency column", InsightsOutcome.Sleep.pick(day))
        assertEquals(actualRest, requireNotNull(series["sleep_performance"]?.get(day.day)), 1e-9)
        assertEquals(67.0, requireNotNull(series["recovery"]?.get(day.day)), 1e-9)
        assertEquals(42.0, requireNotNull(series["hrv"]?.get(day.day)), 1e-9)
        assertEquals(51.0, requireNotNull(series["rhr"]?.get(day.day)), 1e-9)
    }

    @Test fun absentRestDoesNotBecomeRawEfficiency() {
        val day = DailyMetric(
            deviceId = "whoop-test-noop",
            day = "2026-10-02",
            totalSleepMin = 480.0,
            efficiency = 0.99,
        )
        val series = insightOutcomeSeries(listOf(day), emptyMap())
        assertTrue(series["sleep_performance"].isNullOrEmpty())
    }

    @Test fun theResolvedSeriesCanSupplyImportOnlyDays() {
        val day = DailyMetric(deviceId = "whoop-test-noop", day = "2026-10-02")
        val rest = mapOf("2026-10-01" to 81.0, "2026-10-02" to 73.0)
        val series = insightOutcomeSeries(listOf(day), rest)
        assertEquals(rest, series["sleep_performance"])
    }
}
