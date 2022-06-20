package com.bigtreetc.sample.r2dbc.controller.system.mailtemplates;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseSearchForm;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchMailTemplateForm extends BaseSearchForm {

  private static final long serialVersionUID = -6365336122351427141L;

  UUID id;

  // メールテンプレートコード
  String templateCode;

  // メールタイトル
  String subject;

  // メール本文
  String templateBody;
}
