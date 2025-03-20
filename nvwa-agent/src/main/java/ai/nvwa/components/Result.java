package ai.nvwa.components;

import lombok.Data;

import java.io.Serializable;

/**
 * 结果
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = -7850959321603528544L;

    private int code;
    private String message;
    private T data;

    public Result() {}

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Result(ErrorEnum error, T data) {
        this(error.getCode(), error.getMessage(), data);
    }

    public Result(ErrorEnum error, String message) {
        this(error.getCode(), message, null);
    }

    public Result(ErrorEnum error, Object... args) {
        this(error.getCode(), error.format(args), null);
    }

    public static <T> Result<T> build(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> success() {
        return new Result<>(ErrorEnum.SUCCESS);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorEnum.SUCCESS, data);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(ErrorEnum.FAIL, message);
    }

    public static <T> Result<T> fail(Result<?> result) {
        return new Result<>(result.getCode(), result.getMessage(), null);
    }

    public static <T> Result<T> fail(BaseError error, Object... args) {
        if (null == error) {
            return fail("none error info");
        }
        return new Result<>(error.getCode(), error.format(args), null);
    }

    public static <T> Result<T> fail(BaseException exp) {
        if (null == exp) {
            return fail("none exception info");
        }
        return new Result<>(exp.getErrorCode(), exp.getMessage(), null);
    }

    public boolean isSuccessful() {
        return ErrorEnum.SUCCESS.getCode() == this.code;
    }

}


