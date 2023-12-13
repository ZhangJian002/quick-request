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

package io.github.zjay.plugin.quickrequest.generator.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import io.github.zjay.plugin.quickrequest.configurable.MyLineMarkerInfo;
import io.github.zjay.plugin.quickrequest.generator.impl.GoMethodGenerator;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import io.github.zjay.plugin.quickrequest.util.ToolWindowUtil;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhi;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;
import io.github.zjay.plugin.quickrequest.util.go.GoMethod;
import io.github.zjay.plugin.quickrequest.generator.linemarker.tooltip.GoFunctionTooltip;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import quickRequest.icons.PluginIcons;

import java.lang.reflect.Method;

public class GoLineMarkerProvider implements LineMarkerProvider {

    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        MyLineMarkerInfo<PsiElement> lineMarkerInfo = null;
        try {
            ASTNode node = element.getNode();
            if (node instanceof CompositeElement && TwoJinZhiGet.getRealStr(TwoJinZhi.CALL_EXPR).equals((node.getElementType().toString()))) {
                PsiElement goMethod;
                //代表是一个调用
                if (!GoMethod.isExist((goMethod = element.getFirstChild().getLastChild()).getText())) {
                    return null;
                }
                Method getArgumentList = element.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getArgumentList));
                PsiElement invoke1 = (PsiElement) getArgumentList.invoke(element);
                Method getLparen = invoke1.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getLparen));
                PsiElement invoke2 = (PsiElement) getLparen.invoke(invoke1);
                PsiElement needLineMarker = null;
                PsiElement generateParams = null;
                String finalUrl = null;
                for (PsiElement temp = invoke2; temp.getNextSibling() != null; temp = temp.getNextSibling()) {
                    ASTNode tempNode = temp.getNode();
                    if (tempNode instanceof CompositeElement) {
                        //第一个参数
                        String url = getUrlFromPsi(temp);
                        if (StringUtils.isNotBlank(url) && needLineMarker == null) {
                            needLineMarker = temp;
                            finalUrl = url;
                        }
                        if(generateParams != null){
                            continue;
                        }
                        if(TwoJinZhiGet.getRealStr(TwoJinZhi.FUNCTION_LIT).equals((tempNode.getElementType().toString()))){
                            generateParams = generateParamsGet(temp);
                        }else if(TwoJinZhiGet.getRealStr(TwoJinZhi.REFERENCE_EXPRESSION).equals((tempNode.getElementType().toString()))){
                            //调用resolve，解析成方法
                            Method resolve = temp.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.resolve));
                            PsiElement resolveObj = (PsiElement)resolve.invoke(temp);
                            generateParams = generateParamsGet(resolveObj);
                        }
                    }
                }
                if(needLineMarker != null){
                    String method = GoMethod.getMethodType(goMethod.getText());
                    PsiElement finalTemp = needLineMarker;
                    PsiElement finalGenerateParams = generateParams;
                    lineMarkerInfo = new MyLineMarkerInfo<>(goMethod, goMethod.getTextRange(), PluginIcons.fastRequest_editor,
                            new GoFunctionTooltip(needLineMarker, LanguageEnum.go, finalUrl, generateParams),
                            (e, elt) -> {
                                Project project = elt.getProject();
                                ApplicationManager.getApplication().getService(GoMethodGenerator.class).generate(finalTemp, method, finalGenerateParams);
                                ToolWindowUtil.openToolWindow(project);
                                ToolWindowUtil.sendRequest(project, false);
                            },
                            GutterIconRenderer.Alignment.LEFT, () -> "quickRequest");
                }
            }
        } catch (Exception e) {

        }
        return lineMarkerInfo;
    }

    private PsiElement generateParamsGet(PsiElement temp) {
        try {
            //获取方法的代码快对象
            Method getBlock = temp.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getBlock));
            return (PsiElement)getBlock.invoke(temp);
        }catch (Exception e){
            //不是代码快
            return null;
        }
    }

    public static String getUrlFromPsi(PsiElement temp){
        String url = "";
        try {
            ASTNode tempNode = temp.getNode();
            if (TwoJinZhiGet.getRealStr(TwoJinZhi.REFERENCE_EXPRESSION).equals((tempNode.getElementType().toString()))) {
                Method resolve = temp.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.resolve));
                Object invoke3 = resolve.invoke(temp);
                Method getConst = invoke3.getClass().getMethod(TwoJinZhiGet.getRealStr(TwoJinZhi.getValue));
                Object invoke4 = getConst.invoke(invoke3);
                url = invoke4.toString();
            } else if (TwoJinZhiGet.getRealStr(TwoJinZhi.STRING_LITERAL).equals((tempNode.getElementType().toString()))) {
                url = tempNode.getText().replaceAll("\"", "");
            } else {
                url = null;
            }
        }catch (Exception e){

        }
        return url;
    }


}
