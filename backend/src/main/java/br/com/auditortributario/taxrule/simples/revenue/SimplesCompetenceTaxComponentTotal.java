package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record SimplesCompetenceTaxComponentTotal(
        TaxComponent component,
        BigDecimal amount
) {

    public SimplesCompetenceTaxComponentTotal {
        Objects.requireNonNull(
                component,
                "O componente tributário não pode ser nulo."
        );

        Objects.requireNonNull(
                amount,
                "O valor do componente não pode ser nulo."
        );

        if (amount.compareTo(
                BigDecimal.ZERO
        ) < 0) {
            throw new IllegalArgumentException(
                    "O valor consolidado do componente "
                            + "não pode ser negativo."
            );
        }
    }

    public BigDecimal amountForDisplay() {
        return amount.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
}