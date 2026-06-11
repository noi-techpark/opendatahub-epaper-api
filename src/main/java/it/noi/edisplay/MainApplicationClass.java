// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MainApplicationClass extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(MainApplicationClass.class, args);
	}

	@Bean
	public FilterRegistrationBean<Filter> corsFilter() {
		FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>(new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
					throws IOException, javax.servlet.ServletException {
				String origin = request.getHeader("Origin");
				if (origin != null) {
					// Echo the origin back — handles regular origins, and "null" from file:// pages
					response.setHeader("Access-Control-Allow-Origin", origin);
					response.setHeader("Access-Control-Allow-Methods", "*");
					response.setHeader("Access-Control-Allow-Headers", "*");
				}
				if ("OPTIONS".equals(request.getMethod())) {
					response.setStatus(HttpServletResponse.SC_OK);
					return;
				}
				chain.doFilter(request, response);
			}
		});
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}

	@Bean
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

		scheduler.setPoolSize(2);
		scheduler.setThreadNamePrefix("scheduled-task-");
		scheduler.setDaemon(true);

		return scheduler;
	}


}
