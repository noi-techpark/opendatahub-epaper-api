// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
			.select()
			.apis(RequestHandlerSelectors.basePackage("it.noi.edisplay.controller"))
			.paths(PathSelectors.any())
			.build()
			.apiInfo(apiInfo())
			.securityContexts(Arrays.asList(securityContext()))
			.securitySchemes(Arrays.asList(basicAuthScheme()));
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder()
			.securityReferences(Arrays.asList(basicAuthReference()))
			.forPaths(PathSelectors.ant("/api/v1/**"))
			.build();
	}

	private SecurityScheme basicAuthScheme() {
		return new BasicAuth("basicAuth");
	}

	private SecurityReference basicAuthReference() {
		return new SecurityReference("basicAuth", new AuthorizationScope[0]);
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("E-Ink-Displays").description("API to use E-Ink Displays as room information monitors")
//                .termsOfServiceUrl("https://www.example.com/api")
//                .contact(new Contact("Contact", "http://www.example.com", "contact@example.com"))
			.license("GPL v.3").licenseUrl("https://www.gnu.org/licenses/gpl-3.0.en.html").version("0.1").build();
	}
}
