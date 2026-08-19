package br.com.auditortributario.auditcase.subject;

import java.util.Objects;
import java.util.Optional;

public record AuditedSubject(
        AuditedSubjectId id,
        AuditedSubjectType type,
        String displayName,
        Optional<TaxIdentifier> taxIdentifier) {

    public AuditedSubject {
        Objects.requireNonNull(
                id,
                "O identificador do sujeito auditado não pode ser nulo.");

        Objects.requireNonNull(
                type,
                "O tipo do sujeito auditado não pode ser nulo.");

        Objects.requireNonNull(
                displayName,
                "O nome do sujeito auditado não pode ser nulo.");

        Objects.requireNonNull(
                taxIdentifier,
                "O identificador tributário opcional não pode ser nulo.");

        displayName = displayName.trim();

        if (displayName.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do sujeito auditado não pode estar vazio.");
        }
    }

    public static AuditedSubject create(
            AuditedSubjectType type,
            String displayName,
            TaxIdentifier taxIdentifier) {
        Objects.requireNonNull(
                taxIdentifier,
                "O identificador tributário não pode ser nulo.");

        return new AuditedSubject(
                AuditedSubjectId.generate(),
                type,
                displayName,
                Optional.of(
                        taxIdentifier));
    }

    public static AuditedSubject createWithoutTaxIdentifier(
            AuditedSubjectType type,
            String displayName) {
        return new AuditedSubject(
                AuditedSubjectId.generate(),
                type,
                displayName,
                Optional.empty());
    }

    public boolean hasTaxIdentifier() {
        return taxIdentifier.isPresent();
    }
}