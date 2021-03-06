package com.bigtreetc.sample.r2dbc.domain.model.system;

import com.bigtreetc.sample.r2dbc.base.domain.model.BaseEntityImpl;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("staffs")
public class Staff extends BaseEntityImpl implements Persistable<UUID> {

  private static final long serialVersionUID = -3762941082070995608L;

  @Id UUID id;

  String password;

  // 名前
  String firstName;

  // 苗字
  String lastName;

  // メールアドレス
  @Email String email;

  // 電話番号
  @Digits(fraction = 0, integer = 10)
  String tel;

  // パスワードリセットトークン
  String passwordResetToken;

  // トークン失効日
  LocalDateTime tokenExpiresAt;

  @Override
  public boolean isNew() {
    return getCreatedAt() == null;
  }
}
