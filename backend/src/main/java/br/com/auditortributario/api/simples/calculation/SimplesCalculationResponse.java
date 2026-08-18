package br.com.auditortributario.api.simples.calculation;

import br.com.auditortributario.taxrule.simples.FatorRCalculationResult;
import br.com.auditortributario.taxrule.simples.SimplesEffectiveRateResult;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxResult;
import br.com.auditortributario.taxrule.simples.SimplesTaxBracket;
import br.com.auditortributario.taxrule.simples.SimplesTaxBracketSelectionResult;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisResult;

import java.math.BigDecimal;
import java.time.YearMonth;

public record SimplesCalculationResponse(
        YearMonth assessmentPeriod,
        FatorRResponse fatorR,
        RevenueBasisResponse revenueBasis,
        TaxBracketResponse taxBracket,
        EffectiveRateResponse effectiveRate,
        BigDecimal taxableRevenue,
        BigDecimal estimatedTaxAmount,
        String estimatedTaxStatus,
        String estimatedTaxStatusLabel,
        String taxTableVersion) {

    public static SimplesCalculationResponse from(
            SimplesEstimatedTaxResult result) {
        SimplesEffectiveRateResult effectiveRateResult = result.effectiveRateResult();

        SimplesTaxBracketSelectionResult selectionResult = effectiveRateResult.bracketSelectionResult();

        FatorRCalculationResult fatorRResult = selectionResult.fatorRResult();

        TaxBracketRevenueBasisResult revenueBasisResult = selectionResult.revenueBasisResult();

        SimplesTaxBracket bracket = selectionResult.bracket();

        return new SimplesCalculationResponse(
                result.taxableRevenue().period(),

                new FatorRResponse(
                        fatorRResult
                                .fatorR()
                                .value(),

                        fatorRResult
                                .fatorR()
                                .asPercentage(),

                        fatorRResult
                                .calculationBasis()
                                .name(),

                        fatorRResult
                                .calculationBasis()
                                .getDisplayName(),

                        fatorRResult
                                .annex()
                                .name(),

                        fatorRResult
                                .annex()
                                .getDisplayName()),

                new RevenueBasisResponse(
                        revenueBasisResult
                                .basisType()
                                .getCode(),

                        revenueBasisResult
                                .basisType()
                                .getDisplayName(),

                        revenueBasisResult
                                .revenueBasis()),

                new TaxBracketResponse(
                        bracket.number(),

                        bracket.nominalRate(),

                        bracket.nominalRateAsPercentage(),

                        bracket.deduction()),

                new EffectiveRateResponse(
                        effectiveRateResult.effectiveRate(),

                        effectiveRateResult
                                .effectiveRateAsPercentage()),

                result.taxableRevenue()
                        .amount(),

                result.estimatedTaxAmount(),

                result.status()
                        .name(),

                result.status()
                        .getDisplayName(),

                selectionResult
                        .taxTable()
                        .version());
    }

    public record FatorRResponse(
            BigDecimal value,
            BigDecimal percentage,
            String calculationBasis,
            String calculationBasisLabel,
            String annex,
            String annexLabel) {
    }

    public record RevenueBasisResponse(
            String type,
            String typeLabel,
            BigDecimal amount) {
    }

    public record TaxBracketResponse(
            int number,
            BigDecimal nominalRate,
            BigDecimal nominalRatePercentage,
            BigDecimal deduction) {
    }

    public record EffectiveRateResponse(
            BigDecimal value,
            BigDecimal percentage) {
    }
}