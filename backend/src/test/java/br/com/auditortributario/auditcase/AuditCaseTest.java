package br.com.auditortributario.auditcase;

import br.com.auditortributario.auditcase.subject.AuditedSubject;
import br.com.auditortributario.auditcase.subject.AuditedSubjectType;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditCaseTest {

        @Test
        void shouldCreateMonthlyAuditCase() {
                AuditedSubject subject = createSubject();

                AuditCase auditCase = AuditCase.create(
                                subject,
                                AuditCaseType.MONTHLY,
                                TaxRegime.SIMPLES_NACIONAL,
                                AuditPeriod.monthly(
                                                YearMonth.of(
                                                                2026,
                                                                8)));

                assertNotNull(
                                auditCase.id());

                assertNotNull(
                                auditCase.createdAt());

                assertEquals(
                                subject,
                                auditCase.subject());

                assertEquals(
                                AuditCaseStatus.CREATED,
                                auditCase.status());

                assertEquals(
                                AuditCaseType.MONTHLY,
                                auditCase.type());

                assertEquals(
                                TaxRegime.SIMPLES_NACIONAL,
                                auditCase.taxRegime());

                assertTrue(
                                auditCase
                                                .period()
                                                .isSingleMonth());

                assertEquals(
                                1,
                                auditCase.numberOfCompetences());

                assertEquals(
                                AuditCompetenceStatus.PENDING,
                                auditCase
                                                .competences()
                                                .getFirst()
                                                .status());

                assertFalse(
                                auditCase.isCompleted());

                assertFalse(
                                auditCase.allCompetencesCompleted());
        }

        @Test
        void shouldCreateAnnualAuditCaseWithTwelveCompetences() {
                AuditCase auditCase = AuditCase.create(
                                createSubject(),
                                AuditCaseType.ANNUAL,
                                TaxRegime.SIMPLES_NACIONAL,
                                AuditPeriod.annual(
                                                2026));

                assertEquals(
                                12,
                                auditCase.numberOfCompetences());

                assertEquals(
                                YearMonth.of(
                                                2026,
                                                1),
                                auditCase
                                                .competences()
                                                .getFirst()
                                                .period());

                assertEquals(
                                YearMonth.of(
                                                2026,
                                                12),
                                auditCase
                                                .competences()
                                                .getLast()
                                                .period());

                assertTrue(
                                auditCase
                                                .competences()
                                                .stream()
                                                .allMatch(
                                                                competence -> competence
                                                                                .status() == AuditCompetenceStatus.PENDING));
        }

        @Test
        void shouldFindCompetenceInsideAuditCase() {
                AuditCase auditCase = AuditCase.create(
                                createSubject(),
                                AuditCaseType.ANNUAL,
                                TaxRegime.SIMPLES_NACIONAL,
                                AuditPeriod.annual(
                                                2026));

                AuditCompetence competence = auditCase
                                .findCompetence(
                                                YearMonth.of(
                                                                2026,
                                                                8))
                                .orElseThrow();

                assertEquals(
                                YearMonth.of(
                                                2026,
                                                8),
                                competence.period());
        }

        @Test
        void shouldReturnEmptyWhenCompetenceIsOutsideAuditCase() {
                AuditCase auditCase = AuditCase.create(
                                createSubject(),
                                AuditCaseType.ANNUAL,
                                TaxRegime.SIMPLES_NACIONAL,
                                AuditPeriod.annual(
                                                2026));

                assertTrue(
                                auditCase
                                                .findCompetence(
                                                                YearMonth.of(
                                                                                2025,
                                                                                12))
                                                .isEmpty());
        }

        @Test
        void shouldRejectCompetencesThatDoNotMatchAuditPeriod() {
                AuditedSubject subject = createSubject();

                AuditPeriod period = AuditPeriod.annual(
                                2026);

                List<AuditCompetence> invalidCompetences = List.of(
                                AuditCompetence.pending(
                                                YearMonth.of(
                                                                2026,
                                                                1)));

                assertThrows(
                                IllegalArgumentException.class,
                                () -> new AuditCase(
                                                AuditCaseId.generate(),
                                                subject,
                                                AuditCaseType.ANNUAL,
                                                TaxRegime.SIMPLES_NACIONAL,
                                                period,
                                                AuditCaseStatus.CREATED,
                                                invalidCompetences,
                                                Instant.now()));
        }

        @Test
        void shouldCreateAndRestoreAuditCaseId() {
                AuditCaseId generated = AuditCaseId.generate();

                AuditCaseId restored = AuditCaseId.from(
                                generated.toString());

                assertEquals(
                                generated,
                                restored);
        }

        private AuditedSubject createSubject() {
                return AuditedSubject
                                .createWithoutTaxIdentifier(
                                                AuditedSubjectType.BUSINESS,
                                                "Empresa Exemplo Ltda.");
        }
}