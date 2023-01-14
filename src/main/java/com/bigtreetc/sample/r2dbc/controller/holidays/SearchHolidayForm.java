package com.bigtreetc.sample.r2dbc.controller.holidays;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseSearchForm;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchHolidayForm extends BaseSearchForm {

  private static final long serialVersionUID = 7228669911978606034L;

  UUID id;

  // 名称
  String holidayName;

  // 日付
  LocalDate holidayDate;
}
