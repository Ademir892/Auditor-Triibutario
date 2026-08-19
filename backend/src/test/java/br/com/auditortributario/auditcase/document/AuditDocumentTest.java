package br.com.auditortributario.auditcase.document;

import br.com.auditortributario.auditcase.AuditCase;
import br.com.auditortributario.auditcase.AuditCaseType;
import br.com.auditortributario.auditcase.AuditPeriod;
import br.com.auditortributario.auditcase.TaxRegime;
import br.com.auditortributario.auditcase.subject.AuditedSubject;
import br.com.auditortributario.auditcase.subject.AuditedSubjectType;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditDocumentTest {

    @Test
    void shouldCreateCaseLevelDocument() {
        AuditDocument document = AuditDocument.forCase(
                createCase(),
                AuditDocumentType.DECLARATION,
                AuditDocumentSource.USER_UPLOAD,
                "Declaração anual",
                Optional.empty());

        assertTrue(
                document.isCaseLevel());

        assertFalse(
                document.isCompetenceLevel());

        assertEquals(
                AuditDocumentStatus.REGISTERED,
                document.status());

        assertEquals(
                1,
                document.history().size());
    }

    @Test
    void shouldCreateCompetenceDocument() {
        AuditCase auditCase = createCase();

        YearMonth competence = YearMonth.of(
                2026,
                8);

        AuditDocument document = AuditDocument.forCompetence(
                auditCase,
                competence,
                AuditDocumentType.TAX_GUIDE,
                AuditDocumentSource.USER_UPLOAD,
                "DAS agosto 2026",
                Optional.of(
                        AuditDocumentMetadata.basic(
                                "das-agosto-2026.pdf",
                                "application/pdf")));

        assertTrue(
                document.isCompetenceLevel());

        assertTrue(
                document.belongsTo(
                        competence));
    }

    @Test
    void shouldRejectDocumentOutsideAuditPeriod() {
        AuditCase auditCase = createCase();

        assertThrows(
                IllegalArgumentException.class,
                () -> AuditDocument.forCompetence(
                        auditCase,
                        YearMonth.of(
                                2027,
                                1),
                        AuditDocumentType.TAX_GUIDE,
                        AuditDocumentSource.USER_UPLOAD,
                        "Guia inválida",
                        Optional.empty()));
    }

    @Test
    void shouldTrackDocumentProcessingHistory() {
        AuditDocument registered = AuditDocument.forCase(
                createCase(),
                AuditDocumentType.DECLARATION,
                AuditDocumentSource.USER_UPLOAD,
                "Declaração",
                Optional.empty());

        AuditDocument available = registered.markAvailable();

        AuditDocument processing = available.startProcessing();

        AuditDocument processed = processing.markProcessed();

        assertEquals(
                AuditDocumentStatus.PROCESSED,
                processed.status());

        assertEquals(
                4,
                processed.history().size());

        assertEquals(
                AuditDocumentStatus.REGISTERED,
                processed
                        .history()
                        .getFirst()
                        .status());

        assertEquals(
                AuditDocumentStatus.PROCESSED,
                processed
                        .history()
                        .getLast()
                        .status());
    }

    @Test
    void shouldRejectInvalidDocumentTransition() {
        AuditDocument document = AuditDocument.forCase(
                createCase(),
                AuditDocumentType.DECLARATION,
                AuditDocumentSource.USER_UPLOAD,
                "Declaração",
                Optional.empty());

        assertThrows(
                IllegalStateException.class,
                document::markProcessed);
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