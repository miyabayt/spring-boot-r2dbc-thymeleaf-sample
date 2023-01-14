package com.bigtreetc.sample.r2dbc.base.web.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CustomServerLogoutSuccessHandler implements ServerLogoutSuccessHandler {

  @NonNull final RedirectServerLogoutSuccessHandler redirectServerLogoutSuccessHandler;

  @Override
  public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
    return exchange
        .getExchange()
        .getSession()
        .flatMap(WebSession::invalidate)
        .then(redirectServerLogoutSuccessHandler.onLogoutSuccess(exchange, authentication));
  }
}
