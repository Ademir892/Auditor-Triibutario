package br.com.auditortributario.taxrule.simples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthlyRevenueTest {

    @Test
    void shouldCreateMonthlyRevenue() {
        MonthlyRevenue revenue = new MonthlyRevenue(
                YearMonth.of(2026, 3),
                new BigDecimal("10000.00"));

        assertEquals(
                YearMonth.of(2026, 3),
                revenue.period());

        assertEquals(
                new BigDecimal("10000.00"),
                revenue.amount());
    }

    @Test
    void shouldAcceptZeroRevenue() {
        MonthlyRevenue revenue = new MonthlyRevenue(
                YearMonth.of(2026, 3),
                BigDecimal.ZERO);

        assertEquals(
                new BigDecimal("0.00"),
                revenue.amount());
    }

    @Test
    void shouldRejectNegativeRevenue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MonthlyRevenue(
                        YearMonth.of(2026, 3),
                        new BigDecimal("-1.00")));
    }

    @Test
    void shouldRejectFractionOfCent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MonthlyRevenue(
                        YearMonth.of(2026, 3),
                        new BigDecimal("100.001")));
    }

    @Test
    void shouldRejectNullPeriod() {
        assertThrows(
                NullPointerException.class,
                () -> new MonthlyRevenue(
                        null,
                        new BigDecimal("100.00")));
    }

    @Test
    void shouldRejectNullAmount() {
        assertThrows(
                NullPointerException.class,
                () -> new MonthlyRevenue(
                        YearMonth.of(2026, 3),
                        null));
    }
}