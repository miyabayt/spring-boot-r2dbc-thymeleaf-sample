package com.bigtreetc.sample.r2dbc.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCriteria extends User {

  private static final long serialVersionUID = -1;

  // 住所がNULLのデータに絞り込む
  Boolean onlyNullAddress;
}
