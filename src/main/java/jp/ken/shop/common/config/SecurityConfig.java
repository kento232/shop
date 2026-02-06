package jp.ken.shop.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

	@EnableWebSecurity
	@Configuration
	public class SecurityConfig {
		
		// BCryptを前提（DBのmember_passwordはBCryptでハッシュ化して保存）
		  @Bean
		    protected PasswordEncoder passwordEncoder() {
		        return new BCryptPasswordEncoder();
		    }
		  @Bean
		  protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
			
			  http.authorizeHttpRequests(auth -> auth
					.requestMatchers("/login","/top").permitAll()
					.requestMatchers("/css/**").permitAll()
					.anyRequest().authenticated());
			
				http.formLogin(login -> login
					.loginPage("/login")
					.loginProcessingUrl("/login") //Controller から forward される
					.usernameParameter("loginInput")
					.passwordParameter("memberPassword")
					.defaultSuccessUrl("/top", true)
					.failureUrl("/login?error")
					)
					.logout(logout -> logout
					.logoutUrl("/logout")
					.invalidateHttpSession(true)
					.permitAll(
							))
					.sessionManagement(session -> session
					.sessionConcurrency(concurrency -> concurrency
							.maximumSessions(1)
							.maxSessionsPreventsLogin(true)
							)
					);
			return http.build();
			}
		
		
		@Bean
		protected HttpSessionEventPublisher httpSessionEventPublisher() {
			return new HttpSessionEventPublisher();
		}
}
