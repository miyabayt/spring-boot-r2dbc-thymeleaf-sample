package com.bigtreetc.sample.r2dbc.controller.system.codecategories;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseForm;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CodeCategoryForm extends BaseForm {

  private static final long serialVersionUID = -7942742528754164062L;

  UUID id;

  // コード分類コード
  @NotEmpty String categoryCode;

  // コード分類名
  @NotEmpty String categoryName;
}
