package com.bigtreetc.sample.r2dbc.controller.changepassword;

import static com.bigtreetc.sample.r2dbc.base.util.MessageUtils.getMessage;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.AbstractHtmlController;
import com.bigtreetc.sample.r2dbc.domain.service.ChangePasswordService;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Controller
@SessionAttributes(types = {ChangePasswordForm.class})
@Slf4j
public class ChangePasswordController extends AbstractHtmlController {

  @NonNull final ChangePasswordFormValidator changePasswordFormValidator;

  @NonNull final ChangePasswordService changePasswordService;

  @NonNull final PasswordEncoder passwordEncoder;

  @ModelAttribute("changePasswordForm")
  public ChangePasswordForm changePasswordForm() {
    return new ChangePasswordForm();
  }

  @InitBinder("changePasswordForm")
  public void validatorBinder(WebDataBinder binder) {
    binder.addValidators(changePasswordFormValidator);
  }

  /**
   * パスワードのリセット 初期表示
   *
   * @param form
   * @param model
   * @return
   */
  @GetMapping(RESET_PASSWORD_URL)
  public Mono<String> resetPassword(@ModelAttribute ResetPasswordForm form, Model model) {
    return Mono.just("modules/login/resetPassword");
  }

  /**
   * パスワードのリセット メール送信処理
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PostMapping(RESET_PASSWORD_URL)
  public Mono<Rendering> resetPassword(
      @Validated @ModelAttribute ResetPasswordForm form, BindingResult br, WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/resetPassword");
    }

    // パスワードリセットのメールを送信する
    val email = form.getEmail();
    return changePasswordService
        .sendResetPasswordMail(email)
        .flatMap(
            success -> {
              session.getAttributes().put(GLOBAL_MESSAGE, getMessage("resetPassword.sent"));
              return redirectTo("/resetPassword?sent");
            });
  }

  /**
   * パスワード変更 初期表示
   *
   * @param form
   * @param model
   * @return
   */
  @GetMapping(CHANGE_PASSWORD_URL)
  public Mono<Rendering> changePassword(
      @RequestParam Map<String, String> params,
      @ModelAttribute ChangePasswordForm form,
      Model model) {
    if (params.containsKey("done")) {
      return Mono.just(Rendering.view("modules/login/changePassword").build());
    }

    val token = form.getToken();
    return changePasswordService
        .isValidPasswordResetToken(token)
        .map(
            isValid -> {
              if (isValid) {
                return Rendering.view("modules/login/changePassword").build();
              }
              return Rendering.redirectTo("/resetPassword?error").build();
            });
  }

  /**
   * パスワード変更 更新処理
   *
   * @param form
   * @param br
   * @param sessionStatus
   * @param session
   * @return
   */
  @PostMapping(CHANGE_PASSWORD_URL)
  public Mono<Rendering> changePassword(
      @Validated @ModelAttribute ChangePasswordForm form,
      BindingResult br,
      SessionStatus sessionStatus,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/changePassword?token=%s".formatted(form.getToken()));
    }

    val token = form.getToken();
    val password = passwordEncoder.encode(form.getPassword());

    // 有効性をチェックする
    return changePasswordService
        .updatePassword(token, password)
        .flatMap(
            success -> {
              if (success) {
                // セッションのChangePasswordFormをクリアする
                sessionStatus.setComplete();
                return redirectTo("/changePassword?done");
              }
              return redirectTo("/changePassword");
            });
  }
}
