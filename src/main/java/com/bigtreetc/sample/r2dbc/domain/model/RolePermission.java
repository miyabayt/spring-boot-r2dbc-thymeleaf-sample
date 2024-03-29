package com.bigtreetc.sample.r2dbc.domain.model;

import com.bigtreetc.sample.r2dbc.base.domain.model.BaseEntityImpl;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("role_permissions")
public class RolePermission extends BaseEntityImpl implements Persistable<UUID> {

  private static final long serialVersionUID = 4915898548766398327L;

  @Id UUID id;

  // ロールコード
  String roleCode;

  // 権限コード
  String permissionCode;

  // 有効
  Boolean isEnabled;

  @Override
  public boolean isNew() {
    return getCreatedAt() == null;
  }
}
