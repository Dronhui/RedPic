package org.example.redpic.common;
public record Result<T>(
    Integer code,
    String message,
    T data
            ){
    public static<T> Result<T> success(T data){
        return new Result<T>(200,"success",data);

    }
    public static<T> Result<T> error(Integer code,String message){
        return new Result<T>(code,message,null);

    }
}
