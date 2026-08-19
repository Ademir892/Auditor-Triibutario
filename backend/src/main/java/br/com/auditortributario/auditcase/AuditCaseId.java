package br.com.auditortributario.auditcase;

import java.util.Objects;
import java.util.UUID;

public record AuditCaseId(
        UUID value) {

    public AuditCaseId {
        Objects.requireNonNull(
                value,
                "O identificador do caso de auditoria não pode ser nulo.");
    }

    public static AuditCaseId generate() {
        return new AuditCaseId(
                UUID.randomUUID());
    }

    public static AuditCaseId from(
            String value) {
        Objects.requireNonNull(
                value,
                "O identificador textual não pode ser nulo.");

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "O identificador textual não pode estar vazio.");
        }

        return new AuditCaseId(
                UUID.fromString(
                        value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}