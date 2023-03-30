package com.bigtreetc.sample.r2dbc.controller.staffs;

import java.io.Serializable;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchStaffForm implements Serializable {

  private static final long serialVersionUID = 4131372368553937515L;

  UUID id;

  String firstName;

  String lastName;

  String fullName;

  String email;
}
