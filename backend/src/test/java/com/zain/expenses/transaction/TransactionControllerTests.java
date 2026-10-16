package com.zain.expenses.transaction;

import static com.zain.expenses.TestAuth.bearerTokenFor;
import static com.zain.expenses.TestAuth.categoryId;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.zain.expenses.TestcontainersConfiguration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class TransactionControllerTests {

    @Autowired private MockMvc mockMvc;

    private String zain;
    private long food;
    private long transport;

    @BeforeEach
    void logInAndFindCategories() throws Exception {
        zain = bearerTokenFor(mockMvc, "zain@example.com");
        food = categoryId(mockMvc, zain, "Food");
        transport = categoryId(mockMvc, zain, "Transport");
    }

    @Test
    void createsAndFetchesATransaction() throws Exception {
        long id = create(zain, food, "12.50", "2026-10-03", "Lunch");

        mockMvc.perform(get("/api/transactions/{id}", id).header(AUTHORIZATION, zain))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(12.5))
                .andExpect(jsonPath("$.occurredOn").value("2026-10-03"))
                .andExpect(jsonPath("$.note").value("Lunch"))
                .andExpect(jsonPath("$.categoryName").value("Food"));
    }

    @Test
    void listsOneMonthNewestFirst() throws Exception {
        create(zain, food, "12.50", "2026-10-03", "Lunch");
        create(zain, transport, "2.80", "2026-10-15", "Bus");
        create(zain, food, "40.00", "2026-09-28", "Shop");

        mockMvc.perform(
                        get("/api/transactions")
                                .param("month", "2026-10")
                                .header(AUTHORIZATION, zain))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].note").value("Bus"))
                .andExpect(jsonPath("$[1].note").value("Lunch"));
    }

    @Test
    void filtersByCategory() throws Exception {
        create(zain, food, "12.50", "2026-10-03", "Lunch");
        create(zain, transport, "2.80", "2026-10-15", "Bus");

        mockMvc.perform(
                        get("/api/transactions")
                                .param("month", "2026-10")
                                .param("categoryId", String.valueOf(food))
                                .header(AUTHORIZATION, zain))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].note").value("Lunch"));
    }

    @Test
    void updatesATransaction() throws Exception {
        long id = create(zain, food, "12.50", "2026-10-03", "Lunch");

        mockMvc.perform(
                        put("/api/transactions/{id}", id)
                                .header(AUTHORIZATION, zain)
                                .contentType(APPLICATION_JSON)
                                .content(body(transport, "3.10", "2026-10-04", "Train")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(3.1))
                .andExpect(jsonPath("$.occurredOn").value("2026-10-04"))
                .andExpect(jsonPath("$.categoryName").value("Transport"))
                .andExpect(jsonPath("$.note").value("Train"));
    }

    @Test
    void deletesATransaction() throws Exception {
        long id = create(zain, food, "12.50", "2026-10-03", "Lunch");

        mockMvc.perform(delete("/api/transactions/{id}", id).header(AUTHORIZATION, zain))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/transactions/{id}", id).header(AUTHORIZATION, zain))
                .andExpect(status().isNotFound());
    }

    @Test
    void anotherUserCannotSeeChangeOrDeleteMyTransaction() throws Exception {
        long id = create(zain, food, "12.50", "2026-10-03", "Lunch");
        String alex = bearerTokenFor(mockMvc, "alex@example.com");
        long alexsFood = categoryId(mockMvc, alex, "Food");

        mockMvc.perform(get("/api/transactions/{id}", id).header(AUTHORIZATION, alex))
                .andExpect(status().isNotFound());
        mockMvc.perform(
                        put("/api/transactions/{id}", id)
                                .header(AUTHORIZATION, alex)
                                .contentType(APPLICATION_JSON)
                                .content(body(alexsFood, "1.00", "2026-10-03", null)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/transactions/{id}", id).header(AUTHORIZATION, alex))
                .andExpect(status().isNotFound());
        mockMvc.perform(
                        get("/api/transactions")
                                .param("month", "2026-10")
                                .header(AUTHORIZATION, alex))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void cannotUseSomeoneElsesCategory() throws Exception {
        String alex = bearerTokenFor(mockMvc, "alex@example.com");

        mockMvc.perform(
                        post("/api/transactions")
                                .header(AUTHORIZATION, alex)
                                .contentType(APPLICATION_JSON)
                                .content(body(food, "5.00", "2026-10-03", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Category not found"));
    }

    @Test
    void rejectsAmountsThatAreNotPositiveOrHaveTooManyDecimals() throws Exception {
        for (String amount : List.of("0", "-5.00", "1.234")) {
            mockMvc.perform(
                            post("/api/transactions")
                                    .header(AUTHORIZATION, zain)
                                    .contentType(APPLICATION_JSON)
                                    .content(body(food, amount, "2026-10-03", null)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.amount").exists());
        }
    }

    @Test
    void rejectsAnInvalidMonth() throws Exception {
        mockMvc.perform(
                        get("/api/transactions")
                                .param("month", "2026-13")
                                .header(AUTHORIZATION, zain))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiresAToken() throws Exception {
        mockMvc.perform(get("/api/transactions")).andExpect(status().isUnauthorized());
    }

    private long create(String token, long categoryId, String amount, String date, String note)
            throws Exception {
        String response =
                mockMvc.perform(
                                post("/api/transactions")
                                        .header(AUTHORIZATION, token)
                                        .contentType(APPLICATION_JSON)
                                        .content(body(categoryId, amount, date, note)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }

    private static String body(long categoryId, String amount, String date, String note) {
        String noteJson = note == null ? "null" : "\"" + note + "\"";
        return """
        {"categoryId": %d, "amount": %s, "occurredOn": "%s", "note": %s}
        """
                .formatted(categoryId, amount, date, noteJson);
    }
}
