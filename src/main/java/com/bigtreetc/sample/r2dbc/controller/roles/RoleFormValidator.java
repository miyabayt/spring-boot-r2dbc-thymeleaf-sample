package com.bigtreetc.sample.r2dbc.controller.roles;

import com.bigtreetc.sample.r2dbc.base.domain.validator.AbstractValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/** ロール登録 入力チェック */
@Component
public class RoleFormValidator extends AbstractValidator<RoleForm> {

  @Override
  protected void doValidate(RoleForm form, Errors errors) {}
}
