package ai.nvwa.components;

import lombok.EqualsAndHashCode;

/**
 * @description 基础异常
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@EqualsAndHashCode(callSuper = true)
public class BaseException extends RuntimeException {
    private static final long serialVersionUID = -3910324441458150872L;

    private int errorCode;
    private Object data;

    private BaseException() {}

    public BaseException(BaseError error, String message) {
        super(message);
        this.errorCode = error.getCode();
    }

    public BaseException(String message) {
        this(ErrorEnum.EXCEPTION, message);
    }

    public BaseException(int errorCode, String message, Object data) {
        this(message);
        this.errorCode = errorCode;
        this.data = data;
    }

    public BaseException(BaseError error, Object... args) {
        this(error, error.format(args));
    }

    public BaseException(Result<?> result) {
        this(result, result.getMessage());
    }

    public BaseException(Result<?> result, Object data) {
        this(result.getCode(), result.getMessage(), data);
    }

    public int getErrorCode() {
        return errorCode;
    }

    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) data;
    }

    public BaseException setData(Object data) {
        this.data = data;
        return this;
    }

}


