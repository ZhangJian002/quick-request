package io.github.zjay.plugin.quickrequest.util;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ReflectUtils {

    public static Object invokeMethod(Object target, String method){
        try {
            Method targetMethod = target.getClass().getMethod(method);
            targetMethod.setAccessible(true);
            return targetMethod.invoke(target);
        }catch (Exception e){
        }
        return null;
    }

    public static Object getInstance(String classAllName){
        try {
            Class<?> aClass = Class.forName(classAllName);
            Constructor<?> declaredConstructor = aClass.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            return declaredConstructor.newInstance();
        }catch (Exception e){

        }
        return null;
    }

}
