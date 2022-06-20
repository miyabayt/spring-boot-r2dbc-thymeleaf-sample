package com.bigtreetc.sample.r2dbc.controller.system.staffs;

import com.bigtreetc.sample.r2dbc.base.domain.validator.AbstractValidator;
import com.bigtreetc.sample.r2dbc.base.util.ValidateUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/** 担当者登録 入力チェック */
@Component
public class StaffFormValidator extends AbstractValidator<StaffForm> {

  @Override
  protected void doValidate(StaffForm form, Errors errors) {
    // 確認用パスワードと突き合わせる
    if (form.getPassword() != null
        && form.getPasswordConfirm() != null
        && ValidateUtils.isNotEquals(form.getPassword(), form.getPasswordConfirm())) {
      errors.rejectValue("password", "staffs.unmatchPassword");
      errors.rejectValue("passwordConfirm", "staffs.unmatchPassword");
    }
  }
}
