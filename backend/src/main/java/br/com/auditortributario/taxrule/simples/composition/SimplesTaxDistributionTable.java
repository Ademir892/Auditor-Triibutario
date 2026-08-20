package br.com.auditortributario.taxrule.simples.composition;

import br.com.auditortributario.taxrule.simples.SimplesAnnex;

import java.time.YearMonth;
import java.util.Map;
import java.util.Objects;

public record SimplesTaxDistributionTable(
        SimplesAnnex annex,
        String version,
        YearMonth validFrom,
        YearMonth validUntil,
        Map<Integer, SimplesTaxDistributionRule> rules) {

    public SimplesTaxDistributionTable {
        Objects.requireNonNull(
                annex,
                "O anexo não pode ser nulo.");

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
                rules,
                "As regras não podem ser nulas.");

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

        rules = Map.copyOf(
                rules);
    }

    public boolean isValidFor(
            YearMonth assessmentPeriod) {
        Objects.requireNonNull(
                assessmentPeriod,
                "A competência não pode ser nula.");

        return !assessmentPeriod.isBefore(
                validFrom)
                && !assessmentPeriod.isAfter(
                        validUntil);
    }

    public SimplesTaxDistributionRule ruleFor(
            int bracketNumber) {
        SimplesTaxDistributionRule rule = rules.get(
                bracketNumber);

        if (rule == null) {
            throw new IllegalArgumentException(
                    "Não existe regra de repartição "
                            + "para a faixa "
                            + bracketNumber
                            + ".");
        }

        return rule;
    }
}