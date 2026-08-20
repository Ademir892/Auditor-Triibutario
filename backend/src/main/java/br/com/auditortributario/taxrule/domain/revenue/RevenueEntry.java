package br.com.auditortributario.taxrule.domain.revenue;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Set;

public record RevenueEntry(
        RevenueEntryId id,
        YearMonth competence,
        BigDecimal amount,
        RevenueActivityType activityType,
        boolean subjectToFatorR,
        Set<RevenueTaxTreatment> treatments,
        RevenueOrigin origin,
        String description) {

    public RevenueEntry {
        Objects.requireNonNull(
                id,
                "O identificador da receita não pode ser nulo.");

        Objects.requireNonNull(
                competence,
                "A competência da receita não pode ser nula.");

        Objects.requireNonNull(
                amount,
                "O valor da receita não pode ser nulo.");

        Objects.requireNonNull(
                activityType,
                "O tipo de atividade não pode ser nulo.");

        Objects.requireNonNull(
                treatments,
                "Os tratamentos tributários não podem ser nulos.");

        Objects.requireNonNull(
                origin,
                "A origem da receita não pode ser nula.");

        Objects.requireNonNull(
                description,
                "A descrição da receita não pode ser nula.");

        if (amount.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor da receita não pode ser negativo.");
        }

        if (treatments.stream().anyMatch(
                Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Os tratamentos tributários "
                            + "não podem conter valores nulos.");
        }

        treatments = Set.copyOf(
                treatments);

        description = description.trim();

        if (description.isBlank()) {
            throw new IllegalArgumentException(
                    "A descrição da receita não pode estar vazia.");
        }
    }

    public static RevenueEntry create(
            YearMonth competence,
            BigDecimal amount,
            RevenueActivityType activityType,
            boolean subjectToFatorR,
            Set<RevenueTaxTreatment> treatments,
            RevenueOrigin origin,
            String description) {
        return new RevenueEntry(
                RevenueEntryId.generate(),
                competence,
                amount,
                activityType,
                subjectToFatorR,
                treatments,
                origin,
                description);
    }

    public static RevenueEntry standard(
            YearMonth competence,
            BigDecimal amount,
            RevenueActivityType activityType,
            boolean subjectToFatorR,
            RevenueOrigin origin,
            String description) {
        return create(
                competence,
                amount,
                activityType,
                subjectToFatorR,
                Set.of(),
                origin,
                description);
    }

    public boolean hasSpecialTaxTreatment() {
        return !treatments.isEmpty();
    }

    public boolean hasTreatment(
            RevenueTaxTreatment treatment) {
        Objects.requireNonNull(
                treatment,
                "O tratamento pesquisado não pode ser nulo.");

        return treatments.contains(
                treatment);
    }
}