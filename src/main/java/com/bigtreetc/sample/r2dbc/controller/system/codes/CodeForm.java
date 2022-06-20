package com.bigtreetc.sample.r2dbc.controller.system.codes;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseForm;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CodeForm extends BaseForm {

  private static final long serialVersionUID = 7555305356779221873L;

  UUID id;

  // コード名
  @NotEmpty String codeValue;

  // コードエイリアス
  String codeAlias;

  // 表示順
  @NotNull Integer displayOrder;

  // 無効フラグ
  Boolean isInvalid;
}
