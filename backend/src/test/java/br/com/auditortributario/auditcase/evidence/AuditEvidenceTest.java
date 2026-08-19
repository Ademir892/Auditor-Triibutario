package br.com.auditortributario.auditcase.evidence;

import br.com.auditortributario.auditcase.AuditCase;
import br.com.auditortributario.auditcase.AuditCaseType;
import br.com.auditortributario.auditcase.AuditPeriod;
import br.com.auditortributario.auditcase.TaxRegime;
import br.com.auditortributario.auditcase.document.AuditDocument;
import br.com.auditortributario.auditcase.document.AuditDocumentSource;
import br.com.auditortributario.auditcase.document.AuditDocumentType;
import br.com.auditortributario.auditcase.subject.AuditedSubject;
import br.com.auditortributario.auditcase.subject.AuditedSubjectType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditEvidenceTest {

    @Test
    void shouldCreateManualEvidenceForCompetence() {
        AuditCase auditCase = createCase();

        AuditEvidence evidence = AuditEvidence.manualForCompetence(
                auditCase,
                YearMonth.of(
                        2026,
                        8),
                "GUIDE_AMOUNT",
                "Valor informado na guia",
                AuditEvidenceDataType.MONEY,
                "1752.00");

        assertEquals(
                "GUIDE_AMOUNT",
                evidence.code());

        assertEquals(
                AuditEvidenceOrigin.MANUAL_ENTRY,
                evidence.origin());

        assertTrue(
                evidence.documentId().isEmpty());

        assertTrue(
                evidence.competence().isPresent());

        assertFalse(
                evidence.confidence().isPresent());
    }

    @Test
    void shouldCreateEvidenceExtractedByOcr() {
        AuditCase auditCase = createCase();

        AuditDocument document = AuditDocument.forCompetence(
                auditCase,
                YearMonth.of(
                        2026,
                        8),
                AuditDocumentType.TAX_GUIDE,
                AuditDocumentSource.USER_UPLOAD,
                "DAS agosto 2026",
                Optional.empty());

        AuditEvidence evidence = AuditEvidence.fromDocument(
                auditCase,
                document,
                "GUIDE_AMOUNT",
                "Valor encontrado na guia",
                AuditEvidenceDataType.MONEY,
                "R$ 1.752,00",
                "1752.00",
                AuditEvidenceOrigin.OCR,
                new BigDecimal(
                        "0.987"));

        assertEquals(
                AuditEvidenceOrigin.OCR,
                evidence.origin());

        assertEquals(
                "R$ 1.752,00",
                evidence.rawValue());

        assertEquals(
                "1752.00",
                evidence.normalizedValue());

        assertEquals(
                new BigDecimal(
                        "0.987"),
                evidence
                        .confidence()
                        .orElseThrow());

        assertEquals(
                document.id(),
                evidence
                        .documentId()
                        .orElseThrow());
    }

    @Test
    void shouldRejectEvidenceFromDocumentOfAnotherCase() {
        AuditCase firstCase = createCase();

        AuditCase secondCase = createCase();

        AuditDocument document = AuditDocument.forCase(
                firstCase,
                AuditDocumentType.DECLARATION,
                AuditDocumentSource.USER_UPLOAD,
                "Declaração",
                Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> AuditEvidence.fromDocument(
                        secondCase,
                        document,
                        "TEST",
                        "Teste",
                        AuditEvidenceDataType.TEXT,
                        "valor",
                        "valor",
                        AuditEvidenceOrigin.DOCUMENT_TEXT,
                        BigDecimal.ONE));
    }

    @Test
    void shouldRejectInvalidConfidence() {
        AuditCase auditCase = createCase();

        AuditDocument document = AuditDocument.forCase(
                auditCase,
                AuditDocumentType.DECLARATION,
                AuditDocumentSource.USER_UPLOAD,
                "Declaração",
                Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> AuditEvidence.fromDocument(
                        auditCase,
                        document,
                        "TEST",
                        "Teste",
                        AuditEvidenceDataType.TEXT,
                        "valor",
                        "valor",
                        AuditEvidenceOrigin.OCR,
                        new BigDecimal(
                                "1.01")));
    }

    private AuditCase createCase() {
        AuditedSubject subject = AuditedSubject
                .createWithoutTaxIdentifier(
                        AuditedSubjectType.BUSINESS,
                        "Empresa Exemplo Ltda.");

        return AuditCase.create(
                subject,
                AuditCaseType.ANNUAL,
                TaxRegime.SIMPLES_NACIONAL,
                AuditPeriod.annual(
                        2026));
    }
}