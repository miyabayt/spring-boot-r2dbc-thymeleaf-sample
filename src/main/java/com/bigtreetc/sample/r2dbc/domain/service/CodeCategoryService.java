package com.bigtreetc.sample.r2dbc.domain.service;

import com.bigtreetc.sample.r2dbc.base.exception.NoDataFoundException;
import com.bigtreetc.sample.r2dbc.domain.model.CodeCategory;
import com.bigtreetc.sample.r2dbc.domain.model.CodeCategoryCriteria;
import com.bigtreetc.sample.r2dbc.domain.repository.CodeCategoryRepository;
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

/** 分類サービス */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Throwable.class)
public class CodeCategoryService {

  @NonNull final CodeCategoryRepository codeCategoryRepository;

  /**
   * 分類を検索します。
   *
   * @param criteria
   * @param pageable
   * @return
   */
  @Transactional(readOnly = true) // 読み取りのみの場合は指定する
  public Mono<Page<CodeCategory>> findAll(
      final CodeCategoryCriteria criteria, final Pageable pageable) {
    Assert.notNull(criteria, "criteria must not be null");
    Assert.notNull(pageable, "pageable must not be null");
    return codeCategoryRepository.findAll(criteria, pageable);
  }

  /**
   * 分類を取得します。
   *
   * @return
   */
  @Transactional(readOnly = true)
  public Mono<CodeCategory> findOne(final CodeCategory codeCategory) {
    Assert.notNull(codeCategory, "criteria must not be null");
    return codeCategoryRepository.findOne(Example.of(codeCategory));
  }

  /**
   * 分類を取得します。
   *
   * @param id
   * @return
   */
  @Transactional(readOnly = true)
  public Mono<CodeCategory> findById(final UUID id) {
    Assert.notNull(id, "id must not be null");
    return codeCategoryRepository
        .findById(id)
        .switchIfEmpty(Mono.error(new NoDataFoundException("id=" + id + " のデータが見つかりません。")));
  }

  /**
   * 分類を追加します。
   *
   * @param codeCategory
   * @return
   */
  public Mono<CodeCategory> create(final CodeCategory codeCategory) {
    Assert.notNull(codeCategory, "codeCategory must not be null");
    codeCategory.setId(UUID.randomUUID());
    return codeCategoryRepository.save(codeCategory);
  }

  /**
   * 分類を更新します。
   *
   * @param codeCategory
   * @return
   */
  public Mono<CodeCategory> update(final CodeCategory codeCategory) {
    Assert.notNull(codeCategory, "codeCategory must not be null");
    return codeCategoryRepository.save(codeCategory);
  }

  /**
   * 分類を削除します。
   *
   * @return
   */
  public Mono<Void> delete(final UUID id) {
    Assert.notNull(id, "id must not be null");
    return codeCategoryRepository.deleteById(id);
  }
}
