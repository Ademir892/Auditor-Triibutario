package br.com.auditortributario.auditcase.subject;

import java.util.Objects;
import java.util.UUID;

public record AuditedSubjectId(
        UUID value) {

    public AuditedSubjectId {
        Objects.requireNonNull(
                value,
                "O identificador do sujeito auditado não pode ser nulo.");
    }

    public static AuditedSubjectId generate() {
        return new AuditedSubjectId(
                UUID.randomUUID());
    }

    public static AuditedSubjectId from(
            String value) {
        Objects.requireNonNull(
                value,
                "O identificador textual não pode ser nulo.");

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "O identificador textual não pode estar vazio.");
        }

        return new AuditedSubjectId(
                UUID.fromString(
                        value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}