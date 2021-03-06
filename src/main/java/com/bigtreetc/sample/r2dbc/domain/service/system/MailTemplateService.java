package com.bigtreetc.sample.r2dbc.domain.service.system;

import static com.bigtreetc.sample.r2dbc.base.util.ValidateUtils.isNotEmpty;
import static org.springframework.data.relational.core.query.Criteria.where;

import com.bigtreetc.sample.r2dbc.base.exception.NoDataFoundException;
import com.bigtreetc.sample.r2dbc.domain.model.system.MailTemplate;
import com.bigtreetc.sample.r2dbc.domain.repository.system.MailTemplateRepository;
import java.util.ArrayList;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

/** メールテンプレートサービス */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Throwable.class)
public class MailTemplateService {

  @NonNull final R2dbcEntityTemplate r2dbcEntityTemplate;

  @NonNull final MailTemplateRepository mailTemplateRepository;

  /**
   * メールテンプレートを検索します。
   *
   * @return
   */
  @Transactional(readOnly = true) // 読み取りのみの場合は指定する
  public Mono<Page<MailTemplate>> findAll(MailTemplate mailTemplate, Pageable pageable) {
    Assert.notNull(mailTemplate, "mailTemplate must not be null");

    val criteria = new ArrayList<Criteria>();
    if (isNotEmpty(mailTemplate.getCategoryCode())) {
      criteria.add(where("category_code").is(mailTemplate.getCategoryCode()));
    }
    if (isNotEmpty(mailTemplate.getTemplateCode())) {
      criteria.add(where("template_code").is(mailTemplate.getTemplateCode()));
    }
    if (isNotEmpty(mailTemplate.getSubject())) {
      criteria.add(where("subject").like("%%%s%%".formatted(mailTemplate.getSubject())));
    }

    val query = Query.query(Criteria.from(criteria));
    return r2dbcEntityTemplate
        .select(MailTemplate.class)
        .matching(query.with(pageable))
        .all()
        .collectList()
        .zipWith(r2dbcEntityTemplate.count(query, MailTemplate.class))
        .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
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
