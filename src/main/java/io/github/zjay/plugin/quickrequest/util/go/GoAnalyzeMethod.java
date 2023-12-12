package io.github.zjay.plugin.quickrequest.util.go;

import java.util.Objects;

public enum GoAnalyzeMethod {

    Bind,BindJSON,ShouldBind,ShouldBindJSON
    ;


    GoAnalyzeMethod() {
    }

    public static boolean isExist(String name){
        for (GoAnalyzeMethod value : values()) {
            if(Objects.equals(value.name(), name)){
                return true;
            }
        }
        return false;
    }

}
