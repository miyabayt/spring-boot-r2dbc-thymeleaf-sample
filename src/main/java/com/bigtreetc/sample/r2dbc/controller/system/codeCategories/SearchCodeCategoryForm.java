package com.bigtreetc.sample.r2dbc.controller.system.codeCategories;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseSearchForm;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchCodeCategoryForm extends BaseSearchForm {

  private static final long serialVersionUID = -4384562527048697811L;

  UUID id;

  String categoryCode;

  String categoryName;
}
