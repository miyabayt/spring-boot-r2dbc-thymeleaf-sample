package com.bigtreetc.sample.r2dbc.controller.codecategories;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseForm;
import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CodeCategoryForm extends BaseForm {

  private static final long serialVersionUID = -7942742528754164062L;

  UUID id;

  // 分類コード
  @NotEmpty String categoryCode;

  // 分類名
  @NotEmpty String categoryName;
}
