package com.bigtreetc.sample.r2dbc.controller.system.holidays;

import com.bigtreetc.sample.r2dbc.base.web.controller.html.BaseForm;
import java.time.LocalDate;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HolidayForm extends BaseForm {

  private static final long serialVersionUID = 6646321876052100374L;

  UUID id;

  // 名称
  @NotEmpty String holidayName;

  // 日付
  @NotNull LocalDate holidayDate;
}
