package com.bigtreetc.sample.r2dbc.base.web.validator.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.val;
import org.springframework.http.codec.multipart.FilePart;

/** 入力チェック（コンテンツタイプ） */
public class ContentTypeValidator implements ConstraintValidator<ContentType, FilePart> {

  private final List<String> allowed = new ArrayList<>();
  private final List<String> rejected = new ArrayList<>();

  @Override
  public void initialize(ContentType fileExtension) {
    allowed.addAll(Arrays.asList(fileExtension.allowed()));
    rejected.addAll(Arrays.asList(fileExtension.rejected()));
  }

  @Override
  public boolean isValid(FilePart value, ConstraintValidatorContext context) {
    boolean isValid = false;

    try {
      if (value == null || value.headers().getContentType() == null) {
        return true;
      }

      val contentType = value.headers().getContentType().toString();
      if (allowed.contains(contentType)) {
        isValid = true;
      }
      if (rejected.contains(contentType)) {
        isValid = false;
      }
    } catch (final Exception ignore) {
      // ignore
    }

    return isValid;
  }
}
