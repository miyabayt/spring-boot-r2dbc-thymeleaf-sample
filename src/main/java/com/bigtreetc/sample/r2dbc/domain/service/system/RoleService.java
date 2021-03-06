package com.bigtreetc.sample.r2dbc.domain.service.system;

import static com.bigtreetc.sample.r2dbc.base.util.ValidateUtils.isNotEmpty;
import static org.springframework.data.relational.core.query.Criteria.where;

import com.bigtreetc.sample.r2dbc.base.exception.NoDataFoundException;
import com.bigtreetc.sample.r2dbc.domain.model.system.*;
import com.bigtreetc.sample.r2dbc.domain.repository.system.PermissionRepository;
import com.bigtreetc.sample.r2dbc.domain.repository.system.RolePermissionRepository;
import com.bigtreetc.sample.r2dbc.domain.repository.system.RoleRepository;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** ロールサービス */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Throwable.class)
public class RoleService {

  @NonNull final R2dbcEntityTemplate r2dbcEntityTemplate;

  @NonNull final RoleRepository roleRepository;

  @NonNull final RolePermissionRepository rolePermissionRepository;

  @NonNull final PermissionRepository permissionRepository;

  /**
   * ロールを検索します。
   *
   * @param role
   * @param pageable
   * @return
   */
  @Transactional(readOnly = true) // 読み取りのみの場合は指定する
  public Mono<Page<Role>> findAll(Role role, Pageable pageable) {
    Assert.notNull(role, "role must not be null");

    val criteria = new ArrayList<Criteria>();
    if (isNotEmpty(role.getRoleCode())) {
      criteria.add(where("role_code").is("%%%s%%".formatted(role.getRoleCode())));
    }
    if (isNotEmpty(role.getRoleName())) {
      criteria.add(where("role_name").like("%%%s%%".formatted(role.getRoleName())));
    }

    val query = Query.query(Criteria.from(criteria));
    return r2dbcEntityTemplate
        .select(Role.class)
        .matching(query.with(pageable))
        .all()
        .collectList()
        .zipWith(r2dbcEntityTemplate.count(query, Role.class))
        .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
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
