package io.github.zjay.plugin.quickrequest.my;

import com.alibaba.fastjson.JSONObject;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import io.github.zjay.plugin.quickrequest.util.go.GoAnalyzeMethod;
import io.github.zjay.plugin.quickrequest.util.go.GoType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class AnalysisUtils {
    public static LinkedHashMap<String, Object> analysisPsi(PsiElement blockCode){
        try {
            //获取陈述list
            Method getStatementList = blockCode.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getStatementList));
            List statementList = (List)getStatementList.invoke(blockCode);
            //遍历
            for (Object statement : statementList) {
                try {
                    //获取当前陈述的左手表达式
                    Method getLeftHandExprList = statement.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getLeftHandExprList));
                    Object leftHandExprList = getLeftHandExprList.invoke(statement);
                    //获取所有的孩子 即：所有的语句块
                    Method getChildren = leftHandExprList.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getChildren));
                    PsiElement[] children = (PsiElement[])getChildren.invoke(leftHandExprList);
                    for (PsiElement psiElement : children) {
                        //getLastChild得到参数对象，从参数第一个(开始便利
                        PsiElement firstChild = psiElement.getLastChild().getFirstChild();
                        for (PsiElement temp = firstChild; temp.getNextSibling() != null;temp=temp.getNextSibling()){
                            //解析&表达式对象
                            if(TwoJinZhi.getRealStr(TwoJinZhi.UNARY_EXPR).equals((temp.getNode().getElementType().toString())) &&
                                    GoAnalyzeMethod.isExist(psiElement.getFirstChild().getLastChild().getText())){
                                //解析一下得到目标对象
                                Method resolveChild = temp.getLastChild().getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.resolve));
                                Object resolveChildResult = resolveChild.invoke(temp.getLastChild());
                                //获取目标对象的Go类型
                                Method getGoTypeInner = resolveChildResult.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getGoTypeInner), ResolveState.class);
                                Object goTypeInner = getGoTypeInner.invoke(resolveChildResult, ResolveState.initial());
                                //获取Go类型的解析上下文
                                Method contextlessResolve = goTypeInner.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.contextlessResolve));
                                Object contextlessResolveResult = contextlessResolve.invoke(goTypeInner);
                                return analysisType(contextlessResolveResult);
                            }
                        }
                    }
                }catch (Exception e){

                }
            }
        }catch (Exception e){

        }
        return null;
    }

    private static LinkedHashMap<String, Object> analysisType(Object contextlessResolveResult){
        LinkedHashMap<String, Object> targetMap = new LinkedHashMap<>();
        try {
            //获取SpecType
            Method getSpecType = contextlessResolveResult.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getSpecType));
            Object specType = getSpecType.invoke(contextlessResolveResult);
            //此处获取到最终参数的类型，struct 或者基本类型
            Method getType = specType.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getType));
            Object type = getType.invoke(specType);
            try {
                //struct类型再获取所有属性的声明对象
                Method getFieldDeclarationList = type.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getFieldDeclarationList));
                List fieldDeclarationList = (List)getFieldDeclarationList.invoke(type);
                for (Object fieldDeclaration : fieldDeclarationList) {
                    //获取当前属性列表，如Id,Age Int64 可得到Id、Age列表
                    Method getFieldDefinitionList = fieldDeclaration.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getFieldDefinitionList));
                    List fieldDefinitionList = (List)getFieldDefinitionList.invoke(fieldDeclaration);
                    if(!fieldDefinitionList.isEmpty()){
                        for (Object fieldDefinition : fieldDefinitionList) {
                            //获取参数的类型，此处为struct 或者基本类型
                            Method getTypeChild = fieldDeclaration.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getType));
                            PsiElement typeChild = (PsiElement)getTypeChild.invoke(fieldDeclaration);
                            if(TwoJinZhi.getRealStr(TwoJinZhi.MAP_TYPE).equals(typeChild.getNode().getElementType().toString())){
                                //MAP类型
                                handlerMapType(typeChild, targetMap, fieldDefinition);
                            } else if (TwoJinZhi.getRealStr(TwoJinZhi.ARRAY_OR_SLICE_TYPE).equals(typeChild.getNode().getElementType().toString())) {
                                //数组类型
                                handlerArrayType(typeChild, targetMap, fieldDefinition);
                            } else {
                                handlerObjectType(typeChild, targetMap, fieldDefinition, fieldDeclaration);
                            }
                        }
                    }else {
                        //*地址类型
                        handlerExtendsType(targetMap, fieldDeclaration);
                    }
                }
            }catch (Exception e){
                //基本类型处理
                handlerBaseType(contextlessResolveResult, targetMap);
            }

        }catch (Exception e){

        }
        return targetMap;
    }

    private static void handlerBaseType(Object contextlessResolveResult, LinkedHashMap<String, Object> targetMap) {
        try {
            //基本类型
            Method getIdentifier = contextlessResolveResult.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getIdentifier));
            PsiElement goRealType = (PsiElement)getIdentifier.invoke(contextlessResolveResult);
            Object generate = GoType.generate(goRealType.getText());
            targetMap.put("", generate);
        }catch (Exception e1){

        }
    }

    private static void handlerExtendsType(LinkedHashMap<String, Object> targetMap, Object fieldDeclaration) {
        try {
            Method getAnonymousFieldDefinition = fieldDeclaration.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getAnonymousFieldDefinition));
            Object anonymousFieldDefinition = getAnonymousFieldDefinition.invoke(fieldDeclaration);
            if(anonymousFieldDefinition != null){
                Method getTypeForAnonymous = anonymousFieldDefinition.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getType));
                Object invoke = getTypeForAnonymous.invoke(anonymousFieldDefinition);
                Method dd = invoke.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getType));
                Object invoke1 = dd.invoke(invoke);
                Method contextlessResolveValue = invoke1.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.contextlessResolve));
                Object contextlessResolveValueResult = contextlessResolveValue.invoke(invoke1);
                targetMap.putAll(analysisType(contextlessResolveValueResult));
            }
        }catch (Exception e){

        }
    }

    private static void handlerObjectType(PsiElement typeChild, LinkedHashMap<String, Object> targetMap, Object fieldDefinition, Object fieldDeclaration) {
        try {
            //解析这个类型，看到底是什么
            Method contextlessResolveChild = typeChild.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.contextlessResolve));
            Object contextlessResolveChildResult = contextlessResolveChild.invoke(typeChild);
            //获取SpecType
            Method getIdentifier = contextlessResolveChildResult.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getIdentifier));
            PsiElement goRealType = (PsiElement)getIdentifier.invoke(contextlessResolveChildResult);
            Object generate = GoType.generate(goRealType.getText());
            if(!Objects.equals(generate ,0)){
                //获取当前属性声明的tag字符串，如：`json:"id" db:"id"`
                Method getTag = fieldDeclaration.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getTag));
                Object tag = getTag.invoke(fieldDeclaration);
                if(tag != null){
                    Method getValue = tag.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getValue), String.class);
                    Object invoke = getValue.invoke(tag, TwoJinZhi.getRealStr(TwoJinZhi.json));
                    targetMap.put(invoke.toString(), generate);
                }else {
                    Method getName = fieldDefinition.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getName));
                    String string = getName.invoke(fieldDefinition).toString();
                    targetMap.put(string, generate);
                }
            }else {
                //不是默认类型，走
                Method getName = fieldDefinition.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getName));
                targetMap.put(getName.invoke(fieldDefinition).toString(), analysisType(contextlessResolveChildResult));
            }
        }catch (Exception e){

        }
    }

    private static void handlerArrayType(PsiElement typeChild, LinkedHashMap<String, Object> targetMap, Object fieldDefinition) {
        try {
            Method getLength = typeChild.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getLength));
            Object length = getLength.invoke(typeChild);

            Method getArrType = typeChild.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getType));
            Object arrType = getArrType.invoke(typeChild);
            Method contextlessResolveValue = arrType.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.contextlessResolve));
            Object contextlessResolveValueResult = contextlessResolveValue.invoke(arrType);
            Method getName = fieldDefinition.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getName));

            LinkedHashMap<String, Object> stringObjectLinkedHashMap = analysisType(contextlessResolveValueResult);
            Object value = handleMapReturn(stringObjectLinkedHashMap);
            List<Object> list = Arrays.asList(value);
            for (int i = 0; i < Integer.parseInt(length.toString()) - 1; i++){
                LinkedHashMap<String, Object> stringObjectLinkedHashMapChild = analysisType(contextlessResolveValueResult);
                Object childValue = handleMapReturn(stringObjectLinkedHashMapChild);
                list.add(childValue);
            }
            targetMap.put(getName.invoke(fieldDefinition).toString(), list);
        }catch (Exception e){

        }
    }

    private static void handlerMapType(PsiElement typeChild, LinkedHashMap<String, Object> targetMap, Object fieldDefinition) {
        try {
            //map类型
            Method getKeyType = typeChild.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getKeyType));
            Object keyType = getKeyType.invoke(typeChild);
            Method contextlessResolveChild = keyType.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.contextlessResolve));
            Object contextlessResolveChildResult = contextlessResolveChild.invoke(keyType);
            LinkedHashMap<String, Object> mapKeyLinkedHashMap = analysisType(contextlessResolveChildResult);
            Object key = handleMapReturn(mapKeyLinkedHashMap);
            Method getValueType = typeChild.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getValueType));
            Object valueType = getValueType.invoke(typeChild);
            Method contextlessResolveValue = valueType.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.contextlessResolve));
            Object contextlessResolveValueResult = contextlessResolveValue.invoke(valueType);
            LinkedHashMap<String, Object> mapValueLinkedHashMap = analysisType(contextlessResolveValueResult);
            Object value = handleMapReturn(mapValueLinkedHashMap);
            LinkedHashMap<String, Object> tempMap = new LinkedHashMap<>();
            tempMap.put(JSONObject.toJSONString(key), value);
            Method getName = fieldDefinition.getClass().getMethod(TwoJinZhi.getRealStr(TwoJinZhi.getName));
            targetMap.put(getName.invoke(fieldDefinition).toString(), tempMap);
        }catch (Exception e){

        }
    }

    private static Object handleMapReturn(LinkedHashMap<String, Object> mapKeyLinkedHashMap) {
        if(mapKeyLinkedHashMap.size() == 1){
            Object result = mapKeyLinkedHashMap.get("");
            if(result != null){
                return result;
            }
        }
        return mapKeyLinkedHashMap;
    }
}
