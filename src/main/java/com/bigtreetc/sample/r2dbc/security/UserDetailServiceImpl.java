package com.bigtreetc.sample.r2dbc.security;

import static java.util.stream.Collectors.toList;

import com.bigtreetc.sample.r2dbc.domain.model.system.RolePermission;
import com.bigtreetc.sample.r2dbc.domain.model.system.StaffRole;
import com.bigtreetc.sample.r2dbc.domain.repository.system.RolePermissionRepository;
import com.bigtreetc.sample.r2dbc.domain.repository.system.StaffRepository;
import com.bigtreetc.sample.r2dbc.domain.repository.system.StaffRoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
@Slf4j
public class UserDetailServiceImpl implements ReactiveUserDetailsService {

  @NonNull final StaffRepository staffRepository;

  @NonNull final StaffRoleRepository staffRoleRepository;

  @NonNull final RolePermissionRepository rolePermissionRepository;

  @Override
  public Mono<UserDetails> findByUsername(String username) {
    return staffRepository
        .findByEmail(username)
        .switchIfEmpty(
            Mono.error(
                new UsernameNotFoundException("no account found. [username=" + username + "]")))
        .flatMap(
            staff ->
                Mono.just(staff)
                    .zipWith(
                        staffRoleRepository
                            .findByStaffId(staff.getId())
                            .collectList()
                            .map(this::mapToRoleCodes)
                            .flatMap(this::getRolePermissions)
                            .flatMap(
                                rolePermissions -> {
                                  val roleCodes =
                                      rolePermissions.stream()
                                          .map(RolePermission::getRoleCode)
                                          .distinct()
                                          .toList();
                                  val permissionCodes =
                                      rolePermissions.stream()
                                          .map(RolePermission::getPermissionCode)
                                          .toList();
                                  return Mono.just(roleCodes).zipWith(Mono.just(permissionCodes));
                                })
                            .map(
                                tuple2 -> {
                                  val roleCodes = tuple2.getT1();
                                  val authorities = new HashSet<String>();
                                  for (val roleCode : roleCodes) {
                                    authorities.add("ROLE_%s".formatted(roleCode));
                                  }
                                  authorities.addAll(tuple2.getT2());
                                  return AuthorityUtils.createAuthorityList(
                                      authorities.toArray(new String[0]));
                                })))
        .map(
            tuple2 -> {
              val id = Objects.requireNonNull(tuple2.getT1().getId());
              val password = tuple2.getT1().getPassword();
              val authorityList = tuple2.getT2();
              return User.withUsername(id.toString())
                  .password(password)
                  .authorities(authorityList)
                  .build();
            });
  }

  private Mono<List<RolePermission>> getRolePermissions(List<String> roleCodes) {
    return rolePermissionRepository.findByRoleCodeIn(roleCodes).collectList();
  }

  private List<String> mapToRoleCodes(List<StaffRole> staffRoles) {
    return staffRoles.stream().map(StaffRole::getRoleCode).distinct().collect(toList());
  }
}
