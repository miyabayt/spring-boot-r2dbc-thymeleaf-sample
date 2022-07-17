package com.bigtreetc.sample.r2dbc.controller.home;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.bigtreetc.sample.r2dbc.BaseTestContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class HomeControllerTest extends BaseTestContainerTest {

  @Autowired ApplicationContext context;

  WebTestClient webClient;

  @BeforeEach
  void setup() {
    webClient =
        WebTestClient.bindToApplicationContext(context)
            .apply(springSecurity())
            .configureClient()
            .build();
  }

  @Test
  @DisplayName("権限を持つ担当者でホームを開けること")
  @WithMockUser(roles = {"system_admin"})
  void test1() throws Exception {
    webClient.get().uri("/home").exchange().expectStatus().isOk();
  }

  @Test
  @DisplayName("権限を持たない担当者でホームが開けないこと")
  @WithMockUser(roles = {"user"})
  void test2() throws Exception {
    webClient.get().uri("/home").exchange().expectStatus().isForbidden();
  }
}
