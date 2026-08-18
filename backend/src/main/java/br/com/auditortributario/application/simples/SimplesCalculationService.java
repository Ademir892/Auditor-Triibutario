package br.com.auditortributario.application.simples;

import br.com.auditortributario.taxrule.simples.FatorRAutomaticCalculator;
import br.com.auditortributario.taxrule.simples.FatorRCalculationRequest;
import br.com.auditortributario.taxrule.simples.FatorRCalculationResult;
import br.com.auditortributario.taxrule.simples.MonthlyRevenue;
import br.com.auditortributario.taxrule.simples.SimplesEffectiveRateCalculator;
import br.com.auditortributario.taxrule.simples.SimplesEffectiveRateResult;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxCalculator;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxRequest;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxResult;
import br.com.auditortributario.taxrule.simples.SimplesTaxBracketSelectionRequest;
import br.com.auditortributario.taxrule.simples.SimplesTaxBracketSelectionResult;
import br.com.auditortributario.taxrule.simples.SimplesTaxBracketSelector;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisCalculator;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisRequest;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisResult;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public final class SimplesCalculationService {

    private final FatorRAutomaticCalculator fatorRCalculator;
    private final TaxBracketRevenueBasisCalculator revenueBasisCalculator;
    private final SimplesTaxBracketSelector bracketSelector;
    private final SimplesEffectiveRateCalculator effectiveRateCalculator;
    private final SimplesEstimatedTaxCalculator estimatedTaxCalculator;

    public SimplesCalculationService() {
        this.fatorRCalculator = new FatorRAutomaticCalculator();

        this.revenueBasisCalculator = new TaxBracketRevenueBasisCalculator();

        this.bracketSelector = new SimplesTaxBracketSelector();

        this.effectiveRateCalculator = new SimplesEffectiveRateCalculator();

        this.estimatedTaxCalculator = new SimplesEstimatedTaxCalculator();
    }

    public SimplesEstimatedTaxResult calculate(
            SimplesCalculationCommand command) {
        Objects.requireNonNull(
                command,
                "O comando de cálculo não pode ser nulo.");

        FatorRCalculationResult fatorRResult = calculateFatorR(
                command);

        TaxBracketRevenueBasisResult revenueBasisResult = calculateRevenueBasis(
                command);

        SimplesTaxBracketSelectionResult bracketSelectionResult = bracketSelector.select(
                new SimplesTaxBracketSelectionRequest(
                        command.assessmentPeriod(),
                        fatorRResult,
                        revenueBasisResult));

        SimplesEffectiveRateResult effectiveRateResult = effectiveRateCalculator.calculate(
                bracketSelectionResult);

        MonthlyRevenue currentRevenue = new MonthlyRevenue(
                command.assessmentPeriod(),
                command.taxableRevenue());

        return estimatedTaxCalculator.calculate(
                new SimplesEstimatedTaxRequest(
                        currentRevenue,
                        effectiveRateResult));
    }

    private FatorRCalculationResult calculateFatorR(
            SimplesCalculationCommand command) {
        return fatorRCalculator.calculate(
                new FatorRCalculationRequest(
                        command.openingDate(),
                        command.assessmentPeriod(),
                        command.fatorRPayrollBase(),
                        command.fatorRRevenueBase()));
    }

    private TaxBracketRevenueBasisResult calculateRevenueBasis(
            SimplesCalculationCommand command) {
        List<MonthlyRevenue> revenues = new ArrayList<>(
                command.priorMonthlyRevenues());

        revenues.add(
                new MonthlyRevenue(
                        command.assessmentPeriod(),
                        command.taxableRevenue()));

        return revenueBasisCalculator.calculate(
                new TaxBracketRevenueBasisRequest(
                        command.openingDate(),
                        command.assessmentPeriod(),
                        revenues));
    }
}