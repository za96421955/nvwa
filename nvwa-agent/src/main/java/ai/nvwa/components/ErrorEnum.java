package ai.nvwa.components;

/**
 * @description 错误码
 * <p> <功能详细描述> </p>
 */
public enum ErrorEnum implements BaseError {
    SUCCESS(200, "成功"),
    FAIL(500, "失败"),
    EXCEPTION(9999, "系统异常"),

    XXXXXX(-1, "XXXXXX"),

    ;

    private final int code;
    private final String message;

    ErrorEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String format(Object... args) {
        return String.format(this.getMessage(), args);
    }

    @Override
    public BaseError getByCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BaseError e : values()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        return null;
    }

}


