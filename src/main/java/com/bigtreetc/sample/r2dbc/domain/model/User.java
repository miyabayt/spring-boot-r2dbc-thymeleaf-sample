package com.bigtreetc.sample.r2dbc.domain.model;

import com.bigtreetc.sample.r2dbc.base.domain.model.BaseEntityImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("users")
public class User extends BaseEntityImpl implements Persistable<UUID> {

  private static final long serialVersionUID = 4512633005852272922L;

  @Id UUID id;

  // ハッシュ化されたパスワード
  @JsonIgnore String password;

  // 名前
  String firstName;

  // 苗字
  String lastName;

  // メールアドレス
  @Email String email;

  // 電話番号
  @Digits(fraction = 0, integer = 10)
  String tel;

  // 郵便番号
  @NotEmpty String zip;

  // 住所
  @NotEmpty String address;

  // 添付ファイルID
  @JsonIgnore Long uploadFileId;

  // 添付ファイル
  @Transient @JsonIgnore UploadFile uploadFile;

  @Override
  public boolean isNew() {
    return getCreatedAt() == null;
  }
}
