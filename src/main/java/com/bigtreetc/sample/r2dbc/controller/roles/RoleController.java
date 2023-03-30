package com.bigtreetc.sample.r2dbc.controller.roles;

import static com.bigtreetc.sample.r2dbc.base.util.MessageUtils.getMessage;
import static com.bigtreetc.sample.r2dbc.base.util.TypeUtils.toListType;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.MESSAGE_DELETED;

import com.bigtreetc.sample.r2dbc.base.util.CsvUtils;
import com.bigtreetc.sample.r2dbc.base.web.controller.html.AbstractHtmlController;
import com.bigtreetc.sample.r2dbc.domain.model.*;
import com.bigtreetc.sample.r2dbc.domain.service.PermissionService;
import com.bigtreetc.sample.r2dbc.domain.service.RoleService;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
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

/** ロール管理 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/system/roles")
@SessionAttributes(types = {SearchRoleForm.class, RoleForm.class})
@Slf4j
public class RoleController extends AbstractHtmlController {

  @NonNull final RoleFormValidator roleFormValidator;

  @NonNull final RoleService roleService;

  @NonNull final PermissionService permissionService;

  @ModelAttribute("roleForm")
  public RoleForm roleForm() {
    return new RoleForm();
  }

  @ModelAttribute("searchRoleForm")
  public SearchRoleForm searchRoleForm() {
    return new SearchRoleForm();
  }

  @InitBinder("roleForm")
  public void validatorBinder(WebDataBinder binder) {
    binder.addValidators(roleFormValidator);
  }

  /**
   * 登録画面 初期表示
   *
   * @param form
   * @param model
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('role:save')")
  @GetMapping("/new")
  public Mono<String> newRole(
      @ModelAttribute("roleForm") RoleForm form, Model model, WebSession session) {
    if (!form.isNew()) {
      // SessionAttributeに残っている場合は再生成する
      model.addAttribute("roleForm", new RoleForm());
    }

    return getPermissions()
        .flatMap(
            pages -> {
              model.addAttribute("permissions", pages);
              return setBindingResultToAttribute(model, session);
            })
        .thenReturn("modules/system/roles/new");
  }

  /**
   * 登録処理
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('role:save')")
  @PostMapping("/new")
  public Mono<Rendering> newRole(
      @Validated @ModelAttribute("roleForm") RoleForm form, BindingResult br, WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/system/roles/new");
    }

    // 入力値からDTOを作成する
    val inputRole = modelMapper.map(form, Role.class);
    for (val entry : form.getPermissions().entrySet()) {
      val rp = new RolePermission();
      rp.setId(UUID.randomUUID());
      rp.setRoleCode(form.getRoleCode());
      rp.setPermissionCode(entry.getKey());
      rp.setIsEnabled(entry.getValue());
      inputRole.getRolePermissions().add(rp);
    }

    return roleService
        .create(inputRole)
        .flatMap(role -> redirectTo("/system/roles/show/" + role.getId()));
  }

  /**
   * 一覧画面 初期表示
   *
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('role:read')")
  @GetMapping("/find")
  public Mono<String> findRole(
      @ModelAttribute SearchRoleForm form, Pageable pageable, Model model) {
    // 入力値から検索条件を作成する
    val criteria = modelMapper.map(form, RoleCriteria.class);
    return roleService
        .findAll(criteria, pageable)
        .doOnNext(pages -> model.addAttribute("pages", pages))
        .thenReturn("modules/system/roles/find");
  }

  /**
   * 検索結果
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('role:read')")
  @PostMapping("/find")
  public Mono<Rendering> findRole(
      @Validated @ModelAttribute("searchRoleForm") SearchRoleForm form,
      BindingResult br,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
    }

    return redirectTo("/system/roles/find");
  }

  /**
   * 詳細画面
   *
   * @param roleId
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('role:read')")
  @GetMapping("/show/{roleId}")
  public Mono<String> showRole(@PathVariable UUID roleId, Model model) {
    return roleService
        .findById(roleId)
        .zipWith(getPermissions())
        .doOnNext(
            tuple2 -> {
              model.addAttribute("role", tuple2.getT1());
              model.addAttribute("permissions", tuple2.getT2());
            })
        .thenReturn("modules/system/roles/show");
  }

  /**
   * 編集画面 初期表示
   *
   * @param roleId
   * @param form
   * @param model
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('role:save')")
  @GetMapping("/edit/{roleId}")
  public Mono<String> editRole(
      @PathVariable UUID roleId,
      @ModelAttribute("roleForm") RoleForm form,
      Model model,
      WebSession session) {
    return roleService
        .findById(roleId)
        .zipWith(getPermissions())
        .flatMap(
            tuple2 -> {
              // セッションから取得できる場合は、読み込み直さない
              if (!hasErrors(session)) {
                // 取得したDtoをFromに詰め替える
                val role = tuple2.getT1();
                form.setId(role.getId());
                form.setRoleCode(role.getRoleCode());
                form.setRoleName(role.getRoleName());
                for (val p : role.getPermissions()) {
                  val permissionCode = p.getPermissionCode();
                  val isEnabled = role.hasPermission(permissionCode);
                  form.getPermissions().put(permissionCode, isEnabled);
                }
              }
              model.addAttribute("permissions", tuple2.getT2());
              return setBindingResultToAttribute(model, session)
                  .thenReturn("modules/system/roles/new");
            });
  }

  /**
   * 編集画面 更新処理
   *
   * @param form
   * @param br
   * @param roleId
   * @param sessionStatus
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('role:save')")
  @PostMapping("/edit/{roleId}")
  public Mono<Rendering> editRole(
      @Validated @ModelAttribute("roleForm") RoleForm form,
      BindingResult br,
      @PathVariable UUID roleId,
      SessionStatus sessionStatus,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/system/roles/edit/" + roleId);
    }

    return roleService
        .findById(roleId)
        .map(
            role -> {
              role.setRoleCode(form.getRoleCode());
              role.setRoleName(form.getRoleName());

              val permissions = form.getPermissions();
              for (val entry : permissions.entrySet()) {
                val permissionCode = entry.getKey();
                val isEnabled = Boolean.TRUE.equals(entry.getValue());
                role.setPermission(permissionCode, isEnabled);
              }
              return role;
            })
        .flatMap(roleService::update)
        .flatMap(
            role -> {
              // セッションのroleFormをクリアする
              sessionStatus.setComplete();
              return redirectTo("/system/roles/show/" + role.getId());
            });
  }

  /**
   * 削除処理
   *
   * @param roleId
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('role:save')")
  @PostMapping("/remove/{roleId}")
  public Mono<Rendering> removeRole(@PathVariable UUID roleId, WebSession session) {
    session.getAttributes().put(GLOBAL_MESSAGE, getMessage(MESSAGE_DELETED));
    return roleService.delete(roleId).then(redirectTo("/system/roles/find"));
  }

  /**
   * CSVダウンロード
   *
   * @param filename
   * @return
   */
  @PreAuthorize("hasAuthority('role:read')")
  @GetMapping("/download/{filename:.+\\.csv}")
  public Mono<ResponseEntity<Resource>> downloadCsv(
      @PathVariable String filename, ServerHttpResponse response) {
    return roleService
        .findAll(new RoleCriteria(), Pageable.unpaged())
        .map(
            pages -> {
              val csvList = modelMapper.map(pages.getContent(), toListType(RoleCsv.class));
              val dataBuffer = response.bufferFactory().allocateBuffer(1024);
              CsvUtils.writeCsv(RoleCsv.class, csvList, dataBuffer);
              return new InputStreamResource(dataBuffer.asInputStream(true));
            })
        .map(resource -> toResponseEntity(resource, filename, true));
  }

  private Mono<Page<Permission>> getPermissions() {
    return permissionService.findAll(new PermissionCriteria(), Pageable.unpaged());
  }
}
