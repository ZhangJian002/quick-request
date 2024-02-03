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
import io.github.zjay.plugin.quickrequest.generator.impl.RustMethodGenerator;
import io.github.zjay.plugin.quickrequest.generator.linemarker.tooltip.RustFunctionTooltip;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import io.github.zjay.plugin.quickrequest.util.ReflectUtils;
import io.github.zjay.plugin.quickrequest.util.ToolWindowUtil;
import io.github.zjay.plugin.quickrequest.util.rust.RustMethods;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import quickRequest.icons.PluginIcons;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RustLineMarkerProvider implements LineMarkerProvider {

    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        LineMarkerInfo<PsiElement> lineMarkerInfo = null;
        if(Objects.equals(element.getClass().getCanonicalName(), "org.rust.lang.core.psi.impl.RsOuterAttrImpl")){
            try {
                String[] urlAndMethodType = getUrlAndMethodType(element);
                if(urlAndMethodType != null){
                    return new MyLineMarkerInfo<>(element, element.getTextRange(), PluginIcons.fastRequest_editor,
                            new RustFunctionTooltip(element, LanguageEnum.Rust, urlAndMethodType[2], urlAndMethodType[3], null),
                            (e, elt) -> {
                                Project project = elt.getProject();
                                ApplicationManager.getApplication().getService(RustMethodGenerator.class).generate(element, urlAndMethodType[3], null);
                                ToolWindowUtil.openToolWindow(project);
                                ToolWindowUtil.sendRequest(project, false);
                            },
                            GutterIconRenderer.Alignment.LEFT, () -> "quickRequest");
                }
            }catch (Exception e){

            }
        }
        return lineMarkerInfo;
    }




    public static String[] getUrlAndMethodType(PsiElement element) {
        try {
            Object metaItem = ReflectUtils.invokeMethod(element, "getMetaItem");
            Object path = ReflectUtils.invokeMethod(metaItem, "getPath");
            PsiElement identifier = (PsiElement)ReflectUtils.invokeMethod(path, "getIdentifier");
            if(RustMethods.isExist(identifier.getText())){
                Object metaItemArgs = ReflectUtils.invokeMethod(metaItem, "getMetaItemArgs");
                List litExprList = (List)ReflectUtils.invokeMethod(metaItemArgs, "getLitExprList");
                PsiElement stringLiteral = (PsiElement)ReflectUtils.invokeMethod(litExprList.get(0), "getStringLiteral");
                String url = stringLiteral.getText().replaceAll("'", "").replaceAll("\"", "");
                List<String> finalList = new LinkedList<>();
                for (String s : url.split("/")) {
                    if(s.startsWith("{") && s.endsWith("}") || s.startsWith("<") && s.endsWith(">")){
                        Random random = new Random();
                        int randomNumber = random.nextInt(100) + 1; // 生成 1 到 100 之间的随机整数
                        s = randomNumber + "";
                    }
                    finalList.add(s);
                }
                String finalUrl = StringUtils.join(finalList, "/");
                return new String[]{finalUrl, RustMethods.getMethodType(identifier.getText()), url.replaceAll("<", "&lt;").replaceAll(">", "&gt;"), identifier.getText()};
            }
        }catch (Exception e){

        }
        return null;
    }

}
