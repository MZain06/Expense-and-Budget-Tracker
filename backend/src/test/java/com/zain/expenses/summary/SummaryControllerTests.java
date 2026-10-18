package com.zain.expenses.summary;

import static com.zain.expenses.TestAuth.bearerTokenFor;
import static com.zain.expenses.TestAuth.categoryId;
import static com.zain.expenses.TestData.addTransaction;
import static com.zain.expenses.TestData.setBudget;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zain.expenses.TestcontainersConfiguration;
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
class SummaryControllerTests {

    @Autowired private MockMvc mockMvc;

    @Test
    void summarisesAMonthsSpendingAgainstItsBudgets() throws Exception {
        String zain = bearerTokenFor(mockMvc, "zain@example.com");
        long bills = categoryId(mockMvc, zain, "Bills");
        long food = categoryId(mockMvc, zain, "Food");
        long transport = categoryId(mockMvc, zain, "Transport");
        addTransaction(mockMvc, zain, bills, "650.00", "2026-10-01");
        addTransaction(mockMvc, zain, food, "12.50", "2026-10-03");
        addTransaction(mockMvc, zain, food, "42.60", "2026-10-11");
        addTransaction(mockMvc, zain, food, "99.00", "2026-09-30"); // September: not counted
        setBudget(mockMvc, zain, "2026-10", food, "200.00");
        setBudget(mockMvc, zain, "2026-10", transport, "50.00");

        mockMvc.perform(get("/api/summary").param("month", "2026-10").header(AUTHORIZATION, zain))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpent").value(705.1))
                .andExpect(jsonPath("$.totalBudget").value(250.0))
                .andExpect(jsonPath("$.categories.length()").value(3))
                .andExpect(jsonPath("$.categories[0].categoryName").value("Bills"))
                .andExpect(jsonPath("$.categories[0].spent").value(650.0))
                .andExpect(jsonPath("$.categories[0].budget").doesNotExist())
                .andExpect(jsonPath("$.categories[1].categoryName").value("Food"))
                .andExpect(jsonPath("$.categories[1].spent").value(55.1))
                .andExpect(jsonPath("$.categories[1].remaining").value(144.9))
                .andExpect(jsonPath("$.categories[2].categoryName").value("Transport"))
                .andExpect(jsonPath("$.categories[2].spent").value(0.0))
                .andExpect(jsonPath("$.categories[2].remaining").value(50.0));
    }

    @Test
    void anotherUsersSpendingIsNotIncluded() throws Exception {
        String zain = bearerTokenFor(mockMvc, "zain@example.com");
        addTransaction(mockMvc, zain, categoryId(mockMvc, zain, "Food"), "12.50", "2026-10-03");
        String alex = bearerTokenFor(mockMvc, "alex@example.com");

        mockMvc.perform(get("/api/summary").param("month", "2026-10").header(AUTHORIZATION, alex))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpent").value(0.0))
                .andExpect(jsonPath("$.totalBudget").value(0.0))
                .andExpect(jsonPath("$.categories.length()").value(3 - 3));
    }

    @Test
    void requiresAToken() throws Exception {
        mockMvc.perform(get("/api/summary")).andExpect(status().isUnauthorized());
    }
}
