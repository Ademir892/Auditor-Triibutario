package br.com.auditortributario.auditcase.subject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditedSubjectTest {

    @Test
    void shouldCreateBusinessWithCnpj() {
        AuditedSubject subject = AuditedSubject.create(
                AuditedSubjectType.BUSINESS,
                "Empresa Exemplo Ltda.",
                TaxIdentifier.cnpj(
                        "00.000.000/E08G-12"));

        assertNotNull(
                subject.id());

        assertEquals(
                AuditedSubjectType.BUSINESS,
                subject.type());

        assertEquals(
                "Empresa Exemplo Ltda.",
                subject.displayName());

        assertTrue(
                subject.hasTaxIdentifier());

        assertEquals(
                TaxIdentifierType.CNPJ,
                subject
                        .taxIdentifier()
                        .orElseThrow()
                        .type());
    }

    @Test
    void shouldCreateIndividualWithCpf() {
        AuditedSubject subject = AuditedSubject.create(
                AuditedSubjectType.INDIVIDUAL,
                "Pessoa Exemplo",
                TaxIdentifier.cpf(
                        "123.456.789-09"));

        assertEquals(
                AuditedSubjectType.INDIVIDUAL,
                subject.type());

        assertTrue(
                subject.hasTaxIdentifier());

        assertEquals(
                "12345678909",
                subject
                        .taxIdentifier()
                        .orElseThrow()
                        .value());
    }

    @Test
    void shouldCreateSubjectWithoutTaxIdentifier() {
        AuditedSubject subject = AuditedSubject
                .createWithoutTaxIdentifier(
                        AuditedSubjectType.OTHER,
                        "Auditoria sem identificação inicial");

        assertNotNull(
                subject.id());

        assertFalse(
                subject.hasTaxIdentifier());

        assertTrue(
                subject
                        .taxIdentifier()
                        .isEmpty());
    }

    @Test
    void shouldTrimDisplayName() {
        AuditedSubject subject = AuditedSubject
                .createWithoutTaxIdentifier(
                        AuditedSubjectType.INDIVIDUAL,
                        "  Pessoa Exemplo  ");

        assertEquals(
                "Pessoa Exemplo",
                subject.displayName());
    }

    @Test
    void shouldRejectBlankDisplayName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AuditedSubject
                        .createWithoutTaxIdentifier(
                                AuditedSubjectType.INDIVIDUAL,
                                "   "));
    }

    @Test
    void shouldCreateAndRestoreSubjectId() {
        AuditedSubjectId generated = AuditedSubjectId.generate();

        AuditedSubjectId restored = AuditedSubjectId.from(
                generated.toString());

        assertEquals(
                generated,
                restored);
    }
}