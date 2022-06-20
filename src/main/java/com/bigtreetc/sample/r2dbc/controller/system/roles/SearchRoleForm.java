package com.bigtreetc.sample.r2dbc.controller.system.roles;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseSearchForm;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchRoleForm extends BaseSearchForm {

  private static final long serialVersionUID = 7979636448439604680L;

  UUID id;

  // ロールコード
  String roleCode;

  // ロール名
  String roleName;
}
