package com.mfasia.onlineexamsystem.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

@EnableResourceServer
@Configuration
public class ResourceServerConfig extends WebSecurityConfigurerAdapter{

	@Autowired private AuthenticationManager authenticationManager;
	@Autowired private UserDetailsService customUserDetailsService;
	@Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired private DataSource dataSource;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.requestMatchers()
		.and()
			.authorizeRequests()
			.antMatchers("/pages/teachersPanel", "/pages/questionBank", "/pages/questionerDefination").hasAnyAuthority("ROLE_TEACHER")
			.antMatchers("/pages/adminPanel", "/pages/book", "/pages/courseDetails", "/pages/examBoard").hasAnyAuthority("ROLE_ADMIN")
			.antMatchers("/resources/**","/fonts/**", "/customJS/**").permitAll()
			.antMatchers("/pages/regestration","/pages/emailVerification","/user/code/**","/user/save","/courses","/batch").permitAll()
			.anyRequest().authenticated()
		.and()
			.formLogin()
			.loginPage("/login")
			.failureUrl("/login?error=true")
			.successHandler(new CustomAuthenticationSuccessHandler())
			.permitAll()
		.and()
			.exceptionHandling()
			.accessDeniedPage("/pages/403")
		.and()
			.logout()
			.logoutUrl("/logout")
			.logoutSuccessUrl("/login")
			.deleteCookies("JSESSIONID")
			.invalidateHttpSession(true)
			.permitAll()
		.and()
			.rememberMe()
			.rememberMeCookieName("online-exam-system-remember-me")
			.tokenValiditySeconds(24 * 60 * 60)
			.tokenRepository(persistentTokenRepository())
		.and()
			.csrf()
			.disable()
		;
		
	}
	
	@Bean
	public PersistentTokenRepository persistentTokenRepository() {
		JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
		tokenRepository.setDataSource(dataSource);
		return tokenRepository;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.parentAuthenticationManager(authenticationManager)
		.userDetailsService(customUserDetailsService)
		.passwordEncoder(bCryptPasswordEncoder);

	}

	
}
