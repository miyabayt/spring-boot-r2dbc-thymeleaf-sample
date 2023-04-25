package com.bigtreetc.sample.r2dbc.base.web.controller.html;

import static com.bigtreetc.sample.r2dbc.base.util.MessageUtils.getMessage;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

/** 基底HTMLコントローラー */
@Slf4j
public abstract class AbstractHtmlController {

  @Autowired protected ModelMapper modelMapper;

  /**
   * 入力チェックエラーがある場合はtrueを返します。
   *
   * @param session
   * @return
   */
  protected boolean hasErrors(WebSession session) {
    val errors = session.getAttributes().get(MAV_ERRORS);
    if (errors instanceof BeanPropertyBindingResult br) {
      if (br.hasErrors()) {
        return true;
      }
    }

    return false;
  }

  protected Mono<Void> setBindingResultToAttribute(Model model, WebSession session) {
    return Mono.fromRunnable(
        () -> {
          val errors = session.getAttribute(MAV_ERRORS);
          if (errors instanceof BeanPropertyBindingResult br) {
            if (br.hasErrors()) {
              val formName = br.getObjectName();
              val key = BindingResult.MODEL_KEY_PREFIX + formName;
              model.addAttribute(key, errors);
              model.addAttribute(GLOBAL_MESSAGE, getMessage(VALIDATION_ERROR));
            }
            session.getAttributes().remove(MAV_ERRORS);
          }
        });
  }

  protected Mono<Rendering> redirectTo(final String redirectTo) {
    return Mono.fromCallable(() -> Rendering.redirectTo(redirectTo).build());
  }

  @SneakyThrows
  protected ResponseEntity<Resource> toResponseEntity(Resource resource, String filename) {
    return toResponseEntity(resource, filename, false);
  }

  @SneakyThrows
  protected ResponseEntity<Resource> toResponseEntity(
      Resource resource, String filename, boolean isAttachment) {

    val responseEntity =
        ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

    if (isAttachment) {
      val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.name());
      val contentDisposition = String.format("attachment; filename*=UTF-8''%s", encodedFilename);
      responseEntity.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
    }

    return responseEntity.body(resource);
  }

  @SneakyThrows
  protected void setContentDispositionHeader(
      ServerHttpResponse response, String filename, boolean isAttachment) {
    response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

    if (isAttachment) {
      val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
      val contentDisposition = String.format("attachment; filename*=UTF-8''%s", encodedFilename);
      response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
    }
  }
}
