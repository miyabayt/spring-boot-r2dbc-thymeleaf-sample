package com.bigtreetc.sample.r2dbc.base.web.security;

import static com.bigtreetc.sample.r2dbc.base.util.ValidateUtils.isEquals;

import com.bigtreetc.sample.r2dbc.base.util.RequestUtils;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * ログイン画面を表示する際に、有効ではないセッションIDが渡ってきた場合は、 <br>
 * タイムアウトした場合のURLにリダイレクトする。 <br>
 * ただし、AJAX通信の場合は、ステータスコードのみを返す。
 */
@Slf4j
public class DefaultAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

  private final URI location;

  private final URI timeoutUri;

  private ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

  private ServerRequestCache requestCache = new WebSessionServerRequestCache();

  /** @param location */
  public DefaultAuthenticationEntryPoint(String location) {
    this.location = URI.create(location);
    this.timeoutUri = URI.create(location + "?timeout");
  }

  @Override
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
    val request = exchange.getRequest();
    if (RequestUtils.isAjaxRequest(request)) {
      return Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
    }

    val requestedSessionId = getSessionId(exchange);
    if (requestedSessionId != null) {
      return exchange
          .getSession()
          .flatMap(
              session -> {
                val sessionId = session.getId();
                val isExpired = session.isExpired();
                if (isEquals(requestedSessionId, sessionId) && isExpired) {
                  if (log.isDebugEnabled()) {
                    log.debug("セッションがタイムアウトしました。");
                  }
                }
                return redirectTo(exchange, this.timeoutUri);
              });
    }

    return redirectTo(exchange, this.location);
  }

  private Mono<Void> redirectTo(ServerWebExchange exchange, URI location) {
    return this.requestCache
        .saveRequest(exchange)
        .then(this.redirectStrategy.sendRedirect(exchange, location));
  }

  private HttpCookie getSessionId(ServerWebExchange exchange) {
    return exchange.getRequest().getCookies().getFirst("SESSION");
  }
}
