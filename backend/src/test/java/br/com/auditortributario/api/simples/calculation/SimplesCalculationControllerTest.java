package br.com.auditortributario.api.simples.calculation;

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
class SimplesCalculationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void shouldCalculateOpeningMonthThroughHttp() throws Exception {
                String requestBody = """
                                {
                                  "openingDate": "2026-02-10",
                                  "assessmentPeriod": "2026-02",
                                  "fatorRPayrollBase": 6000.00,
                                  "fatorRRevenueBase": 20000.00,
                                  "taxableRevenue": 20000.00,
                                  "priorMonthlyRevenues": []
                                }
                                """;

                mockMvc.perform(
                                post("/api/v1/simples/calculate")
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
                                                                .value("2026-02"))

                                .andExpect(
                                                jsonPath("$.fatorR.value")
                                                                .value(0.30))

                                .andExpect(
                                                jsonPath("$.fatorR.calculationBasis")
                                                                .value("OPENING_MONTH"))

                                .andExpect(
                                                jsonPath("$.fatorR.annex")
                                                                .value("ANEXO_III"))

                                .andExpect(
                                                jsonPath("$.revenueBasis.type")
                                                                .value("RBT12p"))

                                .andExpect(
                                                jsonPath("$.revenueBasis.amount")
                                                                .value(240000.00))

                                .andExpect(
                                                jsonPath("$.taxBracket.number")
                                                                .value(2))

                                .andExpect(
                                                jsonPath("$.taxBracket.nominalRate")
                                                                .value(0.112))

                                .andExpect(
                                                jsonPath("$.taxBracket.deduction")
                                                                .value(9360.00))

                                .andExpect(
                                                jsonPath("$.effectiveRate.value")
                                                                .value(0.073))

                                .andExpect(
                                                jsonPath("$.taxableRevenue")
                                                                .value(20000.00))

                                .andExpect(
                                                jsonPath("$.estimatedTaxAmount")
                                                                .value(1460.00))

                                .andExpect(
                                                jsonPath("$.estimatedTaxStatus")
                                                                .value("PAYABLE"))

                                .andExpect(
                                                jsonPath("$.taxTableVersion")
                                                                .value("2018-2026.1"));
        }

        @Test
        void shouldReturnStructuredValidationError() throws Exception {
                String requestBody = """
                                {
                                  "openingDate": "2026-02-10",
                                  "assessmentPeriod": "2026-02",
                                  "fatorRPayrollBase": 6000.00,
                                  "fatorRRevenueBase": 20000.00,
                                  "taxableRevenue": -1.00,
                                  "priorMonthlyRevenues": []
                                }
                                """;

                mockMvc.perform(
                                post("/api/v1/simples/calculate")
                                                .contentType(
                                                                MediaType.APPLICATION_JSON)
                                                .accept(
                                                                MediaType.APPLICATION_JSON)
                                                .content(
                                                                requestBody))
                                .andExpect(
                                                status().isBadRequest())
                                .andExpect(
                                                jsonPath("$.status")
                                                                .value(400))
                                .andExpect(
                                                jsonPath("$.error")
                                                                .value("Bad Request"))
                                .andExpect(
                                                jsonPath("$.message")
                                                                .value(
                                                                                "Dados da requisição inválidos."))
                                .andExpect(
                                                jsonPath("$.path")
                                                                .value(
                                                                                "/api/v1/simples/calculate"))
                                .andExpect(
                                                jsonPath(
                                                                "$.fieldErrors[0].field")
                                                                .value(
                                                                                "taxableRevenue"));
        }

        @Test
        void shouldReturnBadRequestForDomainValidationError()
                        throws Exception {

                String requestBody = """
                                {
                                  "openingDate": "2026-03-10",
                                  "assessmentPeriod": "2026-02",
                                  "fatorRPayrollBase": 6000.00,
                                  "fatorRRevenueBase": 20000.00,
                                  "taxableRevenue": 20000.00,
                                  "priorMonthlyRevenues": []
                                }
                                """;

                mockMvc.perform(
                                post("/api/v1/simples/calculate")
                                                .contentType(
                                                                MediaType.APPLICATION_JSON)
                                                .accept(
                                                                MediaType.APPLICATION_JSON)
                                                .content(
                                                                requestBody))
                                .andExpect(
                                                status().isBadRequest())
                                .andExpect(
                                                jsonPath("$.status")
                                                                .value(400))
                                .andExpect(
                                                jsonPath("$.error")
                                                                .value("Bad Request"))
                                .andExpect(
                                                jsonPath("$.path")
                                                                .value(
                                                                                "/api/v1/simples/calculate"));
        }

        @Test
        void shouldReturnBadRequestForMalformedJson()
                        throws Exception {

                String malformedBody = """
                                {
                                  "openingDate": "2026-02-10",
                                  "assessmentPeriod": "2026-02",
                                  "taxableRevenue":
                                }
                                """;

                mockMvc.perform(
                                post("/api/v1/simples/calculate")
                                                .contentType(
                                                                MediaType.APPLICATION_JSON)
                                                .accept(
                                                                MediaType.APPLICATION_JSON)
                                                .content(
                                                                malformedBody))
                                .andExpect(
                                                status().isBadRequest())
                                .andExpect(
                                                jsonPath("$.status")
                                                                .value(400))
                                .andExpect(
                                                jsonPath("$.message")
                                                                .value(
                                                                                "O corpo da requisição está ausente "
                                                                                                + "ou possui formato inválido."));
        }
}