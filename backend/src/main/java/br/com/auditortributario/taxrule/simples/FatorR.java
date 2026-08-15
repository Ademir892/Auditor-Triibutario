package br.com.auditortributario.taxrule.simples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record FatorR(BigDecimal value) {

    private static final BigDecimal ANEXO_III_THRESHOLD =
            new BigDecimal("0.28");

    private static final int SCALE = 2;

    public FatorR {
        Objects.requireNonNull(value, "O Fator R não pode ser nulo.");

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O Fator R não pode ser negativo."
            );
        }

        value = value.setScale(SCALE, RoundingMode.DOWN);
    }

    public SimplesAnnex getApplicableAnnex() {
        if (value.compareTo(ANEXO_III_THRESHOLD) >= 0) {
            return SimplesAnnex.ANEXO_III;
        }

        return SimplesAnnex.ANEXO_V;
    }

    public BigDecimal asPercentage() {
        return value
                .movePointRight(2)
                .setScale(2, RoundingMode.DOWN);
    }
}
