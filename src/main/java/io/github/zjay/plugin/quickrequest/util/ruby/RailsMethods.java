package io.github.zjay.plugin.quickrequest.util.ruby;

import java.util.Objects;

public enum RailsMethods {
    get("GET"),post("POST"),put("PUT"),delete("DELETE"),patch("PATCH"),;

    final private String type;

    RailsMethods(String type) {
        this.type = type;
    }


    public static boolean isExist(String name){
        for (RailsMethods value : values()) {
            if(Objects.equals(name, value.name())){
                return true;
            }
        }
        return false;
    }

    public static String getMethodType(String name){
        for (RailsMethods value : values()) {
            if(Objects.equals(name, value.name())){
                return value.type;
            }
        }
        return "GET";
    }
}
