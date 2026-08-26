package br.com.auditortributario.taxrule.simples.revenue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesSublimitTaxContextTest {

    @Test
    void shouldCreateStandardContextWithoutEvaluation() {
        SimplesSublimitTaxContext context = SimplesSublimitTaxContext.standard();

        assertFalse(
                context.hasEvaluation());

        assertFalse(
                context.treatment()
                        .hasExternalObligation());
    }

    @Test
    void shouldCreateOutsideDasContextWithoutEvaluation() {
        SimplesSublimitTaxContext context = SimplesSublimitTaxContext.outsideDas();

        assertFalse(
                context.hasEvaluation());

        assertTrue(
                context.treatment()
                        .hasExternalObligation());
    }
}