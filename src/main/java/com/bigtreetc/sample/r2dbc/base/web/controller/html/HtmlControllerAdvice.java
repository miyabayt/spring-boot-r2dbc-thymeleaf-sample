package com.bigtreetc.sample.r2dbc.base.web.controller.html;

import static com.bigtreetc.sample.r2dbc.base.util.MessageUtils.getMessage;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;

import com.bigtreetc.sample.r2dbc.base.exception.FileNotFoundException;
import com.bigtreetc.sample.r2dbc.base.exception.NoDataFoundException;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.reactive.result.view.CsrfRequestDataValueProcessor;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ControllerAdvice
@Slf4j
public class HtmlControllerAdvice {

  @InitBinder
  public void initBinder(WebDataBinder binder, ServerWebExchange exchange) {
    // 文字列フィールドが未入力の場合に、空文字ではなくNULLをバインドする
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));

    // idカラムを入力値で上書きしない
    binder.setDisallowedFields("*.id");

    // versionカラムを入力値で上書きしない
    binder.setDisallowedFields("*.version");

    val br = binder.getBindingResult();
  }

  @ModelAttribute(CsrfRequestDataValueProcessor.DEFAULT_CSRF_ATTR_NAME)
  public Mono<CsrfToken> csrfToken(ServerWebExchange exchange) {
    return exchange.getAttributeOrDefault(CsrfToken.class.getName(), Mono.empty());
  }

  /**
   * ファイル、データ不存在時の例外をハンドリングする
   *
   * @param ex
   * @return
   */
  @ExceptionHandler({FileNotFoundException.class, NoDataFoundException.class})
  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  public String handleNotFoundException(Exception ex) {
    if (log.isDebugEnabled()) {
      log.debug("not found.", ex);
    }
    return NOTFOUND_VIEW;
  }

  /**
   * 権限不足エラーの例外をハンドリングする
   *
   * @param ex
   * @return
   */
  @ExceptionHandler({AccessDeniedException.class})
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public String handleAccessDeniedException(Exception ex) {
    if (log.isDebugEnabled()) {
      log.debug("forbidden.", ex);
    }
    return FORBIDDEN_VIEW;
  }

  /**
   * 楽観的排他制御により発生する例外をハンドリングする
   *
   * @param ex
   * @param model
   * @param locale
   * @return
   */
  @ExceptionHandler({OptimisticLockingFailureException.class})
  public String handleOptimisticLockingFailureException(Exception ex, Model model, Locale locale) {
    if (log.isDebugEnabled()) {
      log.debug("optimistic locking failure.", ex);
    }

    model.addAttribute(GLOBAL_MESSAGE, getMessage(OPTIMISTIC_LOCKING_FAILURE_ERROR, locale));

    return ERROR_VIEW;
  }

  /**
   * 予期せぬ例外をハンドリングする
   *
   * @param ex
   * @return
   */
  @ExceptionHandler({Exception.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String handleException(Exception ex) {
    log.error("unhandled runtime exception.", ex);
    return ERROR_VIEW;
  }
}
