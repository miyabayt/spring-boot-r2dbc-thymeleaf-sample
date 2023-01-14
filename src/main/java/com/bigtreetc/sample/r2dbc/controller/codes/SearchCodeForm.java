package com.bigtreetc.sample.r2dbc.controller.codes;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseSearchForm;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchCodeForm extends BaseSearchForm {

  private static final long serialVersionUID = 223278986587249949L;

  UUID id;

  String categoryCode;

  String codeKey;

  String codeValue;
}
