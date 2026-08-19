package br.com.auditortributario.auditcase.document;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public record AuditDocumentMetadata(
        String originalFileName,
        String mediaType,
        Optional<Long> sizeBytes,
        Optional<String> sha256) {

    public AuditDocumentMetadata {
        Objects.requireNonNull(
                originalFileName,
                "O nome original do arquivo não pode ser nulo.");

        Objects.requireNonNull(
                mediaType,
                "O tipo de mídia não pode ser nulo.");

        Objects.requireNonNull(
                sizeBytes,
                "O tamanho opcional não pode ser nulo.");

        Objects.requireNonNull(
                sha256,
                "O checksum opcional não pode ser nulo.");

        originalFileName = originalFileName.trim();

        mediaType = mediaType.trim()
                .toLowerCase(
                        Locale.ROOT);

        if (originalFileName.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome original do arquivo não pode estar vazio.");
        }

        if (mediaType.isBlank()) {
            throw new IllegalArgumentException(
                    "O tipo de mídia não pode estar vazio.");
        }

        sizeBytes.ifPresent(
                size -> {
                    if (size < 0) {
                        throw new IllegalArgumentException(
                                "O tamanho do arquivo não pode ser negativo.");
                    }
                });

        sha256 = sha256.map(
                value -> {
                    String normalized = value.trim()
                            .toLowerCase(
                                    Locale.ROOT);

                    if (!normalized.matches(
                            "[a-f0-9]{64}")) {
                        throw new IllegalArgumentException(
                                "O SHA-256 deve possuir "
                                        + "64 caracteres hexadecimais.");
                    }

                    return normalized;
                });
    }

    public static AuditDocumentMetadata basic(
            String originalFileName,
            String mediaType) {
        return new AuditDocumentMetadata(
                originalFileName,
                mediaType,
                Optional.empty(),
                Optional.empty());
    }

    public static AuditDocumentMetadata complete(
            String originalFileName,
            String mediaType,
            long sizeBytes,
            String sha256) {
        return new AuditDocumentMetadata(
                originalFileName,
                mediaType,
                Optional.of(
                        sizeBytes),
                Optional.of(
                        sha256));
    }
}