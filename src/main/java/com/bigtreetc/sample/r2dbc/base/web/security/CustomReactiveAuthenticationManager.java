package com.bigtreetc.sample.r2dbc.base.web.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import reactor.core.publisher.Mono;

@Slf4j
public class CustomReactiveAuthenticationManager
    extends UserDetailsRepositoryReactiveAuthenticationManager {

  private final AccountLockManager accountLockManager;

  public CustomReactiveAuthenticationManager(
      ReactiveUserDetailsService userDetailsService, AccountLockManager accountLockManager) {
    super(userDetailsService);
    this.accountLockManager = accountLockManager;
  }

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    return super.authenticate(authentication)
        .flatMap(
            a -> {
              if (a.isAuthenticated()) {
                return accountLockManager.saveLoginAttemptSucceeded(a);
              }
              return Mono.just(a);
            })
        .onErrorResume(
            (e) -> {
              if (e instanceof BadCredentialsException) {
                return accountLockManager
                    .saveLoginAttemptFailed(authentication)
                    .then(Mono.error(e));
              }
              return Mono.error(e);
            });
  }
}
