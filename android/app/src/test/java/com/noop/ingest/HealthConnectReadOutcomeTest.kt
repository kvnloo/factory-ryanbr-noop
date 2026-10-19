package com.noop.ingest

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** A successful EMPTY HC read may throttle auto-sync; SDK read failures must not. */
class HealthConnectReadOutcomeTest {
    @Test fun cancelledRecordReadCannotBeTreatedAsEmptySuccess() {
        val cancel = kotlin.coroutines.cancellation.CancellationException("cancelled import")
        try {
            HealthConnectImporter.rethrowIfCancelled(cancel)
            org.junit.Assert.fail("Coroutine cancellation was swallowed")
        } catch (caught: kotlin.coroutines.cancellation.CancellationException) {
            org.junit.Assert.assertSame(cancel, caught)
        }
    }

    @Test fun ordinarySdkReadErrorsMayBeSkippedForPartialPermissions() {
        HealthConnectImporter.rethrowIfCancelled(IllegalStateException("SDK read failed"))
    }

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
