package com.zain.expenses.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.zain.expenses.TestcontainersConfiguration;
import com.zain.expenses.transaction.TransactionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
@Import(TestcontainersConfiguration.class)
class DemoDataSeederTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private DemoDataSeeder seeder;
    @Autowired private TransactionRepository transactions;

    @Test
    void demoAccountHasAYearOfSpendingAndThisMonthsBudgets() throws Exception {
        String demo = logInAsDemo();

        String insights =
                mockMvc.perform(get("/api/insights").header(AUTHORIZATION, demo))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.trend.length()").value(12))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        List<Number> monthlyTotals = JsonPath.read(insights, "$.trend[*].spent");
        assertThat(monthlyTotals).allSatisfy(total -> assertThat(total.doubleValue()).isPositive());

        mockMvc.perform(get("/api/budgets").header(AUTHORIZATION, demo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void resettingReplacesTheDataAndKeepsEarlierTokensWorking() throws Exception {
        String demo = logInAsDemo();
        long before = transactions.count();

        seeder.run(new DefaultApplicationArguments());

        assertThat(transactions.count()).isEqualTo(before);
        mockMvc.perform(get("/api/budgets").header(AUTHORIZATION, demo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

    private String logInAsDemo() throws Exception {
        String credentials =
                """
                {"email": "%s", "password": "%s"}
                """
                        .formatted(DemoDataSeeder.EMAIL, DemoDataSeeder.PASSWORD);
        String body =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(credentials))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        String token = JsonPath.read(body, "$.accessToken");
        return "Bearer " + token;
    }
}
