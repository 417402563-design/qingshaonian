package cn.edu.sdua.qingshaonian.dto;

/**
 * 业务异常，由全局异常处理器统一转换为 Result
 */
public class ApiException extends RuntimeException {

    private final int code;

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ApiException(String message) {
        this(1, message);
    }

    public int getCode() {
        return code;
    }
}
