package io.github.zjay.plugin.quickrequest.util.php;

import java.util.Objects;

public enum LaravelMethods {
    get("GET"),post("POST"),put("PUT"),delete("DELETE"),patch("PATCH");

    final private String type;

    LaravelMethods(String type) {
        this.type = type;
    }


    public static boolean isExist(String name){
        for (LaravelMethods value : values()) {
            if(Objects.equals(name.toLowerCase(), value.name().toLowerCase())){
                return true;
            }
        }
        return false;
    }

    public static String getMethodType(String name){
        for (LaravelMethods value : values()) {
            if(Objects.equals(name.toLowerCase(), value.name().toLowerCase())){
                return value.type;
            }
        }
        return "GET";
    }
}
