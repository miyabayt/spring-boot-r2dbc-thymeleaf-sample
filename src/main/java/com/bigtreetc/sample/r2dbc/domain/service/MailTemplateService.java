package com.bigtreetc.sample.r2dbc.domain.service;

import com.bigtreetc.sample.r2dbc.base.exception.NoDataFoundException;
import com.bigtreetc.sample.r2dbc.domain.model.MailTemplate;
import com.bigtreetc.sample.r2dbc.domain.model.MailTemplateCriteria;
import com.bigtreetc.sample.r2dbc.domain.repository.MailTemplateRepository;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

/** メールテンプレートサービス */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Throwable.class)
public class MailTemplateService {

  @NonNull final MailTemplateRepository mailTemplateRepository;

  /**
   * メールテンプレートを検索します。
   *
   * @param criteria
   * @param pageable
   * @return
   */
  @Transactional(readOnly = true) // 読み取りのみの場合は指定する
  public Mono<Page<MailTemplate>> findAll(
      final MailTemplateCriteria criteria, final Pageable pageable) {
    Assert.notNull(criteria, "criteria must not be null");
    Assert.notNull(pageable, "pageable must not be null");
    return mailTemplateRepository.findAll(criteria, pageable);
  }

  /**
   * メールテンプレートを取得します。
   *
   * @param mailTemplate
   * @return
   */
  @Transactional(readOnly = true)
  public Mono<MailTemplate> findOne(MailTemplate mailTemplate) {
    Assert.notNull(mailTemplate, "mailTemplate must not be null");
    return mailTemplateRepository.findOne(Example.of(mailTemplate));
  }

  /**
   * メールテンプレートを取得します。
   *
   * @param id
   * @return
   */
  @Transactional(readOnly = true)
  public Mono<MailTemplate> findById(final UUID id) {
    Assert.notNull(id, "id must not be null");
    return mailTemplateRepository
        .findById(id)
        .switchIfEmpty(Mono.error(new NoDataFoundException("id=" + id + " のデータが見つかりません。")));
  }

  /**
   * メールテンプレートを追加します。
   *
   * @param mailTemplate
   * @return
   */
  public Mono<MailTemplate> create(final MailTemplate mailTemplate) {
    Assert.notNull(mailTemplate, "mailTemplate must not be null");
    mailTemplate.setId(UUID.randomUUID());
    return mailTemplateRepository.save(mailTemplate);
  }

  /**
   * メールテンプレートを更新します。
   *
   * @param mailTemplate
   * @return
   */
  public Mono<MailTemplate> update(final MailTemplate mailTemplate) {
    Assert.notNull(mailTemplate, "mailTemplate must not be null");
    return mailTemplateRepository.save(mailTemplate);
  }

  /**
   * メールテンプレートを削除します。
   *
   * @return
   */
  public Mono<Void> delete(final UUID id) {
    Assert.notNull(id, "id must not be null");
    return mailTemplateRepository.deleteById(id);
  }
}
