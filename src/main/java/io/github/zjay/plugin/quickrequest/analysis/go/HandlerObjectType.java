package io.github.zjay.plugin.quickrequest.analysis.go;

import com.intellij.psi.PsiElement;
import io.github.zjay.plugin.quickrequest.util.GoTwoJinZhi;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;
import io.github.zjay.plugin.quickrequest.util.go.GoType;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Objects;

public class HandlerObjectType {

    public static void handlerObjectType(PsiElement typeChild, LinkedHashMap<String, Object> targetMap, Object fieldDefinition, Object fieldDeclaration) {
        try {
            //解析这个类型，看到底是什么
            Method contextlessResolveChild = typeChild.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.contextlessResolve));
            Object contextlessResolveChildResult = contextlessResolveChild.invoke(typeChild);
            //获取SpecType
            Method getIdentifier = contextlessResolveChildResult.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.getIdentifier));
            PsiElement goRealType = (PsiElement)getIdentifier.invoke(contextlessResolveChildResult);
            Object generate = GoType.generate(goRealType.getText());
            if(!Objects.equals(generate ,0)){
                //获取当前属性声明的tag字符串，如：`json:"id" db:"id"`
                Method getTag = fieldDeclaration.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.getTag));
                Object tag = getTag.invoke(fieldDeclaration);
                if(tag != null){
                    Method getValue = tag.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.getValue), String.class);
                    Object invoke = getValue.invoke(tag, TwoJinZhiGet.getRealStr(GoTwoJinZhi.json));
                    targetMap.put(invoke.toString(), generate);
                }else {
                    Method getName = fieldDefinition.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.getName));
                    String string = getName.invoke(fieldDefinition).toString();
                    targetMap.put(string, generate);
                }
            }else {
                //不是默认类型，走
                Method getName = fieldDefinition.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.getName));
                targetMap.put(getName.invoke(fieldDefinition).toString(), AnalysisType.analysisType(contextlessResolveChildResult));
            }
        }catch (Exception e){

        }
    }
}
