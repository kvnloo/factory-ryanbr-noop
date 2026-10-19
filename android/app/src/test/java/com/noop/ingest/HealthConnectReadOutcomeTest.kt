package com.noop.ingest

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** A successful EMPTY HC read may throttle auto-sync; SDK read failures must not. */
class HealthConnectReadOutcomeTest {
    @Test fun everySelectedReadFailedIsAnImportFailure() {
        assertTrue(HealthConnectImporter.everySelectedHealthReadFailed(attempted = 3, completed = 0))
    }

    @Test fun oneCompletedReadMeansPartialImportCanContinue() {
        assertFalse(HealthConnectImporter.everySelectedHealthReadFailed(attempted = 3, completed = 1))
    }

    @Test fun completedEmptyReadIsStillSuccess() {
        assertFalse(HealthConnectImporter.everySelectedHealthReadFailed(attempted = 1, completed = 1))
    }

    @Test fun noEligibleReadsIsNotAnSdkReadFailure() {
        assertFalse(HealthConnectImporter.everySelectedHealthReadFailed(attempted = 0, completed = 0))
    }
}
