package com.bigtreetc.sample.r2dbc.domain.service;

import com.bigtreetc.sample.r2dbc.base.exception.NoDataFoundException;
import com.bigtreetc.sample.r2dbc.domain.model.Holiday;
import com.bigtreetc.sample.r2dbc.domain.model.HolidayCriteria;
import com.bigtreetc.sample.r2dbc.domain.repository.HolidayRepository;
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

/** 祝日サービス */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Throwable.class)
public class HolidayService {

  @NonNull final HolidayRepository holidayRepository;

  /**
   * 祝日を検索します。
   *
   * @param criteria
   * @param pageable
   * @return
   */
  @Transactional(readOnly = true) // 読み取りのみの場合は指定する
  public Mono<Page<Holiday>> findAll(final HolidayCriteria criteria, final Pageable pageable) {
    Assert.notNull(criteria, "criteria must not be null");
    Assert.notNull(pageable, "pageable must not be null");
    return holidayRepository.findAll(criteria, pageable);
  }

  /**
   * 祝日を取得します。
   *
   * @return
   */
  @Transactional(readOnly = true)
  public Mono<Holiday> findOne(Holiday holiday) {
    Assert.notNull(holiday, "holiday must not be null");
    return holidayRepository.findOne(Example.of(holiday));
  }

  /**
   * 祝日を取得します。
   *
   * @return
   */
  @Transactional(readOnly = true)
  public Mono<Holiday> findById(final UUID id) {
    Assert.notNull(id, "id must not be null");
    return holidayRepository
        .findById(id)
        .switchIfEmpty(Mono.error(new NoDataFoundException("id=" + id + " のデータが見つかりません。")));
  }

  /**
   * 祝日を追加します。
   *
   * @param holiday
   * @return
   */
  public Mono<Holiday> create(final Holiday holiday) {
    Assert.notNull(holiday, "holiday must not be null");
    holiday.setId(UUID.randomUUID());
    return holidayRepository.save(holiday);
  }

  /**
   * 祝日を更新します。
   *
   * @param holiday
   * @return
   */
  public Mono<Holiday> update(final Holiday holiday) {
    Assert.notNull(holiday, "holiday must not be null");
    return holidayRepository.save(holiday);
  }

  /**
   * 祝日を削除します。
   *
   * @return
   */
  public Mono<Void> delete(final UUID id) {
    Assert.notNull(id, "id must not be null");
    return holidayRepository.deleteById(id);
  }
}
