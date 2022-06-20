package com.bigtreetc.sample.r2dbc.controller.system.mailtemplates;

import static com.bigtreetc.sample.r2dbc.base.util.MessageUtils.getMessage;
import static com.bigtreetc.sample.r2dbc.base.util.TypeUtils.toListType;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.MESSAGE_DELETED;

import com.bigtreetc.sample.r2dbc.base.util.CsvUtils;
import com.bigtreetc.sample.r2dbc.base.web.controller.html.AbstractHtmlController;
import com.bigtreetc.sample.r2dbc.domain.model.system.MailTemplate;
import com.bigtreetc.sample.r2dbc.domain.service.system.MailTemplateService;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

/** メールテンプレート管理 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/system/mailTemplates")
@SessionAttributes(types = {SearchMailTemplateForm.class, MailTemplateForm.class})
@Slf4j
public class MailTemplateController extends AbstractHtmlController {

  @NonNull final MailTemplateFormValidator mailTemplateFormValidator;

  @NonNull final MailTemplateService mailTemplateService;

  @ModelAttribute("mailTemplateForm")
  public MailTemplateForm mailTemplateForm() {
    return new MailTemplateForm();
  }

  @ModelAttribute("searchMailTemplateForm")
  public SearchMailTemplateForm searchMailTemplateForm() {
    return new SearchMailTemplateForm();
  }

  @InitBinder("mailTemplateForm")
  public void validatorBinder(WebDataBinder binder) {
    binder.addValidators(mailTemplateFormValidator);
  }

  /**
   * 登録画面 初期表示
   *
   * @param form
   * @param model
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('mailTemplate:save')")
  @GetMapping("/new")
  public Mono<String> newMailTemplate(
      @ModelAttribute("mailTemplateForm") MailTemplateForm form, Model model, WebSession session) {
    if (!form.isNew()) {
      // SessionAttributeに残っている場合は再生成する
      model.addAttribute("mailTemplateForm", new MailTemplateForm());
    }

    return setBindingResultToAttribute(model, session)
        .thenReturn("modules/system/mailTemplates/new");
  }

  /**
   * 登録処理
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('mailTemplate:save')")
  @PostMapping("/new")
  public Mono<Rendering> newMailTemplate(
      @Validated @ModelAttribute("mailTemplateForm") MailTemplateForm form,
      BindingResult br,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/system/mailTemplates/new");
    }

    // 入力値からDTOを作成する
    val inputMailTemplate = modelMapper.map(form, MailTemplate.class);

    return mailTemplateService
        .create(inputMailTemplate)
        .flatMap(mt -> redirectTo("/system/mailTemplates/show/" + mt.getId()));
  }

  /**
   * 一覧画面 初期表示
   *
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('mailTemplate:read')")
  @GetMapping("/find")
  public Mono<String> findMailTemplate(
      @ModelAttribute SearchMailTemplateForm form, Pageable pageable, Model model) {
    // 入力値を詰め替える
    val criteria = modelMapper.map(form, MailTemplate.class);
    return mailTemplateService
        .findAll(criteria, pageable)
        .doOnNext(pages -> model.addAttribute("pages", pages))
        .thenReturn("modules/system/mailTemplates/find");
  }

  /**
   * 検索結果
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('mailTemplate:read')")
  @PostMapping("/find")
  public Mono<Rendering> findMailTemplate(
      @Validated @ModelAttribute("searchMailTemplateForm") SearchMailTemplateForm form,
      BindingResult br,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
    }

    return redirectTo("/system/mailTemplates/find");
  }

  /**
   * 詳細画面
   *
   * @param mailTemplateId
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('mailTemplate:read')")
  @GetMapping("/show/{mailTemplateId}")
  public Mono<String> showMailTemplate(@PathVariable UUID mailTemplateId, Model model) {
    return mailTemplateService
        .findById(mailTemplateId)
        .doOnNext(mailTemplate -> model.addAttribute("mailTemplate", mailTemplate))
        .thenReturn("modules/system/mailTemplates/show");
  }

  /**
   * 編集画面 初期表示
   *
   * @param mailTemplateId
   * @param form
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('mailTemplate:save')")
  @GetMapping("/edit/{mailTemplateId}")
  public Mono<String> editMailTemplate(
      @PathVariable UUID mailTemplateId,
      @ModelAttribute("mailTemplateForm") MailTemplateForm form,
      Model model,
      WebSession session) {
    return mailTemplateService
        .findById(mailTemplateId)
        .flatMap(
            mailTemplate -> {
              // セッションから取得できる場合は、読み込み直さない
              if (!hasErrors(session)) {
                // 取得したDtoをFromに詰め替える
                modelMapper.map(mailTemplate, form);
              }
              return setBindingResultToAttribute(model, session)
                  .thenReturn("modules/system/mailTemplates/new");
            });
  }

  /**
   * 編集画面 更新処理
   *
   * @param form
   * @param br
   * @param mailTemplateId
   * @param sessionStatus
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('mailTemplate:save')")
  @PostMapping("/edit/{mailTemplateId}")
  public Mono<Rendering> editMailTemplate(
      @PathVariable UUID mailTemplateId,
      @Validated @ModelAttribute("mailTemplateForm") MailTemplateForm form,
      BindingResult br,
      SessionStatus sessionStatus,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/system/mailTemplates/edit/" + mailTemplateId);
    }

    return mailTemplateService
        .findById(mailTemplateId)
        .map(
            mailTemplate -> {
              modelMapper.map(form, mailTemplate);
              return mailTemplate;
            })
        .flatMap(mailTemplateService::update)
        .flatMap(
            mailTemplate -> {
              // セッションのstaffFormをクリアする
              sessionStatus.setComplete();
              return redirectTo("/system/mailTemplates/show/" + mailTemplate.getId());
            });
  }

  /**
   * 削除処理
   *
   * @param mailTemplateId
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('mailTemplate:save')")
  @PostMapping("/remove/{mailTemplateId}")
  public Mono<Rendering> removeMailTemplate(@PathVariable UUID mailTemplateId, WebSession session) {
    session.getAttributes().put(GLOBAL_MESSAGE, getMessage(MESSAGE_DELETED));
    return mailTemplateService
        .delete(mailTemplateId)
        .then(redirectTo("/system/mailTemplates/find"));
  }

  /**
   * CSVダウンロード
   *
   * @param filename
   * @return
   */
  @PreAuthorize("hasAuthority('mailTemplate:read')")
  @GetMapping("/download/{filename:.+\\.csv}")
  public Mono<ResponseEntity<Resource>> downloadCsv(
      @PathVariable String filename, ServerHttpResponse response) {
    return mailTemplateService
        .findAll(new MailTemplate(), Pageable.unpaged())
        .map(
            pages -> {
              val csvList = modelMapper.map(pages.getContent(), toListType(MailTemplateCsv.class));
              val dataBuffer = response.bufferFactory().allocateBuffer();
              CsvUtils.writeCsv(MailTemplateCsv.class, csvList, dataBuffer);
              return new InputStreamResource(dataBuffer.asInputStream(true));
            })
        .map(resource -> toResponseEntity(resource, filename, true));
  }
}
