package com.bigtreetc.sample.r2dbc.domain.model;

import java.util.Collection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePermissionCriteria extends RolePermission {

  private static final long serialVersionUID = -1;

  Collection<String> roleCodes;
}
