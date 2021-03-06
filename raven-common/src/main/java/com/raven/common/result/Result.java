package com.raven.common.result;

import java.io.Serializable;
import lombok.Data;

/**
 * Author zxx
 * Description 通用接口返回
 * Date Created on 2018/6/12
 */
@Data
public class Result implements Serializable {

    private static final long serialVersionUID = 5679982684662864356L;

    private Integer code;

    private String msg;

    private Object data;

    private Result() {
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static Result success() {
        Result result = new Result();
        result.setResultCode(ResultCode.COMMON_SUCCESS);
        return result;
    }

    public static Result success(Object data) {
        Result result = new Result();
        result.setResultCode(ResultCode.COMMON_SUCCESS);
        result.setData(data);
        return result;
    }

    public static Result failure(ResultCode resultCode) {
        Result result = new Result();
        result.setResultCode(resultCode);
        result.setMsg(resultCode.getMsg());
        return result;
    }

    public static Result failure(ResultCode resultCode, String msg) {
        Result result = new Result();
        result.setResultCode(resultCode);
        result.setMsg(msg);
        return result;
    }

    private void setResultCode(ResultCode code) {
        this.code = code.getCode();
        this.msg = code.getMsg();
    }
}
