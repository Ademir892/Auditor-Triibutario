package br.com.auditortributario.auditcase;

import br.com.auditortributario.auditcase.subject.AuditedSubject;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record AuditCase(
                AuditCaseId id,
                AuditedSubject subject,
                AuditCaseType type,
                TaxRegime taxRegime,
                AuditPeriod period,
                AuditCaseStatus status,
                List<AuditCompetence> competences,
                Instant createdAt) {

        public AuditCase {
                Objects.requireNonNull(
                                id,
                                "O identificador não pode ser nulo.");

                Objects.requireNonNull(
                                subject,
                                "O sujeito auditado não pode ser nulo.");

                Objects.requireNonNull(
                                type,
                                "O tipo de auditoria não pode ser nulo.");

                Objects.requireNonNull(
                                taxRegime,
                                "O regime tributário não pode ser nulo.");

                Objects.requireNonNull(
                                period,
                                "O período analisado não pode ser nulo.");

                Objects.requireNonNull(
                                status,
                                "O status não pode ser nulo.");

                Objects.requireNonNull(
                                competences,
                                "As competências não podem ser nulas.");

                Objects.requireNonNull(
                                createdAt,
                                "A data de criação não pode ser nula.");

                competences = List.copyOf(
                                competences);

                validateCompetences(
                                period,
                                competences);
        }

        public static AuditCase create(
                        AuditedSubject subject,
                        AuditCaseType type,
                        TaxRegime taxRegime,
                        AuditPeriod period) {
                Objects.requireNonNull(
                                period,
                                "O período analisado não pode ser nulo.");

                List<AuditCompetence> competences = period.months()
                                .stream()
                                .map(
                                                AuditCompetence::pending)
                                .toList();

                return new AuditCase(
                                AuditCaseId.generate(),
                                subject,
                                type,
                                taxRegime,
                                period,
                                AuditCaseStatus.CREATED,
                                competences,
                                Instant.now());
        }

        public Optional<AuditCompetence> findCompetence(
                        YearMonth period) {
                Objects.requireNonNull(
                                period,
                                "A competência pesquisada não pode ser nula.");

                return competences
                                .stream()
                                .filter(
                                                competence -> competence
                                                                .period()
                                                                .equals(
                                                                                period))
                                .findFirst();
        }

        public int numberOfCompetences() {
                return competences.size();
        }

        public boolean allCompetencesCompleted() {
                return competences
                                .stream()
                                .allMatch(
                                                AuditCompetence::isCompleted);
        }

        public boolean isCompleted() {
                return status == AuditCaseStatus.COMPLETED;
        }

        private static void validateCompetences(
                        AuditPeriod period,
                        List<AuditCompetence> competences) {
                if (competences.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "O caso de auditoria deve possuir "
                                                        + "ao menos uma competência.");
                }

                if (competences.stream().anyMatch(Objects::isNull)) {
                        throw new IllegalArgumentException(
                                        "As competências não podem conter valores nulos.");
                }

                List<YearMonth> expectedPeriods = period.months();

                List<YearMonth> actualPeriods = competences
                                .stream()
                                .map(
                                                AuditCompetence::period)
                                .toList();

                if (!actualPeriods.equals(expectedPeriods)) {
                        throw new IllegalArgumentException(
                                        "As competências do caso devem corresponder "
                                                        + "exatamente ao período analisado.");
                }
        }
}