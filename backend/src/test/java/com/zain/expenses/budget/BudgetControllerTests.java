package com.zain.expenses.budget;

import static com.zain.expenses.TestAuth.bearerTokenFor;
import static com.zain.expenses.TestAuth.categoryId;
import static com.zain.expenses.TestData.setBudget;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class BudgetControllerTests {

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
    void setsAndListsTheBudgetsForAMonth() throws Exception {
        mockMvc.perform(
                        put("/api/budgets/{month}/{categoryId}", "2026-10", food)
                                .header(AUTHORIZATION, zain)
                                .contentType(APPLICATION_JSON)
                                .content(limitJson("200.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").isNumber())
                .andExpect(jsonPath("$.categoryName").value("Food"))
                .andExpect(jsonPath("$.limit").value(200.0));
        setBudget(mockMvc, zain, "2026-10", transport, "50.00");

        mockMvc.perform(get("/api/budgets").param("month", "2026-10").header(AUTHORIZATION, zain))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoryName").value("Food"))
                .andExpect(jsonPath("$[1].categoryName").value("Transport"))
                .andExpect(jsonPath("$[1].limit").value(50.0));
    }

    @Test
    void settingABudgetAgainReplacesTheLimit() throws Exception {
        setBudget(mockMvc, zain, "2026-10", food, "200.00");
        setBudget(mockMvc, zain, "2026-10", food, "250.00");

        mockMvc.perform(get("/api/budgets").param("month", "2026-10").header(AUTHORIZATION, zain))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].limit").value(250.0));
    }

    @Test
    void eachMonthHasItsOwnBudgets() throws Exception {
        setBudget(mockMvc, zain, "2026-10", food, "200.00");

        mockMvc.perform(get("/api/budgets").param("month", "2026-11").header(AUTHORIZATION, zain))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rejectsANegativeLimitOrOneWithTooManyDecimals() throws Exception {
        for (String limit : List.of("-5.00", "1.234")) {
            mockMvc.perform(
                            put("/api/budgets/{month}/{categoryId}", "2026-10", food)
                                    .header(AUTHORIZATION, zain)
                                    .contentType(APPLICATION_JSON)
                                    .content(limitJson(limit)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.limit").exists());
        }
    }

    @Test
    void removingABudgetIsSafeToRepeat() throws Exception {
        setBudget(mockMvc, zain, "2026-10", food, "200.00");

        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(
                            delete("/api/budgets/{month}/{categoryId}", "2026-10", food)
                                    .header(AUTHORIZATION, zain))
                    .andExpect(status().isNoContent());
        }
        mockMvc.perform(get("/api/budgets").param("month", "2026-10").header(AUTHORIZATION, zain))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void copiesLastMonthsBudgetsWithoutOverwritingThisMonths() throws Exception {
        setBudget(mockMvc, zain, "2026-09", food, "200.00");
        setBudget(mockMvc, zain, "2026-09", transport, "50.00");
        setBudget(mockMvc, zain, "2026-10", transport, "60.00");

        mockMvc.perform(
                        post("/api/budgets/{month}/copy-from-previous", "2026-10")
                                .header(AUTHORIZATION, zain))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoryName").value("Food"))
                .andExpect(jsonPath("$[0].limit").value(200.0))
                .andExpect(jsonPath("$[1].categoryName").value("Transport"))
                .andExpect(jsonPath("$[1].limit").value(60.0));
    }

    @Test
    void deletingACategoryAlsoDeletesItsBudgets() throws Exception {
        setBudget(mockMvc, zain, "2026-10", food, "200.00");

        mockMvc.perform(delete("/api/categories/{id}", food).header(AUTHORIZATION, zain))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/budgets").param("month", "2026-10").header(AUTHORIZATION, zain))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void budgetsArePrivateToEachUser() throws Exception {
        setBudget(mockMvc, zain, "2026-10", food, "200.00");
        String alex = bearerTokenFor(mockMvc, "alex@example.com");

        mockMvc.perform(get("/api/budgets").param("month", "2026-10").header(AUTHORIZATION, alex))
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(
                        put("/api/budgets/{month}/{categoryId}", "2026-10", food)
                                .header(AUTHORIZATION, alex)
                                .contentType(APPLICATION_JSON)
                                .content(limitJson("1.00")))
                .andExpect(status().isNotFound());
        mockMvc.perform(
                        delete("/api/budgets/{month}/{categoryId}", "2026-10", food)
                                .header(AUTHORIZATION, alex))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsAnInvalidMonth() throws Exception {
        mockMvc.perform(
                        put("/api/budgets/{month}/{categoryId}", "2026-13", food)
                                .header(AUTHORIZATION, zain)
                                .contentType(APPLICATION_JSON)
                                .content(limitJson("10.00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiresAToken() throws Exception {
        mockMvc.perform(get("/api/budgets")).andExpect(status().isUnauthorized());
    }

    private static String limitJson(String limit) {
        return """
        {"limit": %s}
        """
                .formatted(limit);
    }
}
