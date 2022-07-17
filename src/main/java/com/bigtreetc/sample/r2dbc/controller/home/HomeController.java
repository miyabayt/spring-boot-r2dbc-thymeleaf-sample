package com.bigtreetc.sample.r2dbc.controller.home;

import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.HOME_URL;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.AbstractHtmlController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class HomeController extends AbstractHtmlController {

  @PreAuthorize("hasAnyRole('system_admin', 'operation_admin', 'operator')")
  @GetMapping({HOME_URL, "/home"})
  public Mono<String> index(Model model) {
    return Mono.just("modules/home/index");
  }
}
