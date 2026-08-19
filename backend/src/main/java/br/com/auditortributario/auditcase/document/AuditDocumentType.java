package br.com.auditortributario.auditcase.document;

public enum AuditDocumentType {

    TAX_GUIDE(
            "Guia tributária"),

    TAX_ASSESSMENT(
            "Apuração tributária"),

    DECLARATION(
            "Declaração"),

    PAYMENT_RECEIPT(
            "Comprovante de pagamento"),

    INVOICE(
            "Nota fiscal"),

    PAYROLL_REPORT(
            "Relatório de folha"),

    ACCOUNTING_REPORT(
            "Relatório contábil"),

    OTHER(
            "Outro documento");

    private final String displayName;

    AuditDocumentType(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}