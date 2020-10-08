//package it.noi.edisplay.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//
//
////disabled for now, so Swagger can be accessed. Will be replaced with OAuth authorization
//
////@Configuration
////@EnableWebSecurity
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//	private static final String[] AUTH_LIST = { //
//		"/swagger-resources/**",
//		"/swagger-ui.html",
//		"/v2/api-docs",
//		"/webjars/**",
//		"/socket/*"
//	};
//
//	@Override
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication().withUser("user").password("password").roles("USER").and().withUser("admin")
//			.password("admin").roles("USER", "ADMIN");
//	}
//
//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		http.authorizeRequests()
//			.antMatchers(AUTH_LIST)
//			.authenticated()
//			.and().sessionManagement()
//			.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//			.csrf().disable();
//	}
//
//
//}
