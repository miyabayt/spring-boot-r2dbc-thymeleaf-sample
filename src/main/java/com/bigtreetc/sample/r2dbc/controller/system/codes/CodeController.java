package com.bigtreetc.sample.r2dbc.controller.system.codes;

import static com.bigtreetc.sample.r2dbc.base.util.MessageUtils.getMessage;
import static com.bigtreetc.sample.r2dbc.base.util.TypeUtils.toListType;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.MESSAGE_DELETED;

import com.bigtreetc.sample.r2dbc.base.util.CsvUtils;
import com.bigtreetc.sample.r2dbc.base.web.controller.html.AbstractHtmlController;
import com.bigtreetc.sample.r2dbc.domain.model.system.Code;
import com.bigtreetc.sample.r2dbc.domain.model.system.CodeCategory;
import com.bigtreetc.sample.r2dbc.domain.service.system.CodeCategoryService;
import com.bigtreetc.sample.r2dbc.domain.service.system.CodeService;
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

/** コード管理 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/system/codes")
@SessionAttributes(types = {SearchCodeForm.class, CodeForm.class})
@Slf4j
public class CodeController extends AbstractHtmlController {

  @NonNull final CodeFormValidator codeFormValidator;

  @NonNull final CodeCategoryService codeCategoryService;

  @NonNull final CodeService codeService;

  @ModelAttribute("codeForm")
  public CodeForm codeForm() {
    return new CodeForm();
  }

  @ModelAttribute("searchCodeForm")
  public SearchCodeForm searchCodeForm() {
    return new SearchCodeForm();
  }

  @InitBinder("codeForm")
  public void validatorBinder(WebDataBinder binder) {
    binder.addValidators(codeFormValidator);
  }

  /**
   * 登録画面 初期表示
   *
   * @param form
   * @param model
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('code:save')")
  @GetMapping("/new")
  public Mono<String> newCode(
      @ModelAttribute("codeForm") CodeForm form, Model model, WebSession session) {
    if (!form.isNew()) {
      // SessionAttributeに残っている場合は再生成する
      model.addAttribute("codeForm", new CodeForm());
    }

    return setBindingResultToAttribute(model, session).thenReturn("modules/system/codes/new");
  }

  /**
   * 登録処理
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('code:save')")
  @PostMapping("/new")
  public Mono<Rendering> newCode(
      @Validated @ModelAttribute("codeForm") CodeForm form, BindingResult br, WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/system/codes/new");
    }

    // 入力値からDTOを作成する
    val inputCode = modelMapper.map(form, Code.class);

    return codeService
        .create(inputCode)
        .flatMap(code -> redirectTo("/system/codes/show/" + code.getId()));
  }

  /**
   * 一覧画面 初期表示
   *
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('code:read')")
  @GetMapping("/find")
  public Mono<String> findCode(
      @ModelAttribute("searchCodeForm") SearchCodeForm form, Pageable pageable, Model model) {
    // 入力値から検索条件を作成する
    val criteria = modelMapper.map(form, Code.class);
    return codeService
        .findAll(criteria, pageable)
        .zipWith(codeCategoryService.findAll(new CodeCategory(), Pageable.unpaged()))
        .doOnNext(
            tuple2 -> {
              model.addAttribute("pages", tuple2.getT1());
              model.addAttribute("codeCategories", tuple2.getT2());
            })
        .thenReturn("modules/system/codes/find");
  }

  /**
   * 検索結果
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('code:read')")
  @PostMapping("/find")
  public Mono<Rendering> findCode(
      @Validated @ModelAttribute("searchCodeForm") SearchCodeForm form,
      BindingResult br,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
    }

    return redirectTo("/system/codes/find");
  }

  /**
   * 詳細画面
   *
   * @param codeId
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('code:read')")
  @GetMapping("/show/{codeId}")
  public Mono<String> showCode(@PathVariable UUID codeId, Model model) {
    return codeService
        .findById(codeId)
        .doOnNext(code -> model.addAttribute("code", code))
        .thenReturn("modules/system/codes/show");
  }

  /**
   * 編集画面 初期表示
   *
   * @param codeId
   * @param form
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('code:save')")
  @GetMapping("/edit/{codeId}")
  public Mono<String> editCode(
      @PathVariable UUID codeId,
      @ModelAttribute("codeForm") CodeForm form,
      Model model,
      WebSession session) {
    return codeService
        .findById(codeId)
        .flatMap(
            code -> {
              // セッションから取得できる場合は、読み込み直さない
              if (!hasErrors(session)) {
                // 取得したDtoをFromに詰め替える
                modelMapper.map(code, form);
              }
              return setBindingResultToAttribute(model, session)
                  .thenReturn("modules/system/codes/new");
            });
  }

  /**
   * 編集画面 更新処理
   *
   * @param form
   * @param br
   * @param codeId
   * @param sessionStatus
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('code:save')")
  @PostMapping("/edit/{codeId}")
  public Mono<Rendering> editCode(
      @Validated @ModelAttribute("codeForm") CodeForm form,
      BindingResult br,
      @PathVariable UUID codeId,
      SessionStatus sessionStatus,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/system/codes/edit/" + codeId);
    }

    return codeService
        .findById(codeId)
        .map(
            code -> {
              modelMapper.map(form, code);
              return code;
            })
        .flatMap(codeService::update)
        .flatMap(
            code -> {
              // セッションのcodeFormをクリアする
              sessionStatus.setComplete();
              return redirectTo("/system/codes/show/" + code.getId());
            });
  }

  /**
   * 削除処理
   *
   * @param codeId
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('code:save')")
  @PostMapping("/remove/{codeId}")
  public Mono<Rendering> removeCode(@PathVariable UUID codeId, WebSession session) {
    session.getAttributes().put(GLOBAL_MESSAGE, getMessage(MESSAGE_DELETED));
    return codeService.delete(codeId).then(redirectTo("/system/codes/find"));
  }

  /**
   * CSVダウンロード
   *
   * @param filename
   * @return
   */
  @PreAuthorize("hasAuthority('code:read')")
  @GetMapping("/download/{filename:.+\\.csv}")
  public Mono<ResponseEntity<Resource>> downloadCsv(
      @PathVariable String filename, ServerHttpResponse response) {
    return codeService
        .findAll(new Code(), Pageable.unpaged())
        .map(
            pages -> {
              val csvList = modelMapper.map(pages.getContent(), toListType(CodeCsv.class));
              val dataBuffer = response.bufferFactory().allocateBuffer();
              CsvUtils.writeCsv(CodeCsv.class, csvList, dataBuffer);
              return new InputStreamResource(dataBuffer.asInputStream(true));
            })
        .map(resource -> toResponseEntity(resource, filename, true));
  }
}
