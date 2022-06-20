package com.bigtreetc.sample.r2dbc.base.web.filter;

import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;

import com.bigtreetc.sample.r2dbc.base.util.MessageUtils;
import java.util.HashMap;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
public class SetRedirectAttributeFilter implements WebFilter {

  @Setter
  private ServerWebExchangeMatcher requiresAuthenticationMatcher =
      ServerWebExchangeMatchers.anyExchange();

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    return this.requiresAuthenticationMatcher
        .matches(exchange)
        .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
        .flatMap(
            (matchResult) ->
                exchange
                    .getSession()
                    .flatMap(
                        session -> {
                          if (exchange.getRequest().getMethod() != HttpMethod.GET) {
                            return Mono.empty();
                          }

                          val attributes = exchange.getAttributes();
                          val message = session.getAttribute(GLOBAL_MESSAGE);
                          if (message != null) {
                            attributes.put(GLOBAL_MESSAGE, message);
                            session.getAttributes().remove(GLOBAL_MESSAGE);
                          }

                          val locale = exchange.getLocaleContext().getLocale();
                          val pulldownOption = MessageUtils.getMessage(MAV_PULLDOWN_OPTION, locale);
                          val constants = new HashMap<String, Object>();
                          constants.put(MAV_PULLDOWN_OPTION, pulldownOption);
                          attributes.put(MAV_CONST, constants);

                          return Mono.empty();
                        }))
        .switchIfEmpty(chain.filter(exchange))
        .then();
  }
}
