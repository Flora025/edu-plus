package com.edu.base.exception;

/**
 * 自定义异常类型 包装runtime exception
 */
public class EduPlusException extends RuntimeException {
    private String errMessage;

    public EduPlusException() {
        super();
    }

    public EduPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(CommonError commonError){
        throw new EduPlusException(commonError.getErrMessage());
    }
    public static void cast(String errMessage){
        throw new EduPlusException(errMessage);
    }

}
