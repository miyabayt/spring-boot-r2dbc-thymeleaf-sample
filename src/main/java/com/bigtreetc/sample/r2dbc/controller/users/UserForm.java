package com.bigtreetc.sample.r2dbc.controller.users;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseForm;
import com.bigtreetc.sample.r2dbc.base.web.validator.annotation.ContentType;
import java.util.UUID;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;

@Setter
@Getter
public class UserForm extends BaseForm {

  private static final long serialVersionUID = -6807767990335584883L;

  UUID id;

  // 名前
  @NotEmpty String firstName;

  // 苗字
  @NotEmpty String lastName;

  @NotEmpty String password;

  @NotEmpty String passwordConfirm;

  // メールアドレス
  @NotEmpty @Email String email;

  // 電話番号
  @Digits(fraction = 0, integer = 10)
  String tel;

  // 郵便番号
  String zip;

  // 住所
  String address;

  // 添付ファイル
  @ContentType(
      allowed = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE})
  transient FilePart userImage; // serializableではないのでtransientにする
}
