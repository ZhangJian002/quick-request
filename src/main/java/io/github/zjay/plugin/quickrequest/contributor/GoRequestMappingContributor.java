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

import com.goide.psi.*;
import com.goide.psi.impl.*;
import com.goide.stubs.index.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.GoTwoJinZhi;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;
import io.github.zjay.plugin.quickrequest.util.go.GoMethod;

import java.util.*;

public class GoRequestMappingContributor extends OtherRequestMappingByNameContributor{


    private static Set<GoFunctionDeclaration> goFunctionDeclarations = new HashSet<>();
    @Override
    List<OtherRequestEntity> getPsiElementSearchers(Project project) {
        return getResultList(project);
    }

    public static List<OtherRequestEntity> getResultList(Project project){
        List<OtherRequestEntity> resultList = new LinkedList<>();
        try {
            Class.forName("com.goide.stubs.index.GoFunctionIndex");
            Collection<GoFunctionDeclaration> collection = StubIndex.getElements(GoFunctionIndex.KEY, TwoJinZhiGet.getRealStr(Constant.MAIN), project, GlobalSearchScope.projectScope(project), GoFunctionDeclaration.class);
            analyzeFunc(collection, resultList);
            goFunctionDeclarations.clear();
        }catch (Exception e){

        }
        return resultList;
    }

    private static void analyzeFunc(Collection<GoFunctionDeclaration> collection, List<OtherRequestEntity> resultList) {
        goFunctionDeclarations.addAll(collection);
        for (GoFunctionDeclaration element : collection) {
            GoBlock block = element.getBlock();
            List<GoStatement> statementList = block.getStatementList();
            for (GoStatement goStatement : statementList) {
                try {
                    PsiElement firstChild = goStatement.getFirstChild().getFirstChild();
                    judgeCallExpr(firstChild, goStatement, resultList);
                }catch (Exception e){

                }
            }
        }
    }

    private static void judgeCallExpr(PsiElement firstChild, GoStatement goStatement, List<OtherRequestEntity> resultList) {
        if (TwoJinZhiGet.getRealStr(GoTwoJinZhi.CALL_EXPR).equals((firstChild.getNode().getElementType().toString()))) {
            if (GoMethod.isExist(( firstChild.getFirstChild().getLastChild()).getText())) {
                GoReferenceExpressionImpl firstChild1 = (GoReferenceExpressionImpl)firstChild.getFirstChild().getFirstChild();
                PsiElement resolve1 = firstChild1.resolve();
                if(resolve1 instanceof GoVarDefinition){
                    GoVarDefinition resolve = (GoVarDefinition)resolve1;
                    if(judgeGin(resolve.getGoTypeInner(ResolveState.initial()).getText())){
                        PsiElement methodPsi = firstChild.getFirstChild().getLastChild();
                        resultList.add(new OtherRequestEntity(methodPsi, getUrl(firstChild), methodPsi.getText()));
                    }
                } else if (resolve1 instanceof GoParamDefinitionImpl) {
                    GoParamDefinitionImpl resolve = (GoParamDefinitionImpl)resolve1;
                    if(judgeGin(resolve.getGoTypeInner(ResolveState.initial()).getText())){
                        //就是
                        PsiElement methodPsi = firstChild.getFirstChild().getLastChild();
                        resultList.add(new OtherRequestEntity(methodPsi, getUrl(firstChild), methodPsi.getText()));
                    }
                }
            }else {
                //是否需要再加 .getFirstChild() 取决于有没有 xx.调用
                GoReferenceExpressionImpl firstChild1 = (GoReferenceExpressionImpl)firstChild.getFirstChild();
                GoFunctionDeclarationImpl resolve = (GoFunctionDeclarationImpl)firstChild1.resolve();
                if(resolve == null){
                    return;
                }
                if(!goFunctionDeclarations.contains(resolve)){
                    analyzeFunc(Arrays.asList(resolve), resultList);
                }
            }
        }else if(firstChild instanceof GoVarDefinition){
            GoVarDefinition resolve = (GoVarDefinition)firstChild;
            if(judgeGin(resolve.getGoTypeInner(ResolveState.initial()).getText())){
                //就是
                PsiElement lastChild = goStatement.getFirstChild().getLastChild();
                if (!Objects.equals(TwoJinZhiGet.getRealStr(GoTwoJinZhi.GIN_Default), lastChild.getText())) {
                    judgeCallExpr(lastChild, goStatement, resultList);
                }
            }
        }
    }

    public static String getUrl(PsiElement firstChild) {
        GoCallExprImpl firstChild11 = (GoCallExprImpl) firstChild;
        GoArgumentList argumentList = firstChild11.getArgumentList();
        GoExpression goExpression1 = argumentList.getExpressionList().get(0);
        return goExpression1.getValue().getString();
    }

    private static boolean judgeGin(String target){
        return Objects.equals(TwoJinZhiGet.getRealStr(GoTwoJinZhi.Engine), target) || Objects.equals(TwoJinZhiGet.getRealStr(GoTwoJinZhi.GIN_ENGINE), target);
    }


}
