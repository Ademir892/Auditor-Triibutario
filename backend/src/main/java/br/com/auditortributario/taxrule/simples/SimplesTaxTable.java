package br.com.auditortributario.taxrule.simples;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public record SimplesTaxTable(
        String version,
        SimplesAnnex annex,
        YearMonth validFrom,
        YearMonth validUntil,
        List<SimplesTaxBracket> brackets,
        String legalReference) {

    public SimplesTaxTable {
        version = requireText(
                version,
                "version");

        Objects.requireNonNull(
                annex,
                "O anexo não pode ser nulo.");

        Objects.requireNonNull(
                validFrom,
                "A competência inicial de vigência não pode ser nula.");

        Objects.requireNonNull(
                validUntil,
                "A competência final de vigência não pode ser nula.");

        if (validUntil.isBefore(validFrom)) {
            throw new IllegalArgumentException(
                    "A competência final não pode ser anterior "
                            + "à competência inicial.");
        }

        Objects.requireNonNull(
                brackets,
                "As faixas tributárias não podem ser nulas.");

        if (brackets.isEmpty()) {
            throw new IllegalArgumentException(
                    "A tabela deve possuir pelo menos uma faixa tributária.");
        }

        brackets = List.copyOf(brackets);

        validateBrackets(brackets);

        legalReference = requireText(
                legalReference,
                "legalReference");
    }

    public boolean isValidFor(
            YearMonth assessmentPeriod) {
        Objects.requireNonNull(
                assessmentPeriod,
                "O período de apuração não pode ser nulo.");

        return !assessmentPeriod.isBefore(validFrom)
                && !assessmentPeriod.isAfter(validUntil);
    }

    public SimplesTaxBracket findBracket(
            BigDecimal revenueBasis) {
        Objects.requireNonNull(
                revenueBasis,
                "A base de receita não pode ser nula.");

        if (revenueBasis.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A base de receita não pode ser negativa.");
        }

        for (SimplesTaxBracket bracket : brackets) {
            if (revenueBasis.compareTo(
                    bracket.maximumRevenue()) <= 0) {
                return bracket;
            }
        }

        throw new IllegalArgumentException(
                "A base de receita ultrapassa o limite "
                        + "da tabela do Simples Nacional.");
    }

    public BigDecimal maximumRevenue() {
        return brackets
                .get(brackets.size() - 1)
                .maximumRevenue();
    }

    private static void validateBrackets(
            List<SimplesTaxBracket> brackets) {
        BigDecimal previousMaximum = null;

        for (int index = 0; index < brackets.size(); index++) {
            SimplesTaxBracket bracket = Objects.requireNonNull(
                    brackets.get(index),
                    "A faixa tributária não pode ser nula.");

            int expectedNumber = index + 1;

            if (bracket.number() != expectedNumber) {
                throw new IllegalArgumentException(
                        "As faixas tributárias devem ser numeradas "
                                + "sequencialmente a partir de 1.");
            }

            if (previousMaximum != null
                    && bracket.maximumRevenue()
                            .compareTo(previousMaximum) <= 0) {
                throw new IllegalArgumentException(
                        "Os limites das faixas devem ser crescentes.");
            }

            previousMaximum = bracket.maximumRevenue();
        }
    }

    private static String requireText(
            String value,
            String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " não pode ser nulo ou vazio.");
        }

        return value;
    }
}