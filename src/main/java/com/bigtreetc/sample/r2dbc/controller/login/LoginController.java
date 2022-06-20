package com.bigtreetc.sample.r2dbc.controller.login;

import static com.bigtreetc.sample.r2dbc.base.util.MessageUtils.getMessage;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.AbstractHtmlController;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class LoginController extends AbstractHtmlController {

  /**
   * 初期表示
   *
   * @param exchange
   * @param model
   * @return
   */
  @GetMapping(LOGIN_URL)
  public Mono<String> index(ServerWebExchange exchange, Model model) {
    val params = exchange.getRequest().getQueryParams();
    if (params.containsKey("error")) {
      model.addAttribute(GLOBAL_MESSAGE, getMessage("login.failed"));
    } else if (params.containsKey("timeout")) {
      model.addAttribute(GLOBAL_MESSAGE, getMessage("login.timeout"));
    } else if (params.containsKey("logout")) {
      model.addAttribute(GLOBAL_MESSAGE, getMessage("logout.success"));
    }

    return Mono.just("modules/login/login");
  }
}
