package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxComponentTreatment;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxTreatment;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxTreatmentEffect;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Aplica os efeitos do sublimite depois do cálculo ordinário da receita.
 *
 * <p>Os cálculos transitórios de ICMS e ISS pertencem às suas classes
 * especializadas. Esta classe somente substitui o componente local do DAS
 * pelo valor já apurado e preserva todos os demais componentes.</p>
 */
public final class SimplesSublimitRevenueTaxAdjuster {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private static final String RULE_CODE = "SIMPLES_SUBLIMIT_REVENUE_ADJUSTMENT";

    private static final String RULE_VERSION = "LC123-CGSN140-2018-2026";

    public SimplesSublimitAdjustedRevenueTaxResult adjust(
            SimplesRevenueTaxProcessingResult originalResult,
            SimplesSublimitTaxContext context) {
        return adjust(originalResult, context, Optional.empty(), Optional.empty());
    }

    public SimplesSublimitAdjustedRevenueTaxResult adjust(
            SimplesRevenueTaxProcessingResult originalResult,
            SimplesSublimitTaxContext context,
            Optional<SimplesIcmsTransitionalCalculationResult> icmsTransitionalResult,
            Optional<SimplesIssTransitionalCalculationResult> issTransitionalResult) {
        Objects.requireNonNull(originalResult, "O resultado tributário original não pode ser nulo.");
        Objects.requireNonNull(context, "O contexto do sublimite não pode ser nulo.");
        Objects.requireNonNull(icmsTransitionalResult, "O resultado transitório opcional do ICMS não pode ser nulo.");
        Objects.requireNonNull(issTransitionalResult, "O resultado transitório opcional do ISS não pode ser nulo.");

        if (!originalResult.isFinal() || originalResult.finalTaxAmount().isEmpty()) {
            throw new IllegalArgumentException(
                    "O sublimite somente pode ajustar um resultado tributário final.");
        }

        SimplesRevenueTaxRoute route = originalResult.classification().route().orElseThrow(
                () -> new IllegalArgumentException(
                        "O sublimite exige uma rota tributária resolvida."));

        return switch (context.treatment()) {
            case IN_DAS_STANDARD -> standardResult(
                    originalResult,
                    context,
                    icmsTransitionalResult,
                    issTransitionalResult);
            case IN_DAS_TRANSITIONAL -> transitionalResult(
                    originalResult,
                    context,
                    route,
                    icmsTransitionalResult,
                    issTransitionalResult);
            case OUTSIDE_DAS -> outsideDasResult(originalResult, context, route);
        };
    }

    private SimplesSublimitAdjustedRevenueTaxResult standardResult(
            SimplesRevenueTaxProcessingResult originalResult,
            SimplesSublimitTaxContext context,
            Optional<SimplesIcmsTransitionalCalculationResult> icmsTransitionalResult,
            Optional<SimplesIssTransitionalCalculationResult> issTransitionalResult) {
        if (icmsTransitionalResult.isPresent() || issTransitionalResult.isPresent()) {
            throw new IllegalArgumentException(
                    "O tratamento normal do sublimite não aceita cálculo transitório.");
        }

        return result(
                originalResult,
                context.treatment(),
                originalResult.adjustedComponents(),
                originalResult.finalTaxAmount().orElseThrow(),
                originalResult.hasExternalObligation(),
                "A receita permanece integralmente sujeita ao DAS.");
    }

    private SimplesSublimitAdjustedRevenueTaxResult transitionalResult(
            SimplesRevenueTaxProcessingResult originalResult,
            SimplesSublimitTaxContext context,
            SimplesRevenueTaxRoute route,
            Optional<SimplesIcmsTransitionalCalculationResult> icmsTransitionalResult,
            Optional<SimplesIssTransitionalCalculationResult> issTransitionalResult) {
        TaxComponent component = localComponentFor(route);
        BigDecimal transitionalAmount;
        BigDecimal excessRatio;

        if (component == TaxComponent.ICMS) {
            SimplesIcmsTransitionalCalculationResult calculation = icmsTransitionalResult.orElseThrow(
                    () -> new IllegalArgumentException(
                            "O tratamento transitório do ICMS exige seu cálculo transitório."));
            validateIcmsCalculation(originalResult, context, route, calculation);
            if (issTransitionalResult.isPresent()) {
                throw new IllegalArgumentException("Uma receita de ICMS não pode receber cálculo transitório de ISS.");
            }
            transitionalAmount = calculation.icmsAmount();
            excessRatio = calculation.monthlyExcess().excessRatio();
        } else {
            SimplesIssTransitionalCalculationResult calculation = issTransitionalResult.orElseThrow(
                    () -> new IllegalArgumentException(
                            "O tratamento transitório do ISS exige seu cálculo transitório."));
            validateIssCalculation(originalResult, context, route, calculation);
            if (icmsTransitionalResult.isPresent()) {
                throw new IllegalArgumentException("Uma receita de ISS não pode receber cálculo transitório de ICMS.");
            }
            transitionalAmount = calculation.issAmount();
            excessRatio = calculation.monthlyExcess().excessRatio();
        }

        List<SimplesAdjustedTaxComponent> components = replaceLocalComponent(
                originalResult,
                component,
                excessRatio,
                transitionalAmount,
                RevenueTaxTreatmentEffect.REDUCE_COMPONENT,
                "A parcela excedente do sublimite recebeu o cálculo transitório de " + component + ".");

        BigDecimal finalAmount = sumAdjustedAmounts(components);

        return result(
                originalResult,
                context.treatment(),
                components,
                finalAmount,
                originalResult.hasExternalObligation(),
                "O componente " + component + " foi recomposto entre a parcela dentro do sublimite e a parcela transitória.");
    }

    private SimplesSublimitAdjustedRevenueTaxResult outsideDasResult(
            SimplesRevenueTaxProcessingResult originalResult,
            SimplesSublimitTaxContext context,
            SimplesRevenueTaxRoute route) {
        TaxComponent component = localComponentFor(route);

        List<SimplesAdjustedTaxComponent> components = replaceLocalComponent(
                originalResult,
                component,
                BigDecimal.ONE,
                BigDecimal.ZERO,
                RevenueTaxTreatmentEffect.REQUIRE_EXTERNAL_CALCULATION,
                "O sublimite determina o recolhimento de " + component + " fora do DAS.");

        return result(
                originalResult,
                context.treatment(),
                components,
                sumAdjustedAmounts(components),
                true,
                "O componente " + component + " foi excluído do DAS e exige obrigação externa.");
    }

    private List<SimplesAdjustedTaxComponent> replaceLocalComponent(
            SimplesRevenueTaxProcessingResult originalResult,
            TaxComponent targetComponent,
            BigDecimal excessRatio,
            BigDecimal transitionalAmount,
            RevenueTaxTreatmentEffect effect,
            String explanation) {
        boolean found = false;
        List<SimplesAdjustedTaxComponent> adjusted = new ArrayList<>();

        for (SimplesAdjustedTaxComponent component : originalResult.adjustedComponents()) {
            if (component.component() != targetComponent) {
                adjusted.add(component);
                continue;
            }

            found = true;
            BigDecimal withinSublimitAmount = component.adjustedAmount().multiply(
                    BigDecimal.ONE.subtract(excessRatio, MATH_CONTEXT), MATH_CONTEXT);
            BigDecimal finalComponentAmount = withinSublimitAmount.add(transitionalAmount, MATH_CONTEXT);
            BigDecimal finalEffectiveRate = originalResult.revenue().amount().signum() == 0
                    ? BigDecimal.ZERO
                    : finalComponentAmount.divide(originalResult.revenue().amount(), MATH_CONTEXT);

            List<RevenueTaxComponentTreatment> treatments = new ArrayList<>(component.appliedTreatments());
            treatments.add(new RevenueTaxComponentTreatment(
                    RevenueTaxTreatment.SIMPLIFIED_REGIME_SUBLIMIT,
                    targetComponent,
                    effect,
                    explanation));

            adjusted.add(new SimplesAdjustedTaxComponent(
                    targetComponent,
                    component.originalEffectiveRate(),
                    finalEffectiveRate,
                    component.originalAmount(),
                    finalComponentAmount,
                    treatments));
        }

        if (!found) {
            throw new IllegalArgumentException(
                    "O resultado tributário não possui o componente " + targetComponent + " necessário para o sublimite.");
        }

        return List.copyOf(adjusted);
    }

    private TaxComponent localComponentFor(SimplesRevenueTaxRoute route) {
        return switch (route) {
            case ANNEX_I, ANNEX_II -> TaxComponent.ICMS;
            case ANNEX_III, ANNEX_IV, ANNEX_V -> TaxComponent.ISS;
        };
    }

    private void validateIcmsCalculation(
            SimplesRevenueTaxProcessingResult originalResult,
            SimplesSublimitTaxContext context,
            SimplesRevenueTaxRoute route,
            SimplesIcmsTransitionalCalculationResult calculation) {
        if (calculation.route() != route
                || calculation.segregatedRevenueAmount().compareTo(originalResult.revenue().amount()) != 0
                || !calculation.monthlyExcess().evaluationResult().equals(context.evaluation().orElseThrow())) {
            throw new IllegalArgumentException("O cálculo transitório do ICMS não corresponde à receita e ao sublimite informados.");
        }
    }

    private void validateIssCalculation(
            SimplesRevenueTaxProcessingResult originalResult,
            SimplesSublimitTaxContext context,
            SimplesRevenueTaxRoute route,
            SimplesIssTransitionalCalculationResult calculation) {
        if (calculation.route() != route
                || calculation.segregatedRevenueAmount().compareTo(originalResult.revenue().amount()) != 0
                || !calculation.monthlyExcess().evaluationResult().equals(context.evaluation().orElseThrow())) {
            throw new IllegalArgumentException("O cálculo transitório do ISS não corresponde à receita e ao sublimite informados.");
        }
    }

    private BigDecimal sumAdjustedAmounts(List<SimplesAdjustedTaxComponent> components) {
        return components.stream()
                .map(SimplesAdjustedTaxComponent::adjustedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private SimplesSublimitAdjustedRevenueTaxResult result(
            SimplesRevenueTaxProcessingResult originalResult,
            SimplesSublimitTaxTreatment treatment,
            List<SimplesAdjustedTaxComponent> components,
            BigDecimal finalAmount,
            boolean externalObligation,
            String outcome) {
        TaxDecision decision = new TaxDecision(
                RULE_CODE,
                RULE_VERSION,
                "Ajuste do resultado de receita segregada pelos efeitos do sublimite.",
                "Receita=" + originalResult.revenue().amount().toPlainString()
                        + "; Rota=" + originalResult.classification().route().orElseThrow(),
                "Tratamento=" + treatment + "; Componentes=" + components.size(),
                outcome + "; ValorFinalDAS=" + finalAmount.toPlainString(),
                "Lei Complementar nº 123/2006, art. 18, §§ 16, 16-A, 17 e 17-A; Resolução CGSN nº 140/2018, art. 24.");

        return new SimplesSublimitAdjustedRevenueTaxResult(
                originalResult,
                treatment,
                components,
                Optional.of(finalAmount),
                true,
                externalObligation,
                decision);
    }
}
