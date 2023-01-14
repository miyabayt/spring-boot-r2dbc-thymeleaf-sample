package com.bigtreetc.sample.r2dbc.controller.roles;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseForm;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RoleForm extends BaseForm {

  private static final long serialVersionUID = 7555305356779221873L;

  UUID id;

  // ロールコード
  @NotEmpty String roleCode;

  // ロール名
  @NotEmpty String roleName;

  // 権限
  Map<String, Boolean> permissions = new HashMap<>();
}
