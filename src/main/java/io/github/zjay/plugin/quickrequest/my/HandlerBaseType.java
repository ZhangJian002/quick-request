package io.github.zjay.plugin.quickrequest.my;

import com.intellij.psi.PsiElement;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhi;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;
import io.github.zjay.plugin.quickrequest.util.go.GoType;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;

public class HandlerBaseType {

    public static void handlerBaseType(Object contextlessResolveResult, LinkedHashMap<String, Object> targetMap) {
        try {
            //基本类型
            Method getIdentifier = contextlessResolveResult.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getIdentifier));
            PsiElement goRealType = (PsiElement)getIdentifier.invoke(contextlessResolveResult);
            Object generate = GoType.generate(goRealType.getText());
            targetMap.put("", generate);
        }catch (Exception e1){

        }
    }
}
