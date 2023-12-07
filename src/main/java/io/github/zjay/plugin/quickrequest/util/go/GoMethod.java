package io.github.zjay.plugin.quickrequest.util.go;

import java.util.Objects;

public enum GoMethod {

    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH"),
    PostForm("POST"),
    HandleFunc("GET"),
    Handle("GET"),
    ;

    final String type;

    GoMethod(String type) {
        this.type = type;
    }

    public static boolean isExist(String name){
        for (GoMethod value : values()) {
            if(Objects.equals(value.name(), name)){
                return true;
            }
        }
        return false;
    }

    public static String getMethodType(String name){
        for (GoMethod value : values()) {
            if(Objects.equals(value.name(), name)){
                return value.type;
            }
        }
        return "GET";
    }

}
