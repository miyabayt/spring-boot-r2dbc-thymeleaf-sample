package com.bigtreetc.sample.r2dbc.controller.system.holidays;

import static com.bigtreetc.sample.r2dbc.base.util.MessageUtils.getMessage;
import static com.bigtreetc.sample.r2dbc.base.util.TypeUtils.toListType;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.MESSAGE_DELETED;

import com.bigtreetc.sample.r2dbc.base.util.CsvUtils;
import com.bigtreetc.sample.r2dbc.base.web.controller.html.AbstractHtmlController;
import com.bigtreetc.sample.r2dbc.domain.model.system.Holiday;
import com.bigtreetc.sample.r2dbc.domain.service.system.HolidayService;
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

/** 祝日管理 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/system/holidays")
@SessionAttributes(types = {SearchHolidayForm.class, HolidayForm.class})
@Slf4j
public class HolidayController extends AbstractHtmlController {

  @NonNull final HolidayFormValidator holidayFormValidator;

  @NonNull final HolidayService holidayService;

  @ModelAttribute("holidayForm")
  public HolidayForm holidayForm() {
    return new HolidayForm();
  }

  @ModelAttribute("searchHolidayForm")
  public SearchHolidayForm searchHolidayForm() {
    return new SearchHolidayForm();
  }

  @InitBinder("holidayForm")
  public void validatorBinder(WebDataBinder binder) {
    binder.addValidators(holidayFormValidator);
  }

  /**
   * 登録画面 初期表示
   *
   * @param form
   * @param model
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('holiday:save')")
  @GetMapping("/new")
  public Mono<String> newHoliday(
      @ModelAttribute("holidayForm") HolidayForm form, Model model, WebSession session) {
    // SessionAttributeに残っている場合は再生成する
    if (!form.isNew()) {
      model.addAttribute("holidayForm", new HolidayForm());
    }

    return setBindingResultToAttribute(model, session).thenReturn("modules/system/holidays/new");
  }

  /**
   * 登録処理
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('holiday:save')")
  @PostMapping("/new")
  public Mono<Rendering> newHoliday(
      @Validated @ModelAttribute("holidayForm") HolidayForm form,
      BindingResult br,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/system/holidays/new");
    }

    // 入力値からDTOを作成する
    val inputHoliday = modelMapper.map(form, Holiday.class);

    return holidayService
        .create(inputHoliday)
        .flatMap(holiday -> redirectTo("/system/holidays/show/" + holiday.getId()));
  }

  /**
   * 一覧画面 初期表示
   *
   * @param form
   * @param pageable
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('holiday:read')")
  @GetMapping("/find")
  public Mono<String> findHoliday(
      @ModelAttribute("searchHolidayForm") SearchHolidayForm form, Pageable pageable, Model model) {
    // 入力値から検索条件を作成する
    val criteria = modelMapper.map(form, Holiday.class);
    return holidayService
        .findAll(criteria, pageable)
        .doOnNext(pages -> model.addAttribute("pages", pages))
        .thenReturn("modules/system/holidays/find");
  }

  /**
   * 検索結果
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('holiday:read')")
  @PostMapping("/find")
  public Mono<Rendering> findHoliday(
      @Validated @ModelAttribute("searchHolidayForm") SearchHolidayForm form,
      BindingResult br,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
    }

    return redirectTo("/system/holidays/find");
  }

  /**
   * 詳細画面
   *
   * @param holidayId
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('holiday:read')")
  @GetMapping("/show/{holidayId}")
  public Mono<String> showHoliday(@PathVariable UUID holidayId, Model model) {
    return holidayService
        .findById(holidayId)
        .doOnNext(holiday -> model.addAttribute("holiday", holiday))
        .thenReturn("modules/system/holidays/show");
  }

  /**
   * 編集画面 初期表示
   *
   * @param holidayId
   * @param form
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('holiday:save')")
  @GetMapping("/edit/{holidayId}")
  public Mono<String> editHoliday(
      @PathVariable UUID holidayId,
      @ModelAttribute("holidayForm") HolidayForm form,
      Model model,
      WebSession session) {
    return holidayService
        .findById(holidayId)
        .flatMap(
            holiday -> {
              // セッションから取得できる場合は、読み込み直さない
              if (!hasErrors(session)) {
                // 取得したDtoをFromに詰め替える
                modelMapper.map(holiday, form);
              }
              return setBindingResultToAttribute(model, session)
                  .thenReturn("modules/system/holidays/new");
            });
  }

  /**
   * 編集画面 更新処理
   *
   * @param form
   * @param br
   * @param holidayId
   * @param sessionStatus
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('holiday:save')")
  @PostMapping("/edit/{holidayId}")
  public Mono<Rendering> editHoliday(
      @Validated @ModelAttribute("holidayForm") HolidayForm form,
      BindingResult br,
      @PathVariable UUID holidayId,
      SessionStatus sessionStatus,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/system/holidays/edit/" + holidayId);
    }

    return holidayService
        .findById(holidayId)
        .map(
            holiday -> {
              modelMapper.map(form, holiday);
              return holiday;
            })
        .flatMap(holidayService::update)
        .flatMap(
            holiday -> {
              // セッションのholidayFormをクリアする
              sessionStatus.setComplete();
              return redirectTo("/system/holidays/show/" + holiday.getId());
            });
  }

  /**
   * 削除処理
   *
   * @param holidayId
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('holiday:save')")
  @PostMapping("/remove/{holidayId}")
  public Mono<Rendering> removeHoliday(@PathVariable UUID holidayId, WebSession session) {
    session.getAttributes().put(GLOBAL_MESSAGE, getMessage(MESSAGE_DELETED));
    return holidayService.delete(holidayId).then(redirectTo("/system/holidays/find"));
  }

  /**
   * CSVダウンロード
   *
   * @param filename
   * @return
   */
  @PreAuthorize("hasAuthority('holiday:read')")
  @GetMapping("/download/{filename:.+\\.csv}")
  public Mono<ResponseEntity<Resource>> downloadCsv(
      @PathVariable String filename, ServerHttpResponse response) {
    return holidayService
        .findAll(new Holiday(), Pageable.unpaged())
        .map(
            pages -> {
              val csvList = modelMapper.map(pages.getContent(), toListType(HolidayCsv.class));
              val dataBuffer = response.bufferFactory().allocateBuffer();
              CsvUtils.writeCsv(HolidayCsv.class, csvList, dataBuffer);
              return new InputStreamResource(dataBuffer.asInputStream(true));
            })
        .map(resource -> toResponseEntity(resource, filename, true));
  }
}
