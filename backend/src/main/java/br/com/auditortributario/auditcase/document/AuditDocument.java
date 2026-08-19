package br.com.auditortributario.auditcase.document;

import br.com.auditortributario.auditcase.AuditCase;
import br.com.auditortributario.auditcase.AuditCaseId;

import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record AuditDocument(
        AuditDocumentId id,
        AuditCaseId auditCaseId,
        AuditDocumentType type,
        AuditDocumentSource source,
        String displayName,
        Optional<YearMonth> competence,
        Optional<AuditDocumentMetadata> metadata,
        AuditDocumentStatus status,
        List<AuditDocumentEvent> history,
        Instant registeredAt) {

    public AuditDocument {
        Objects.requireNonNull(
                id,
                "O identificador do documento não pode ser nulo.");

        Objects.requireNonNull(
                auditCaseId,
                "O identificador do caso não pode ser nulo.");

        Objects.requireNonNull(
                type,
                "O tipo do documento não pode ser nulo.");

        Objects.requireNonNull(
                source,
                "A origem do documento não pode ser nula.");

        Objects.requireNonNull(
                displayName,
                "O nome do documento não pode ser nulo.");

        Objects.requireNonNull(
                competence,
                "A competência opcional não pode ser nula.");

        Objects.requireNonNull(
                metadata,
                "Os metadados opcionais não podem ser nulos.");

        Objects.requireNonNull(
                status,
                "O status do documento não pode ser nulo.");

        Objects.requireNonNull(
                history,
                "O histórico não pode ser nulo.");

        Objects.requireNonNull(
                registeredAt,
                "A data de registro não pode ser nula.");

        displayName = displayName.trim();

        if (displayName.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do documento não pode estar vazio.");
        }

        history = List.copyOf(
                history);

        if (history.isEmpty()) {
            throw new IllegalArgumentException(
                    "O documento deve possuir histórico.");
        }
    }

    public static AuditDocument forCase(
            AuditCase auditCase,
            AuditDocumentType type,
            AuditDocumentSource source,
            String displayName,
            Optional<AuditDocumentMetadata> metadata) {
        Objects.requireNonNull(
                auditCase,
                "O caso de auditoria não pode ser nulo.");

        return create(
                auditCase,
                type,
                source,
                displayName,
                Optional.empty(),
                metadata);
    }

    public static AuditDocument forCompetence(
            AuditCase auditCase,
            YearMonth competence,
            AuditDocumentType type,
            AuditDocumentSource source,
            String displayName,
            Optional<AuditDocumentMetadata> metadata) {
        Objects.requireNonNull(
                auditCase,
                "O caso de auditoria não pode ser nulo.");

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

        return create(
                auditCase,
                type,
                source,
                displayName,
                Optional.of(
                        competence),
                metadata);
    }

    public boolean isCaseLevel() {
        return competence.isEmpty();
    }

    public boolean isCompetenceLevel() {
        return competence.isPresent();
    }

    public boolean belongsTo(
            YearMonth period) {
        Objects.requireNonNull(
                period,
                "A competência pesquisada não pode ser nula.");

        return competence
                .map(
                        value -> value.equals(
                                period))
                .orElse(
                        false);
    }

    public AuditDocument markAvailable() {
        return changeStatus(
                AuditDocumentStatus.AVAILABLE,
                "Documento disponível para processamento.");
    }

    public AuditDocument startProcessing() {
        return changeStatus(
                AuditDocumentStatus.PROCESSING,
                "Processamento do documento iniciado.");
    }

    public AuditDocument markProcessed() {
        return changeStatus(
                AuditDocumentStatus.PROCESSED,
                "Documento processado com sucesso.");
    }

    public AuditDocument markFailed(
            String reason) {
        Objects.requireNonNull(
                reason,
                "O motivo da falha não pode ser nulo.");

        String normalizedReason = reason.trim();

        if (normalizedReason.isBlank()) {
            throw new IllegalArgumentException(
                    "O motivo da falha não pode estar vazio.");
        }

        return changeStatus(
                AuditDocumentStatus.FAILED,
                "Falha no processamento: "
                        + normalizedReason);
    }

    private static AuditDocument create(
            AuditCase auditCase,
            AuditDocumentType type,
            AuditDocumentSource source,
            String displayName,
            Optional<YearMonth> competence,
            Optional<AuditDocumentMetadata> metadata) {
        Objects.requireNonNull(
                metadata,
                "Os metadados opcionais não podem ser nulos.");

        Instant registeredAt = Instant.now();

        List<AuditDocumentEvent> history = List.of(
                new AuditDocumentEvent(
                        AuditDocumentStatus.REGISTERED,
                        registeredAt,
                        "Documento registrado no caso de auditoria."));

        return new AuditDocument(
                AuditDocumentId.generate(),
                auditCase.id(),
                type,
                source,
                displayName,
                competence,
                metadata,
                AuditDocumentStatus.REGISTERED,
                history,
                registeredAt);
    }

    private AuditDocument changeStatus(
            AuditDocumentStatus nextStatus,
            String description) {
        if (!canTransitionTo(
                nextStatus)) {
            throw new IllegalStateException(
                    "Transição inválida de "
                            + status
                            + " para "
                            + nextStatus
                            + ".");
        }

        List<AuditDocumentEvent> updatedHistory = new ArrayList<>(
                history);

        updatedHistory.add(
                new AuditDocumentEvent(
                        nextStatus,
                        Instant.now(),
                        description));

        return new AuditDocument(
                id,
                auditCaseId,
                type,
                source,
                displayName,
                competence,
                metadata,
                nextStatus,
                updatedHistory,
                registeredAt);
    }

    private boolean canTransitionTo(
            AuditDocumentStatus nextStatus) {
        return switch (status) {

            case REGISTERED ->
                nextStatus == AuditDocumentStatus.AVAILABLE
                        || nextStatus == AuditDocumentStatus.FAILED;

            case AVAILABLE ->
                nextStatus == AuditDocumentStatus.PROCESSING
                        || nextStatus == AuditDocumentStatus.FAILED;

            case PROCESSING ->
                nextStatus == AuditDocumentStatus.PROCESSED
                        || nextStatus == AuditDocumentStatus.FAILED;

            case FAILED ->
                nextStatus == AuditDocumentStatus.PROCESSING;

            case PROCESSED ->
                false;
        };
    }
}