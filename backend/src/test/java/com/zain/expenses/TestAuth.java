package com.zain.expenses;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Shared helpers for tests that need a logged-in user. */
public final class TestAuth {

    private TestAuth() {}

    /** Registers a new user, logs them in and returns the value for an Authorization header. */
    public static String bearerTokenFor(MockMvc mockMvc, String email) throws Exception {
        String credentials =
                """
                {"email": "%s", "password": "correct-horse-battery"}
                """
                        .formatted(email);
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(credentials))
                .andExpect(status().isCreated());
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

    /** Looks up the id of one of the user's categories by its name. */
    public static long categoryId(MockMvc mockMvc, String bearerToken, String name)
            throws Exception {
        String body =
                mockMvc.perform(
                                get("/api/categories")
                                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        List<Number> ids = JsonPath.read(body, "$[?(@.name == '" + name + "')].id");
        return ids.get(0).longValue();
    }
}
