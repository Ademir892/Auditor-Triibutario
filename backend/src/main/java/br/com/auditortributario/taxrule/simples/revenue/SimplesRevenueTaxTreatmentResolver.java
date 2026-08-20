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

                case MONOPHASIC -> {
                    results.add(
                            new RevenueTaxComponentTreatment(
                                    treatment,
                                    TaxComponent.PIS_PASEP,
                                    RevenueTaxTreatmentEffect.REQUIRE_SPECIAL_CALCULATION,
                                    "Receita monofásica exige tratamento "
                                            + "específico de PIS/Pasep."));

                    results.add(
                            new RevenueTaxComponentTreatment(
                                    treatment,
                                    TaxComponent.COFINS,
                                    RevenueTaxTreatmentEffect.REQUIRE_SPECIAL_CALCULATION,
                                    "Receita monofásica exige tratamento "
                                            + "específico de COFINS."));
                }

                case TAX_SUBSTITUTION ->
                    results.add(
                            new RevenueTaxComponentTreatment(
                                    treatment,
                                    TaxComponent.ICMS,
                                    RevenueTaxTreatmentEffect.REQUIRE_SPECIAL_CALCULATION,
                                    "Receita sujeita à substituição "
                                            + "tributária exige tratamento "
                                            + "específico do ICMS."));

                case ISS_WITHHELD ->
                    results.add(
                            new RevenueTaxComponentTreatment(
                                    treatment,
                                    TaxComponent.ISS,
                                    RevenueTaxTreatmentEffect.WITHHOLD_COMPONENT,
                                    "Receita com ISS retido exige "
                                            + "tratamento específico do ISS."));

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

    private List<RevenueTaxComponentTreatment> reductionTreatments(
            RevenueTaxTreatment treatment) {
        return List.of(
                specialCalculation(
                        treatment,
                        TaxComponent.IRPJ,
                        "Receita com redução exige regra específica "
                                + "para determinar o impacto no IRPJ."),
                specialCalculation(
                        treatment,
                        TaxComponent.CSLL,
                        "Receita com redução exige regra específica "
                                + "para determinar o impacto na CSLL."),
                specialCalculation(
                        treatment,
                        TaxComponent.COFINS,
                        "Receita com redução exige regra específica "
                                + "para determinar o impacto na COFINS."),
                specialCalculation(
                        treatment,
                        TaxComponent.PIS_PASEP,
                        "Receita com redução exige regra específica "
                                + "para determinar o impacto no PIS/Pasep."),
                specialCalculation(
                        treatment,
                        TaxComponent.CPP,
                        "Receita com redução exige regra específica "
                                + "para determinar o impacto na CPP."),
                specialCalculation(
                        treatment,
                        TaxComponent.ISS,
                        "Receita com redução exige regra específica "
                                + "para determinar o impacto no ISS."),
                specialCalculation(
                        treatment,
                        TaxComponent.ICMS,
                        "Receita com redução exige regra específica "
                                + "para determinar o impacto no ICMS."));
    }

    private List<RevenueTaxComponentTreatment> exemptionTreatments(
            RevenueTaxTreatment treatment) {
        return List.of(
                specialCalculation(
                        treatment,
                        TaxComponent.IRPJ,
                        "Receita com isenção exige identificação "
                                + "dos componentes efetivamente alcançados."),
                specialCalculation(
                        treatment,
                        TaxComponent.CSLL,
                        "Receita com isenção exige identificação "
                                + "dos componentes efetivamente alcançados."),
                specialCalculation(
                        treatment,
                        TaxComponent.COFINS,
                        "Receita com isenção exige identificação "
                                + "dos componentes efetivamente alcançados."),
                specialCalculation(
                        treatment,
                        TaxComponent.PIS_PASEP,
                        "Receita com isenção exige identificação "
                                + "dos componentes efetivamente alcançados."),
                specialCalculation(
                        treatment,
                        TaxComponent.CPP,
                        "Receita com isenção exige identificação "
                                + "dos componentes efetivamente alcançados."),
                specialCalculation(
                        treatment,
                        TaxComponent.ISS,
                        "Receita com isenção exige identificação "
                                + "dos componentes efetivamente alcançados."),
                specialCalculation(
                        treatment,
                        TaxComponent.ICMS,
                        "Receita com isenção exige identificação "
                                + "dos componentes efetivamente alcançados."));
    }

    private List<RevenueTaxComponentTreatment> exportTreatments(
            RevenueTaxTreatment treatment) {
        return List.of(
                specialCalculation(
                        treatment,
                        TaxComponent.PIS_PASEP,
                        "Receita de exportação exige tratamento "
                                + "específico de PIS/Pasep."),
                specialCalculation(
                        treatment,
                        TaxComponent.COFINS,
                        "Receita de exportação exige tratamento "
                                + "específico de COFINS."),
                specialCalculation(
                        treatment,
                        TaxComponent.ICMS,
                        "Receita de exportação exige tratamento "
                                + "específico do ICMS."),
                specialCalculation(
                        treatment,
                        TaxComponent.ISS,
                        "Receita de exportação de serviços exige "
                                + "análise específica quanto ao ISS."));
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