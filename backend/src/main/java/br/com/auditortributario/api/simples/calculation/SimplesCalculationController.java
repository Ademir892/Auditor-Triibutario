package br.com.auditortributario.api.simples.calculation;

import br.com.auditortributario.application.simples.SimplesCalculationService;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxResult;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simples")
public final class SimplesCalculationController {

    private final SimplesCalculationService calculationService;

    public SimplesCalculationController(
            SimplesCalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @PostMapping("/calculate")
    public SimplesCalculationResponse calculate(
            @Valid @RequestBody SimplesCalculationRequest request) {
        SimplesEstimatedTaxResult result = calculationService.calculate(
                request.toCommand());

        return SimplesCalculationResponse.from(
                result);
    }
}