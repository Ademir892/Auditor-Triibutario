package br.com.auditortributario.application.simples.composition ;



import br.com.auditortributario.application.simples.SimplesCalculationCommand;
import br.com.auditortributario.application.simples.SimplesCalculationService;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxResult;
import br.com.auditortributario.taxrule.simples.composition.SimplesTaxCompositionCalculator;
import br.com.auditortributario.taxrule.simples.composition.SimplesTaxCompositionResult;

import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public final class SimplesCompositionService {

    private final SimplesCalculationService calculationService;

    private final SimplesTaxCompositionCalculator compositionCalculator;

    public SimplesCompositionService(
            SimplesCalculationService calculationService
    ) {
        this.calculationService =
                Objects.requireNonNull(
                        calculationService,
                        "O serviço de cálculo não pode ser nulo."
                );

        this.compositionCalculator =
                new SimplesTaxCompositionCalculator();
    }

    public SimplesCompositionResult calculate(
            SimplesCalculationCommand command
    ) {
        Objects.requireNonNull(
                command,
                "O comando de cálculo não pode ser nulo."
        );

        SimplesEstimatedTaxResult estimatedTaxResult =
                calculationService.calculate(
                        command
                );

        SimplesTaxCompositionResult compositionResult =
                compositionCalculator.calculate(
                        estimatedTaxResult
                );

        return new SimplesCompositionResult(
                estimatedTaxResult,
                compositionResult
        );
    }
}