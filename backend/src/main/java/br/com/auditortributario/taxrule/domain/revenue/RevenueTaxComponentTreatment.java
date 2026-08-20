package br.com.auditortributario.taxrule.domain.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;

import java.util.Objects;

public record RevenueTaxComponentTreatment(
                RevenueTaxTreatment treatment,
                TaxComponent component,
                RevenueTaxTreatmentEffect effect,
                String explanation) {

        public RevenueTaxComponentTreatment {
                Objects.requireNonNull(
                                treatment,
                                "O tratamento tributário não pode ser nulo.");

                Objects.requireNonNull(
                                component,
                                "O componente tributário não pode ser nulo.");

                Objects.requireNonNull(
                                effect,
                                "O efeito tributário não pode ser nulo.");

                Objects.requireNonNull(
                                explanation,
                                "A explicação não pode ser nula.");

                explanation = explanation.trim();

                if (explanation.isBlank()) {
                        throw new IllegalArgumentException(
                                        "A explicação não pode estar vazia.");
                }
        }
}