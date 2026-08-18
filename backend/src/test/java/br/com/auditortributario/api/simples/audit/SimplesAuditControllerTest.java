package br.com.auditortributario.api.simples.audit;


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
class SimplesAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAuditDivergentSimplesCalculationThroughHttp()
            throws Exception {

        String requestBody =
                """
                {
                  "calculation": {
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
                  },
                  "guideAmount": 1752.00,
                  "reportedFatorR": 0.25,
                  "reportedAnnex": "ANEXO_V",
                  "reportedBracketNumber": 3,
                  "reportedEffectiveRate": 0.1752
                }
                """;

        mockMvc.perform(
                        post("/api/v1/simples/audit")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .accept(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.assessmentPeriod")
                                .value("2026-01")
                )

                .andExpect(
                        jsonPath("$.status")
                                .value("DIVERGENT")
                )

                .andExpect(
                        jsonPath("$.severity")
                                .value("HIGH")
                )

                .andExpect(
                        jsonPath("$.principalCause.code")
                                .value(
                                        "FACTOR_R_OR_ANNEX"
                                )
                )

                .andExpect(
                        jsonPath("$.amount.status")
                                .value("DIVERGENT")
                )

                .andExpect(
                        jsonPath("$.amount.expectedAmount")
                                .value(997.20)
                )

                .andExpect(
                        jsonPath("$.amount.reportedAmount")
                                .value(1752.00)
                )

                .andExpect(
                        jsonPath("$.amount.signedDifference")
                                .value(754.80)
                )

                .andExpect(
                        jsonPath("$.amount.absoluteDifference")
                                .value(754.80)
                )

                .andExpect(
                        jsonPath("$.amount.reportedAmountHigherThanExpected")
                                .value(true)
                )

                .andExpect(
                        jsonPath("$.structure.status")
                                .value("DIVERGENT")
                )

                .andExpect(
                        jsonPath("$.structure.severity")
                                .value("HIGH")
                )

                .andExpect(
                        jsonPath("$.findings.length()")
                                .value(4)
                )

                .andExpect(
                        jsonPath("$.findings[0].code")
                                .value(
                                        "ANNEX_MISMATCH"
                                )
                )

                .andExpect(
                        jsonPath("$.findings[0].severity")
                                .value("HIGH")
                )

                .andExpect(
                        jsonPath("$.findings[1].code")
                                .value(
                                        "FATOR_R_MISMATCH"
                                )
                )

                .andExpect(
                        jsonPath("$.findings[2].code")
                                .value(
                                        "AMOUNT_DIVERGENCE"
                                )
                )

                .andExpect(
                        jsonPath("$.findings[3].code")
                                .value(
                                        "EFFECTIVE_RATE_MISMATCH"
                                )
                )

                .andExpect(
                        jsonPath("$.recommendedChecks")
                                .isArray()
                )

                .andExpect(
                        jsonPath("$.executiveSummary")
                                .isString()
                );
    }

    @Test
    void shouldReturnCompatibleAuditThroughHttp()
            throws Exception {

        String requestBody =
                """
                {
                  "calculation": {
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
                  },
                  "guideAmount": 997.20,
                  "reportedFatorR": 0.30,
                  "reportedAnnex": "ANEXO_III",
                  "reportedBracketNumber": 3,
                  "reportedEffectiveRate": 0.09972
                }
                """;

        mockMvc.perform(
                        post("/api/v1/simples/audit")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .accept(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value("COMPATIBLE")
                )

                .andExpect(
                        jsonPath("$.severity")
                                .value("NONE")
                )

                .andExpect(
                        jsonPath("$.principalCause.code")
                                .value("NONE")
                )

                .andExpect(
                        jsonPath("$.amount.status")
                                .value("EXACT_MATCH")
                )

                .andExpect(
                        jsonPath("$.structure.status")
                                .value("COMPATIBLE")
                )

                .andExpect(
                        jsonPath("$.findings.length()")
                                .value(0)
                );
    }

    @Test
    void shouldReturnReviewRequiredWhenStructuralDataIsMissing()
            throws Exception {

        String requestBody =
                """
                {
                  "calculation": {
                    "openingDate": "2026-02-10",
                    "assessmentPeriod": "2026-02",
                    "fatorRPayrollBase": 6000.00,
                    "fatorRRevenueBase": 20000.00,
                    "taxableRevenue": 20000.00,
                    "priorMonthlyRevenues": []
                  },
                  "guideAmount": 1460.00
                }
                """;

        mockMvc.perform(
                        post("/api/v1/simples/audit")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .accept(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value("REVIEW_REQUIRED")
                )

                .andExpect(
                        jsonPath("$.severity")
                                .value("LOW")
                )

                .andExpect(
                        jsonPath("$.principalCause.code")
                                .value(
                                        "INSUFFICIENT_DATA"
                                )
                )

                .andExpect(
                        jsonPath("$.amount.status")
                                .value("EXACT_MATCH")
                )

                .andExpect(
                        jsonPath("$.structure.status")
                                .value(
                                        "INSUFFICIENT_DATA"
                                )
                )

                .andExpect(
                        jsonPath("$.findings.length()")
                                .value(4)
                );
    }

    @Test
    void shouldRejectNegativeGuideAmount()
            throws Exception {

        String requestBody =
                """
                {
                  "calculation": {
                    "openingDate": "2026-02-10",
                    "assessmentPeriod": "2026-02",
                    "fatorRPayrollBase": 6000.00,
                    "fatorRRevenueBase": 20000.00,
                    "taxableRevenue": 20000.00,
                    "priorMonthlyRevenues": []
                  },
                  "guideAmount": -1.00
                }
                """;

        mockMvc.perform(
                        post("/api/v1/simples/audit")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .accept(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )

                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Dados da requisição inválidos."
                                )
                );
    }
}
