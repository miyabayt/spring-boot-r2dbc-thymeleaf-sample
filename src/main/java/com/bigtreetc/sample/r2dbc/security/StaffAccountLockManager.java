package com.bigtreetc.sample.r2dbc.security;

import com.bigtreetc.sample.r2dbc.base.web.security.AccountLockManager;
import com.bigtreetc.sample.r2dbc.domain.repository.StaffRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class StaffAccountLockManager implements AccountLockManager {

  public static final String LOGIN_ATTEMPT_USER = "n/a";

  @NonNull final StaffRepository staffRepository;

  @NonNull final Integer maxAttempts;

  @Override
  public Mono<Authentication> saveLoginAttemptSucceeded(Authentication authentication) {
    val loginUser = (LoginStaff) authentication.getPrincipal();
    val username = loginUser.getUsername();
    return staffRepository
        .findById(UUID.fromString(username))
        .flatMap(
            staff -> {
              staff.setLoginAttemptCount(0);
              staff.setAccountLockedAt(null);
              return staffRepository.save(staff).thenReturn(authentication);
            });
  }

  @Override
  public Mono<Authentication> saveLoginAttemptFailed(Authentication authentication) {
    val username = (String) authentication.getPrincipal();
    return staffRepository
        .findByEmail(username)
        .flatMap(
            staff -> {
              val loginAttemptCount = staff.getLoginAttemptCount();
              staff.setLoginAttemptCount(loginAttemptCount + 1);
              staff.setUpdatedBy(LOGIN_ATTEMPT_USER);

              if (maxAttempts <= staff.getLoginAttemptCount()) {
                // 最大の試行回数に達した場合
                staff.setAccountLockedAt(LocalDateTime.now());
              }

              return staffRepository.save(staff).thenReturn(authentication);
            });
  }
}
