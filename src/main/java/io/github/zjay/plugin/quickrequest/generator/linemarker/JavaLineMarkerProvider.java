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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import io.github.zjay.plugin.quickrequest.generator.linemarker.tooltip.JavaFunctionTooltip;
import quickRequest.icons.PluginIcons;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.configurable.MyLineMarkerInfo;
import io.github.zjay.plugin.quickrequest.service.GeneratorUrlService;
import io.github.zjay.plugin.quickrequest.util.ToolWindowUtil;
import org.jetbrains.annotations.NotNull;

public class JavaLineMarkerProvider implements LineMarkerProvider {

    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {

        LineMarkerInfo<PsiElement> lineMarkerInfo;

        if (element instanceof PsiIdentifier && element.getParent() instanceof PsiMethod) {
            if (!judgeMethod(element)) {
                return null;
            }
            PsiMethod methodElement = (PsiMethod) element.getParent();
            lineMarkerInfo = new MyLineMarkerInfo<>(element, element.getTextRange(), PluginIcons.fastRequest_editor,
                    new JavaFunctionTooltip(methodElement, LanguageEnum.java),
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
        PsiMethod psiMethod = (PsiMethod) psiElement.getParent();
        Constant.SpringMappingConfig[] mappingConfigArray = Constant.SpringMappingConfig.values();
        PsiAnnotation annotationRequestMapping = null;
        for (Constant.SpringMappingConfig mappingConfig : mappingConfigArray) {
            String code = mappingConfig.getCode();
            annotationRequestMapping = psiMethod.getAnnotation(code);
            if (annotationRequestMapping != null) {
                break;
            }
        }
        if (annotationRequestMapping == null) {
            Constant.JaxRsMappingMethodConfig[] jaxRsMappingConfigArray = Constant.JaxRsMappingMethodConfig.values();
            for (Constant.JaxRsMappingMethodConfig mappingConfig : jaxRsMappingConfigArray) {
                String code = mappingConfig.getCode();
                annotationRequestMapping = psiMethod.getAnnotation(code);
                if (annotationRequestMapping != null) {
                    break;
                }
            }
        }
        return annotationRequestMapping != null;
    }

}
