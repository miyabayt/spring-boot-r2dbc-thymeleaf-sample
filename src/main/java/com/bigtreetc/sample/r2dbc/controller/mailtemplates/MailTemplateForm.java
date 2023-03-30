package com.bigtreetc.sample.r2dbc.controller.mailtemplates;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseForm;
import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MailTemplateForm extends BaseForm {

  private static final long serialVersionUID = -5860252006532570164L;

  UUID id;

  // カテゴリコード
  String categoryCode;

  // メールテンプレートコード
  @NotEmpty String templateCode;

  // メールタイトル
  @NotEmpty String subject;

  // メール本文
  @NotEmpty String templateBody;
}
