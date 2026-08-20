package br.com.auditortributario.taxrule.domain.revenue;

import java.util.Objects;
import java.util.UUID;

public record RevenueEntryId(
        UUID value) {

    public RevenueEntryId {
        Objects.requireNonNull(
                value,
                "O identificador da receita não pode ser nulo.");
    }

    public static RevenueEntryId generate() {
        return new RevenueEntryId(
                UUID.randomUUID());
    }

    public static RevenueEntryId from(
            String value) {
        Objects.requireNonNull(
                value,
                "O identificador textual da receita não pode ser nulo.");

        String normalized = value.trim();

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                    "O identificador textual da receita não pode estar vazio.");
        }

        return new RevenueEntryId(
                UUID.fromString(
                        normalized));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}