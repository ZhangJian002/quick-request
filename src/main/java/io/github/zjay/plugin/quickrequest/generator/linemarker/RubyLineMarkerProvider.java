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
import com.intellij.psi.PsiElement;
import io.github.zjay.plugin.quickrequest.configurable.MyLineMarkerInfo;
import io.github.zjay.plugin.quickrequest.generator.impl.RubyMethodGenerator;
import io.github.zjay.plugin.quickrequest.generator.linemarker.tooltip.RubyFunctionTooltip;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import io.github.zjay.plugin.quickrequest.util.ReflectUtils;
import io.github.zjay.plugin.quickrequest.util.ToolWindowUtil;
import io.github.zjay.plugin.quickrequest.util.ruby.RailsMethods;
import org.jetbrains.annotations.NotNull;
import quickRequest.icons.PluginIcons;

import java.util.List;
import java.util.Objects;

public class RubyLineMarkerProvider implements LineMarkerProvider {

    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        LineMarkerInfo<PsiElement> lineMarkerInfo = null;
        if(Objects.equals("org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall.RCallImpl", element.getClass().getCanonicalName())){
            PsiElement psiElement = element.getParent().getParent().getParent().getParent().getParent();
            if(Objects.equals("org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.blocks.RCompoundStatementImpl", psiElement.getClass().getCanonicalName())){
                String text = (String)ReflectUtils.invokeMethod(psiElement, "getText");
                if(text.contains("Rails.application.routes.draw")){
                    String name = (String)ReflectUtils.invokeMethod(element, "getName");
                    if(RailsMethods.isExist(name)){
                        String[] urlAndMethodType = getUrlAndMethodType(element);
                        return new MyLineMarkerInfo<>(element, element.getTextRange(), PluginIcons.fastRequest_editor,
                                new RubyFunctionTooltip(element, LanguageEnum.Ruby, urlAndMethodType[0], null),
                                (e, elt) -> {
                                    Project project = elt.getProject();
                                    ApplicationManager.getApplication().getService(RubyMethodGenerator.class).generate(element, urlAndMethodType[0], null);
                                    ToolWindowUtil.openToolWindow(project);
                                    ToolWindowUtil.sendRequest(project, false);
                                },
                                GutterIconRenderer.Alignment.LEFT, () -> "quickRequest");
                    }
                }
            }
        }
        return lineMarkerInfo;
    }

    public static String[] getUrlAndMethodType(PsiElement psiElement) {
        List<PsiElement> arguments = (List<PsiElement>)ReflectUtils.invokeMethod(psiElement, "getArguments");
        PsiElement argument = arguments.get(0);
        String url = "";
        String canonicalName = argument.getClass().getCanonicalName();
        if(Objects.equals("org.jetbrains.plugins.ruby.ruby.lang.psi.impl.assoc.RAssocImpl", canonicalName)){
            PsiElement key = (PsiElement)ReflectUtils.invokeMethod(argument, "getKey");
            if(Objects.equals("org.jetbrains.plugins.ruby.ruby.lang.psi.impl.variables.RIdentifierImpl", key.getClass().getCanonicalName())){
                PsiElement resolve = key.getReference().resolve();
                PsiElement parent = resolve.getParent();
                Object value = ReflectUtils.invokeMethod(parent, "getValue");
                url = (String) ReflectUtils.invokeMethod(value, "getText");
            }else if(Objects.equals("org.jetbrains.plugins.ruby.ruby.lang.psi.impl.basicTypes.stringLiterals.baseString.RStringLiteralImpl", key.getClass().getCanonicalName())){
                url = key.getText();
            }
        } else if (Objects.equals("org.jetbrains.plugins.ruby.ruby.lang.psi.impl.basicTypes.stringLiterals.baseString.RDStringLiteralImpl", canonicalName)) {
            url = argument.getText();
        } else if (Objects.equals("org.jetbrains.plugins.ruby.ruby.lang.psi.impl.variables.RIdentifierImpl", canonicalName)) {
            PsiElement resolve = argument.getReference().resolve();
            PsiElement parent = resolve.getParent();
            Object value = ReflectUtils.invokeMethod(parent, "getValue");
            url = (String) ReflectUtils.invokeMethod(value, "getText");
        }
        String name = (String)ReflectUtils.invokeMethod(psiElement, "getName");
        return new String[]{url.replaceAll("\"", "").replaceAll("'", ""), RailsMethods.getMethodType(name)};
    }

}
