package io.github.zjay.plugin.quickrequest.grapyQL;

public enum GraphQLTypeEnum {

    Query,
    Mutation,
    Subscription,
    ;

    public static boolean isExist(String name){
        for (GraphQLTypeEnum value : values()) {
            if(value.name().equals(name)){
                return true;
            }
        }
        return false;
    }

}
