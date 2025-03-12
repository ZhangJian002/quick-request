package io.github.zjay.plugin.quickrequest.grapyQL;

public enum GraphQLDataTypeEnum {

    ID,
    String,
    Boolean,
    Float,
    Int,
    ;

    public static boolean isExist(String name){
        for (GraphQLDataTypeEnum value : values()) {
            if(value.name().equals(name)){
                return true;
            }
        }
        return false;
    }

}
