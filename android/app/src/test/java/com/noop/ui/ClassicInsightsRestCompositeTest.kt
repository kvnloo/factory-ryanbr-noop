package com.noop.ui

import com.noop.analytics.EffectRanker
import com.noop.data.DailyMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Classic Insights Rest outcome, including experiment and relationship inputs.
 * Synthetic measurements only. No wearer data, device or clinical claim.
 */
class ClassicInsightsRestCompositeTest {
    private val factor = "Did you have caffeine late in the day?"

    @Test fun restComparisonUsesCompositeNotTheDailyEfficiencyFraction() {
        val days = (1..10).map { n ->
            DailyMetric(
                deviceId = "whoop-test-noop",
                day = "2026-09-%02d".format(n),
                efficiency = 0.94,  // Deliberately unchanged across the two groups.
                recovery = 70.0,
            )
        }
        val yes = days.take(5).map { it.day }.toSet()
        val no = days.drop(5).map { it.day }.toSet()
        val rest = days.mapIndexed { n, d -> d.day to (if (n < 5) 60.0 + n else 80.0 + n - 5) }.toMap()

        val model = buildModel(
            days = days,
            behaviours = mapOf(factor to yes),
            controls = mapOf(factor to no),
            restCompositeByDay = rest,
        )
        assertEquals(rest, model.outcomeByDay[Outcome.Sleep])
        assertEquals(days.map { it.day to rest.getValue(it.day) }, model.seriesByOutcome[Outcome.Sleep])
        assertNull("Rest must never pick 0..1 efficiency", Outcome.Sleep.pick(days.first()))

        // Both the ordinary effect cards and the experimental snapshot read
        // model.outcomeByDay, so exercise the ranker's real input, not a parallel
        // comparison implementation.
        val ranked = EffectRanker.rankNoLag(
            model.behaviours, model.controls, requireNotNull(model.outcomeByDay[Outcome.Sleep]), "Rest",
        )
        assertEquals(1, ranked.size)
        assertEquals(62.0, ranked.single().meanWith, 1e-9)
        assertEquals(82.0, ranked.single().meanWithout, 1e-9)
        assertEquals(-20.0, ranked.single().delta, 1e-9)
    }

    @Test fun noCompositeMeansNoRestEvenWhenEfficiencyExists() {
        val day = DailyMetric(deviceId = "whoop-test-noop", day = "2026-09-11", efficiency = 0.99)
        val model = buildModel(
            days = listOf(day),
            behaviours = emptyMap(),
            controls = emptyMap(),
            restCompositeByDay = emptyMap(),
        )
        assertTrue(model.outcomeByDay[Outcome.Sleep].isNullOrEmpty())
        assertTrue(model.seriesByOutcome[Outcome.Sleep].isNullOrEmpty())
    }

    @Test fun onlyCachedDaysAppearAndOtherDailyOutcomesAreUnchanged() {
        val day = DailyMetric(
            deviceId = "whoop-test-noop",
            day = "2026-09-12",
            efficiency = 0.91,
            recovery = 75.0,
            avgHrv = 48.0,
            restingHr = 53,
        )
        val model = buildModel(
            days = listOf(day),
            behaviours = emptyMap(),
            controls = emptyMap(),
            restCompositeByDay = mapOf(day.day to 83.0, "2026-09-13" to 99.0),
        )
        assertEquals(mapOf(day.day to 83.0), model.outcomeByDay[Outcome.Sleep])
        assertFalse(model.outcomeByDay[Outcome.Sleep]!!.containsKey("2026-09-13"))
        assertEquals(75.0, model.outcomeByDay[Outcome.Recovery]!![day.day]!!, 1e-9)
        assertEquals(48.0, model.outcomeByDay[Outcome.Hrv]!![day.day]!!, 1e-9)
        assertEquals(53.0, model.outcomeByDay[Outcome.Rhr]!![day.day]!!, 1e-9)
    }
}
