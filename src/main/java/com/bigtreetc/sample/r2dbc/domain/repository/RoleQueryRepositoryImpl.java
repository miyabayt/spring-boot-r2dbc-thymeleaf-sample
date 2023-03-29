package com.bigtreetc.sample.r2dbc.domain.repository;

import static com.bigtreetc.sample.r2dbc.base.domain.sql.DomaUtils.toSelectOptions;

import com.bigtreetc.sample.r2dbc.base.domain.sql.DomaDatabaseClient;
import com.bigtreetc.sample.r2dbc.base.domain.sql.DomaSqlBuilder;
import com.bigtreetc.sample.r2dbc.domain.model.Role;
import com.bigtreetc.sample.r2dbc.domain.model.RoleCriteria;
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
public class RoleQueryRepositoryImpl implements RoleQueryRepository {

  @NonNull final DomaDatabaseClient databaseClient;

  /**
   * 指定された条件で担当者マスタを検索します。
   *
   * @param criteria
   * @param pageable
   * @return
   */
  public Mono<Page<Role>> findAll(final RoleCriteria criteria, final Pageable pageable) {
    val selectOptions = toSelectOptions(pageable);
    val sqlBuilder =
        DomaSqlBuilder.builder()
            .sqlFilePath(
                "META-INF/com/bigtreetc/sample/r2dbc/domain/repository/RoleQueryRepository/findAll.sql")
            .addParameter("criteria", RoleCriteria.class, criteria)
            .options(selectOptions);

    return databaseClient
        .all(sqlBuilder, Role.class)
        .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
  }
}
