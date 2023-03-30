package com.bigtreetc.sample.r2dbc.controller.staffs;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseForm;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StaffForm extends BaseForm {

  private static final long serialVersionUID = -6807767990335584883L;

  UUID id;

  // 名
  @NotEmpty String firstName;

  // 性
  @NotEmpty String lastName;

  // パスワード
  @NotEmpty String password;

  // パスワード確認用
  @NotEmpty String passwordConfirm;

  // メールアドレス
  @NotEmpty @Email String email;

  // 電話番号
  @Digits(fraction = 0, integer = 10)
  String tel;
}
