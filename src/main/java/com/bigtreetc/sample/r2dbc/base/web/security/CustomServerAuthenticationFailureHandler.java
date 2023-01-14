package com.bigtreetc.sample.r2dbc.base.web.security;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class CustomServerAuthenticationFailureHandler
    implements ServerAuthenticationFailureHandler {

  private final String location;

  private ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

  public CustomServerAuthenticationFailureHandler(String location) {
    this.location = location;
  }

  @Override
  public Mono<Void> onAuthenticationFailure(
      WebFilterExchange webFilterExchange, AuthenticationException exception) {
    ServerWebExchange exchange = webFilterExchange.getExchange();
    String loginUrl = this.location;
    if (exception instanceof LockedException) {
      loginUrl += "?locked";
    } else {
      loginUrl += "?error";
    }
    return this.redirectStrategy.sendRedirect(exchange, URI.create(loginUrl));
  }
}
