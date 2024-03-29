package com.bigtreetc.sample.r2dbc.domain.model;

import com.bigtreetc.sample.r2dbc.base.domain.model.BaseEntityImpl;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("mail_templates")
public class MailTemplate extends BaseEntityImpl implements Persistable<UUID> {

  private static final long serialVersionUID = -2997823123579780864L;

  @Id UUID id;

  // カテゴリコード
  String categoryCode;

  // メールテンプレートコード
  String templateCode;

  // メールタイトル
  String subject;

  // メール本文
  String templateBody;

  @Override
  public boolean isNew() {
    return getCreatedAt() == null;
  }
}
