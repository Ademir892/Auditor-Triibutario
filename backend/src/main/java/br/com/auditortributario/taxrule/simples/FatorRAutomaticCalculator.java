package br.com.auditortributario.taxrule.simples;

import java.util.Objects;

public final class FatorRAutomaticCalculator {

    private final FatorRCalculator calculator;
    private final FatorRPeriodClassifier periodClassifier;

    public FatorRAutomaticCalculator() {
        this(
                new FatorRCalculator(),
                new FatorRPeriodClassifier());
    }

    FatorRAutomaticCalculator(
            FatorRCalculator calculator,
            FatorRPeriodClassifier periodClassifier) {
        this.calculator = Objects.requireNonNull(
                calculator,
                "O calculador do Fator R não pode ser nulo.");

        this.periodClassifier = Objects.requireNonNull(
                periodClassifier,
                "O classificador de período não pode ser nulo.");
    }

    public FatorRCalculationResult calculate(
            FatorRCalculationRequest request) {
        Objects.requireNonNull(
                request,
                "A requisição de cálculo não pode ser nula.");

        FatorRCalculationBasis calculationBasis = periodClassifier.classify(
                request.openingDate(),
                request.assessmentPeriod());

        return switch (calculationBasis) {
            case OPENING_MONTH ->
                calculator.calculateOpeningMonth(
                        request.payrollBase(),
                        request.revenueBase());

            case UNDER_13_MONTHS ->
                calculator.calculateUnderThirteenMonths(
                        request.payrollBase(),
                        request.revenueBase());

            case STANDARD_12_MONTHS ->
                calculator.calculate(
                        request.payrollBase(),
                        request.revenueBase());
        };
    }
}