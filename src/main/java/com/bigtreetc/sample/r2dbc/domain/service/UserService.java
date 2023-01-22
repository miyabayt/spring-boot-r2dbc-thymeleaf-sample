package com.bigtreetc.sample.r2dbc.domain.service;

import com.bigtreetc.sample.r2dbc.base.exception.NoDataFoundException;
import com.bigtreetc.sample.r2dbc.domain.model.User;
import com.bigtreetc.sample.r2dbc.domain.model.UserCriteria;
import com.bigtreetc.sample.r2dbc.domain.repository.UserRepository;
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

/** ユーザサービス */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Throwable.class)
public class UserService {

  @NonNull final UserRepository userRepository;

  /**
   * ユーザを検索します。
   *
   * @param criteria
   * @param pageable
   * @return
   */
  @Transactional(readOnly = true) // 読み取りのみの場合は指定する
  public Mono<Page<User>> findAll(final UserCriteria criteria, final Pageable pageable) {
    Assert.notNull(criteria, "criteria must not be null");
    Assert.notNull(pageable, "pageable must not be null");
    return userRepository.findAll(criteria, pageable);
  }

  /**
   * ユーザを取得します。
   *
   * @return
   */
  @Transactional(readOnly = true)
  public Mono<User> findOne(User user) {
    Assert.notNull(user, "criteria must not be null");
    return userRepository.findOne(Example.of(user));
  }

  /**
   * ユーザを取得します。
   *
   * @return
   */
  @Transactional(readOnly = true)
  public Mono<User> findById(final UUID id) {
    Assert.notNull(id, "id must not be null");
    return userRepository
        .findById(id)
        .switchIfEmpty(Mono.error(new NoDataFoundException("id=" + id + " のデータが見つかりません。")));
  }

  /**
   * ユーザを追加します。
   *
   * @param user
   * @return
   */
  public Mono<User> create(final User user) {
    Assert.notNull(user, "user must not be null");
    user.setId(UUID.randomUUID());
    return userRepository.save(user);
  }

  /**
   * ユーザを更新します。
   *
   * @param user
   * @return
   */
  public Mono<User> update(final User user) {
    Assert.notNull(user, "user must not be null");
    return userRepository.save(user);
  }

  /**
   * ユーザを削除します。
   *
   * @return
   */
  public Mono<Void> delete(final UUID id) {
    Assert.notNull(id, "id must not be null");
    return userRepository.deleteById(id);
  }
}
