package com.bigtreetc.sample.r2dbc.controller.staffs;

import static com.bigtreetc.sample.r2dbc.base.util.MessageUtils.getMessage;
import static com.bigtreetc.sample.r2dbc.base.util.ValidateUtils.isNotEmpty;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;

import com.bigtreetc.sample.r2dbc.base.util.CsvUtils;
import com.bigtreetc.sample.r2dbc.base.web.controller.html.AbstractHtmlController;
import com.bigtreetc.sample.r2dbc.domain.model.Staff;
import com.bigtreetc.sample.r2dbc.domain.model.StaffCriteria;
import com.bigtreetc.sample.r2dbc.domain.service.StaffService;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** 担当者管理 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/system/staffs")
@SessionAttributes(types = {SearchStaffForm.class, StaffForm.class})
@Slf4j
public class StaffController extends AbstractHtmlController {

  @NonNull final StaffFormValidator staffFormValidator;

  @NonNull final StaffService staffService;

  @NonNull final PasswordEncoder passwordEncoder;

  @ModelAttribute("staffForm")
  public StaffForm staffForm() {
    return new StaffForm();
  }

  @ModelAttribute("searchStaffForm")
  public SearchStaffForm searchStaffForm() {
    return new SearchStaffForm();
  }

  @InitBinder("staffForm")
  public void validatorBinder(WebDataBinder binder) {
    binder.addValidators(staffFormValidator);
  }

  /**
   * 登録画面 初期表示
   *
   * @param form
   * @param model
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('staff:save')")
  @GetMapping("/new")
  public Mono<String> newStaff(
      @ModelAttribute("staffForm") StaffForm form, Model model, WebSession session) {
    // SessionAttributeに残っている場合は再生成する
    if (!form.isNew()) {
      model.addAttribute("staffForm", new StaffForm());
    }

    return setBindingResultToAttribute(model, session).thenReturn("modules/system/staffs/new");
  }

  /**
   * 登録処理
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('staff:save')")
  @PostMapping("/new")
  public Mono<Rendering> newStaff(
      @Validated @ModelAttribute("staffForm") StaffForm form,
      BindingResult br,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/system/staffs/new");
    }

    // 入力値からDTOを作成する
    val inputStaff = modelMapper.map(form, Staff.class);
    val password = form.getPassword();

    // パスワードをハッシュ化する
    inputStaff.setPassword(passwordEncoder.encode(password));

    return staffService
        .create(inputStaff)
        .flatMap(staff -> redirectTo("/system/staffs/show/" + staff.getId()));
  }

  /**
   * 一覧画面 初期表示
   *
   * @param form
   * @param pageable
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('staff:read')")
  @GetMapping("/find")
  public Mono<String> findStaff(
      @ModelAttribute SearchStaffForm form, Pageable pageable, Model model) {
    // 入力値を詰め替える
    val criteria = modelMapper.map(form, StaffCriteria.class);
    return staffService
        .findAll(criteria, pageable)
        .doOnNext(pages -> model.addAttribute("pages", pages))
        .thenReturn("modules/system/staffs/find");
  }

  /**
   * 検索結果
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('staff:read')")
  @PostMapping("/find")
  public Mono<Rendering> findStaff(
      @Validated @ModelAttribute("searchStaffForm") SearchStaffForm form,
      BindingResult br,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
    }

    return redirectTo("/system/staffs/find");
  }

  /**
   * 詳細画面
   *
   * @param staffId
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('staff:read')")
  @GetMapping("/show/{staffId}")
  public Mono<String> showStaff(@PathVariable UUID staffId, Model model) {
    return staffService
        .findById(staffId)
        .doOnNext(staff -> model.addAttribute("staff", staff))
        .thenReturn("modules/system/staffs/show");
  }

  /**
   * 編集画面 初期表示
   *
   * @param staffId
   * @param form
   * @param model
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('staff:save')")
  @GetMapping("/edit/{staffId}")
  public Mono<String> editStaff(
      @PathVariable UUID staffId,
      @ModelAttribute("staffForm") StaffForm form,
      Model model,
      WebSession session) {
    return staffService
        .findById(staffId)
        .flatMap(
            staff -> {
              // セッションから取得できる場合は、読み込み直さない
              if (!hasErrors(session)) {
                // 取得したDtoをFromに詰め替える
                modelMapper.map(staff, form);
              }
              return setBindingResultToAttribute(model, session)
                  .thenReturn("modules/system/staffs/new");
            });
  }

  /**
   * 編集画面 更新処理
   *
   * @param staffId
   * @param form
   * @param br
   * @param session
   * @param sessionStatus
   * @return
   */
  @PreAuthorize("hasAuthority('staff:save')")
  @PostMapping("/edit/{staffId}")
  public Mono<Rendering> editStaff(
      @PathVariable UUID staffId,
      @Validated @ModelAttribute("staffForm") StaffForm form,
      BindingResult br,
      WebSession session,
      SessionStatus sessionStatus) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/system/staffs/edit/" + staffId);
    }

    return staffService
        .findById(staffId)
        .map(
            staff -> {
              modelMapper.map(form, staff);
              val password = staff.getPassword();
              if (isNotEmpty(password)) {
                staff.setPassword(passwordEncoder.encode(password));
              }
              return staff;
            })
        .flatMap(staffService::update)
        .flatMap(
            staff -> {
              // セッションのstaffFormをクリアする
              sessionStatus.setComplete();
              return redirectTo("/system/staffs/show/" + staff.getId());
            });
  }

  /**
   * 削除処理
   *
   * @param staffId
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('staff:save')")
  @PostMapping("/remove/{staffId}")
  public Mono<Rendering> removeStaff(@PathVariable UUID staffId, WebSession session) {
    session.getAttributes().put(GLOBAL_MESSAGE, getMessage(MESSAGE_DELETED));
    return staffService.delete(staffId).then(redirectTo("/system/staffs/find"));
  }

  /**
   * CSVダウンロード
   *
   * @param filename
   * @param response
   * @return
   */
  @PreAuthorize("hasAuthority('staff:read')")
  @GetMapping("/download/{filename:.+\\.csv}")
  public Mono<Void> downloadCsv(@PathVariable String filename, ServerHttpResponse response) {
    // ダウンロード時のファイル名をセットする
    setContentDispositionHeader(response, filename, true);

    val dataBufferFactory = response.bufferFactory();
    val criteria = new StaffCriteria();
    val data = staffService.findAll(criteria);
    val dataBufferFlux =
        CsvUtils.writeCsv(
            dataBufferFactory,
            StaffCsv.class,
            data,
            staff -> modelMapper.map(staff, StaffCsv.class));

    return response.writeAndFlushWith(dataBufferFlux.map(Flux::just));
  }
}
