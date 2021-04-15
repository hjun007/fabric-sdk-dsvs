package com.bjca.fabricsdkdsvsrest.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    private int code;
    private String message;
    private Object data;

    public static Response success(Object data){
        return success(null, data);
    }

    public static Response success(String message, Object data){
        return new Response(0, message, data);
    }

    public static Response fail(int code, String message, Object data){
        return new Response(code, message, data);
    }
}