package com.bigtreetc.sample.r2dbc.base.web.security;

import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

public interface AccountLockManager {

  Mono<Authentication> saveLoginAttemptSucceeded(Authentication authentication);

  Mono<Authentication> saveLoginAttemptFailed(Authentication authentication);
}
