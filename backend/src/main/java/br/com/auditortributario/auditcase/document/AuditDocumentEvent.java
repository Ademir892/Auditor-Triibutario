package br.com.auditortributario.auditcase.document;

import java.time.Instant;
import java.util.Objects;

public record AuditDocumentEvent(
        AuditDocumentStatus status,
        Instant occurredAt,
        String description) {

    public AuditDocumentEvent {
        Objects.requireNonNull(
                status,
                "O status do evento não pode ser nulo.");

        Objects.requireNonNull(
                occurredAt,
                "A data do evento não pode ser nula.");

        Objects.requireNonNull(
                description,
                "A descrição do evento não pode ser nula.");

        description = description.trim();

        if (description.isBlank()) {
            throw new IllegalArgumentException(
                    "A descrição do evento não pode estar vazia.");
        }
    }
}