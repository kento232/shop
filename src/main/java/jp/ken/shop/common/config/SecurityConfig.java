package jp.ken.shop.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

	@EnableWebSecurity
	@Configuration
	public class SecurityConfig {
		
		
		  @Bean
		   PasswordEncoder passwordEncoder() {
		        return new BCryptPasswordEncoder();
		    }

		  @Bean
		      SessionRegistry sessionRegistry() {
		         return new SessionRegistryImpl();
		     }

		
		  @Bean
		  protected SecurityFilterChain securityFilterChain(HttpSecurity http,
				  UserDetailsService userDetailsService,
                  PasswordEncoder passwordEncoder) throws Exception {
			  
			
			  http.authorizeHttpRequests(auth -> auth
					.requestMatchers("/login","/top","/shop/registerform","/","/error").permitAll()
					.requestMatchers("/css/**","/images/**", "/js/**").permitAll()
					.anyRequest().authenticated());
			
				http.formLogin(login -> login
					.loginPage("/login")
					.loginProcessingUrl("/perform_login") //Controller から forward される
					.usernameParameter("loginInput")
					.passwordParameter("memberPassword")
					

					.defaultSuccessUrl("/top", true)
					.failureUrl("/login?error") 
					
					)
					.logout(logout -> logout
					.logoutUrl("/logout")
					.logoutSuccessUrl("/top")
					.invalidateHttpSession(true)
					.deleteCookies("JSESSIONID")
					.clearAuthentication(true)
					);   
					
					http.sessionManagement(session -> session
					.sessionConcurrency(concurrency -> concurrency
							.maximumSessions(1)
							.maxSessionsPreventsLogin(true)
							.sessionRegistry(sessionRegistry())  
			                )
			        );

			return http.build();
			}
		

@Bean
		protected HttpSessionEventPublisher httpSessionEventPublisher() {
			return new HttpSessionEventPublisher();
		}	

}
