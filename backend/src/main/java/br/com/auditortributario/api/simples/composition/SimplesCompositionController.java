package br.com.auditortributario.api.simples.composition;

import br.com.auditortributario.api.simples.calculation.SimplesCalculationRequest;
import br.com.auditortributario.application.simples.composition.SimplesCompositionResult;
import br.com.auditortributario.application.simples.composition.SimplesCompositionService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simples/calculate")
public final class SimplesCompositionController {

    private final SimplesCompositionService compositionService;

    public SimplesCompositionController(
            SimplesCompositionService compositionService) {
        this.compositionService = compositionService;
    }

    @PostMapping("/composition")
    public SimplesCompositionResponse calculateComposition(
            @Valid @RequestBody SimplesCalculationRequest request) {
        SimplesCompositionResult result = compositionService.calculate(
                request.toCommand());

        return SimplesCompositionResponse.from(
                result);
    }
}