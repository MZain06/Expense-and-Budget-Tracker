package com.zain.expenses;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Shared helpers that create test data through the API, the same way the frontend does. */
public final class TestData {

    private TestData() {}

    /** Adds money out without a note, e.g. amount "12.50" on date "2026-10-03". */
    public static void addTransaction(
            MockMvc mockMvc, String bearerToken, long categoryId, String amount, String date)
            throws Exception {
        addTransaction(mockMvc, bearerToken, categoryId, amount, date, "OUT");
    }

    /** Adds money in without a note, like pay or a refund. */
    public static void addMoneyIn(
            MockMvc mockMvc, String bearerToken, long categoryId, String amount, String date)
            throws Exception {
        addTransaction(mockMvc, bearerToken, categoryId, amount, date, "IN");
    }

    /** Sets a category's budget for a month, e.g. month "2026-10" and limit "200.00". */
    public static void setBudget(
            MockMvc mockMvc, String bearerToken, String month, long categoryId, String limit)
            throws Exception {
        String body =
                """
                {"limit": %s}
                """
                        .formatted(limit);
        mockMvc.perform(
                        put("/api/budgets/{month}/{categoryId}", month, categoryId)
                                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isOk());
    }

    private static void addTransaction(
            MockMvc mockMvc,
            String bearerToken,
            long categoryId,
            String amount,
            String date,
            String direction)
            throws Exception {
        String body =
                """
                {"categoryId": %d, "amount": %s, "occurredOn": "%s", "direction": "%s"}
                """
                        .formatted(categoryId, amount, date, direction);
        mockMvc.perform(
                        post("/api/transactions")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isCreated());
    }
}
