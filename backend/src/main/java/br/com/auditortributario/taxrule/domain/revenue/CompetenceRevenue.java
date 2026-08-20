package br.com.auditortributario.taxrule.domain.revenue;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public record CompetenceRevenue(
        YearMonth competence,
        List<RevenueEntry> entries) {

    public CompetenceRevenue {
        Objects.requireNonNull(
                competence,
                "A competência não pode ser nula.");

        Objects.requireNonNull(
                entries,
                "As receitas não podem ser nulas.");

        if (entries.stream().anyMatch(
                Objects::isNull)) {
            throw new IllegalArgumentException(
                    "As receitas não podem conter valores nulos.");
        }

        for (RevenueEntry entry : entries) {
            if (!entry
                    .competence()
                    .equals(
                            competence)) {

                throw new IllegalArgumentException(
                        "Todas as receitas devem pertencer "
                                + "à competência "
                                + competence
                                + ".");
            }
        }

        entries = List.copyOf(
                entries);
    }

    public BigDecimal totalAmount() {
        return entries
                .stream()
                .map(
                        RevenueEntry::amount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);
    }

    public BigDecimal amountByActivity(
            RevenueActivityType activityType) {
        Objects.requireNonNull(
                activityType,
                "O tipo de atividade não pode ser nulo.");

        return entries
                .stream()
                .filter(
                        entry -> entry.activityType() == activityType)
                .map(
                        RevenueEntry::amount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);
    }

    public BigDecimal amountSubjectToFatorR() {
        return entries
                .stream()
                .filter(
                        RevenueEntry::subjectToFatorR)
                .map(
                        RevenueEntry::amount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);
    }

    public BigDecimal amountWithTreatment(
            RevenueTaxTreatment treatment) {
        Objects.requireNonNull(
                treatment,
                "O tratamento tributário não pode ser nulo.");

        return entries
                .stream()
                .filter(
                        entry -> entry.hasTreatment(
                                treatment))
                .map(
                        RevenueEntry::amount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);
    }

    public boolean hasMultipleActivities() {
        return entries
                .stream()
                .map(
                        RevenueEntry::activityType)
                .distinct()
                .limit(
                        2)
                .count() > 1;
    }

    public boolean hasSpecialTaxTreatments() {
        return entries
                .stream()
                .anyMatch(
                        RevenueEntry::hasSpecialTaxTreatment);
    }

    public int numberOfEntries() {
        return entries.size();
    }
}