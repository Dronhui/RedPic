package org.example.redpic.exception;

public class BizException extends RuntimeException {
    private final Integer code;
    public BizException(String message){
        super(message);
        this.code=400;
    }
    public BizException(Integer code, String message){
        super(message);
        this.code=code;
    }
    public Integer getCode(){
        return code;
    }

}
