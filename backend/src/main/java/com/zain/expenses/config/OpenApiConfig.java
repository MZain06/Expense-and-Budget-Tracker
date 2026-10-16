package com.zain.expenses.config;

import com.zain.expenses.security.CurrentUserId;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

/** Describes the API for the generated OpenAPI docs and Swagger UI. */
@Configuration
@OpenAPIDefinition(
        info =
                @Info(
                        title = "Expense Tracker API",
                        version = "v1",
                        description = "Personal expense and budget tracking"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {

    static {
        // The user id comes from the token, not the request, so keep it out of the docs.
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUserId.class);
    }
}
