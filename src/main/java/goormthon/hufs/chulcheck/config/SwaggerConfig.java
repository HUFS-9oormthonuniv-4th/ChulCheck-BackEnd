package goormthon.hufs.chulcheck.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		Info info = new Info()
			.title("User Management API")
			.version("1.0")
			.description("API for managing users and their profile images.");

		Server server1 = new Server().url("http://localhost:8080");
		Server server2 = new Server().url("https://api.chulcheck.kr");
		List<Server> servers = new ArrayList<>();
		servers.add(server1);
		servers.add(server2);

		return new OpenAPI()
			.info(info)
			.servers(servers);
		// .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
		// .components(new io.swagger.v3.oas.models.Components());
		// .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
		// 	.type(SecurityScheme.Type.HTTP)
		// 	.scheme("bearer")
		// 	.bearerFormat("JWT")));
	}
}
