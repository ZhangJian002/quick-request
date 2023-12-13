package io.github.zjay.plugin.quickrequest.my;

import com.intellij.psi.PsiElement;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhi;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class HandlerArrayType {

    public static void handlerArrayType(PsiElement typeChild, LinkedHashMap<String, Object> targetMap, Object fieldDefinition) {
        try {
            Method getLength = typeChild.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getLength));
            Object length = getLength.invoke(typeChild);

            Method getArrType = typeChild.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getType));
            Object arrType = getArrType.invoke(typeChild);
            Method contextlessResolveValue = arrType.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.contextlessResolve));
            Object contextlessResolveValueResult = contextlessResolveValue.invoke(arrType);
            Method getName = fieldDefinition.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getName));

            LinkedHashMap<String, Object> stringObjectLinkedHashMap = AnalysisType.analysisType(contextlessResolveValueResult);
            Object value = HandlerMapType.handleMapReturn(stringObjectLinkedHashMap);
            List<Object> list = Arrays.asList(value);
            for (int i = 0; i < Integer.parseInt(length.toString()) - 1; i++){
                LinkedHashMap<String, Object> stringObjectLinkedHashMapChild = AnalysisType.analysisType(contextlessResolveValueResult);
                Object childValue = HandlerMapType.handleMapReturn(stringObjectLinkedHashMapChild);
                list.add(childValue);
            }
            targetMap.put(getName.invoke(fieldDefinition).toString(), list);
        }catch (Exception e){

        }
    }
}
