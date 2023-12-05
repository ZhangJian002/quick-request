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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import io.github.zjay.plugin.fastrequest.configurable.MyLineMarkerInfo;
import io.github.zjay.plugin.fastrequest.generator.impl.GoMethodGenerator;
import io.github.zjay.plugin.fastrequest.util.LanguageEnum;
import io.github.zjay.plugin.fastrequest.util.ToolWindowUtil;
import io.github.zjay.plugin.fastrequest.util.go.GoMethod;
import io.github.zjay.plugin.fastrequest.view.linemarker.tooltip.GoFunctionTooltip;
import org.jetbrains.annotations.NotNull;
import quickRequest.icons.PluginIcons;

import java.util.Objects;

public class GoLineMarkerProvider implements LineMarkerProvider {


    private static LeafPsiElement pointElement = null;
    private static LeafPsiElement targetElement = null;

    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        LineMarkerInfo<PsiElement> lineMarkerInfo = null;
        if (element instanceof LeafPsiElement && GoMethod.isExist(element.getText())
                && "identifier".equals((((LeafPsiElement) element).getElementType().toString())) && pointElement != null) {
            targetElement = (LeafPsiElement) element;
        }else if(element instanceof LeafPsiElement && "string".equals((((LeafPsiElement) element).getElementType().toString()))){
            if(pointElement != null && targetElement != null){
                String method = GoMethod.getMethodType(targetElement.getText());
                lineMarkerInfo = new MyLineMarkerInfo<>(targetElement, targetElement.getTextRange(), PluginIcons.fastRequest_editor,
                        new GoFunctionTooltip(element, LanguageEnum.go),
                        (e, elt) -> {
                            Project project = elt.getProject();
                            ApplicationManager.getApplication().getService(GoMethodGenerator.class).generate(element, method, null);
                            ToolWindowUtil.openToolWindow(project);
                            ToolWindowUtil.sendRequest(project, false);
                        },
                        GutterIconRenderer.Alignment.LEFT, () -> "quickRequest");
                pointElement= null;
                targetElement = null;
            }
        } else if (element instanceof LeafPsiElement && Objects.equals(".", element.getText())) {
            pointElement = (LeafPsiElement) element;
        }else if(element instanceof LeafPsiElement && Objects.equals("(", element.getText())){

        }else if(!(element instanceof LeafPsiElement) || element instanceof PsiWhiteSpace){

        } else {
            pointElement= null;
            targetElement = null;
        }
        return lineMarkerInfo;
    }

}
