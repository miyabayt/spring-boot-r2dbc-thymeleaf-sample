package com.bigtreetc.sample.r2dbc;

import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;

import com.bigtreetc.sample.r2dbc.base.web.security.*;
import com.bigtreetc.sample.r2dbc.domain.repository.StaffRepository;
import com.bigtreetc.sample.r2dbc.security.StaffAccountLockManager;
import java.net.URI;
import lombok.val;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public CorsWebFilter corsWebFilter(CorsProperties corsProperties) {
    val corsConfig = new CorsConfiguration();
    corsConfig.setAllowCredentials(corsProperties.getAllowCredentials());
    corsConfig.setAllowedHeaders(corsProperties.getAllowedHeaders());
    corsConfig.setAllowedMethods(corsProperties.getAllowedMethods());
    corsConfig.setAllowedOrigins(corsProperties.getAllowedOrigins());
    corsConfig.setExposedHeaders(corsProperties.getExposedHeaders());
    corsConfig.setMaxAge(corsProperties.getMaxAge());
    val source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);
    return new CorsWebFilter(source);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(
      ServerHttpSecurity http, ReactiveAuthenticationManager authenticationManager) {

    // CookieにCSRFトークンを保存する
    http.csrf()
        .tokenFromMultipartDataEnabled(true)
        .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse());

    String[] permittedUrls = {
      LOGIN_URL,
      ERROR_URL,
      RESET_PASSWORD_URL,
      CHANGE_PASSWORD_URL,
      WEBJARS_URL,
      STATIC_RESOURCES_URL,
      ACTUATOR_URL
    };

    val authenticationSuccessHandler = new RedirectServerAuthenticationSuccessHandler();
    val logoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
    logoutSuccessHandler.setLogoutSuccessUrl(URI.create(LOGIN_URL));
    val customLogoutSuccessHandler = new CustomServerLogoutSuccessHandler(logoutSuccessHandler);
    val loginFailureHandler = new CustomServerAuthenticationFailureHandler(LOGIN_URL);

    http.authorizeExchange()
        .pathMatchers(permittedUrls)
        .permitAll()
        .anyExchange()
        .authenticated()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(new DefaultAuthenticationEntryPoint(LOGIN_URL))
        .accessDeniedHandler(new HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN))
        .and()
        .formLogin()
        .loginPage(LOGIN_URL)
        .authenticationManager(authenticationManager)
        .authenticationSuccessHandler(authenticationSuccessHandler)
        .authenticationFailureHandler(loginFailureHandler)
        .and()
        .logout()
        .logoutUrl(LOGOUT_URL)
        .logoutSuccessHandler(customLogoutSuccessHandler);

    return http.build();
  }

  @Bean
  public StaffAccountLockManager staffAccountLockManager(StaffRepository systemAdminRepository) {
    return new StaffAccountLockManager(systemAdminRepository, 5);
  }

  @Bean
  public ReactiveAuthenticationManager authenticationManager(
      ReactiveUserDetailsService userDetailsService,
      AccountLockManager accountLockManager,
      PasswordEncoder passwordEncoder) {
    val authenticationManager =
        new CustomReactiveAuthenticationManager(userDetailsService, accountLockManager);
    authenticationManager.setPasswordEncoder(passwordEncoder);
    return authenticationManager;
  }
}
