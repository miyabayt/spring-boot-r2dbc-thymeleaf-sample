package com.bigtreetc.sample.r2dbc.controller.users.users;

import static com.bigtreetc.sample.r2dbc.base.util.MessageUtils.getMessage;
import static com.bigtreetc.sample.r2dbc.base.util.TypeUtils.toListType;
import static com.bigtreetc.sample.r2dbc.base.util.ValidateUtils.isNotEmpty;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.*;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.MESSAGE_DELETED;

import com.bigtreetc.sample.r2dbc.base.util.CsvUtils;
import com.bigtreetc.sample.r2dbc.base.web.controller.html.AbstractHtmlController;
import com.bigtreetc.sample.r2dbc.domain.model.system.UploadFile;
import com.bigtreetc.sample.r2dbc.domain.model.user.User;
import com.bigtreetc.sample.r2dbc.domain.service.users.UserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

/** ユーザ管理 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/users/users")
@SessionAttributes(types = {SearchUserForm.class, UserForm.class})
@Slf4j
public class UserController extends AbstractHtmlController {

  @NonNull final UserFormValidator userFormValidator;

  @NonNull final UserService userService;

  @NonNull final PasswordEncoder passwordEncoder;

  @ModelAttribute("userForm")
  public UserForm userForm() {
    return new UserForm();
  }

  @ModelAttribute("searchUserForm")
  public SearchUserForm searchUserForm() {
    return new SearchUserForm();
  }

  @InitBinder("userForm")
  public void validatorBinder(WebDataBinder binder) {
    binder.addValidators(userFormValidator);
  }

  /**
   * 登録画面 初期表示
   *
   * @param form
   * @param model
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('user:save')")
  @GetMapping("/new")
  public Mono<String> newUser(
      @ModelAttribute("userForm") UserForm form, Model model, WebSession session) {
    // SessionAttributeに残っている場合は再生成する
    if (!form.isNew()) {
      model.addAttribute("userForm", new UserForm());
    }

    return setBindingResultToAttribute(model, session).thenReturn("modules/users/users/new");
  }

  /**
   * 登録処理
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('user:save')")
  @PostMapping("/new")
  public Mono<Rendering> newUser(
      @Validated @ModelAttribute("userForm") UserForm form, BindingResult br, WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/users/users/new");
    }

    // 入力値からDTOを作成する
    val inputUser = modelMapper.map(form, User.class);
    val password = form.getPassword();

    // パスワードをハッシュ化する
    inputUser.setPassword(passwordEncoder.encode(password));

    return userService
        .create(inputUser)
        .flatMap(user -> redirectTo("/users/users/show/" + user.getId()));
  }

  /**
   * 一覧画面 初期表示
   *
   * @param model
   * @param pageable
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('user:read')")
  @GetMapping("/find")
  public Mono<String> findUser(
      @ModelAttribute SearchUserForm form, Pageable pageable, Model model) {
    // 入力値を詰め替える
    val criteria = modelMapper.map(form, User.class);
    return userService
        .findAll(criteria, pageable)
        .doOnNext(pages -> model.addAttribute("pages", pages))
        .thenReturn("modules/users/users/find");
  }

  /**
   * 検索結果
   *
   * @param form
   * @param br
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('user:read')")
  @PostMapping("/find")
  public Mono<Rendering> findUser(
      @Validated @ModelAttribute("searchUserForm") SearchUserForm form,
      BindingResult br,
      WebSession session) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
    }

    return redirectTo("/users/users/find");
  }

  /**
   * 詳細画面
   *
   * @param userId
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('user:read')")
  @GetMapping("/show/{userId}")
  public Mono<String> showUser(@PathVariable UUID userId, Model model) {
    return userService
        .findById(userId)
        .doOnNext(
            user -> {
              model.addAttribute("user", user);
              if (user.getUploadFile() != null) {
                // 添付ファイルを取得する
                val uploadFile = user.getUploadFile();

                // Base64デコードして解凍する
                val base64data = Base64Utils.encodeToString(uploadFile.getContent());
                model.addAttribute("image", "data:image/png;base64," + base64data);
              }
            })
        .thenReturn("modules/users/users/show");
  }

  /**
   * 編集画面 初期表示
   *
   * @param userId
   * @param form
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('user:save')")
  @GetMapping("/edit/{userId}")
  public Mono<String> editUser(
      @PathVariable UUID userId,
      @ModelAttribute("userForm") UserForm form,
      Model model,
      WebSession session) {
    return userService
        .findById(userId)
        .flatMap(
            user -> {
              // セッションから取得できる場合は、読み込み直さない
              if (!hasErrors(session)) {
                // 取得したDtoをFromに詰め替える
                modelMapper.map(user, form);
              }
              return setBindingResultToAttribute(model, session);
            })
        .thenReturn("modules/users/users/new");
  }

  /**
   * 編集画面 更新処理
   *
   * @param form
   * @param br
   * @param userId
   * @param session
   * @param sessionStatus
   * @return
   */
  @PreAuthorize("hasAuthority('user:save')")
  @PostMapping("/edit/{userId}")
  public Mono<Rendering> editUser(
      @PathVariable UUID userId,
      @Validated @ModelAttribute("userForm") UserForm form,
      BindingResult br,
      WebSession session,
      SessionStatus sessionStatus) {
    // 入力チェックエラーがある場合は、元の画面にもどる
    if (br.hasErrors()) {
      session.getAttributes().put(MAV_ERRORS, br);
      return redirectTo("/users/users/edit/" + userId);
    }

    return userService
        .findById(userId)
        .flatMap(
            user -> {
              modelMapper.map(form, user);
              val password = user.getPassword();
              if (isNotEmpty(password)) {
                user.setPassword(passwordEncoder.encode(password));
              }
              val image = form.getUserImage();
              if (image != null) {
                return image
                    .content()
                    .collectList()
                    .map(
                        content -> {
                          val uploadFile = new UploadFile();
                          user.setUploadFile(uploadFile);
                          return user;
                        });
              }
              return Mono.just(user);
            })
        .flatMap(userService::update)
        .flatMap(
            user -> {
              // セッションのstaffFormをクリアする
              sessionStatus.setComplete();
              return redirectTo("/users/users/show/" + user.getId());
            });
  }

  /**
   * 削除処理
   *
   * @param userId
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('user:save')")
  @PostMapping("/remove/{userId}")
  public Mono<Rendering> removeUser(@PathVariable UUID userId, WebSession session) {
    session.getAttributes().put(GLOBAL_MESSAGE, getMessage(MESSAGE_DELETED));
    return userService.delete(userId).then(redirectTo("/users/users/find"));
  }

  /**
   * CSVダウンロード
   *
   * @param filename
   * @return
   */
  @PreAuthorize("hasAuthority('user:read')")
  @GetMapping("/download/{filename:.+\\.csv}")
  public Mono<ResponseEntity<Resource>> downloadCsv(
      @PathVariable String filename, ServerHttpResponse response) {
    return userService
        .findAll(new User(), Pageable.unpaged())
        .map(
            pages -> {
              val csvList = modelMapper.map(pages.getContent(), toListType(UserCsv.class));
              val dataBuffer = response.bufferFactory().allocateBuffer();
              CsvUtils.writeCsv(UserCsv.class, csvList, dataBuffer);
              return new InputStreamResource(dataBuffer.asInputStream(true));
            })
        .map(resource -> toResponseEntity(resource, filename, true));
  }
}
