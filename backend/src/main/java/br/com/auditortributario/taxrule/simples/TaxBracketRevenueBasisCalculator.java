package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TaxBracketRevenueBasisCalculator {

        private static final BigDecimal TWELVE = new BigDecimal("12");

        private static final MathContext CALCULATION_CONTEXT = MathContext.DECIMAL128;

        private static final String RULE_CODE = "SIMPLES_TAX_BRACKET_REVENUE_BASIS";

        private static final String RULE_VERSION = "2018-2026.1";

        private static final YearMonth VALID_FROM = YearMonth.of(2018, 1);

        private static final YearMonth VALID_UNTIL = YearMonth.of(2026, 12);

        private static final String LEGAL_REFERENCE = "Lei Complementar nº 123/2006, art. 18, §§ 1º e 2º; "
                        + "Manual do PGDAS-D e DEFIS, item 8.3.";

        public TaxBracketRevenueBasisResult calculate(
                        TaxBracketRevenueBasisRequest request) {
                Objects.requireNonNull(
                                request,
                                "A requisição não pode ser nula.");

                validateAssessmentPeriod(
                                request.assessmentPeriod());

                YearMonth openingMonth = YearMonth.from(
                                request.openingDate());

                if (request.assessmentPeriod().isBefore(
                                openingMonth)) {
                        throw new IllegalArgumentException(
                                        "O período de apuração não pode ser anterior "
                                                        + "ao mês de abertura da empresa.");
                }

                Map<YearMonth, MonthlyRevenue> revenueByPeriod = createRevenueMap(
                                request.monthlyRevenues(),
                                openingMonth,
                                request.assessmentPeriod());

                long monthsElapsed = ChronoUnit.MONTHS.between(
                                openingMonth,
                                request.assessmentPeriod());

                if (monthsElapsed == 0) {
                        return calculateFirstMonth(
                                        request.assessmentPeriod(),
                                        revenueByPeriod);
                }

                if (monthsElapsed < 12) {
                        return calculateFirstTwelveMonths(
                                        openingMonth,
                                        request.assessmentPeriod(),
                                        revenueByPeriod);
                }

                return calculateStandardRbt12(
                                request.assessmentPeriod(),
                                revenueByPeriod);
        }

        private TaxBracketRevenueBasisResult calculateFirstMonth(
                        YearMonth assessmentPeriod,
                        Map<YearMonth, MonthlyRevenue> revenueByPeriod) {
                MonthlyRevenue currentRevenue = requireRevenue(
                                assessmentPeriod,
                                revenueByPeriod);

                BigDecimal revenueBasis = currentRevenue.amount()
                                .multiply(
                                                TWELVE,
                                                CALCULATION_CONTEXT)
                                .setScale(
                                                2,
                                                RoundingMode.HALF_UP);

                List<MonthlyRevenue> revenuesUsed = List.of(currentRevenue);

                TaxDecision decision = createDecision(
                                TaxBracketRevenueBasisType.RBT12_PROPORTIONALIZED,
                                revenueBasis,
                                revenuesUsed,
                                "Primeiro mês de atividade.",
                                "A receita do próprio período de apuração "
                                                + "foi multiplicada por 12.");

                return new TaxBracketRevenueBasisResult(
                                TaxBracketRevenueBasisType.RBT12_PROPORTIONALIZED,
                                revenueBasis,
                                revenuesUsed,
                                decision);
        }

        private TaxBracketRevenueBasisResult calculateFirstTwelveMonths(
                        YearMonth openingMonth,
                        YearMonth assessmentPeriod,
                        Map<YearMonth, MonthlyRevenue> revenueByPeriod) {
                List<MonthlyRevenue> revenuesUsed = getRevenuesBetween(
                                openingMonth,
                                assessmentPeriod.minusMonths(1),
                                revenueByPeriod);

                BigDecimal total = sumRevenues(
                                revenuesUsed);

                BigDecimal months = BigDecimal.valueOf(
                                revenuesUsed.size());

                BigDecimal average = total.divide(
                                months,
                                CALCULATION_CONTEXT);

                BigDecimal revenueBasis = average.multiply(
                                TWELVE,
                                CALCULATION_CONTEXT)
                                .setScale(
                                                2,
                                                RoundingMode.HALF_UP);

                TaxDecision decision = createDecision(
                                TaxBracketRevenueBasisType.RBT12_PROPORTIONALIZED,
                                revenueBasis,
                                revenuesUsed,
                                "Empresa dentro dos 12 primeiros meses de atividade.",
                                "Foi calculada a média aritmética das receitas "
                                                + "dos meses anteriores ao período de apuração "
                                                + "e o resultado foi multiplicado por 12.");

                return new TaxBracketRevenueBasisResult(
                                TaxBracketRevenueBasisType.RBT12_PROPORTIONALIZED,
                                revenueBasis,
                                revenuesUsed,
                                decision);
        }

        private TaxBracketRevenueBasisResult calculateStandardRbt12(
                        YearMonth assessmentPeriod,
                        Map<YearMonth, MonthlyRevenue> revenueByPeriod) {
                YearMonth firstPeriod = assessmentPeriod.minusMonths(12);

                YearMonth lastPeriod = assessmentPeriod.minusMonths(1);

                List<MonthlyRevenue> revenuesUsed = getRevenuesBetween(
                                firstPeriod,
                                lastPeriod,
                                revenueByPeriod);

                BigDecimal revenueBasis = sumRevenues(
                                revenuesUsed)
                                .setScale(
                                                2,
                                                RoundingMode.UNNECESSARY);

                TaxDecision decision = createDecision(
                                TaxBracketRevenueBasisType.RBT12,
                                revenueBasis,
                                revenuesUsed,
                                "Empresa fora dos 12 primeiros meses de atividade.",
                                "Foram somadas as receitas dos 12 meses "
                                                + "anteriores ao período de apuração.");

                return new TaxBracketRevenueBasisResult(
                                TaxBracketRevenueBasisType.RBT12,
                                revenueBasis,
                                revenuesUsed,
                                decision);
        }

        private Map<YearMonth, MonthlyRevenue> createRevenueMap(
                        List<MonthlyRevenue> revenues,
                        YearMonth openingMonth,
                        YearMonth assessmentPeriod) {
                Map<YearMonth, MonthlyRevenue> result = new HashMap<>();

                for (MonthlyRevenue revenue : revenues) {
                        Objects.requireNonNull(
                                        revenue,
                                        "O histórico não pode conter receita nula.");

                        if (revenue.period().isBefore(openingMonth)) {
                                throw new IllegalArgumentException(
                                                "Existe receita informada antes da abertura "
                                                                + "da empresa: "
                                                                + revenue.period()
                                                                + ".");
                        }

                        if (revenue.period().isAfter(assessmentPeriod)) {
                                throw new IllegalArgumentException(
                                                "Existe receita informada após o período "
                                                                + "de apuração: "
                                                                + revenue.period()
                                                                + ".");
                        }

                        MonthlyRevenue previous = result.put(
                                        revenue.period(),
                                        revenue);

                        if (previous != null) {
                                throw new IllegalArgumentException(
                                                "Existe mais de uma receita informada "
                                                                + "para a competência "
                                                                + revenue.period()
                                                                + ".");
                        }
                }

                return result;
        }

        private List<MonthlyRevenue> getRevenuesBetween(
                        YearMonth firstPeriod,
                        YearMonth lastPeriod,
                        Map<YearMonth, MonthlyRevenue> revenueByPeriod) {
                List<MonthlyRevenue> result = new ArrayList<>();

                YearMonth current = firstPeriod;

                while (!current.isAfter(lastPeriod)) {
                        result.add(
                                        requireRevenue(
                                                        current,
                                                        revenueByPeriod));

                        current = current.plusMonths(1);
                }

                result.sort(
                                Comparator.comparing(
                                                MonthlyRevenue::period));

                return List.copyOf(result);
        }

        private MonthlyRevenue requireRevenue(
                        YearMonth period,
                        Map<YearMonth, MonthlyRevenue> revenueByPeriod) {
                MonthlyRevenue revenue = revenueByPeriod.get(period);

                if (revenue == null) {
                        throw new IllegalArgumentException(
                                        "Receita não informada para a competência "
                                                        + period
                                                        + ". Informe 0,00 explicitamente "
                                                        + "quando não houver faturamento.");
                }

                return revenue;
        }

        private BigDecimal sumRevenues(
                        List<MonthlyRevenue> revenues) {
                return revenues.stream()
                                .map(MonthlyRevenue::amount)
                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);
        }

        private TaxDecision createDecision(
                        TaxBracketRevenueBasisType basisType,
                        BigDecimal revenueBasis,
                        List<MonthlyRevenue> revenuesUsed,
                        String situation,
                        String calculationExplanation) {
                String input = createRevenueInput(
                                revenuesUsed);

                String description = situation
                                + " "
                                + calculationExplanation;

                String condition = "Base utilizada para enquadramento nas faixas: "
                                + basisType.getCode()
                                + ".";

                String result = basisType.getCode()
                                + " = "
                                + revenueBasis.toPlainString()
                                + ".";

                return new TaxDecision(
                                RULE_CODE,
                                RULE_VERSION,
                                description,
                                input,
                                condition,
                                result,
                                LEGAL_REFERENCE);
        }

        private String createRevenueInput(
                        List<MonthlyRevenue> revenues) {
                StringBuilder builder = new StringBuilder(
                                "Receitas utilizadas: ");

                for (int index = 0; index < revenues.size(); index++) {

                        MonthlyRevenue revenue = revenues.get(index);

                        if (index > 0) {
                                builder.append("; ");
                        }

                        builder.append(
                                        revenue.period());

                        builder.append(" = ");

                        builder.append(
                                        revenue.amount().toPlainString());
                }

                return builder.toString();
        }

        private void validateAssessmentPeriod(
                        YearMonth assessmentPeriod) {
                if (assessmentPeriod.isBefore(VALID_FROM)
                                || assessmentPeriod.isAfter(VALID_UNTIL)) {

                        throw new IllegalArgumentException(
                                        "A versão "
                                                        + RULE_VERSION
                                                        + " da regra de receita para enquadramento "
                                                        + "é válida entre "
                                                        + VALID_FROM
                                                        + " e "
                                                        + VALID_UNTIL
                                                        + ".");
                }
        }
}