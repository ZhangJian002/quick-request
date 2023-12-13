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

public class AnalysisPsi {
    public static LinkedHashMap<String, Object> analysisPsi(PsiElement blockCode){
        try {
            //获取陈述list
            Method getStatementList = blockCode.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getStatementList));
            List statementList = (List)getStatementList.invoke(blockCode);
            //遍历
            for (Object statement : statementList) {
                try {
                    //获取当前陈述的左手表达式
                    Method getLeftHandExprList = statement.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getLeftHandExprList));
                    Object leftHandExprList = getLeftHandExprList.invoke(statement);
                    //获取所有的孩子 即：所有的语句块
                    Method getChildren = leftHandExprList.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getChildren));
                    PsiElement[] children = (PsiElement[])getChildren.invoke(leftHandExprList);
                    for (PsiElement psiElement : children) {
                        //getLastChild得到参数对象，从参数第一个(开始便利
                        PsiElement firstChild = psiElement.getLastChild().getFirstChild();
                        for (PsiElement temp = firstChild; temp.getNextSibling() != null;temp=temp.getNextSibling()){
                            //解析&表达式对象
                            if(TwoJinZhiGet.getRealStr(TwoJinZhi.UNARY_EXPR).equals((temp.getNode().getElementType().toString())) &&
                                    GoAnalyzeMethod.isExist(psiElement.getFirstChild().getLastChild().getText())){
                                //解析一下得到目标对象
                                Method resolveChild = temp.getLastChild().getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.resolve));
                                Object resolveChildResult = resolveChild.invoke(temp.getLastChild());
                                //获取目标对象的Go类型
                                Method getGoTypeInner = resolveChildResult.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getGoTypeInner), ResolveState.class);
                                Object goTypeInner = getGoTypeInner.invoke(resolveChildResult, ResolveState.initial());
                                //获取Go类型的解析上下文
                                Method contextlessResolve = goTypeInner.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.contextlessResolve));
                                Object contextlessResolveResult = contextlessResolve.invoke(goTypeInner);
                                return AnalysisType.analysisType(contextlessResolveResult);
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
}
