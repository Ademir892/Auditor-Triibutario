package br.com.auditortributario.auditcase.subject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaxIdentifierTest {

    @Test
    void shouldNormalizeCpf() {
        TaxIdentifier identifier = TaxIdentifier.cpf(
                "123.456.789-09");

        assertEquals(
                TaxIdentifierType.CPF,
                identifier.type());

        assertEquals(
                "12345678909",
                identifier.value());
    }

    @Test
    void shouldNormalizeTraditionalCnpj() {
        TaxIdentifier identifier = TaxIdentifier.cnpj(
                "12.345.678/0001-90");

        assertEquals(
                TaxIdentifierType.CNPJ,
                identifier.type());

        assertEquals(
                "12345678000190",
                identifier.value());
    }

    @Test
    void shouldAcceptAlphanumericCnpj() {
        TaxIdentifier identifier = TaxIdentifier.cnpj(
                "00.000.000/E08G-12");

        assertEquals(
                TaxIdentifierType.CNPJ,
                identifier.type());

        assertEquals(
                "00000000E08G12",
                identifier.value());
    }

    @Test
    void shouldNormalizeLowercaseAlphanumericCnpj() {
        TaxIdentifier identifier = TaxIdentifier.cnpj(
                "00.000.000/e08g-12");

        assertEquals(
                "00000000E08G12",
                identifier.value());
    }

    @Test
    void shouldRejectInvalidCpfLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TaxIdentifier.cpf(
                        "123"));
    }

    @Test
    void shouldRejectInvalidCnpjLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TaxIdentifier.cnpj(
                        "123"));
    }

    @Test
    void shouldRejectAlphabeticCheckDigitsInCnpj() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TaxIdentifier.cnpj(
                        "00.000.000/E08G-AB"));
    }

    @Test
    void shouldAcceptOtherIdentifier() {
        TaxIdentifier identifier = TaxIdentifier.other(
                " IDENTIFICADOR-EXTERNO-001 ");

        assertEquals(
                TaxIdentifierType.OTHER,
                identifier.type());

        assertEquals(
                "IDENTIFICADOR-EXTERNO-001",
                identifier.value());
    }
}