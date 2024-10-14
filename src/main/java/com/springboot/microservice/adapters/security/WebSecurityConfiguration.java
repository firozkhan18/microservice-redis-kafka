/**
 * (C) Copyright 2021 Firoz Khan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springboot.microservice.adapters.security;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.filter.OncePerRequestFilter;

import com.springboot.microservice.server.config.ServiceConfiguration;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

	@Autowired
	private ServiceConfiguration serviceConfig;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		String apiPath = serviceConfig.getApiDocPath();

		http.authorizeRequests().requestMatchers("/api/**").permitAll().anyRequest().authenticated().and()
				.exceptionHandling().accessDeniedPage("/403").and().csrf().disable();

		http.headers().frameOptions().deny();

		String hostName = serviceConfig.getServerHost();

		http.headers()
				.contentSecurityPolicy("default-src 'self'; " + "script-src 'self' *." + hostName + "; "
						+ "object-src 'self' *." + hostName + "; "
						+ "img-src 'self'; media-src 'self'; frame-src 'self'; font-src 'self'; connect-src 'self'");

		return http.build();
	}

	public void configure(WebSecurity web) {
		web.ignoring().requestMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
	}

	@Bean
	public StrictHttpFirewall httpFirewall() {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowedHttpMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
		return firewall;
	}

	private static class CsrfTokenResponseHeaderBindingFilter extends OncePerRequestFilter {

		@Override
		protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
				jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain filterChain)
				throws jakarta.servlet.ServletException, IOException {
			CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
			response.setHeader("X-CSRF-HEADER", token.getHeaderName());
			response.setHeader("X-CSRF-PARAM", token.getParameterName());
			response.setHeader("X-CSRF-TOKEN", token.getToken());
			filterChain.doFilter(request, response);
		}
	}
}
