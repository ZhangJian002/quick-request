package io.github.zjay.plugin.quickrequest.my;

import com.alibaba.fastjson.JSONObject;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhi;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;
import io.github.zjay.plugin.quickrequest.util.go.GoAnalyzeMethod;
import io.github.zjay.plugin.quickrequest.util.go.GoType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class AnalysisType {

    public static LinkedHashMap<String, Object> analysisType(Object contextlessResolveResult){
        LinkedHashMap<String, Object> targetMap = new LinkedHashMap<>();
        try {
            //获取SpecType
            Method getSpecType = contextlessResolveResult.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getSpecType));
            Object specType = getSpecType.invoke(contextlessResolveResult);
            //此处获取到最终参数的类型，struct 或者基本类型
            Method getType = specType.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getType));
            Object type = getType.invoke(specType);
            try {
                //struct类型再获取所有属性的声明对象
                Method getFieldDeclarationList = type.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getFieldDeclarationList));
                List fieldDeclarationList = (List)getFieldDeclarationList.invoke(type);
                for (Object fieldDeclaration : fieldDeclarationList) {
                    //获取当前属性列表，如Id,Age Int64 可得到Id、Age列表
                    Method getFieldDefinitionList = fieldDeclaration.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getFieldDefinitionList));
                    List fieldDefinitionList = (List)getFieldDefinitionList.invoke(fieldDeclaration);
                    if(!fieldDefinitionList.isEmpty()){
                        for (Object fieldDefinition : fieldDefinitionList) {
                            //获取参数的类型，此处为struct 或者基本类型
                            Method getTypeChild = fieldDeclaration.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getType));
                            PsiElement typeChild = (PsiElement)getTypeChild.invoke(fieldDeclaration);
                            if(TwoJinZhiGet.getRealStr(TwoJinZhi.MAP_TYPE).equals(typeChild.getNode().getElementType().toString())){
                                //MAP类型
                                HandlerMapType.handlerMapType(typeChild, targetMap, fieldDefinition);
                            } else if (TwoJinZhiGet.getRealStr(TwoJinZhi.ARRAY_OR_SLICE_TYPE).equals(typeChild.getNode().getElementType().toString())) {
                                //数组类型
                                HandlerArrayType.handlerArrayType(typeChild, targetMap, fieldDefinition);
                            } else {
                                HandlerObjectType.handlerObjectType(typeChild, targetMap, fieldDefinition, fieldDeclaration);
                            }
                        }
                    }else {
                        //*地址类型
                        HandlerExtendsType.handlerExtendsType(targetMap, fieldDeclaration);
                    }
                }
            }catch (Exception e){
                //基本类型处理
                HandlerBaseType.handlerBaseType(contextlessResolveResult, targetMap);
            }

        }catch (Exception e){

        }
        return targetMap;
    }


}
