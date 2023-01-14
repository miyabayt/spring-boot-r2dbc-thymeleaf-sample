package com.bigtreetc.sample.r2dbc.controller.holidays;

import com.bigtreetc.sample.r2dbc.base.domain.validator.AbstractValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/** 祝日登録 入力チェック */
@Component
public class HolidayFormValidator extends AbstractValidator<HolidayForm> {

  @Override
  protected void doValidate(HolidayForm form, Errors errors) {}
}
