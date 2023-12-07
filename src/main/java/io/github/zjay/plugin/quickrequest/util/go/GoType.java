package io.github.zjay.plugin.quickrequest.util.go;

import io.github.zjay.plugin.quickrequest.util.StringUtils;

import java.util.Objects;
import java.util.Random;

public enum GoType {
    bool("bool"),rune("rune"),uintptr("uintptr"),string("string"),
    INT("int"),int8("int8"),int16("int16"),int32("int32"),int64("int64"),
    uint("uint"),uint8("uint8"),uint16("uint16"),uint32("uint32"),uint64("uint64"),
    float32("float32"),float64("float64"),complex64("complex64"),complex128("complex128"),
    ;

    private final String value;

    private static final Random random = new Random();

    GoType(String value) {
        this.value = value;
    }

    public static Object generate(String name){
        for (GoType value : values()) {
            if(Objects.equals(name, value.value)){
                if(Objects.equals(name, bool.value)){
                    return true;
                }else if(Objects.equals(name, string.value)){
                    return StringUtils.randomString("go","_",5,"name+random");
                }else{
                    return random.nextInt(100) + 1;
                }
            }
        }
        return 0;
    }
}
