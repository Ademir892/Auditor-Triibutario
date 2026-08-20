package br.com.auditortributario.taxrule.domain.revenue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompetenceRevenueTest {

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    @Test
    void shouldAggregateSegregatedRevenue() {
        CompetenceRevenue revenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        service(
                                "35000.00",
                                true),
                        commerce(
                                "25000.00",
                                Set.of()),
                        commerce(
                                "20000.00",
                                Set.of(
                                        RevenueTaxTreatment.MONOPHASIC)),
                        commerce(
                                "20000.00",
                                Set.of(
                                        RevenueTaxTreatment.TAX_SUBSTITUTION))));

        assertEquals(
                new BigDecimal(
                        "100000.00"),
                revenue.totalAmount());

        assertEquals(
                4,
                revenue.numberOfEntries());
    }

    @Test
    void shouldCalculateRevenueByActivity() {
        CompetenceRevenue revenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        service(
                                "35000.00",
                                true),
                        commerce(
                                "65000.00",
                                Set.of())));

        assertEquals(
                new BigDecimal(
                        "35000.00"),
                revenue.amountByActivity(
                        RevenueActivityType.SERVICE));

        assertEquals(
                new BigDecimal(
                        "65000.00"),
                revenue.amountByActivity(
                        RevenueActivityType.COMMERCE));

        assertTrue(
                revenue.hasMultipleActivities());
    }

    @Test
    void shouldCalculateAmountSubjectToFatorR() {
        CompetenceRevenue revenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        service(
                                "30000.00",
                                true),
                        service(
                                "20000.00",
                                false),
                        commerce(
                                "50000.00",
                                Set.of())));

        assertEquals(
                new BigDecimal(
                        "30000.00"),
                revenue.amountSubjectToFatorR());
    }

    @Test
    void shouldCalculateAmountBySpecialTreatment() {
        CompetenceRevenue revenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        commerce(
                                "20000.00",
                                Set.of(
                                        RevenueTaxTreatment.MONOPHASIC)),
                        commerce(
                                "15000.00",
                                Set.of(
                                        RevenueTaxTreatment.MONOPHASIC,
                                        RevenueTaxTreatment.REDUCTION)),
                        commerce(
                                "10000.00",
                                Set.of(
                                        RevenueTaxTreatment.TAX_SUBSTITUTION)),
                        commerce(
                                "55000.00",
                                Set.of())));

        assertEquals(
                new BigDecimal(
                        "35000.00"),
                revenue.amountWithTreatment(
                        RevenueTaxTreatment.MONOPHASIC));

        assertEquals(
                new BigDecimal(
                        "10000.00"),
                revenue.amountWithTreatment(
                        RevenueTaxTreatment.TAX_SUBSTITUTION));

        assertEquals(
                new BigDecimal(
                        "15000.00"),
                revenue.amountWithTreatment(
                        RevenueTaxTreatment.REDUCTION));

        assertTrue(
                revenue.hasSpecialTaxTreatments());
    }

    @Test
    void shouldRepresentEmptyCompetenceRevenue() {
        CompetenceRevenue revenue = new CompetenceRevenue(
                COMPETENCE,
                List.of());

        assertEquals(
                BigDecimal.ZERO,
                revenue.totalAmount());

        assertEquals(
                0,
                revenue.numberOfEntries());

        assertFalse(
                revenue.hasMultipleActivities());

        assertFalse(
                revenue.hasSpecialTaxTreatments());
    }

    @Test
    void shouldRejectEntryFromDifferentCompetence() {
        RevenueEntry invalidRevenue = RevenueEntry.standard(
                YearMonth.of(
                        2026,
                        7),
                new BigDecimal(
                        "1000.00"),
                RevenueActivityType.SERVICE,
                true,
                RevenueOrigin.MANUAL_ENTRY,
                "Receita de julho");

        assertThrows(
                IllegalArgumentException.class,
                () -> new CompetenceRevenue(
                        COMPETENCE,
                        List.of(
                                invalidRevenue)));
    }

    private RevenueEntry service(
            String amount,
            boolean subjectToFatorR) {
        return RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        amount),
                RevenueActivityType.SERVICE,
                subjectToFatorR,
                RevenueOrigin.MANUAL_ENTRY,
                "Receita de serviços");
    }

    private RevenueEntry commerce(
            String amount,
            Set<RevenueTaxTreatment> treatments) {
        return RevenueEntry.create(
                COMPETENCE,
                new BigDecimal(
                        amount),
                RevenueActivityType.COMMERCE,
                false,
                treatments,
                RevenueOrigin.MANUAL_ENTRY,
                "Receita de comércio");
    }
}