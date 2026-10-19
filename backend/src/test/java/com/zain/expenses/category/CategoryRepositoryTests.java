package com.zain.expenses.category;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zain.expenses.TestcontainersConfiguration;
import com.zain.expenses.user.User;
import com.zain.expenses.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CategoryRepositoryTests {

    @Autowired private UserRepository users;
    @Autowired private CategoryRepository categories;

    @Test
    void databaseRejectsANameThatOnlyDiffersInCase() {
        User user = users.save(new User("zain@example.com", "hash"));
        categories.saveAndFlush(new Category(user, "Food"));

        // The service checks this first, but the database rule still holds if two requests race.
        assertThatThrownBy(() -> categories.saveAndFlush(new Category(user, "food")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
