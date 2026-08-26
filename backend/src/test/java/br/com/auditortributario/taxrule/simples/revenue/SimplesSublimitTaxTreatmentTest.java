package br.com.auditortributario.taxrule.simples.revenue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesSublimitTaxTreatmentTest {

    @Test
    void shouldNotRequireExternalObligationForStandardTreatment() {
        assertFalse(
                SimplesSublimitTaxTreatment.IN_DAS_STANDARD
                        .hasExternalObligation());
    }

    @Test
    void shouldNotRequireExternalObligationForTransitionalTreatment() {
        assertFalse(
                SimplesSublimitTaxTreatment.IN_DAS_TRANSITIONAL
                        .hasExternalObligation());
    }

    @Test
    void shouldRequireExternalObligationForOutsideDasTreatment() {
        assertTrue(
                SimplesSublimitTaxTreatment.OUTSIDE_DAS
                        .hasExternalObligation());
    }
}