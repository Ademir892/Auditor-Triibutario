package br.com.auditortributario.auditcase;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditCompetenceTest {

    @Test
    void shouldCreatePendingCompetence() {
        AuditCompetence competence = AuditCompetence.pending(
                YearMonth.of(
                        2026,
                        8));

        assertEquals(
                AuditCompetenceStatus.PENDING,
                competence.status());

        assertFalse(
                competence.isCompleted());
    }

    @Test
    void shouldMoveThroughCompetenceStates() {
        AuditCompetence pending = AuditCompetence.pending(
                YearMonth.of(
                        2026,
                        8));

        AuditCompetence inProgress = pending.start();

        AuditCompetence requiresInformation = inProgress.requireInformation();

        AuditCompetence completed = requiresInformation.complete();

        assertEquals(
                AuditCompetenceStatus.PENDING,
                pending.status());

        assertEquals(
                AuditCompetenceStatus.IN_PROGRESS,
                inProgress.status());

        assertEquals(
                AuditCompetenceStatus.REQUIRES_INFORMATION,
                requiresInformation.status());

        assertEquals(
                AuditCompetenceStatus.COMPLETED,
                completed.status());

        assertTrue(
                completed.isCompleted());
    }
}