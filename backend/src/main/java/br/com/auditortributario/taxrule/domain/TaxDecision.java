package br.com.auditortributario.taxrule.domain;

public record TaxDecision(
        String ruleCode,
        String ruleVersion,
        String description,
        String input,
        String condition,
        String result,
        String legalReference
) {

    public TaxDecision {
        ruleCode = requireText(ruleCode, "ruleCode");
        ruleVersion = requireText(ruleVersion, "ruleVersion");
        description = requireText(description, "description");
        input = requireText(input, "input");
        condition = requireText(condition, "condition");
        result = requireText(result, "result");
        legalReference = requireText(legalReference, "legalReference");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " não pode ser nulo ou vazio."
            );
        }

        return value;
    }
}
