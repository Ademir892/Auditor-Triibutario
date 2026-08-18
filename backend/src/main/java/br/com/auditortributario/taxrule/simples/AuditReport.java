package br.com.auditortributario.taxrule.simples;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record AuditReport(
        String title,
        YearMonth assessmentPeriod,
        String executiveSummary,
        List<AuditReportSection> sections,
        List<String> references) {

    public AuditReport {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "O título do relatório não pode ser nulo ou vazio.");
        }

        Objects.requireNonNull(
                assessmentPeriod,
                "A competência do relatório não pode ser nula.");

        if (executiveSummary == null
                || executiveSummary.isBlank()) {

            throw new IllegalArgumentException(
                    "O resumo executivo não pode ser nulo ou vazio.");
        }

        Objects.requireNonNull(
                sections,
                "As seções do relatório não podem ser nulas.");

        Objects.requireNonNull(
                references,
                "As referências do relatório não podem ser nulas.");

        sections = List.copyOf(
                sections);

        references = List.copyOf(
                references);
    }

    public Optional<AuditReportSection> findSection(
            String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        return sections.stream()
                .filter(
                        section -> section.code().equals(code))
                .findFirst();
    }
}