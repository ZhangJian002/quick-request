package io.github.zjay.plugin.quickrequest.util.python;

import java.util.Objects;

public enum FlaskMethods {
    get("GET"),post("POST"),put("PUT"),delete("DELETE"),patch("PATCH"),route("GET");

    final private String type;

    FlaskMethods(String type) {
        this.type = type;
    }


    public static boolean isExist(String name){
        for (FlaskMethods value : values()) {
            if(Objects.equals(name, value.name())){
                return true;
            }
        }
        return false;
    }

    public static String getMethodType(String name){
        for (FlaskMethods value : values()) {
            if(Objects.equals(name, value.name())){
                return value.type;
            }
        }
        return "GET";
    }
}
