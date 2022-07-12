package com.bigtreetc.sample.r2dbc.controller.system.uploadfiles;

import static com.bigtreetc.sample.r2dbc.base.util.MessageUtils.getMessage;
import static com.bigtreetc.sample.r2dbc.base.web.BaseWebConst.GLOBAL_MESSAGE;
import static java.util.stream.Collectors.toList;

import com.bigtreetc.sample.r2dbc.base.domain.helper.FileHelper;
import com.bigtreetc.sample.r2dbc.base.util.FileUtils;
import com.bigtreetc.sample.r2dbc.base.web.controller.html.AbstractHtmlController;
import java.nio.file.Paths;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Controller
@RequestMapping("/system/uploadFiles")
@Slf4j
public class UploadFileController extends AbstractHtmlController implements InitializingBean {

  @Value(
      "${application.fileUploadLocation:#{systemProperties['java.io.tmpdir']}}") // 設定ファイルに定義されたアップロード先を取得する
  String fileUploadLocation;

  @NonNull final FileHelper fileHelper;

  /**
   * 一覧画面
   *
   * @param model
   * @return
   */
  @PreAuthorize("hasAuthority('uploadFile')")
  @GetMapping("/list")
  public Mono<String> listFiles(Model model) {
    return Mono.fromCallable(
            () -> {
              // ファイル名のリストを作成する
              val location = Paths.get(fileUploadLocation);
              return fileHelper.listAllFiles(location).stream()
                  .map(path -> path.getFileName().toString())
                  .collect(toList());
            })
        .doOnNext(filenames -> model.addAttribute("filenames", filenames))
        .thenReturn("modules/system/uploadFiles/list");
  }

  /**
   * ファイル内容表示
   *
   * @param filename
   * @return
   */
  @PreAuthorize("hasAuthority('uploadFile')")
  @GetMapping("/{filename:.+}")
  public Mono<ResponseEntity<Resource>> serveFile(@PathVariable String filename) {
    return Mono.fromCallable(() -> fileHelper.loadFile(Paths.get(fileUploadLocation), filename))
        .map(resource -> toResponseEntity(resource, filename));
  }

  /**
   * ファイルダウンロード
   *
   * @param filename
   * @return
   */
  @PreAuthorize("hasAuthority('uploadFile')")
  @GetMapping("/download/{filename:.+}")
  public Mono<ResponseEntity<Resource>> downloadFile(@PathVariable String filename) {
    return Mono.fromCallable(() -> fileHelper.loadFile(Paths.get(fileUploadLocation), filename))
        .map(resource -> toResponseEntity(resource, filename, true));
  }

  /**
   * ファイルアップロード
   *
   * @param file
   * @param session
   * @return
   */
  @PreAuthorize("hasAuthority('uploadFile')")
  @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<Rendering> uploadFile(@RequestPart("file") FilePart file, WebSession session) {
    val filename = file.filename();
    val outputPath = Paths.get(fileUploadLocation).resolve(filename);
    return file.transferTo(outputPath)
        .doOnSuccess(
            done ->
                session
                    .getAttributes()
                    .put(GLOBAL_MESSAGE, getMessage("uploadFiles.upload.success")))
        .then(redirectTo("/system/uploadFiles/list"));
  }

  /**
   * ファイルアップロード（Ajax）
   *
   * @param file
   * @return
   */
  @PreAuthorize("hasAuthority('uploadFile')")
  @PostMapping(
      path = "/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      headers = "x-requested-with=XMLHttpRequest")
  public Mono<ResponseEntity<?>> uploadFile(@RequestPart("file") FilePart file) {
    val filename = file.filename();
    val outputPath = Paths.get(fileUploadLocation).resolve(filename);
    val body =
        Map.<String, Object>of(
            "message", getMessage("uploadFiles.upload.success"), "success", true);
    return file.transferTo(outputPath).thenReturn(ResponseEntity.ok().body(body));
  }

  /**
   * ファイル削除
   *
   * @param filename
   * @return
   */
  @PreAuthorize("hasAuthority('uploadFile')")
  @DeleteMapping(path = "/delete/{filename:.+}")
  public Mono<Rendering> deleteFile(@PathVariable String filename) {
    return Mono.fromRunnable(() -> FileUtils.deleteFile(Paths.get(fileUploadLocation), filename))
        .then(redirectTo("/system/uploadFiles/list"));
  }

  @Override
  public void afterPropertiesSet() {
    // アップロードディレクトリ
    val location = Paths.get(fileUploadLocation);

    // ディレクトリがない場合は作成する
    FileUtils.createDirectories(location);
  }
}
