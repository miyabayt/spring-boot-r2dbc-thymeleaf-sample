package com.bigtreetc.sample.r2dbc.domain.model.system;

import com.bigtreetc.sample.r2dbc.base.domain.model.BaseEntityImpl;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("holidays")
public class Holiday extends BaseEntityImpl implements Persistable<UUID> {

  private static final long serialVersionUID = 2399051382620886703L;

  // 祝日ID
  @Id UUID id;

  // 名称
  String holidayName;

  // 日付
  LocalDate holidayDate;

  @Override
  public boolean isNew() {
    return getCreatedAt() == null;
  }
}
