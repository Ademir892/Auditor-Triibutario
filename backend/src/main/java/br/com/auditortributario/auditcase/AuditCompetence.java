package br.com.auditortributario.auditcase;

import java.time.YearMonth;
import java.util.Objects;

public record AuditCompetence(
        YearMonth period,
        AuditCompetenceStatus status) {

    public AuditCompetence {
        Objects.requireNonNull(
                period,
                "A competência não pode ser nula.");

        Objects.requireNonNull(
                status,
                "O status da competência não pode ser nulo.");
    }

    public static AuditCompetence pending(
            YearMonth period) {
        return new AuditCompetence(
                period,
                AuditCompetenceStatus.PENDING);
    }

    public AuditCompetence start() {
        return new AuditCompetence(
                period,
                AuditCompetenceStatus.IN_PROGRESS);
    }

    public AuditCompetence complete() {
        return new AuditCompetence(
                period,
                AuditCompetenceStatus.COMPLETED);
    }

    public AuditCompetence requireInformation() {
        return new AuditCompetence(
                period,
                AuditCompetenceStatus.REQUIRES_INFORMATION);
    }

    public boolean isCompleted() {
        return status == AuditCompetenceStatus.COMPLETED;
    }
}