package jp.ken.shop.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

	private final LoginSuccessHandler loginSuccessHandler;

	public SecurityConfig(LoginSuccessHandler loginSuccessHandler) {
		this.loginSuccessHandler = loginSuccessHandler;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	@Bean
	protected SecurityFilterChain securityFilterChain(HttpSecurity http,
			UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) throws Exception {

		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/products/**", "/favicon.ico", "/products", "/login", "/top", "/shop/registerform",
						"/", "/error", "/confirm")
				.permitAll()
				.requestMatchers("/cart/add").permitAll()
				.requestMatchers("/cart").permitAll()
				.requestMatchers("/css/**", "/images/**", "/js/**", "/webjars/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/cart/add").permitAll()
				.requestMatchers(HttpMethod.POST, "/cart/delete").permitAll()
				.requestMatchers(HttpMethod.POST, "/shop/register").permitAll()
				// API（カテゴリ一覧・検索）は未ログインでも使えるように
				.requestMatchers(HttpMethod.POST, "/cart/api/items/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/cart/api/count").permitAll()
				// それ以外のAPI方針（必要に応じて変更）
				.requestMatchers("/cart/api/**").authenticated()
				// 画面はとりあえず全許可など、要件で調整

				.requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/products/search").permitAll()
				.requestMatchers("/perform_login").permitAll()

				.anyRequest().authenticated());

		http.formLogin(login -> login
				.loginPage("/login")
				.loginProcessingUrl("/perform_login") //Controller から forward される
				.usernameParameter("loginInput")
				.passwordParameter("memberPassword")
				.successHandler(loginSuccessHandler)
				.failureUrl("/login?error")

		)
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/top")
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID")
						.clearAuthentication(true));

		http.sessionManagement(session -> session
				.sessionConcurrency(concurrency -> concurrency
						.maximumSessions(1)
						.maxSessionsPreventsLogin(true)
						.sessionRegistry(sessionRegistry())));
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/cart/add"));
		http
				.securityContext(context -> context
						.securityContextRepository(securityContextRepository()));

		return http.build();
	}

	@Bean
	protected HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

}
