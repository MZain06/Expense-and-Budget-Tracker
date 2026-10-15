package com.zain.expenses.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zain.expenses.TestcontainersConfiguration;
import com.zain.expenses.category.Category;
import com.zain.expenses.category.CategoryRepository;
import com.zain.expenses.user.User;
import com.zain.expenses.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class RegistrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository users;
    @Autowired private CategoryRepository categories;

    @Test
    void registersUserWithHashedPasswordAndDefaultCategories() throws Exception {
        register("Zain@Example.com", "correct-horse-battery")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("zain@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        User saved = users.findByEmail("zain@example.com").orElseThrow();
        assertThat(saved.getPasswordHash()).startsWith("$2").isNotEqualTo("correct-horse-battery");
        assertThat(categories.findByUserIdOrderByNameAsc(saved.getId()))
                .extracting(Category::getName)
                .containsExactly(
                        "Bills",
                        "Entertainment",
                        "Food",
                        "Health",
                        "Other",
                        "Shopping",
                        "Transport");
    }

    @Test
    void rejectsAnEmailThatIsAlreadyRegisteredWhateverItsCase() throws Exception {
        register("zain@example.com", "correct-horse-battery").andExpect(status().isCreated());

        register("ZAIN@example.com", "another-password")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("An account with this email already exists"));
    }

    @Test
    void rejectsAnInvalidEmailAndATooShortPassword() throws Exception {
        register("not-an-email", "short")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    private ResultActions register(String email, String password) throws Exception {
        return mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"email": "%s", "password": "%s"}
                                """
                                        .formatted(email, password)));
    }
}
