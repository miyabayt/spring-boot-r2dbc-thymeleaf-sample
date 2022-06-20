package com.bigtreetc.sample.r2dbc.base.web.controller.html;

import java.io.Serializable;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class BaseForm implements Serializable {

  private static final long serialVersionUID = 8152154411566544341L;

  // 改定番号
  Integer version;

  /**
   * 既存レコードがないデータであるか
   *
   * @return
   */
  public boolean isNew() {
    return getId() == null;
  }

  /**
   * IdカラムのGetter
   *
   * @return
   */
  public abstract UUID getId();
}
