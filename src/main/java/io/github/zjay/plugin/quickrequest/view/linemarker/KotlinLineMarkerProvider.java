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

package io.github.zjay.plugin.quickrequest.view.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.configurable.MyLineMarkerInfo;
import io.github.zjay.plugin.quickrequest.service.GeneratorUrlService;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import io.github.zjay.plugin.quickrequest.util.ToolWindowUtil;
import io.github.zjay.plugin.quickrequest.view.linemarker.tooltip.KotlinFunctionTooltip;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.asJava.LightClassUtilsKt;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import quickRequest.icons.PluginIcons;

import java.util.Objects;

public class KotlinLineMarkerProvider implements LineMarkerProvider {

    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {

        LineMarkerInfo<PsiElement> lineMarkerInfo;

        if (element instanceof LeafPsiElement && "IDENTIFIER".equals(((LeafPsiElement)element).getElementType().toString()) && element.getParent() instanceof KtNamedFunction) {
            if (!judgeMethod(element)) {
                return null;
            }
            KtNamedFunction ktNamedFunction = (KtNamedFunction)element.getParent();
            PsiMethod methodElement = LightClassUtilsKt.getRepresentativeLightMethod(ktNamedFunction);
            lineMarkerInfo = new MyLineMarkerInfo<>(element, element.getTextRange(), PluginIcons.fastRequest_editor,
                    new KotlinFunctionTooltip(methodElement, LanguageEnum.Kotlin),
                    (e, elt) -> {
                        Project project = elt.getProject();
                        GeneratorUrlService generatorUrlService = ApplicationManager.getApplication().getService(GeneratorUrlService.class);
                        ToolWindowUtil.generatorUrlAndSend(project, generatorUrlService, methodElement, false);
                    },
                    GutterIconRenderer.Alignment.LEFT, () -> "quickRequest");
            return lineMarkerInfo;
        }
        return null;
    }



    private boolean judgeMethod(@NotNull PsiElement psiElement) {
        KtNamedFunction ktNamedFunction = (KtNamedFunction)psiElement.getParent();
        return ktNamedFunction.getAnnotationEntries()
           .stream()
           .filter(paramKtAnnotationEntry -> (paramKtAnnotationEntry.getShortName() != null))
           .anyMatch(paramKtAnnotationEntry -> {
               Constant.SpringMappingConfig[] mappingConfigArray = Constant.SpringMappingConfig.values();
               for (Constant.SpringMappingConfig mappingConfig : mappingConfigArray) {
                   String code = mappingConfig.getCode();
                   String[] split = code.split("\\.");
                   if(Objects.equals(split[split.length-1], paramKtAnnotationEntry.getShortName().asString())){
                       return true;
                   }
               }
               return false;
            }
           );

    }

}
