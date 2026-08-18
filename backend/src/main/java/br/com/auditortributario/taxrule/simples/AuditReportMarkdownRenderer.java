package br.com.auditortributario.taxrule.simples;

import java.util.Objects;

public final class AuditReportMarkdownRenderer {

    public String render(
            AuditReport report) {
        Objects.requireNonNull(
                report,
                "O relatório não pode ser nulo.");

        StringBuilder markdown = new StringBuilder();

        markdown.append("# ");
        markdown.append(
                report.title());
        markdown.append("\n\n");

        markdown.append("**Competência:** ");
        markdown.append(
                AuditReportValueFormatter
                        .formatPeriod(
                                report.assessmentPeriod()));
        markdown.append("\n\n");

        markdown.append("## Resumo executivo\n\n");
        markdown.append(
                report.executiveSummary());
        markdown.append("\n\n");

        for (AuditReportSection section : report.sections()) {

            markdown.append("## ");
            markdown.append(
                    section.title());
            markdown.append("\n\n");

            markdown.append(
                    section.content());

            markdown.append("\n\n");
        }

        markdown.append(
                "## Referências e critérios\n\n");

        if (report.references().isEmpty()) {
            markdown.append(
                    "Nenhuma referência registrada.");
        } else {
            for (String reference : report.references()) {

                markdown.append("- ");
                markdown.append(
                        reference);
                markdown.append('\n');
            }
        }

        return markdown
                .toString()
                .strip();
    }
}