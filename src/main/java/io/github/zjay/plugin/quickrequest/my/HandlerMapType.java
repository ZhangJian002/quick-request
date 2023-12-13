package io.github.zjay.plugin.quickrequest.my;

import com.alibaba.fastjson.JSONObject;
import com.intellij.psi.PsiElement;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhi;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;

public class HandlerMapType {

    public static void handlerMapType(PsiElement typeChild, LinkedHashMap<String, Object> targetMap, Object fieldDefinition) {
        try {
            //map类型
            Method getKeyType = typeChild.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getKeyType));
            Object keyType = getKeyType.invoke(typeChild);
            Method contextlessResolveChild = keyType.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.contextlessResolve));
            Object contextlessResolveChildResult = contextlessResolveChild.invoke(keyType);
            LinkedHashMap<String, Object> mapKeyLinkedHashMap = AnalysisType.analysisType(contextlessResolveChildResult);
            Object key = handleMapReturn(mapKeyLinkedHashMap);
            Method getValueType = typeChild.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getValueType));
            Object valueType = getValueType.invoke(typeChild);
            Method contextlessResolveValue = valueType.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.contextlessResolve));
            Object contextlessResolveValueResult = contextlessResolveValue.invoke(valueType);
            LinkedHashMap<String, Object> mapValueLinkedHashMap = AnalysisType.analysisType(contextlessResolveValueResult);
            Object value = handleMapReturn(mapValueLinkedHashMap);
            LinkedHashMap<String, Object> tempMap = new LinkedHashMap<>();
            tempMap.put(JSONObject.toJSONString(key), value);
            Method getName = fieldDefinition.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getName));
            targetMap.put(getName.invoke(fieldDefinition).toString(), tempMap);
        }catch (Exception e){

        }
    }

    public static Object handleMapReturn(LinkedHashMap<String, Object> mapKeyLinkedHashMap) {
        if(mapKeyLinkedHashMap.size() == 1){
            Object result = mapKeyLinkedHashMap.get("");
            if(result != null){
                return result;
            }
        }
        return mapKeyLinkedHashMap;
    }
}
