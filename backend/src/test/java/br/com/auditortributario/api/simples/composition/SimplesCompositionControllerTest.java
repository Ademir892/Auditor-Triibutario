package br.com.auditortributario.api.simples.composition;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SimplesCompositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnOfficialAnnexIIICompositionThroughHttp()
            throws Exception {

        String requestBody = """
                {
                  "openingDate": "2024-01-10",
                  "assessmentPeriod": "2026-01",
                  "fatorRPayrollBase": 150000.00,
                  "fatorRRevenueBase": 500000.00,
                  "taxableRevenue": 10000.00,
                  "priorMonthlyRevenues": [
                    {
                      "period": "2025-01",
                      "amount": 41666.66
                    },
                    {
                      "period": "2025-02",
                      "amount": 41666.66
                    },
                    {
                      "period": "2025-03",
                      "amount": 41666.66
                    },
                    {
                      "period": "2025-04",
                      "amount": 41666.66
                    },
                    {
                      "period": "2025-05",
                      "amount": 41666.66
                    },
                    {
                      "period": "2025-06",
                      "amount": 41666.66
                    },
                    {
                      "period": "2025-07",
                      "amount": 41666.66
                    },
                    {
                      "period": "2025-08",
                      "amount": 41666.66
                    },
                    {
                      "period": "2025-09",
                      "amount": 41666.66
                    },
                    {
                      "period": "2025-10",
                      "amount": 41666.66
                    },
                    {
                      "period": "2025-11",
                      "amount": 41666.66
                    },
                    {
                      "period": "2025-12",
                      "amount": 41666.74
                    }
                  ]
                }
                """;

        mockMvc.perform(
                post(
                        "/api/v1/simples/calculate/composition")
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .accept(
                                MediaType.APPLICATION_JSON)
                        .content(
                                requestBody))
                .andExpect(
                        status().isOk())

                .andExpect(
                        jsonPath("$.assessmentPeriod")
                                .value("2026-01"))

                .andExpect(
                        jsonPath("$.annex")
                                .value("ANEXO_III"))

                .andExpect(
                        jsonPath("$.bracketNumber")
                                .value(3))

                .andExpect(
                        jsonPath("$.totalTaxAmount")
                                .value(997.20))

                .andExpect(
                        jsonPath("$.fullyAllocated")
                                .value(true))

                .andExpect(
                        jsonPath("$.issCapApplied")
                                .value(false))

                .andExpect(
                        jsonPath("$.components.length()")
                                .value(6))

                .andExpect(
                        jsonPath("$.components[0].code")
                                .value("IRPJ"))

                .andExpect(
                        jsonPath("$.components[0].amount")
                                .value(39.89))

                .andExpect(
                        jsonPath("$.components[1].code")
                                .value("CSLL"))

                .andExpect(
                        jsonPath("$.components[1].amount")
                                .value(34.90))

                .andExpect(
                        jsonPath("$.components[2].code")
                                .value("COFINS"))

                .andExpect(
                        jsonPath("$.components[2].amount")
                                .value(136.02))

                .andExpect(
                        jsonPath("$.components[3].code")
                                .value("PIS_PASEP"))

                .andExpect(
                        jsonPath("$.components[3].amount")
                                .value(29.52))

                .andExpect(
                        jsonPath("$.components[4].code")
                                .value("CPP"))

                .andExpect(
                        jsonPath("$.components[4].amount")
                                .value(432.78))

                .andExpect(
                        jsonPath("$.components[5].code")
                                .value("ISS"))

                .andExpect(
                        jsonPath("$.components[5].amount")
                                .value(324.09));
    }
}