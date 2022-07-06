package com.bigtreetc.sample.r2dbc.controller.users.users;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseSearchForm;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchUserForm extends BaseSearchForm {

  private static final long serialVersionUID = 4131372368553937515L;

  UUID id;

  String firstName;

  String lastName;

  String email;
}
