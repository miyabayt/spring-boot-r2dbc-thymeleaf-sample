package com.bigtreetc.sample.r2dbc.base.web.security;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.security")
public class ApplicationSecurityConfigurationProperty {

  private List<String> permittedUrls = new ArrayList<>();

  private JwtConfig jwt;

  @Setter
  @Getter
  public static class JwtConfig {
    private AccessTokenConfig accessToken;
    private RefreshTokenConfig refreshToken;
  }

  @Setter
  @Getter
  public static class AccessTokenConfig {
    private String signingKey;
    private int expiredIn = 60;
  }

  @Setter
  @Getter
  public static class RefreshTokenConfig {
    private String salt;
  }
}
