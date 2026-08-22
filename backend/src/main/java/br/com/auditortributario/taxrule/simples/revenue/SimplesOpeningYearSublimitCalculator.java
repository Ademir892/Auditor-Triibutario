package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Objects;

public final class SimplesOpeningYearSublimitCalculator {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private static final BigDecimal TWELVE = new BigDecimal("12");

    private static final String RULE_CODE = "SIMPLES_OPENING_YEAR_SUBLIMIT";

    private static final String RULE_VERSION = "LC123-CGSN140-2018-2026";

    private static final String LEGAL_REFERENCE = "Lei Complementar nº 123/2006, art. 3º, § 11; "
            + "Resolução CGSN nº 140/2018, art. 12, § 2º.";

    public SimplesOpeningYearSublimitResult calculate(
            LocalDate openingDate,
            BigDecimal annualSublimit) {
        Objects.requireNonNull(
                openingDate,
                "A data de abertura não pode ser nula.");

        Objects.requireNonNull(
                annualSublimit,
                "O sublimite anual não pode ser nulo.");

        if (annualSublimit.compareTo(
                BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O sublimite anual deve ser maior que zero.");
        }

        /*
         * A fração de mês é considerada mês completo.
         *
         * Portanto, a quantidade de meses depende apenas
         * do mês da abertura:
         *
         * janeiro -> 12 meses
         * fevereiro -> 11 meses
         * ...
         * dezembro -> 1 mês
         */
        int monthsConsidered = 13 - openingDate.getMonthValue();

        BigDecimal monthlyReference = annualSublimit.divide(
                TWELVE,
                MATH_CONTEXT);

        BigDecimal proportionalizedSublimit = monthlyReference.multiply(
                BigDecimal.valueOf(
                        monthsConsidered),
                MATH_CONTEXT);

        TaxDecision decision = createDecision(
                openingDate,
                annualSublimit,
                monthlyReference,
                monthsConsidered,
                proportionalizedSublimit);

        return new SimplesOpeningYearSublimitResult(
                openingDate,
                annualSublimit,
                monthlyReference,
                monthsConsidered,
                proportionalizedSublimit,
                decision);
    }

    private TaxDecision createDecision(
            LocalDate openingDate,
            BigDecimal annualSublimit,
            BigDecimal monthlyReference,
            int monthsConsidered,
            BigDecimal proportionalizedSublimit) {
        return new TaxDecision(
                RULE_CODE,
                RULE_VERSION,
                "Cálculo do sublimite proporcional "
                        + "no ano-calendário de início de atividade.",
                "DataAbertura="
                        + openingDate
                        + "; SublimiteAnual="
                        + annualSublimit.toPlainString(),
                "ReferenciaMensal="
                        + monthlyReference.toPlainString()
                        + "; MesesConsiderados="
                        + monthsConsidered
                        + "; FracaoMesConsideradaMesCompleto=true",
                "SublimiteProporcional="
                        + proportionalizedSublimit.toPlainString(),
                LEGAL_REFERENCE);
    }
}