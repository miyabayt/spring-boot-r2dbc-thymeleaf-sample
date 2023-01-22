package com.bigtreetc.sample.r2dbc.domain.service;

import com.bigtreetc.sample.r2dbc.base.exception.NoDataFoundException;
import com.bigtreetc.sample.r2dbc.domain.model.Permission;
import com.bigtreetc.sample.r2dbc.domain.model.PermissionCriteria;
import com.bigtreetc.sample.r2dbc.domain.repository.PermissionRepository;
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

/** 権限サービス */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Throwable.class)
public class PermissionService {

  @NonNull final PermissionRepository permissionRepository;

  /**
   * 権限を検索します。
   *
   * @param criteria
   * @param pageable
   * @return
   */
  @Transactional(readOnly = true) // 読み取りのみの場合は指定する
  public Mono<Page<Permission>> findAll(
      final PermissionCriteria criteria, final Pageable pageable) {
    Assert.notNull(criteria, "criteria must not be null");
    Assert.notNull(pageable, "pageable must not be null");
    return permissionRepository.findAll(criteria, pageable);
  }

  /**
   * 権限を取得します。
   *
   * @return
   */
  @Transactional(readOnly = true)
  public Mono<Permission> findOne(Permission permission) {
    Assert.notNull(permission, "permission must not be null");
    return permissionRepository.findOne(Example.of(permission));
  }

  /**
   * 権限を取得します。
   *
   * @return
   */
  @Transactional(readOnly = true)
  public Mono<Permission> findById(final UUID id) {
    Assert.notNull(id, "id must not be null");
    return permissionRepository
        .findById(id)
        .switchIfEmpty(Mono.error(new NoDataFoundException("id=" + id + " のデータが見つかりません。")));
  }

  /**
   * 権限を追加します。
   *
   * @param permission
   * @return
   */
  public Mono<Permission> create(final Permission permission) {
    Assert.notNull(permission, "permission must not be null");
    permission.setId(UUID.randomUUID());
    return permissionRepository.save(permission);
  }

  /**
   * 権限を更新します。
   *
   * @param permission
   * @return
   */
  public Mono<Permission> update(final Permission permission) {
    Assert.notNull(permission, "permission must not be null");
    return permissionRepository.save(permission);
  }

  /**
   * 権限を削除します。
   *
   * @return
   */
  public Mono<Void> delete(final UUID id) {
    Assert.notNull(id, "id must not be null");
    return permissionRepository.deleteById(id);
  }
}
