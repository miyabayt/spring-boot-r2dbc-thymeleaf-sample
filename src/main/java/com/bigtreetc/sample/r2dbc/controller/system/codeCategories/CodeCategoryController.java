package com.bigtreetc.sample.r2dbc.controller.system.codecategories;

import static com.bigtreetc.sample.r2dbc.base.util.MessageUtils.getMessage;
import static com.bigtreetc.sample.r2dbc.base.util.TypeUtils.toListType;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.MESSAGE_DELETED;

import com.bigtreetc.sample.r2dbc.base.util.CsvUtils;
import com.bigtreetc.sample.r2dbc.base.web.controller.html.AbstractHtmlController;
import com.bigtreetc.sample.r2dbc.domain.model.system.CodeCategory;
import com.bigtreetc.sample.r2dbc.domain.service.system.CodeCategoryService;
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

/** コード分類管理 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/system/codeCategories")
@SessionAttributes(types = {SearchCodeCategoryForm.class, CodeCategoryForm.class})
@Slf4j
public class CodeCategoryController extends AbstractHtmlController {

  @NonNull final CodeCategoryFormValidator codeCategoryFormValidator;

  @NonNull final CodeCategoryService codeCategoryService;

  @ModelAttribute("codeCategoryForm")
  public CodeCategoryForm codeCategoryForm() {
    return new CodeCategoryForm();
  }

  @ModelAttribute("searchCodeCategoryForm")
  public SearchCodeCategoryForm searchcodeCategoryForm() {
    return new SearchCodeCategoryForm();
  }

  @InitBinder("codeCategoryForm")
  public void validatorBinder(WebDataBinder binder) {
    binder.addValidators(codeCategoryFormValidator);
  }

  /**
   * 登録画面 初期表示
   *
   * @param form
   * @param model
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('codeCategory:save')")
  @GetMapping("/new")
  public Mono<String> newCode(
      @ModelAttribute("codeCategoryForm") CodeCategoryForm form, Model model, WebSession session) {
    if (!form.isNew()) {
      // SessionAttributeに残っている場合は再生成する
      model.addAttribute("codeCategoryForm", new CodeCategoryForm());
    }

    return setBindingResultToAttribute(model, session)
        .thenReturn("modules/system/codeCategories/new");
  }

  /**
   * 登録処理
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('codeCategory:save')")
  @PostMapping("/new")
  public Mono<Rendering> newCode(
      @Validated @ModelAttribute("codeCategoryForm") CodeCategoryForm form,
      BindingResult br,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/system/codeCategories/new");
    }

    // 入力値からDTOを作成する
    val inputCodeCategory = modelMapper.map(form, CodeCategory.class);

    return codeCategoryService
        .create(inputCodeCategory)
        .flatMap(codeCategory -> redirectTo("/system/codeCategories/show/" + codeCategory.getId()));
  }

  /**
   * 一覧画面 初期表示
   *
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('codeCategory:read')")
  @GetMapping("/find")
  public Mono<String> findCodeCategory(
      @ModelAttribute("searchCodeCategoryForm") SearchCodeCategoryForm form,
      Pageable pageable,
      Model model) {
    // 入力値から検索条件を作成する
    val criteria = modelMapper.map(form, CodeCategory.class);
    return codeCategoryService
        .findAll(criteria, pageable)
        .zipWith(codeCategoryService.findAll(new CodeCategory(), Pageable.unpaged()))
        .doOnNext(
            tuple2 -> {
              model.addAttribute("pages", tuple2.getT1());
              model.addAttribute("codeCategories", tuple2.getT2());
            })
        .thenReturn("modules/system/codeCategories/find");
  }

  /**
   * 検索結果
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('codeCategory:read')")
  @PostMapping("/find")
  public Mono<Rendering> findCodeCategory(
      @Validated @ModelAttribute("searchCodeCategoryForm") SearchCodeCategoryForm form,
      BindingResult br,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
    }

    return redirectTo("/system/codeCategories/find");
  }

  /**
   * 詳細画面
   *
   * @param codeCategoryId
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('codeCategory:read')")
  @GetMapping("/show/{codeCategoryId}")
  public Mono<String> showCodeCategory(@PathVariable UUID codeCategoryId, Model model) {
    return codeCategoryService
        .findById(codeCategoryId)
        .doOnNext(codeCategory -> model.addAttribute("codeCategory", codeCategory))
        .thenReturn("modules/system/codeCategories/show");
  }

  /**
   * 編集画面 初期表示
   *
   * @param codeCategoryId
   * @param form
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('codeCategory:save')")
  @GetMapping("/edit/{codeCategoryId}")
  public Mono<String> editCodeCategory(
      @PathVariable UUID codeCategoryId,
      @ModelAttribute("codeCategoryForm") CodeCategoryForm form,
      Model model,
      WebSession session) {
    return codeCategoryService
        .findById(codeCategoryId)
        .flatMap(
            codeCategory -> {
              // セッションから取得できる場合は、読み込み直さない
              if (!hasErrors(session)) {
                // 取得したDtoをFromに詰め替える
                modelMapper.map(codeCategory, form);
              }
              return setBindingResultToAttribute(model, session)
                  .thenReturn("modules/system/codeCategories/new");
            });
  }

  /**
   * 編集画面 更新処理
   *
   * @param form
   * @param br
   * @param codeCategoryId
   * @param sessionStatus
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('codeCategory:save')")
  @PostMapping("/edit/{codeCategoryId}")
  public Mono<Rendering> editCodeCategory(
      @Validated @ModelAttribute("codeCategoryForm") CodeCategoryForm form,
      BindingResult br,
      @PathVariable UUID codeCategoryId,
      SessionStatus sessionStatus,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/system/codeCategories/edit/" + codeCategoryId);
    }

    return codeCategoryService
        .findById(codeCategoryId)
        .map(
            codeCategory -> {
              modelMapper.map(form, codeCategory);
              return codeCategory;
            })
        .flatMap(codeCategoryService::update)
        .flatMap(
            codeCategory -> {
              // セッションのcodeCategoryFormをクリアする
              sessionStatus.setComplete();
              return redirectTo("/system/codeCategories/show/" + codeCategory.getId());
            });
  }

  /**
   * 削除処理
   *
   * @param codeCategoryId
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('codeCategory:save')")
  @PostMapping("/remove/{codeCategoryId}")
  public Mono<Rendering> removeCodeCategory(@PathVariable UUID codeCategoryId, WebSession session) {
    session.getAttributes().put(GLOBAL_MESSAGE, getMessage(MESSAGE_DELETED));
    return codeCategoryService
        .delete(codeCategoryId)
        .then(redirectTo("/system/codeCategories/find"));
  }

  /**
   * CSVダウンロード
   *
   * @param filename
   * @return
   */
  @PreAuthorize("hasAuthority('codeCategory:read')")
  @GetMapping("/download/{filename:.+\\.csv}")
  public Mono<ResponseEntity<Resource>> downloadCsv(
      @PathVariable String filename, ServerHttpResponse response) {
    return codeCategoryService
        .findAll(new CodeCategory(), Pageable.unpaged())
        .map(
            pages -> {
              val csvList = modelMapper.map(pages.getContent(), toListType(CodeCategoryCsv.class));
              val dataBuffer = response.bufferFactory().allocateBuffer();
              CsvUtils.writeCsv(CodeCategoryCsv.class, csvList, dataBuffer);
              return new InputStreamResource(dataBuffer.asInputStream(true));
            })
        .map(resource -> toResponseEntity(resource, filename, true));
  }
}
