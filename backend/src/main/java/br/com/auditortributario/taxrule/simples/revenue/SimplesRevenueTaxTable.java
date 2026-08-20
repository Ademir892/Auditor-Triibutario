package br.com.auditortributario.taxrule.simples.revenue;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public record SimplesRevenueTaxTable(
        SimplesRevenueTaxRoute route,
        String version,
        YearMonth validFrom,
        YearMonth validUntil,
        List<SimplesRevenueTaxBracket> brackets) {

    public SimplesRevenueTaxTable {
        Objects.requireNonNull(
                route,
                "A rota tributária não pode ser nula.");

        Objects.requireNonNull(
                version,
                "A versão não pode ser nula.");

        Objects.requireNonNull(
                validFrom,
                "A vigência inicial não pode ser nula.");

        Objects.requireNonNull(
                validUntil,
                "A vigência final não pode ser nula.");

        Objects.requireNonNull(
                brackets,
                "As faixas não podem ser nulas.");

        version = version.trim();

        if (version.isBlank()) {
            throw new IllegalArgumentException(
                    "A versão não pode estar vazia.");
        }

        if (validUntil.isBefore(
                validFrom)) {
            throw new IllegalArgumentException(
                    "A vigência final não pode ser anterior à inicial.");
        }

        brackets = List.copyOf(
                brackets);

        if (brackets.size() != 6) {
            throw new IllegalArgumentException(
                    "A tabela deve possuir exatamente seis faixas.");
        }

        for (int index = 0; index < brackets.size(); index++) {

            SimplesRevenueTaxBracket bracket = brackets.get(
                    index);

            if (bracket == null) {
                throw new IllegalArgumentException(
                        "A tabela não pode conter faixas nulas.");
            }

            if (bracket.route() != route) {
                throw new IllegalArgumentException(
                        "Todas as faixas devem pertencer "
                                + "à mesma rota da tabela.");
            }

            if (bracket.number() != index + 1) {
                throw new IllegalArgumentException(
                        "As faixas devem estar ordenadas "
                                + "de 1 até 6.");
            }
        }
    }

    public boolean isValidFor(
            YearMonth competence) {
        Objects.requireNonNull(
                competence,
                "A competência não pode ser nula.");

        return !competence.isBefore(
                validFrom)
                && !competence.isAfter(
                        validUntil);
    }
}