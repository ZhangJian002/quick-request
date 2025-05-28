package io.github.zjay.plugin.quickrequest.util;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtils {

    public static Object getParentField(Object target, String fieldName){
        try {
            Field field = getField(target.getClass(), fieldName);
            return field.get(target);
        }catch (Exception e){

        }
        return null;
    }

    static Field getField(Class<?> aClass, String fieldName){
        try {
            Field field = aClass.getDeclaredField(fieldName);
            //可能异常
            field.setAccessible(true);
            return field;
        }catch (Exception e){
            Class<?> superclass = aClass.getSuperclass();
            if (superclass != null){
                return getField(superclass, fieldName);
            }
            return null;
        }
    }

    public static Object invokeMethod(Object target, String method){
        try {
            Method targetMethod = target.getClass().getMethod(method);
            targetMethod.setAccessible(true);
            return targetMethod.invoke(target);
        }catch (Exception e){
        }
        return null;
    }

    public static Object invokeMethod(Object target, String method, Class<?> clz, Object... params){
        try {
            Method targetMethod = target.getClass().getMethod(method, clz);
            targetMethod.setAccessible(true);
            return targetMethod.invoke(target, params);
        }catch (Exception e){
        }
        return null;
    }

    public static Object invokeStaticMethod(Class <?> target, String method, Class<?> clz, Object... params){
        try {
            Method targetMethod = target.getDeclaredMethod(method, clz);
            targetMethod.setAccessible(true);
            return targetMethod.invoke(null, params);
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

    public static Object getStaticFieldValue(String className,  String fieldName) {
        try {
            // 动态加载类
            Class<?> clazz = Class.forName(className);
            // 获取静态变量字段
            Field staticField = clazz.getDeclaredField(fieldName);

            // 设置访问权限（针对非公有字段）
            staticField.setAccessible(true);
            // 读取值
            return staticField.get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
