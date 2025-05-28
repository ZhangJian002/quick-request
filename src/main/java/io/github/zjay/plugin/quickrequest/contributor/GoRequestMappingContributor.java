/*
 * Copyright 2021 zjay(darzjay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.zjay.plugin.quickrequest.contributor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.GoTwoJinZhi;
import io.github.zjay.plugin.quickrequest.util.ReflectUtils;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;
import io.github.zjay.plugin.quickrequest.util.file.FileUtil;
import io.github.zjay.plugin.quickrequest.util.go.GoMethod;

import java.util.*;

public class GoRequestMappingContributor extends OtherRequestMappingByNameContributor{

    private static Set<PsiElement> goFunctionDeclarations = new HashSet<>();


    @Override
    List<OtherRequestEntity> getPsiElementSearchers(Project project) {
        return getResultList(project);
    }

    public static List<OtherRequestEntity> getResultList(Project project){
        List<OtherRequestEntity> resultList = new ArrayList<>();
        try {
            Class.forName("com.goide.stubs.index.GoFunctionIndex");
            StubIndexKey<String, PsiElement> key = (StubIndexKey<String, PsiElement>)ReflectUtils.getStaticFieldValue("com.goide.stubs.index.GoFunctionIndex", "KEY");
            Class<PsiElement> goFunctionDeclaration = (Class<PsiElement>)Class.forName("com.goide.psi.GoFunctionDeclaration");
            Collection<PsiElement> collection = StubIndex.getElements(key, TwoJinZhiGet.getRealStr(Constant.MAIN), project, GlobalSearchScope.projectScope(project), goFunctionDeclaration);
            analyzeFunc(collection, resultList, project);
            goFunctionDeclarations.clear();
        }catch (Exception e){

        }
        return resultList;
    }

    private static void analyzeFunc(Collection<PsiElement> collection, List<OtherRequestEntity> resultList, Project project) {
        for (PsiElement element : collection) {
            VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
            if(!FileUtil.isProjectFile(project, virtualFile)){
                continue;
            }

            String text = element.getText();
            if (!text.contains("gin.Default()") && !text.contains("gin.Engine")) {
                continue;
            }
            goFunctionDeclarations.add(element);
            PsiElement block = (PsiElement)ReflectUtils.invokeMethod(element, "getBlock");
            List<PsiElement> statementList = (List<PsiElement>)ReflectUtils.invokeMethod(block, "getStatementList");
            for (PsiElement goStatement : statementList) {
                try {
                    PsiElement firstChild = goStatement.getFirstChild().getFirstChild();
                    judgeCallExpr(firstChild, goStatement, resultList, project);
                }catch (Exception e){

                }
            }
        }
    }

    private static void judgeCallExpr(PsiElement firstChild, PsiElement goStatement, List<OtherRequestEntity> resultList, Project project) {
        if (TwoJinZhiGet.getRealStr(GoTwoJinZhi.CALL_EXPR).equals((firstChild.getNode().getElementType().toString()))) {
            if (GoMethod.isExist(( firstChild.getFirstChild().getLastChild()).getText())) {
                PsiElement firstChild1 = firstChild.getFirstChild().getFirstChild();
                PsiElement resolve1 = (PsiElement)ReflectUtils.invokeMethod(firstChild1, "resolve");
                String canonicalName = resolve1.getClass().getCanonicalName();
                if(Objects.equals(canonicalName, "com.goide.psi.GoVarDefinition") || Objects.equals(canonicalName, "com.goide.psi.impl.GoParamDefinitionImpl")){
                    Object getGoTypeInner = ReflectUtils.invokeMethod(resolve1, "getGoTypeInner", ResolveState.class, ResolveState.initial());
                    String text = (String)ReflectUtils.invokeMethod(getGoTypeInner, "getText");
                    if(judgeGin(text)){
                        PsiElement methodPsi = firstChild.getFirstChild().getLastChild();
                        resultList.add(new OtherRequestEntity(methodPsi, getUrl(firstChild), methodPsi.getText()));
                    }
                }
            }else {
                //是否需要再加 .getFirstChild() 取决于有没有 xx.调用
                PsiElement firstChild1 = firstChild.getFirstChild();
                PsiElement resolve = (PsiElement)ReflectUtils.invokeMethod(firstChild1, "resolve");
                if(resolve == null){
                    return;
                }
                if(!goFunctionDeclarations.contains(resolve)){
                    analyzeFunc(Arrays.asList(resolve), resultList, project);
                }
            }
        }else if(Objects.equals(firstChild.getClass().getCanonicalName(), "com.goide.psi.GoVarDefinition")){
            Object getGoTypeInner = ReflectUtils.invokeMethod(firstChild, "getGoTypeInner", ResolveState.class, ResolveState.initial());
            String text = (String)ReflectUtils.invokeMethod(getGoTypeInner, "getText");
            if(judgeGin(text)){
                //就是
                PsiElement lastChild = goStatement.getFirstChild().getLastChild();
                if (!Objects.equals(TwoJinZhiGet.getRealStr(GoTwoJinZhi.GIN_Default), lastChild.getText())) {
                    judgeCallExpr(lastChild, goStatement, resultList, project);
                }
            }
        }
    }

    public static String getUrl(PsiElement firstChild) {
        PsiElement argumentList = (PsiElement)ReflectUtils.invokeMethod(firstChild, "getArgumentList");
        List<PsiElement> getExpressionList = (List<PsiElement>)ReflectUtils.invokeMethod(argumentList, "getExpressionList");
        PsiElement goExpression1 = getExpressionList.get(0);
        Object value = ReflectUtils.invokeMethod(goExpression1, "getValue");
        return (String)ReflectUtils.invokeMethod(value, "getString");
    }

    private static boolean judgeGin(String target){
        return Objects.equals(TwoJinZhiGet.getRealStr(GoTwoJinZhi.Engine), target) || Objects.equals(TwoJinZhiGet.getRealStr(GoTwoJinZhi.GIN_ENGINE), target);
    }


}
