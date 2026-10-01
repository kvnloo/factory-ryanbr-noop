package com.noop.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the additive experiment-evidence schema and Room version bump. */
class ExperimentEvidenceMigrationTest {
    @Test
    fun migrationIsAdditiveAndReachesRoom36() {
        val sql = WhoopDatabase.EXPERIMENT_EVIDENCE_MIGRATION_SQL
        assertEquals(5, sql.size)

        assertTrue(sql[0].contains("experimentContract"))
        assertTrue(sql[1].contains("experimentReceipt"))
        assertTrue(sql[2].contains("idx_experimentReceipt_contract"))
        assertTrue(sql[3].contains("experimentReceiptSource"))
        assertTrue(sql[4].contains("experimentReceiptConfounder"))

        for (statement in sql) {
            val upper = statement.uppercase()
            assertTrue(upper.startsWith("CREATE "))
            for (banned in listOf("DROP ", "DELETE ", "UPDATE ", "ALTER ")) {
                assertFalse("migration must not contain $banned", upper.contains(banned))
            }
        }

        assertEquals(35, WhoopDatabase.MIGRATION_35_36.startVersion)
        assertEquals(36, WhoopDatabase.MIGRATION_35_36.endVersion)
        assertEquals(36, WhoopDatabase.SCHEMA_VERSION)
    }
}
