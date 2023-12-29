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
import io.github.zjay.plugin.quickrequest.configurable.MyLineMarkerInfo;
import io.github.zjay.plugin.quickrequest.generator.impl.PhpMethodGenerator;
import io.github.zjay.plugin.quickrequest.generator.linemarker.tooltip.PhpFunctionTooltip;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import io.github.zjay.plugin.quickrequest.util.ToolWindowUtil;
import org.jetbrains.annotations.NotNull;
import quickRequest.icons.PluginIcons;

import java.lang.reflect.Method;
import java.util.Objects;

public class PhpLineMarkerProvider implements LineMarkerProvider {


    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        LineMarkerInfo<PsiElement> lineMarkerInfo = null;
        try {
            if(Objects.equals(element.getClass().getCanonicalName(), "com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl")){
                Method getClassReference = element.getClass().getMethod("getClassReference");
                Object classReference = getClassReference.invoke(element);
                Method getName = classReference.getClass().getMethod("getName");
                Object name = getName.invoke(classReference);
                if(Objects.equals(name, "Route")){
                    //请求方式
                    Method getParameters = element.getClass().getMethod("getParameters");
                    Object[] parameters = (Object[])getParameters.invoke(element);
                    String url = getString((PsiElement) parameters[0]);
                    return new MyLineMarkerInfo<>(element, element.getTextRange(), PluginIcons.fastRequest_editor,
                            new PhpFunctionTooltip(element, LanguageEnum.php, url, null),
                            (e, elt) -> {
                                Project project = elt.getProject();
                                ApplicationManager.getApplication().getService(PhpMethodGenerator.class).generate(element, url, null);
                                ToolWindowUtil.openToolWindow(project);
                                ToolWindowUtil.sendRequest(project, false);
                            },
                            GutterIconRenderer.Alignment.LEFT, () -> "quickRequest");
                }
            }
        }catch (Exception e){

        }
        return lineMarkerInfo;
    }

    private String getString(PsiElement parameter) {
        String url = "";
        try {
            String canonicalName = parameter.getClass().getCanonicalName();
            if(Objects.equals(canonicalName, "com.jetbrains.php.lang.psi.elements.impl.StringLiteralExpressionImpl")){
                Method getContents = parameter.getClass().getMethod("getContents");
                url = getContents.invoke(parameter).toString();
            }else if(Objects.equals(canonicalName, "com.jetbrains.php.lang.psi.elements.impl.ConstantReferenceImpl")){
                Method resolve = parameter.getClass().getMethod("resolve");
                Object resolveResult = resolve.invoke(parameter);
                if(Objects.equals(resolveResult.getClass().getCanonicalName(), "com.jetbrains.php.lang.psi.elements.impl.ConstantImpl")){
                    Method getValue = resolveResult.getClass().getMethod("getValue");
                    PsiElement value = (PsiElement)getValue.invoke(resolveResult);
                    url = value.getText().replaceAll("\"", "").replaceAll("'", "");
                }
            }

        }catch (Exception e){

        }
        return url;
    }

}
