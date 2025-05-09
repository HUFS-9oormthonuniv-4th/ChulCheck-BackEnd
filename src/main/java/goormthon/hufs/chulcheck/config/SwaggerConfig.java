package goormthon.hufs.chulcheck.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		Info info = new Info()
			.title("User Management API")
			.version("1.0")
			.description("API for managing users and their profile images.");

		return new OpenAPI()
			.info(info);
		// .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
		// .components(new io.swagger.v3.oas.models.Components());
		// .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
		// 	.type(SecurityScheme.Type.HTTP)
		// 	.scheme("bearer")
		// 	.bearerFormat("JWT")));
	}
}
