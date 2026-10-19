package com.zain.expenses.insights;

import static com.zain.expenses.TestAuth.bearerTokenFor;
import static com.zain.expenses.TestAuth.categoryId;
import static com.zain.expenses.TestData.addTransaction;
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
class InsightsControllerTests {

    @Autowired private MockMvc mockMvc;

    @Test
    void describesAFinishedMonth() throws Exception {
        String zain = bearerTokenFor(mockMvc, "zain@example.com");
        long food = categoryId(mockMvc, zain, "Food");
        long transport = categoryId(mockMvc, zain, "Transport");
        addTransaction(mockMvc, zain, food, "10.00", "2026-08-05");
        addTransaction(mockMvc, zain, food, "30.00", "2026-09-05");
        addTransaction(mockMvc, zain, transport, "60.00", "2026-09-20");

        mockMvc.perform(get("/api/insights").param("month", "2026-09").header(AUTHORIZATION, zain))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spent").value(90.0))
                .andExpect(jsonPath("$.spentLastMonth").value(10.0))
                .andExpect(jsonPath("$.averagePerDay").value(3.0))
                .andExpect(jsonPath("$.projectedTotal").doesNotExist())
                .andExpect(jsonPath("$.threeMonthAverage").value(3.33))
                .andExpect(jsonPath("$.trend.length()").value(12))
                .andExpect(jsonPath("$.trend[11].month").value("2026-09"))
                .andExpect(jsonPath("$.trend[11].spent").value(90.0))
                .andExpect(jsonPath("$.trend[10].spent").value(10.0))
                .andExpect(jsonPath("$.biggestChanges.length()").value(2))
                .andExpect(jsonPath("$.biggestChanges[0].categoryName").value("Transport"))
                .andExpect(jsonPath("$.biggestChanges[0].change").value(60.0))
                .andExpect(jsonPath("$.biggestChanges[1].categoryName").value("Food"));
    }

    @Test
    void requiresAToken() throws Exception {
        mockMvc.perform(get("/api/insights")).andExpect(status().isUnauthorized());
    }
}
