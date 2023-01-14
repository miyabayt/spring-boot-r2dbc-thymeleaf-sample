package com.bigtreetc.sample.r2dbc.controller.users;

import static com.bigtreetc.sample.r2dbc.base.util.ValidateUtils.isNotEquals;

import com.bigtreetc.sample.r2dbc.base.domain.validator.AbstractValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/** ユーザ登録 入力チェック */
@Component
public class UserFormValidator extends AbstractValidator<UserForm> {

  @Override
  protected void doValidate(UserForm form, Errors errors) {

    // 確認用パスワードと突き合わせる
    if (isNotEquals(form.getPassword(), form.getPasswordConfirm())) {
      errors.rejectValue("password", "users.unmatchPassword");
      errors.rejectValue("passwordConfirm", "users.unmatchPassword");
    }
  }
}
