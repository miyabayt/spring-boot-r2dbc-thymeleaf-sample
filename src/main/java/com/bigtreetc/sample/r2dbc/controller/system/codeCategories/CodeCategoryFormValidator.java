package com.bigtreetc.sample.r2dbc.controller.system.codeCategories;

import com.bigtreetc.sample.r2dbc.base.domain.validator.AbstractValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/** コード分類マスタ登録 入力チェック */
@Component
public class CodeCategoryFormValidator extends AbstractValidator<CodeCategoryForm> {

  @Override
  protected void doValidate(CodeCategoryForm form, Errors errors) {}
}
