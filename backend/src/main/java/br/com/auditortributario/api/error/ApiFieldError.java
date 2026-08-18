package br.com.auditortributario.api.error;

public record ApiFieldError(
        String field,
        String message) {
}