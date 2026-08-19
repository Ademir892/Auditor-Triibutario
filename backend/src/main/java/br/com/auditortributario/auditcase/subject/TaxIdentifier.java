package br.com.auditortributario.auditcase.subject;

import java.util.Locale;
import java.util.Objects;

public record TaxIdentifier(
        TaxIdentifierType type,
        String value) {

    public TaxIdentifier {
        Objects.requireNonNull(
                type,
                "O tipo do identificador tributário não pode ser nulo.");

        Objects.requireNonNull(
                value,
                "O identificador tributário não pode ser nulo.");

        value = normalize(
                type,
                value);
    }

    public static TaxIdentifier cpf(
            String value) {
        return new TaxIdentifier(
                TaxIdentifierType.CPF,
                value);
    }

    public static TaxIdentifier cnpj(
            String value) {
        return new TaxIdentifier(
                TaxIdentifierType.CNPJ,
                value);
    }

    public static TaxIdentifier other(
            String value) {
        return new TaxIdentifier(
                TaxIdentifierType.OTHER,
                value);
    }

    private static String normalize(
            TaxIdentifierType type,
            String value) {
        return switch (type) {

            case CPF ->
                normalizeCpf(
                        value);

            case CNPJ ->
                normalizeCnpj(
                        value);

            case OTHER ->
                normalizeOther(
                        value);
        };
    }

    private static String normalizeCpf(
            String value) {
        String normalized = value
                .trim()
                .replaceAll(
                        "[.\\-\\s]",
                        "");

        if (!normalized.matches("\\d{11}")) {
            throw new IllegalArgumentException(
                    "O CPF deve possuir 11 dígitos.");
        }

        return normalized;
    }

    private static String normalizeCnpj(
            String value) {
        String normalized = value
                .trim()
                .toUpperCase(
                        Locale.ROOT)
                .replaceAll(
                        "[./\\-\\s]",
                        "");

        if (!normalized.matches(
                "[A-Z0-9]{12}[0-9]{2}")) {
            throw new IllegalArgumentException(
                    "O CNPJ deve possuir 14 posições, "
                            + "com as 12 primeiras alfanuméricas "
                            + "e as 2 últimas numéricas.");
        }

        return normalized;
    }

    private static String normalizeOther(
            String value) {
        String normalized = value.trim();

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                    "O identificador tributário não pode estar vazio.");
        }

        return normalized;
    }
}