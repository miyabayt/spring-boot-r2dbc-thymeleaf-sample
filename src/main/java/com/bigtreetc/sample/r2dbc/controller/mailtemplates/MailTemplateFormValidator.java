package com.bigtreetc.sample.r2dbc.controller.mailtemplates;

import com.bigtreetc.sample.r2dbc.base.domain.validator.AbstractValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/** メールテンプレート登録 入力チェック */
@Component
public class MailTemplateFormValidator extends AbstractValidator<MailTemplateForm> {

  @Override
  protected void doValidate(MailTemplateForm form, Errors errors) {}
}
