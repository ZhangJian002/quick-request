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

import com.goide.GoFileType;
import com.goide.psi.GoArgumentList;
import com.goide.psi.GoExpression;
import com.goide.psi.GoVarDefinition;
import com.goide.psi.impl.GoCallExprImpl;
import com.goide.psi.impl.GoParamDefinitionImpl;
import com.goide.psi.impl.GoReferenceExpressionImpl;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveState;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.GoTwoJinZhi;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;
import io.github.zjay.plugin.quickrequest.util.go.GoMethod;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class GoRequestMappingContributorBack extends OtherRequestMappingByNameContributor{


    @Override
    List<OtherRequestEntity> getPsiElementSearchers(Project project) {
        return getResultList(project);
    }

    public static List<OtherRequestEntity> getResultList(Project project){
        List<OtherRequestEntity> resultList = new LinkedList<>();
        PsiManager psiManager = PsiManager.getInstance(project);
        Collection<VirtualFile> virtualFiles = FilenameIndex.getAllFilesByExt(project, "go", GlobalSearchScope.projectScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            if(!isGinAppFile(virtualFile)){
                continue;
            }
            PsiElement[] psiElements = PsiTreeUtil.collectElements(psiManager.findFile(virtualFile), psiElement -> true);
            for (PsiElement psiElement : psiElements) {
                judgeCallExpr(psiElement, resultList);
            }

        }
        return resultList;
    }

    private static void judgeCallExpr(PsiElement firstChild, List<OtherRequestEntity> resultList) {
        try {
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
                }
            }
        }catch (Exception e){

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

    private static boolean isGinAppFile(VirtualFile child) {
        if (!child.isDirectory()) {
            CharSequence text = LoadTextUtil.loadText(child);
            String document = text.toString();
            if (document.contains("gin.Default()") || document.contains("gin.Engine")) {
                return true;
            }
        }
        return false;
    }


}
