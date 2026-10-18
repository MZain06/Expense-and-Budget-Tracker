package com.zain.expenses;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Shared helpers that create test data through the API, the same way the frontend does. */
public final class TestData {

    private TestData() {}

    /** Adds a transaction without a note, e.g. amount "12.50" on date "2026-10-03". */
    public static void addTransaction(
            MockMvc mockMvc, String bearerToken, long categoryId, String amount, String date)
            throws Exception {
        String body =
                """
                {"categoryId": %d, "amount": %s, "occurredOn": "%s"}
                """
                        .formatted(categoryId, amount, date);
        mockMvc.perform(
                        post("/api/transactions")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isCreated());
    }
}
