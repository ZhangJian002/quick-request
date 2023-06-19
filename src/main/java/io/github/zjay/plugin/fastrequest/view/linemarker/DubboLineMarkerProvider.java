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
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiAnnotationImpl;
import com.intellij.psi.impl.source.tree.java.PsiArrayInitializerMemberValueImpl;
import com.intellij.util.keyFMap.KeyFMap;
import free.icons.PluginIcons;
import io.github.zjay.plugin.fastrequest.config.Constant;
import io.github.zjay.plugin.fastrequest.configurable.MyLineMarkerInfo;
import io.github.zjay.plugin.fastrequest.service.GeneratorUrlService;
import io.github.zjay.plugin.fastrequest.util.ToolWindowUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DubboLineMarkerProvider implements LineMarkerProvider {

    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {

        LineMarkerInfo<PsiElement> lineMarkerInfo;

        if (element instanceof PsiIdentifier && element.getParent() instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) element.getParent();
            if (!judgeMethod(psiMethod)) {
                return null;
            }
            PsiMethod methodElement = (PsiMethod) element.getParent();
            lineMarkerInfo = new MyLineMarkerInfo<>(element, element.getTextRange(), PluginIcons.fastRequest_editor,
                    new FunctionTooltip(methodElement),
                    (e, elt) -> {
                        Project project = elt.getProject();
                        GeneratorUrlService generatorUrlService = ApplicationManager.getApplication().getService(GeneratorUrlService.class);
                        ToolWindowUtil.generatorUrlAndSend(project, generatorUrlService, methodElement, false);
                    },
                    GutterIconRenderer.Alignment.LEFT, () -> "fastRequest");
            return lineMarkerInfo;
        }
        return null;
    }



    public static boolean judgeMethod(@NotNull PsiMethod psiMethod) {
        Constant.DubboMethodConfig[] dubboMethodConfig = Constant.DubboMethodConfig.values();
        for (Constant.DubboMethodConfig mappingConfig : dubboMethodConfig) {
            String code = mappingConfig.getCode();
            PsiClass psiClass = (PsiClass) psiMethod.getParent();
            if(psiMethod.getAnnotation("java.lang.Override") == null || psiClass.getInterfaces().length == 0){
                //不是实现方法 或者 没有实现接口的 跳过
                return false;
            }
            PsiAnnotation annotationRequestMapping = psiClass.getAnnotation(code);
            if (annotationRequestMapping != null) {
                PsiAnnotationMemberValue methods = annotationRequestMapping.findAttributeValue("methods");
                //没有这个属性 直接返回true
                if(methods == null || Objects.equals(methods.getText().replaceAll("^\"|\"$", ""), "{}")){
                    return true;
                }
                try {
                    //methods有多个@Method
                    PsiArrayInitializerMemberValueImpl methods1 = (PsiArrayInitializerMemberValueImpl) methods;
                    PsiAnnotationMemberValue[] initializers = methods1.getInitializers();
                    if(initializers.length > 0){
                        //确定是多个
                        for (PsiAnnotationMemberValue initializer : initializers) {
                            PsiAnnotation initializer1 = (PsiAnnotation) initializer;
                            PsiAnnotationMemberValue attributeValue = initializer1.findAttributeValue("name");
                            if(attributeValue != null && Objects.equals(psiMethod.getName(), attributeValue.getText().replaceAll("^\"|\"$", ""))){
                                return true;
                            }
                        }
                    }else {
                        //没有 返回true
                        return true;
                    }
                }catch (Exception e){
                    try {
                        PsiAnnotationImpl methods1 = (PsiAnnotationImpl) methods;
                        PsiAnnotationMemberValue attributeValue = methods1.findAttributeValue("name");
                        if(attributeValue != null && Objects.equals(psiMethod.getName(), attributeValue.getText().replaceAll("^\"|\"$", ""))){
                            return true;
                        }
                    }catch (Exception e1){
                        //ignore
                    }
                }
            }

        }
        return false;
    }

}
