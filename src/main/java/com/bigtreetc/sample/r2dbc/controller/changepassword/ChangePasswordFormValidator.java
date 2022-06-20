package com.bigtreetc.sample.r2dbc.controller.changepassword;

import static com.bigtreetc.sample.r2dbc.base.util.ValidateUtils.isNotEquals;

import com.bigtreetc.sample.r2dbc.base.domain.validator.AbstractValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/** コード登録 入力チェック */
@Component
public class ChangePasswordFormValidator extends AbstractValidator<ChangePasswordForm> {

  @Override
  protected void doValidate(ChangePasswordForm form, Errors errors) {
    // 確認用パスワードと突き合わせる
    if (isNotEquals(form.getPassword(), form.getPasswordConfirm())) {
      errors.rejectValue("password", "changePassword.unmatchPassword");
      errors.rejectValue("passwordConfirm", "changePassword.unmatchPassword");
    }
  }
}
