package com.bigtreetc.sample.r2dbc.domain.repository;

import static com.bigtreetc.sample.r2dbc.base.domain.sql.DomaUtils.toSelectOptions;

import com.bigtreetc.sample.r2dbc.base.domain.sql.DomaDatabaseClient;
import com.bigtreetc.sample.r2dbc.base.domain.sql.DomaSqlBuilder;
import com.bigtreetc.sample.r2dbc.domain.model.Code;
import com.bigtreetc.sample.r2dbc.domain.model.CodeCriteria;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class CodeQueryRepositoryImpl implements CodeQueryRepository {

  @NonNull final DomaDatabaseClient databaseClient;

  /**
   * 指定された条件でコードを検索します。
   *
   * @param criteria
   * @param pageable
   * @return
   */
  public Mono<Page<Code>> findAll(final CodeCriteria criteria, final Pageable pageable) {
    val selectOptions = toSelectOptions(pageable);
    val sqlBuilder =
        DomaSqlBuilder.builder()
            .sqlFilePath(
                "META-INF/com/bigtreetc/sample/r2dbc/domain/repository/CodeQueryRepository/findAll.sql")
            .addParameter("criteria", CodeCriteria.class, criteria)
            .options(selectOptions);

    return databaseClient
        .all(sqlBuilder, Code.class)
        .collectList()
        .map(list -> new PageImpl<>(list, pageable, selectOptions.getCount()));
  }
}
