package FYP;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {	
		
	@Bean
	public MemberDetailsService memberDetailsService() {
		return new MemberDetailsService();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(memberDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		
		return authProvider;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
	            .requestMatchers("/members/edit/*",
	                             "/members/save",
	                             "/",
	                             "/members",
	                             "/previewPage",
	                             "/info/edit/*").hasRole("ADMIN")
	            .requestMatchers("/mockpass", "/admin/register", "/admin/registered").permitAll() // Ensures /mockpass is accessible to all
	            .requestMatchers("/bootstrap/*/*").permitAll()
	            .requestMatchers("/images/*").permitAll()
	            .requestMatchers("/uploads/items/**").permitAll()
	            .anyRequest().authenticated())
	        .formLogin((login) -> login.loginPage("/login").permitAll().defaultSuccessUrl("/"))
	        .logout((logout) -> logout.logoutSuccessUrl("/"))
	        .exceptionHandling((exceptionHandling) -> exceptionHandling.accessDeniedPage("/403"));

	    return http.build();
	}

}

