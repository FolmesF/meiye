package com.zhangmei.booking.common;

import lombok.Data;
import org.slf4j.MDC;

@Data
public class R<T> {

    private int code;
    private String message;
    private T data;
    private String traceId;

    public static <T> R<T> ok(T data) {
        R<T> result = new R<>();
        result.code = 0;
        result.message = "ok";
        result.data = data;
        result.traceId = MDC.get("traceId");
        return result;
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> result = new R<>();
        result.code = code;
        result.message = message;
        result.traceId = MDC.get("traceId");
        return result;
    }
}
