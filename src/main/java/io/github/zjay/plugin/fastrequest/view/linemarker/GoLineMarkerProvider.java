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

package io.github.zjay.plugin.fastrequest.view.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.Function;
import io.github.zjay.plugin.fastrequest.configurable.MyLineMarkerInfo;
import io.github.zjay.plugin.fastrequest.generator.impl.GoMethodGenerator;
import io.github.zjay.plugin.fastrequest.util.LanguageEnum;
import io.github.zjay.plugin.fastrequest.util.ToolWindowUtil;
import io.github.zjay.plugin.fastrequest.util.go.GoMethod;
import io.github.zjay.plugin.fastrequest.view.linemarker.tooltip.GoFunctionTooltip;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import quickRequest.icons.PluginIcons;

import java.lang.reflect.Method;
import java.util.Objects;

public class GoLineMarkerProvider implements LineMarkerProvider {

    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        LineMarkerInfo<PsiElement> lineMarkerInfo = null;
        try {
            ASTNode node = element.getNode();
            if (node instanceof CompositeElement && "CALL_EXPR".equals((node.getElementType().toString()))) {
                PsiElement goMethod;
                //代表是一个调用
                if (!GoMethod.isExist((goMethod = element.getFirstChild().getLastChild()).getText())) {
                    return null;
                }
                Method getExpression = element.getClass().getMethod("getExpression");
                Method getArgumentList = element.getClass().getMethod("getArgumentList");
                PsiElement invoke = (PsiElement) getExpression.invoke(element);
                PsiElement invoke1 = (PsiElement) getArgumentList.invoke(element);
                Method getLparen = invoke1.getClass().getMethod("getLparen");
                PsiElement invoke2 = (PsiElement) getLparen.invoke(invoke1);
                for (PsiElement temp = invoke2; temp.getNextSibling() != null; temp = temp.getNextSibling()) {
                    ASTNode tempNode = temp.getNode();
                    if (tempNode instanceof CompositeElement) {
                        //第一个参数
                        String url = getUrlFromPsi(temp);
                        if (StringUtils.isNotBlank(url)) {
                            String method = GoMethod.getMethodType(goMethod.getText());
                            PsiElement finalTemp = temp;
                            lineMarkerInfo = new MyLineMarkerInfo<>(goMethod, goMethod.getTextRange(), PluginIcons.fastRequest_editor,
                                    new GoFunctionTooltip(temp, LanguageEnum.go, url),
                                    (e, elt) -> {
                                        Project project = elt.getProject();
                                        ApplicationManager.getApplication().getService(GoMethodGenerator.class).generate(finalTemp, method, null);
                                        ToolWindowUtil.openToolWindow(project);
                                        ToolWindowUtil.sendRequest(project, false);
                                    },
                                    GutterIconRenderer.Alignment.LEFT, () -> "quickRequest");
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
        return lineMarkerInfo;
    }

    public static String getUrlFromPsi(PsiElement temp){
        String url = "";
        try {
            ASTNode tempNode = temp.getNode();
            if ("REFERENCE_EXPRESSION".equals((tempNode.getElementType().toString()))) {
                Method resolve = temp.getClass().getMethod("resolve");
                Object invoke3 = resolve.invoke(temp);
                Method getConst = invoke3.getClass().getMethod("getValue");
                Object invoke4 = getConst.invoke(invoke3);
                url = invoke4.toString();
            } else if ("STRING_LITERAL".equals((tempNode.getElementType().toString()))) {
                url = tempNode.getText();
            } else {
                url = null;
            }
        }catch (Exception e){

        }
        return url;
    }
}