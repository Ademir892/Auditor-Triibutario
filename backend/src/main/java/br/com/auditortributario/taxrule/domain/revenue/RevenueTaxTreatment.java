package br.com.auditortributario.taxrule.domain.revenue;

public enum RevenueTaxTreatment {

        MONOPHASIC(
                        "Tributação monofásica"),

        /**
         * Mantido temporariamente para compatibilidade com código já criado.
         *
         * Não deve ser utilizado para cálculo automático porque não informa
         * se o contribuinte está na condição de substituto ou substituído.
         */
        @Deprecated
        TAX_SUBSTITUTION(
                        "Substituição tributária sem definição da posição do contribuinte"),

        ICMS_ST_SUBSTITUTED(
                        "ICMS-ST - contribuinte substituído"),

        ICMS_ST_SUBSTITUTE(
                        "ICMS-ST - contribuinte substituto"),

        ICMS_ANTICIPATION_WITH_CLOSURE(
                        "Antecipação de ICMS com encerramento de tributação"),

        EXPORT(
                        "Receita de exportação"),

        EXEMPTION(
                        "Isenção"),

        REDUCTION(
                        "Redução tributária"),

        ISS_WITHHELD(
                        "ISS retido");

        private final String displayName;

        RevenueTaxTreatment(
                        String displayName) {
                this.displayName = displayName;
        }

        public String getDisplayName() {
                return displayName;
        }
}