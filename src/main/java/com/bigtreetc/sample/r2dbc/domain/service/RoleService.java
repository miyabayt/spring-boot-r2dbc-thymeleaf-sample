package com.bigtreetc.sample.r2dbc.domain.service;

import static com.bigtreetc.sample.r2dbc.base.domain.sql.DomaUtils.toSelectOptions;

import com.bigtreetc.sample.r2dbc.base.domain.sql.DomaSqlBuilder;
import com.bigtreetc.sample.r2dbc.base.exception.NoDataFoundException;
import com.bigtreetc.sample.r2dbc.domain.model.Role;
import com.bigtreetc.sample.r2dbc.domain.model.RoleCriteria;
import com.bigtreetc.sample.r2dbc.domain.model.RolePermission;
import com.bigtreetc.sample.r2dbc.domain.repository.PermissionRepository;
import com.bigtreetc.sample.r2dbc.domain.repository.RolePermissionRepository;
import com.bigtreetc.sample.r2dbc.domain.repository.RoleRepository;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** ロールサービス */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Throwable.class)
public class RoleService {

  @NonNull final R2dbcEntityTemplate r2dbcEntityTemplate;

  @NonNull final MappingR2dbcConverter converter;

  @NonNull final Dialect dialect;

  @NonNull final RoleRepository roleRepository;

  @NonNull final RolePermissionRepository rolePermissionRepository;

  @NonNull final PermissionRepository permissionRepository;

  /**
   * ロールを検索します。
   *
   * @param criteria
   * @param pageable
   * @return
   */
  @Transactional(readOnly = true) // 読み取りのみの場合は指定する
  public Mono<Page<Role>> findAll(RoleCriteria criteria, Pageable pageable) {
    Assert.notNull(criteria, "role must not be null");

    val selectOptions = toSelectOptions(pageable);
    val sql =
        DomaSqlBuilder.builder()
            .dialect(dialect)
            .sqlFilePath(
                "META-INF/com/bigtreetc/sample/r2dbc/domain/service/RoleService/findAll.sql")
            .addParameter("criteria", RoleCriteria.class, criteria)
            .options(selectOptions)
            .build();

    return r2dbcEntityTemplate
        .getDatabaseClient()
        .sql(sql)
        .map((row, rowMetaData) -> converter.read(Role.class, row, rowMetaData))
        .all()
        .collectList()
        .map(list -> new PageImpl<>(list, pageable, selectOptions.getCount()));
  }

  /**
   * ロールを取得します。
   *
   * @return
   */
  @Transactional(readOnly = true)
  public Mono<Role> findOne(Role role) {
    Assert.notNull(role, "role must not be null");
    return roleRepository
        .findOne(Example.of(role))
        .flatMap(this::getRolePermissions)
        .flatMap(this::getPermissions);
  }

  /**
   * ロールを取得します。
   *
   * @return
   */
  @Transactional(readOnly = true)
  public Mono<Role> findById(final UUID id) {
    Assert.notNull(id, "id must not be null");
    return roleRepository
        .findById(id)
        .flatMap(this::getRolePermissions)
        .flatMap(this::getPermissions)
        .switchIfEmpty(Mono.error(new NoDataFoundException("id=" + id + " のデータが見つかりません。")));
  }

  /**
   * ロールを追加します。
   *
   * @param role
   * @return
   */
  public Mono<Role> create(final Role role) {
    Assert.notNull(role, "role must not be null");
    role.setId(UUID.randomUUID());
    return roleRepository
        .save(role)
        .flatMapMany(r -> Flux.fromIterable(r.getRolePermissions()))
        .collectList()
        .flatMapMany(rolePermissionRepository::saveAll)
        .collectList()
        .thenReturn(role);
  }

  /**
   * ロールを更新します。
   *
   * @param role
   * @return
   */
  public Mono<Role> update(final Role role) {
    Assert.notNull(role, "role must not be null");
    return roleRepository
        .save(role)
        .flatMapMany(r -> Flux.fromIterable(r.getRolePermissions()))
        .collectList()
        .flatMapMany(rolePermissionRepository::saveAll)
        .collectList()
        .thenReturn(role);
  }

  /**
   * ロールを削除します。
   *
   * @return
   */
  public Mono<Void> delete(final UUID id) {
    Assert.notNull(id, "id must not be null");
    return roleRepository
        .findById(id)
        .flatMap(
            role -> {
              val roleCode = role.getRoleCode();
              return roleRepository
                  .deleteByRoleCode(roleCode)
                  .then(rolePermissionRepository.deleteByRoleCode(roleCode));
            });
  }

  private Mono<Role> getRolePermissions(Role r) {
    return rolePermissionRepository
        .findByRoleCode(r.getRoleCode())
        .collectList()
        .map(
            rp -> {
              r.getRolePermissions().addAll(rp);
              return r;
            });
  }

  private Mono<Role> getPermissions(Role role) {
    val permissionCodes =
        role.getRolePermissions().stream()
            .map(RolePermission::getPermissionCode)
            .collect(Collectors.toList());
    return permissionRepository
        .findByPermissionCodeIn(permissionCodes)
        .collectList()
        .flatMap(
            p -> {
              role.getPermissions().addAll(p);
              return Mono.just(role);
            });
  }
}
