package com.bigtreetc.sample.r2dbc.controller.changepassword;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetPasswordForm implements Serializable {

  private static final long serialVersionUID = -2603586366253013510L;

  @NotEmpty String email;

  String token;
}
