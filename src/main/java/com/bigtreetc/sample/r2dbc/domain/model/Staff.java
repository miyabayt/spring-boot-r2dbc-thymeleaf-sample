package com.bigtreetc.sample.r2dbc.domain.model;

import com.bigtreetc.sample.r2dbc.base.domain.model.BaseEntityImpl;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("staffs")
public class Staff extends BaseEntityImpl implements Persistable<UUID> {

  private static final long serialVersionUID = -3762941082070995608L;

  @Id UUID id;

  String password;

  // 名
  String firstName;

  // 姓
  String lastName;

  // 氏名
  @Transient String fullName;

  // メールアドレス
  @Email String email;

  // 電話番号
  @Digits(fraction = 0, integer = 10)
  String tel;

  // パスワードリセットトークン
  String passwordResetToken;

  // トークン失効日
  LocalDateTime tokenExpiresAt;

  // ログイン試行回数
  Integer loginAttemptCount;

  // アカウントロック日時
  LocalDateTime accountLockedAt;

  @Override
  public boolean isNew() {
    return getCreatedAt() == null;
  }
}
