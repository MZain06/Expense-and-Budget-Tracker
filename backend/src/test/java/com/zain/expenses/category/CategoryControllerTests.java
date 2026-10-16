package com.zain.expenses.category;

import static com.zain.expenses.TestAuth.bearerTokenFor;
import static com.zain.expenses.TestAuth.categoryId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zain.expenses.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class CategoryControllerTests {

    @Autowired private MockMvc mockMvc;

    @Test
    void listsTheUsersCategoriesAlphabetically() throws Exception {
        String token = bearerTokenFor(mockMvc, "zain@example.com");

        mockMvc.perform(get("/api/categories").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].name").value("Bills"))
                .andExpect(jsonPath("$[6].name").value("Transport"));
    }

    @Test
    void eachUserOnlySeesTheirOwnCategories() throws Exception {
        String zain = bearerTokenFor(mockMvc, "zain@example.com");
        String alex = bearerTokenFor(mockMvc, "alex@example.com");

        assertThat(categoryId(mockMvc, zain, "Food"))
                .isNotEqualTo(categoryId(mockMvc, alex, "Food"));
        mockMvc.perform(get("/api/categories").header(HttpHeaders.AUTHORIZATION, alex))
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    void requiresAToken() throws Exception {
        mockMvc.perform(get("/api/categories")).andExpect(status().isUnauthorized());
    }
}
