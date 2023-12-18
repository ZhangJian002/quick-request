package io.github.zjay.plugin.quickrequest.analysis.go;

import io.github.zjay.plugin.quickrequest.util.GoTwoJinZhi;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;

public class HandlerExtendsType {

    public static void handlerExtendsType(LinkedHashMap<String, Object> targetMap, Object fieldDeclaration) {
        try {
            Method getAnonymousFieldDefinition = fieldDeclaration.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.getAnonymousFieldDefinition));
            Object anonymousFieldDefinition = getAnonymousFieldDefinition.invoke(fieldDeclaration);
            if(anonymousFieldDefinition != null){
                Method getTypeForAnonymous = anonymousFieldDefinition.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.getType));
                Object invoke = getTypeForAnonymous.invoke(anonymousFieldDefinition);
                Method dd = invoke.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.getType));
                Object invoke1 = dd.invoke(invoke);
                Method contextlessResolveValue = invoke1.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.contextlessResolve));
                Object contextlessResolveValueResult = contextlessResolveValue.invoke(invoke1);
                targetMap.putAll(AnalysisType.analysisType(contextlessResolveValueResult));
            }
        }catch (Exception e){

        }
    }
}
