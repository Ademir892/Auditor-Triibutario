package br.com.auditortributario.auditcase.document;

import java.util.Objects;
import java.util.UUID;

public record AuditDocumentId(
        UUID value) {

    public AuditDocumentId {
        Objects.requireNonNull(
                value,
                "O identificador do documento não pode ser nulo.");
    }

    public static AuditDocumentId generate() {
        return new AuditDocumentId(
                UUID.randomUUID());
    }

    public static AuditDocumentId from(
            String value) {
        Objects.requireNonNull(
                value,
                "O identificador textual não pode ser nulo.");

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "O identificador textual não pode estar vazio.");
        }

        return new AuditDocumentId(
                UUID.fromString(
                        value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}