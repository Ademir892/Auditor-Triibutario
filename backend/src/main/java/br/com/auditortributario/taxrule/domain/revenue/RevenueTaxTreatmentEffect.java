package br.com.auditortributario.taxrule.domain.revenue;

public enum RevenueTaxTreatmentEffect {

        EXCLUDE_COMPONENT(
                        "Excluir componente do valor devido no Simples Nacional"),

        REQUIRE_SPECIAL_CALCULATION(
                        "Exigir regra adicional antes de concluir o cálculo"),

        REQUIRE_EXTERNAL_CALCULATION(
                        "Exigir cálculo ou recolhimento fora do Simples Nacional"),

        REDUCE_COMPONENT(
                        "Aplicar redução ao componente"),

        WITHHOLD_COMPONENT(
                        "Componente retido e não incluído no recolhimento pelo Simples");

        private final String displayName;

        RevenueTaxTreatmentEffect(
                        String displayName) {
                this.displayName = displayName;
        }

        public String getDisplayName() {
                return displayName;
        }
}