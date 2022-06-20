package com.bigtreetc.sample.r2dbc.base.web;

/** 定数定義 */
public interface BaseWebConst {

  /** ---- Message ---- */
  String GLOBAL_MESSAGE = "GlobalMessage";

  String VALIDATION_ERROR = "ValidationError";

  String DUPLICATE_KEY_ERROR = "DuplicateKeyError";

  String OPTIMISTIC_LOCKING_FAILURE_ERROR = "OptimisticLockingFailureError";

  String CLAIM_LOCK_FAILED_ERROR = "ClaimLockFailedError";

  String DOUBLE_SUBMIT_ERROR = "DoubleSubmitError";

  String FILE_NOT_FOUND_ERROR = "FileNotFoundError";

  String NO_DATA_FOUND_ERROR = "NoDataFoundError";

  String DATA_IN_USE_ERROR = "DataInUseError";

  String UNAUTHORIZED_ERROR = "UnauthorizedError";

  String ACCESS_DENIED_ERROR = "AccessDeniedError";

  String ACCOUNT_LOCKED_ERROR = "AccountLockedError";

  String ACCOUNT_DISABLED_ERROR = "AccountDisabledError";

  String UNEXPECTED_ERROR = "UnexpectedError";

  String MESSAGE_DELETED = "Deleted";

  String MESSAGE_SUCCESS = "Success";

  /** ---- View ---- * */
  String ERROR_VIEW = "error/500.html";

  String NOTFOUND_VIEW = "error/404.html";

  String FORBIDDEN_VIEW = "error/403.html";

  /** ---- ViewComponents ---- */
  String MAV_CONST = "Const";

  String MAV_ERRORS = "errors";

  String MAV_PULLDOWN_OPTION = "PulldownOption";

  String MAV_CODE_CATEGORIES = "CodeCategories";

  /** ---- URLs ---- */
  String HOME_URL = "/";

  String ERROR_URL = "/error";

  String LOGIN_URL = "/login";

  String RESET_PASSWORD_URL = "/resetPassword";

  String CHANGE_PASSWORD_URL = "/changePassword";

  String LOGOUT_URL = "/logout";

  String WEBJARS_URL = "/webjars/**";

  String STATIC_RESOURCES_URL = "/static/**";

  String ACTUATOR_URL = "/actuator/**";

  /** ---- Security ---- */
  String PERMIT_ALL = "permitAll";
}
