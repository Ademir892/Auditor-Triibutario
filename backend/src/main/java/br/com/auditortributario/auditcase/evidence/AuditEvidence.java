package br.com.auditortributario.auditcase.evidence;

import br.com.auditortributario.auditcase.AuditCase;
import br.com.auditortributario.auditcase.AuditCaseId;
import br.com.auditortributario.auditcase.document.AuditDocument;
import br.com.auditortributario.auditcase.document.AuditDocumentId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Optional;

public record AuditEvidence(
        AuditEvidenceId id,
        AuditCaseId auditCaseId,
        Optional<AuditDocumentId> documentId,
        Optional<YearMonth> competence,
        String code,
        String description,
        AuditEvidenceDataType dataType,
        String rawValue,
        String normalizedValue,
        AuditEvidenceOrigin origin,
        Optional<BigDecimal> confidence,
        Instant registeredAt) {

    public AuditEvidence {
        Objects.requireNonNull(
                id,
                "O identificador da evidência não pode ser nulo.");

        Objects.requireNonNull(
                auditCaseId,
                "O identificador do caso não pode ser nulo.");

        Objects.requireNonNull(
                documentId,
                "O documento opcional não pode ser nulo.");

        Objects.requireNonNull(
                competence,
                "A competência opcional não pode ser nula.");

        Objects.requireNonNull(
                code,
                "O código da evidência não pode ser nulo.");

        Objects.requireNonNull(
                description,
                "A descrição da evidência não pode ser nula.");

        Objects.requireNonNull(
                dataType,
                "O tipo do valor não pode ser nulo.");

        Objects.requireNonNull(
                rawValue,
                "O valor bruto não pode ser nulo.");

        Objects.requireNonNull(
                normalizedValue,
                "O valor normalizado não pode ser nulo.");

        Objects.requireNonNull(
                origin,
                "A origem da evidência não pode ser nula.");

        Objects.requireNonNull(
                confidence,
                "A confiança opcional não pode ser nula.");

        Objects.requireNonNull(
                registeredAt,
                "A data de registro não pode ser nula.");

        code = requireText(
                code,
                "O código da evidência não pode estar vazio.");

        description = requireText(
                description,
                "A descrição da evidência não pode estar vazia.");

        rawValue = requireText(
                rawValue,
                "O valor bruto não pode estar vazio.");

        normalizedValue = requireText(
                normalizedValue,
                "O valor normalizado não pode estar vazio.");

        confidence.ifPresent(
                value -> {
                    if (value.compareTo(
                            BigDecimal.ZERO) < 0
                            || value.compareTo(
                                    BigDecimal.ONE) > 0) {

                        throw new IllegalArgumentException(
                                "A confiança deve estar entre 0 e 1.");
                    }
                });
    }

    public static AuditEvidence manualForCompetence(
            AuditCase auditCase,
            YearMonth competence,
            String code,
            String description,
            AuditEvidenceDataType dataType,
            String value) {
        Objects.requireNonNull(
                auditCase,
                "O caso de auditoria não pode ser nulo.");

        validateCompetence(
                auditCase,
                competence);

        return new AuditEvidence(
                AuditEvidenceId.generate(),
                auditCase.id(),
                Optional.empty(),
                Optional.of(
                        competence),
                code,
                description,
                dataType,
                value,
                value,
                AuditEvidenceOrigin.MANUAL_ENTRY,
                Optional.empty(),
                Instant.now());
    }

    public static AuditEvidence fromDocument(
            AuditCase auditCase,
            AuditDocument document,
            String code,
            String description,
            AuditEvidenceDataType dataType,
            String rawValue,
            String normalizedValue,
            AuditEvidenceOrigin origin,
            BigDecimal confidence) {
        Objects.requireNonNull(
                auditCase,
                "O caso de auditoria não pode ser nulo.");

        Objects.requireNonNull(
                document,
                "O documento não pode ser nulo.");

        if (!document
                .auditCaseId()
                .equals(
                        auditCase.id())) {

            throw new IllegalArgumentException(
                    "O documento não pertence ao caso de auditoria.");
        }

        return new AuditEvidence(
                AuditEvidenceId.generate(),
                auditCase.id(),
                Optional.of(
                        document.id()),
                document.competence(),
                code,
                description,
                dataType,
                rawValue,
                normalizedValue,
                origin,
                Optional.ofNullable(
                        confidence),
                Instant.now());
    }

    private static void validateCompetence(
            AuditCase auditCase,
            YearMonth competence) {
        Objects.requireNonNull(
                competence,
                "A competência não pode ser nula.");

        if (auditCase
                .findCompetence(
                        competence)
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "A competência "
                            + competence
                            + " não pertence ao caso de auditoria.");
        }
    }

    private static String requireText(
            String value,
            String message) {
        String normalized = value.trim();

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                    message);
        }

        return normalized;
    }
}