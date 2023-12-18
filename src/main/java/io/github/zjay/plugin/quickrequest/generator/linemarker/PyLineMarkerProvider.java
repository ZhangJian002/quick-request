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
import io.github.zjay.plugin.quickrequest.generator.impl.PyMethodGenerator;
import io.github.zjay.plugin.quickrequest.generator.linemarker.tooltip.PythonFunctionTooltip;
import io.github.zjay.plugin.quickrequest.util.*;
import io.github.zjay.plugin.quickrequest.util.python.FlaskMethods;
import org.jetbrains.annotations.NotNull;
import quickRequest.icons.PluginIcons;

import java.lang.reflect.Method;
import java.util.Objects;

public class PyLineMarkerProvider implements LineMarkerProvider {

    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        LineMarkerInfo<PsiElement> lineMarkerInfo = null;
        try {
            if(Objects.equals(element.getClass().getName(), TwoJinZhiGet.getRealStr(PythonTwoJinZhi.PyFunctionImpl))){
                Method getDecoratorList = element.getClass().getMethod(TwoJinZhiGet.getRealStr(PythonTwoJinZhi.getDecoratorList));
                Object decoratorList = getDecoratorList.invoke(element);
                Method getDecorators = decoratorList.getClass().getMethod(TwoJinZhiGet.getRealStr(PythonTwoJinZhi.getDecorators));
                Object[] decorators = (Object[]) getDecorators.invoke(decoratorList);
                for (Object decorator : decorators) {
                    Method getName = decorator.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.getName));
                    Object name = getName.invoke(decorator);
                    if(FlaskMethods.isExist((String) name)){
                        Method getArgumentList = decorator.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.getArgumentList));
                        Object argumentList = getArgumentList.invoke(decorator);
                        Method getArguments = argumentList.getClass().getMethod(TwoJinZhiGet.getRealStr(PythonTwoJinZhi.getArguments));
                        PsiElement[] arguments = (PsiElement[])getArguments.invoke(argumentList);
                        for (PsiElement argument : arguments) {
                            if(TwoJinZhiGet.getRealStr(PythonTwoJinZhi.Py_REFERENCE_EXPRESSION).equals(argument.getNode().getElementType().toString())){
                                Method getReference = argument.getClass().getMethod(TwoJinZhiGet.getRealStr(PythonTwoJinZhi.getReference));
                                Object reference = getReference.invoke(argument);
                                Method resolve = reference.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.resolve));
                                Object resolveValue = resolve.invoke(reference);
                                Method getReference1 = resolveValue.getClass().getMethod(TwoJinZhiGet.getRealStr(PythonTwoJinZhi.findAssignedValue));
                                argument = (PsiElement) getReference1.invoke(resolveValue);
                            }
                            if(TwoJinZhiGet.getRealStr(PythonTwoJinZhi.Py_STRING_LITERAL_EXPRESSION).equals(argument.getNode().getElementType().toString())){
                                Method getStringValue = argument.getClass().getMethod(TwoJinZhiGet.getRealStr(PythonTwoJinZhi.getStringValue));
                                String url = (String)getStringValue.invoke(argument);
                                return new MyLineMarkerInfo<>(element, element.getTextRange(), PluginIcons.fastRequest_editor,
                                        new PythonFunctionTooltip(element, LanguageEnum.Python, url, null),
                                        (e, elt) -> {
                                            Project project = elt.getProject();
                                            ApplicationManager.getApplication().getService(PyMethodGenerator.class).generate(element, url, null);
                                            ToolWindowUtil.openToolWindow(project);
                                            ToolWindowUtil.sendRequest(project, false);
                                        },
                                        GutterIconRenderer.Alignment.LEFT, () -> "quickRequest");
                            }
                        }
                    }
                }
            }
        }catch (Exception e){

        }
        return lineMarkerInfo;
    }

    public static String[] getMethodType(PsiElement element){
        try {
            Method getDecoratorList = element.getClass().getMethod(TwoJinZhiGet.getRealStr(PythonTwoJinZhi.getDecoratorList));
            Object decoratorList = getDecoratorList.invoke(element);
            Method getDecorators = decoratorList.getClass().getMethod(TwoJinZhiGet.getRealStr(PythonTwoJinZhi.getDecorators));
            Object[] decorators = (Object[]) getDecorators.invoke(decoratorList);
            for (Object decorator : decorators) {
                Method getName = decorator.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.getName));
                Object name = getName.invoke(decorator);
                if(FlaskMethods.isExist((String) name)){
                    Method getMethodName = element.getClass().getMethod(TwoJinZhiGet.getRealStr(GoTwoJinZhi.getName));
                    String methodName = (String)getMethodName.invoke(element);
                    return new String[]{FlaskMethods.getMethodType((String) name),methodName};
                }
            }
        }catch (Exception e){

        }
        return null;
    }

}
