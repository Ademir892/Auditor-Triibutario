package br.com.auditortributario.taxrule.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaxComponentTest {

    @Test
    void shouldIdentifyFederalComponents() {
        assertEquals(
                TaxJurisdiction.FEDERAL,
                TaxComponent.IRPJ
                        .getJurisdiction());

        assertEquals(
                TaxJurisdiction.FEDERAL,
                TaxComponent.CSLL
                        .getJurisdiction());

        assertEquals(
                TaxJurisdiction.FEDERAL,
                TaxComponent.COFINS
                        .getJurisdiction());

        assertEquals(
                TaxJurisdiction.FEDERAL,
                TaxComponent.PIS_PASEP
                        .getJurisdiction());

        assertEquals(
                TaxJurisdiction.FEDERAL,
                TaxComponent.CPP
                        .getJurisdiction());
    }

    @Test
    void shouldIdentifyMunicipalIss() {
        assertEquals(
                TaxJurisdiction.MUNICIPAL,
                TaxComponent.ISS
                        .getJurisdiction());
    }

    @Test
    void shouldIdentifyStateIcms() {
        assertEquals(
                TaxJurisdiction.STATE,
                TaxComponent.ICMS
                        .getJurisdiction());
    }

    @Test
    void shouldExposeHumanReadableNames() {
        assertEquals(
                "COFINS",
                TaxComponent.COFINS
                        .getDisplayName());

        assertEquals(
                "PIS/Pasep",
                TaxComponent.PIS_PASEP
                        .getDisplayName());
    }
}