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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.zjay.plugin.quickrequest.configurable.MyLineMarkerInfo;
import io.github.zjay.plugin.quickrequest.generator.impl.PyMethodGenerator;
import io.github.zjay.plugin.quickrequest.generator.impl.RubyMethodGenerator;
import io.github.zjay.plugin.quickrequest.generator.linemarker.tooltip.PythonFunctionTooltip;
import io.github.zjay.plugin.quickrequest.generator.linemarker.tooltip.RubyFunctionTooltip;
import io.github.zjay.plugin.quickrequest.util.*;
import io.github.zjay.plugin.quickrequest.util.python.FlaskMethods;
import io.github.zjay.plugin.quickrequest.util.ruby.RailsMethods;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.rails.RailsUtil;
import org.jetbrains.plugins.ruby.rails.actions.RailsActionsUtil;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.ruby.codeInsight.references.RIdentifierReference;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPossibleCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtilBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RListOfExpressions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.assoc.RAssocImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.basicTypes.stringLiterals.baseString.RDStringLiteralImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.basicTypes.stringLiterals.baseString.RStringLiteralImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.blocks.RCompoundStatementImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.expressions.RAssignmentExpressionImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.iterators.RDoBlockCallImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall.RCallImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.variables.RIdentifierImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.indexes.RubyAllInstanceVariablesIndex;
import org.jetbrains.plugins.ruby.ruby.lang.psi.indexes.RubyAnonymousDefiningCallIndex;
import org.jetbrains.plugins.ruby.ruby.lang.psi.indexes.RubyMethodNameIndex;
import org.jetbrains.plugins.ruby.ruby.lang.psi.iterators.RCodeBlock;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RubyCallType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.fields.RInstanceVariable;
import quickRequest.icons.PluginIcons;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class RubyLineMarkerProvider implements LineMarkerProvider {

    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        LineMarkerInfo<PsiElement> lineMarkerInfo = null;

        if(element instanceof RCallImpl){
            if(element.getParent().getParent().getParent().getParent().getParent() instanceof RCompoundStatementImpl){
                RCompoundStatementImpl parent = (RCompoundStatementImpl) element.getParent().getParent().getParent().getParent().getParent();
                if(parent.getText().contains("Rails.application.routes.draw")){
                    RCallImpl rCall = (RCallImpl) element;
                    if(RailsMethods.isExist(rCall.getName())){
                        String[] urlAndMethodType = getUrlAndMethodType(rCall);
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
        RCallImpl rCall = (RCallImpl) psiElement;
        List<RPsiElement> arguments = rCall.getArguments();
        RPsiElement argument = arguments.get(0);
        String url = "";
        if(argument instanceof RAssocImpl){
            RAssocImpl rAssoc = (RAssocImpl) argument;
            RPsiElement key = rAssoc.getKey();
            if(key instanceof RIdentifierImpl){
                RIdentifierImpl rIdentifier = (RIdentifierImpl) key;
                RIdentifierImpl resolve = (RIdentifierImpl)rIdentifier.getReference().resolve();
                RAssignmentExpressionImpl parent = (RAssignmentExpressionImpl) resolve.getParent();
                url = parent.getValue().getText();
            }else if(key instanceof RStringLiteralImpl){
                url = key.getText();
            }
        } else if (argument instanceof RDStringLiteralImpl) {
            RDStringLiteralImpl stringLiteral = (RDStringLiteralImpl) argument;
            url = stringLiteral.getText();
        } else if (argument instanceof RIdentifierImpl) {
            RIdentifierImpl rIdentifier = (RIdentifierImpl) argument;
            RIdentifierImpl resolve = (RIdentifierImpl)rIdentifier.getReference().resolve();
            RAssignmentExpressionImpl parent = (RAssignmentExpressionImpl) resolve.getParent();
            url = parent.getValue().getText();
        }
        return new String[]{url.replaceAll("\"", "").replaceAll("'", ""), RailsMethods.getMethodType(rCall.getName())};
    }

}
