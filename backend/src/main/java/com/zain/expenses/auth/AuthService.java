package com.zain.expenses.auth;

import com.zain.expenses.category.Category;
import com.zain.expenses.category.CategoryKind;
import com.zain.expenses.category.CategoryRepository;
import com.zain.expenses.security.TokenService;
import com.zain.expenses.user.User;
import com.zain.expenses.user.UserRepository;
import com.zain.expenses.user.UserResponse;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    static final List<String> DEFAULT_CATEGORIES =
            List.of("Bills", "Entertainment", "Food", "Health", "Other", "Shopping", "Transport");
    static final List<String> DEFAULT_INCOME_CATEGORIES = List.of("Other income", "Salary");

    private final UserRepository users;
    private final CategoryRepository categories;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(
            UserRepository users,
            CategoryRepository categories,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.users = users;
        this.categories = categories;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalise(request.email());
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }
        User user = users.save(new User(email, passwordEncoder.encode(request.password())));
        categories.saveAll(
                Stream.concat(
                                DEFAULT_CATEGORIES.stream().map(name -> new Category(user, name)),
                                DEFAULT_INCOME_CATEGORIES.stream()
                                        .map(name -> new Category(user, name, CategoryKind.INCOME)))
                        .toList());
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user =
                users.findByEmail(normalise(request.email()))
                        .filter(
                                found ->
                                        passwordEncoder.matches(
                                                request.password(), found.getPasswordHash()))
                        .orElseThrow(InvalidCredentialsException::new);
        TokenService.IssuedToken token = tokenService.issueFor(user);
        return TokenResponse.bearer(token.value(), token.expiresInSeconds());
    }

    static String normalise(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
