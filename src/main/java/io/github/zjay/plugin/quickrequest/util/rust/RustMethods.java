package io.github.zjay.plugin.quickrequest.util.rust;

import java.util.Objects;

public enum RustMethods {
    get("GET"),post("POST"),put("PUT"),delete("DELETE"),patch("PATCH"),route("GET");

    final private String type;

    RustMethods(String type) {
        this.type = type;
    }


    public static boolean isExist(String name){
        for (RustMethods value : values()) {
            if(Objects.equals(name.toLowerCase(), value.name().toLowerCase())){
                return true;
            }
        }
        return false;
    }

    public static String getMethodType(String name){
        for (RustMethods value : values()) {
            if(Objects.equals(name.toLowerCase(), value.name().toLowerCase())){
                return value.type;
            }
        }
        return "GET";
    }
}
