package br.com.auditortributario.taxrule.simples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FatorRTest {

    @Test
    void shouldUseAnnexVWhenFatorRIsBelowTwentyEightPercent() {
        FatorR fatorR = new FatorR(new BigDecimal("0.25"));

        assertEquals(
                SimplesAnnex.ANEXO_V,
                fatorR.getApplicableAnnex()
        );
    }

    @Test
    void shouldUseAnnexIIIWhenFatorRIsExactlyTwentyEightPercent() {
        FatorR fatorR = new FatorR(new BigDecimal("0.28"));

        assertEquals(
                SimplesAnnex.ANEXO_III,
                fatorR.getApplicableAnnex()
        );
    }

    @Test
    void shouldUseAnnexIIIWhenFatorRIsAboveTwentyEightPercent() {
        FatorR fatorR = new FatorR(new BigDecimal("0.30"));

        assertEquals(
                SimplesAnnex.ANEXO_III,
                fatorR.getApplicableAnnex()
        );
    }

    @Test
    void shouldTruncateFatorRToTwoDecimalPlacesWithoutRounding() {
        FatorR fatorR = new FatorR(new BigDecimal("0.2774"));

        assertEquals(
                new BigDecimal("0.27"),
                fatorR.value()
        );

        assertEquals(
                SimplesAnnex.ANEXO_V,
                fatorR.getApplicableAnnex()
        );
    }

    @Test
    void shouldReturnFatorRAsPercentage() {
        FatorR fatorR = new FatorR(new BigDecimal("0.28"));

        assertEquals(
                new BigDecimal("28.00"),
                fatorR.asPercentage()
        );
    }

    @Test
    void shouldRejectNegativeFatorR() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FatorR(new BigDecimal("-0.01"))
        );
    }

    @Test
    void shouldRejectNullFatorR() {
        assertThrows(
                NullPointerException.class,
                () -> new FatorR(null)
        );
    }
}
