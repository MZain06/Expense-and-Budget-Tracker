package com.zain.expenses.category;

import static com.zain.expenses.TestAuth.bearerTokenFor;
import static com.zain.expenses.TestAuth.categoryId;
import static com.zain.expenses.TestData.addTransaction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class CategoryControllerTests {

    @Autowired private MockMvc mockMvc;

    @Test
    void listsTheUsersCategoriesAlphabetically() throws Exception {
        String token = bearerTokenFor(mockMvc, "zain@example.com");

        mockMvc.perform(get("/api/categories").header(AUTHORIZATION, token))
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
        mockMvc.perform(get("/api/categories").header(AUTHORIZATION, alex))
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    void requiresAToken() throws Exception {
        mockMvc.perform(get("/api/categories")).andExpect(status().isUnauthorized());
    }

    @Test
    void createsACategoryWithATidiedName() throws Exception {
        String zain = bearerTokenFor(mockMvc, "zain@example.com");

        mockMvc.perform(
                        post("/api/categories")
                                .header(AUTHORIZATION, zain)
                                .contentType(APPLICATION_JSON)
                                .content(nameJson("  Eating   out ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Eating out"));
        mockMvc.perform(get("/api/categories").header(AUTHORIZATION, zain))
                .andExpect(jsonPath("$.length()").value(8));
    }

    @Test
    void rejectsANameTheUserAlreadyHasWhateverItsCase() throws Exception {
        String zain = bearerTokenFor(mockMvc, "zain@example.com");

        mockMvc.perform(
                        post("/api/categories")
                                .header(AUTHORIZATION, zain)
                                .contentType(APPLICATION_JSON)
                                .content(nameJson("food")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("You already have a category called food"));
    }

    @Test
    void rejectsABlankName() throws Exception {
        String zain = bearerTokenFor(mockMvc, "zain@example.com");

        mockMvc.perform(
                        post("/api/categories")
                                .header(AUTHORIZATION, zain)
                                .contentType(APPLICATION_JSON)
                                .content(nameJson("   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void renamesACategory() throws Exception {
        String zain = bearerTokenFor(mockMvc, "zain@example.com");
        long food = categoryId(mockMvc, zain, "Food");

        mockMvc.perform(
                        put("/api/categories/{id}", food)
                                .header(AUTHORIZATION, zain)
                                .contentType(APPLICATION_JSON)
                                .content(nameJson("Groceries")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Groceries"));
    }

    @Test
    void canChangeTheCaseOfItsOwnName() throws Exception {
        String zain = bearerTokenFor(mockMvc, "zain@example.com");
        long food = categoryId(mockMvc, zain, "Food");

        mockMvc.perform(
                        put("/api/categories/{id}", food)
                                .header(AUTHORIZATION, zain)
                                .contentType(APPLICATION_JSON)
                                .content(nameJson("FOOD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("FOOD"));
    }

    @Test
    void refusesToRenameToAnotherCategorysName() throws Exception {
        String zain = bearerTokenFor(mockMvc, "zain@example.com");
        long food = categoryId(mockMvc, zain, "Food");

        mockMvc.perform(
                        put("/api/categories/{id}", food)
                                .header(AUTHORIZATION, zain)
                                .contentType(APPLICATION_JSON)
                                .content(nameJson("transport")))
                .andExpect(status().isConflict());
    }

    @Test
    void deletesAnUnusedCategory() throws Exception {
        String zain = bearerTokenFor(mockMvc, "zain@example.com");
        long food = categoryId(mockMvc, zain, "Food");

        mockMvc.perform(delete("/api/categories/{id}", food).header(AUTHORIZATION, zain))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/categories").header(AUTHORIZATION, zain))
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void refusesToDeleteACategoryThatHasTransactions() throws Exception {
        String zain = bearerTokenFor(mockMvc, "zain@example.com");
        long food = categoryId(mockMvc, zain, "Food");
        addTransaction(mockMvc, zain, food, "12.50", "2026-10-03");

        mockMvc.perform(delete("/api/categories/{id}", food).header(AUTHORIZATION, zain))
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.detail")
                                .value("Food still has 1 transaction. Move or delete it first."));
    }

    @Test
    void anotherUserCannotRenameOrDeleteMyCategory() throws Exception {
        String zain = bearerTokenFor(mockMvc, "zain@example.com");
        String alex = bearerTokenFor(mockMvc, "alex@example.com");
        long food = categoryId(mockMvc, zain, "Food");

        mockMvc.perform(
                        put("/api/categories/{id}", food)
                                .header(AUTHORIZATION, alex)
                                .contentType(APPLICATION_JSON)
                                .content(nameJson("Mine now")))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/categories/{id}", food).header(AUTHORIZATION, alex))
                .andExpect(status().isNotFound());
    }

    private static String nameJson(String name) {
        return """
        {"name": "%s"}
        """
                .formatted(name);
    }
}
