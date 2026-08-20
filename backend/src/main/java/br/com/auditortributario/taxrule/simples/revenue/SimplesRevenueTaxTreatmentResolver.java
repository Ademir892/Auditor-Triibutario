package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxComponentTreatment;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxTreatment;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxTreatmentEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SimplesRevenueTaxTreatmentResolver {

        public List<RevenueTaxComponentTreatment> resolve(
                        RevenueEntry revenue) {
                Objects.requireNonNull(
                                revenue,
                                "A receita não pode ser nula.");

                List<RevenueTaxComponentTreatment> results = new ArrayList<>();

                for (RevenueTaxTreatment treatment : revenue.treatments()) {

                        switch (treatment) {

                                case MONOPHASIC ->
                                        addMonophasicTreatments(
                                                        results,
                                                        treatment);

                                case TAX_SUBSTITUTION ->
                                        addLegacyTaxSubstitutionTreatment(
                                                        results,
                                                        treatment);

                                case ICMS_ST_SUBSTITUTED ->
                                        results.add(
                                                        new RevenueTaxComponentTreatment(
                                                                        treatment,
                                                                        TaxComponent.ICMS,
                                                                        RevenueTaxTreatmentEffect.EXCLUDE_COMPONENT,
                                                                        "Na condição de contribuinte substituído, "
                                                                                        + "o percentual correspondente ao ICMS "
                                                                                        + "deve ser desconsiderado desta receita "
                                                                                        + "no cálculo do Simples Nacional."));

                                case ICMS_ST_SUBSTITUTE ->
                                        results.add(
                                                        new RevenueTaxComponentTreatment(
                                                                        treatment,
                                                                        TaxComponent.ICMS,
                                                                        RevenueTaxTreatmentEffect.REQUIRE_EXTERNAL_CALCULATION,
                                                                        "Na condição de contribuinte substituto, "
                                                                                        + "o ICMS da operação própria permanece "
                                                                                        + "no Simples Nacional, enquanto o ICMS "
                                                                                        + "devido por substituição tributária "
                                                                                        + "possui apuração e recolhimento próprios."));

                                case ICMS_ANTICIPATION_WITH_CLOSURE ->
                                        results.add(
                                                        new RevenueTaxComponentTreatment(
                                                                        treatment,
                                                                        TaxComponent.ICMS,
                                                                        RevenueTaxTreatmentEffect.EXCLUDE_COMPONENT,
                                                                        "Na antecipação de ICMS com encerramento "
                                                                                        + "de tributação, o percentual de ICMS "
                                                                                        + "é desconsiderado desta receita "
                                                                                        + "no cálculo do Simples Nacional."));

                                case ISS_WITHHELD ->
                                        results.add(
                                                        new RevenueTaxComponentTreatment(
                                                                        treatment,
                                                                        TaxComponent.ISS,
                                                                        RevenueTaxTreatmentEffect.WITHHOLD_COMPONENT,
                                                                        "Quando o ISS foi retido, o percentual "
                                                                                        + "correspondente ao ISS deve ser "
                                                                                        + "desconsiderado desta receita "
                                                                                        + "no cálculo do Simples Nacional."));

                                case REDUCTION ->
                                        results.addAll(
                                                        reductionTreatments(
                                                                        treatment));

                                case EXEMPTION ->
                                        results.addAll(
                                                        exemptionTreatments(
                                                                        treatment));

                                case EXPORT ->
                                        results.addAll(
                                                        exportTreatments(
                                                                        treatment));
                        }
                }

                return List.copyOf(
                                results);
        }

        private void addMonophasicTreatments(
                        List<RevenueTaxComponentTreatment> results,
                        RevenueTaxTreatment treatment) {
                results.add(
                                new RevenueTaxComponentTreatment(
                                                treatment,
                                                TaxComponent.PIS_PASEP,
                                                RevenueTaxTreatmentEffect.EXCLUDE_COMPONENT,
                                                "Na receita sujeita à tributação monofásica, "
                                                                + "o percentual correspondente ao PIS/Pasep "
                                                                + "é desconsiderado no cálculo "
                                                                + "do Simples Nacional."));

                results.add(
                                new RevenueTaxComponentTreatment(
                                                treatment,
                                                TaxComponent.COFINS,
                                                RevenueTaxTreatmentEffect.EXCLUDE_COMPONENT,
                                                "Na receita sujeita à tributação monofásica, "
                                                                + "o percentual correspondente à COFINS "
                                                                + "é desconsiderado no cálculo "
                                                                + "do Simples Nacional."));
        }

        private void addLegacyTaxSubstitutionTreatment(
                        List<RevenueTaxComponentTreatment> results,
                        RevenueTaxTreatment treatment) {
                results.add(
                                new RevenueTaxComponentTreatment(
                                                treatment,
                                                TaxComponent.ICMS,
                                                RevenueTaxTreatmentEffect.REQUIRE_SPECIAL_CALCULATION,
                                                "A classificação genérica TAX_SUBSTITUTION "
                                                                + "não informa se o contribuinte é substituto "
                                                                + "ou substituído. É necessário determinar "
                                                                + "a posição tributária antes de alterar "
                                                                + "o componente ICMS."));
        }

        private List<RevenueTaxComponentTreatment> reductionTreatments(
                        RevenueTaxTreatment treatment) {
                return List.of(
                                specialCalculation(
                                                treatment,
                                                TaxComponent.IRPJ,
                                                "A redução informada não possui regra suficiente "
                                                                + "para alterar automaticamente o IRPJ."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.CSLL,
                                                "A redução informada não possui regra suficiente "
                                                                + "para alterar automaticamente a CSLL."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.COFINS,
                                                "A redução informada não possui regra suficiente "
                                                                + "para alterar automaticamente a COFINS."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.PIS_PASEP,
                                                "A redução informada não possui regra suficiente "
                                                                + "para alterar automaticamente o PIS/Pasep."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.CPP,
                                                "A redução informada não possui regra suficiente "
                                                                + "para alterar automaticamente a CPP."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.ISS,
                                                "A redução informada não possui regra suficiente "
                                                                + "para alterar automaticamente o ISS."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.ICMS,
                                                "A redução informada não possui regra suficiente "
                                                                + "para alterar automaticamente o ICMS."));
        }

        private List<RevenueTaxComponentTreatment> exemptionTreatments(
                        RevenueTaxTreatment treatment) {
                return List.of(
                                specialCalculation(
                                                treatment,
                                                TaxComponent.IRPJ,
                                                "É necessário identificar quais componentes "
                                                                + "são efetivamente alcançados pela isenção."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.CSLL,
                                                "É necessário identificar quais componentes "
                                                                + "são efetivamente alcançados pela isenção."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.COFINS,
                                                "É necessário identificar quais componentes "
                                                                + "são efetivamente alcançados pela isenção."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.PIS_PASEP,
                                                "É necessário identificar quais componentes "
                                                                + "são efetivamente alcançados pela isenção."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.CPP,
                                                "É necessário identificar quais componentes "
                                                                + "são efetivamente alcançados pela isenção."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.ISS,
                                                "É necessário identificar quais componentes "
                                                                + "são efetivamente alcançados pela isenção."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.ICMS,
                                                "É necessário identificar quais componentes "
                                                                + "são efetivamente alcançados pela isenção."));
        }

        private List<RevenueTaxComponentTreatment> exportTreatments(
                        RevenueTaxTreatment treatment) {
                return List.of(
                                specialCalculation(
                                                treatment,
                                                TaxComponent.PIS_PASEP,
                                                "Receita de exportação exige regra específica "
                                                                + "antes da alteração do PIS/Pasep."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.COFINS,
                                                "Receita de exportação exige regra específica "
                                                                + "antes da alteração da COFINS."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.ICMS,
                                                "Receita de exportação exige regra específica "
                                                                + "antes da alteração do ICMS."),

                                specialCalculation(
                                                treatment,
                                                TaxComponent.ISS,
                                                "Exportação de serviços exige análise específica "
                                                                + "para determinar o tratamento do ISS."));
        }

        private RevenueTaxComponentTreatment specialCalculation(
                        RevenueTaxTreatment treatment,
                        TaxComponent component,
                        String explanation) {
                return new RevenueTaxComponentTreatment(
                                treatment,
                                component,
                                RevenueTaxTreatmentEffect.REQUIRE_SPECIAL_CALCULATION,
                                explanation);
        }
}