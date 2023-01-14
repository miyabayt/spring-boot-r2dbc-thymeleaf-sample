package com.bigtreetc.sample.r2dbc.security;

import com.bigtreetc.sample.r2dbc.domain.model.Staff;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public class LoginStaff extends org.springframework.security.core.userdetails.User {

  private static final long serialVersionUID = -3501461554747331434L;

  private Staff staff;

  /**
   * コンストラクタ
   *
   * @param staff
   * @param authorities
   */
  public LoginStaff(
      Staff staff,
      String username,
      String password,
      Collection<? extends GrantedAuthority> authorities) {
    super(username, password, authorities);
    this.staff = staff;
  }

  public Staff getStaff() {
    return this.staff;
  }

  public boolean hasRole(String role) {
    return getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
  }
}
