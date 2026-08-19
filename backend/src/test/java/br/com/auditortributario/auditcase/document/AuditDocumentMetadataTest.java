package br.com.auditortributario.auditcase.document;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditDocumentMetadataTest {

    @Test
    void shouldCreateBasicMetadata() {
        AuditDocumentMetadata metadata = AuditDocumentMetadata.basic(
                " guia.pdf ",
                " APPLICATION/PDF ");

        assertEquals(
                "guia.pdf",
                metadata.originalFileName());

        assertEquals(
                "application/pdf",
                metadata.mediaType());

        assertTrue(
                metadata.sizeBytes().isEmpty());

        assertTrue(
                metadata.sha256().isEmpty());
    }

    @Test
    void shouldCreateCompleteMetadata() {
        String sha256 = "aaaaaaaaaaaaaaaa"
                + "aaaaaaaaaaaaaaaa"
                + "aaaaaaaaaaaaaaaa"
                + "aaaaaaaaaaaaaaaa";

        AuditDocumentMetadata metadata = AuditDocumentMetadata.complete(
                "guia.pdf",
                "application/pdf",
                2048,
                sha256);

        assertEquals(
                2048L,
                metadata
                        .sizeBytes()
                        .orElseThrow());

        assertEquals(
                sha256,
                metadata
                        .sha256()
                        .orElseThrow());
    }

    @Test
    void shouldRejectInvalidSha256() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AuditDocumentMetadata.complete(
                        "guia.pdf",
                        "application/pdf",
                        2048,
                        "invalido"));
    }
}