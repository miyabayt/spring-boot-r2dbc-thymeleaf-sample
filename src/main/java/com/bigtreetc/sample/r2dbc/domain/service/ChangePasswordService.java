package com.bigtreetc.sample.r2dbc.domain.service;

import static com.bigtreetc.sample.r2dbc.base.util.ValidateUtils.isEmpty;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.CHANGE_PASSWORD_URL;

import com.bigtreetc.sample.r2dbc.base.domain.helper.SendMailHelper;
import com.bigtreetc.sample.r2dbc.base.exception.NoDataFoundException;
import com.bigtreetc.sample.r2dbc.domain.model.MailTemplate;
import com.bigtreetc.sample.r2dbc.domain.model.Staff;
import com.bigtreetc.sample.r2dbc.domain.repository.MailTemplateRepository;
import com.bigtreetc.sample.r2dbc.domain.repository.StaffRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

/** パスワード変更サービス */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Throwable.class)
public class ChangePasswordService {

  @Value("${spring.mail.properties.mail.from}")
  String fromAddress;

  @Value("${application.siteUrl}")
  String siteUrl;

  @NonNull final StaffRepository staffRepository;

  @NonNull final MailTemplateRepository mailTemplateRepository;

  @NonNull final SendMailHelper sendMailHelper;

  /**
   * パスワードリセットメールを送信します。
   *
   * @param email
   */
  public Mono<Boolean> sendResetPasswordMail(String email) {
    Assert.notNull(fromAddress, "fromAddress must be set.");
    // トークンを発行する
    val token = UUID.randomUUID().toString();
    return staffRepository
        .findByEmail(email)
        .flatMap(
            staff -> {
              staff.setPasswordResetToken(token);
              staff.setTokenExpiresAt(LocalDateTime.now().plusHours(2)); // 2時間後に失効させる
              return staffRepository.save(staff);
            })
        .zipWith(getMailTemplate("passwordReset"))
        .flatMap(
            tuple2 -> {
              // メールを作成する
              val mailTemplate = tuple2.getT2();
              val subject = mailTemplate.getSubject();
              val templateBody = mailTemplate.getTemplateBody();

              val staff = tuple2.getT1();
              Map<String, Object> objects = new HashMap<>();
              objects.put("staff", staff);
              objects.put("url", "%s%s?token=%s".formatted(siteUrl, CHANGE_PASSWORD_URL, token));

              // テンプレートエンジンにかける
              val body = sendMailHelper.getMailBody(templateBody, objects);

              // メールを送信する
              sendMailHelper.sendMail(fromAddress, new String[] {staff.getEmail()}, subject, body);

              return Mono.just(true);
            })
        .switchIfEmpty(Mono.just(false));
  }

  /**
   * トークンの有効性をチェックします。
   *
   * @param token
   * @return
   */
  public Mono<Boolean> isValidPasswordResetToken(String token) {
    if (isEmpty(token)) {
      return Mono.just(false);
    }

    val staff = new Staff();
    staff.setPasswordResetToken(token);
    return staffRepository.exists(Example.of(staff));
  }

  /**
   * パスワードを更新します。
   *
   * @param token
   * @param password
   * @return
   */
  public Mono<Boolean> updatePassword(String token, String password) {
    val staff = new Staff();
    staff.setPasswordResetToken(token);
    return staffRepository
        .findOne(Example.of(staff))
        .flatMap(
            s -> {
              // パスワードをリセットする
              s.setPasswordResetToken(null);
              s.setTokenExpiresAt(null);
              s.setPassword(password);
              return staffRepository.save(s);
            })
        .map(s -> true)
        .switchIfEmpty(Mono.just(false));
  }

  /**
   * メールテンプレートを取得する。
   *
   * @param templateCode
   * @return
   */
  protected Mono<MailTemplate> getMailTemplate(String templateCode) {
    return mailTemplateRepository
        .findByTemplateCode(templateCode)
        .switchIfEmpty(
            Mono.error(
                new NoDataFoundException("templateCode=" + templateCode + " のデータが見つかりません。")));
  }
}
