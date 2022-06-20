package com.bigtreetc.sample.r2dbc.controller.home;

import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.HOME_URL;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.AbstractHtmlController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController extends AbstractHtmlController {

  @GetMapping({HOME_URL, "/home"})
  public String index(Model model) {
    return "modules/home/index";
  }
}
