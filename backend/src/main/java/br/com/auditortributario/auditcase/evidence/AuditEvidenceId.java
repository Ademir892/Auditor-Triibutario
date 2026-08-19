package br.com.auditortributario.auditcase.evidence;

import java.util.Objects;
import java.util.UUID;

public record AuditEvidenceId(
        UUID value) {

    public AuditEvidenceId {
        Objects.requireNonNull(
                value,
                "O identificador da evidência não pode ser nulo.");
    }

    public static AuditEvidenceId generate() {
        return new AuditEvidenceId(
                UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}