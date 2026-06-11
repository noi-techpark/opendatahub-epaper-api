// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.config;

import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

	@Bean
	public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		KeycloakAuthenticationProvider provider = keycloakAuthenticationProvider();
		provider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
		auth.authenticationProvider(provider);
	}

	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		// bearer-only, no session needed
		return new NullAuthenticatedSessionStrategy();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		http
			.cors().configurationSource(request -> {
				CorsConfiguration config = new CorsConfiguration();
				String origin = request.getHeader("Origin");
				// Echo the origin back — handles regular origins and "null" from file:// pages
				config.addAllowedOrigin(origin != null ? origin : "*");
				config.addAllowedMethod("*");
				config.addAllowedHeader("*");
				return config;
			}).and()
			.csrf().disable()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.authorizeRequests()
				// Display hardware endpoints — no auth, called by e-paper devices
				.antMatchers(HttpMethod.POST, "/display/sync-status/**").permitAll()
				// Image endpoints — loaded via <img src> in the webapp, cannot send auth headers
				.antMatchers(HttpMethod.GET, "/display/get-image/**").permitAll()
				.antMatchers(HttpMethod.GET, "/template/get-image/**").permitAll()
				.antMatchers(HttpMethod.GET, "/ScheduledContent/get-image/**").permitAll()
				// Swagger UI
				.antMatchers("/", "/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs", "/webjars/**").permitAll()
				// Everything else requires a valid Keycloak bearer token with the admin client role
				.anyRequest().hasRole("admin");
	}
}
