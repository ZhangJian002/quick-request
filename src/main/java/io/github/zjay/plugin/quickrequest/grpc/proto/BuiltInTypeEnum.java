package io.github.zjay.plugin.quickrequest.grpc.proto;

import com.intellij.openapi.util.NlsSafe;
import io.github.zjay.plugin.quickrequest.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

public enum BuiltInTypeEnum {

    STRING,
    BYTES,
    BOOL,
    DOUBLE,
    FLOAT,
    UINT32,
    UINT64,
    FIXED32,
    FIXED64,
    INT32,
    INT64,
    SINT32,
    SINT64,
    SFIXED32,
    SFIXED64;

    private static final Random random = new Random();
    public static boolean exist(final Object value) {
        if (value == null) {
            return false;
        }
        for (BuiltInTypeEnum builtInTypeEnum : values()) {
            if (builtInTypeEnum.name().equalsIgnoreCase(value.toString())) {
                return true;
            }
        }
        return false;
    }

    public static BuiltInTypeEnum getBuiltInTypeEnum(final Object value) {
        if (value == null) {
            return null;
        }
        for (BuiltInTypeEnum builtInTypeEnum : values()) {
            if (builtInTypeEnum.name().equalsIgnoreCase(value.toString())) {
                return builtInTypeEnum;
            }
        }
        return null;
    }

    public static Object getRandomValue(Object name, @NlsSafe String text){
        BuiltInTypeEnum builtInTypeEnum = getBuiltInTypeEnum(name);
        switch (builtInTypeEnum){
            case STRING:
                return StringUtils.randomString(text,"_",5,"name+random");
            case BYTES:
                return Base64.getEncoder().encodeToString(StringUtils.randomString(text,"_",5,"name+random").getBytes(StandardCharsets.UTF_8));
            case BOOL:
                return true;
            default:
                return random.nextInt(100) + 1;

        }

    }

}
