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

package io.github.zjay.plugin.quickrequest.contributor;

import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyCallExpressionImpl;
import com.jetbrains.python.psi.impl.PyDecoratorImpl;
import com.jetbrains.python.psi.impl.PyFunctionImpl;
import com.jetbrains.python.psi.impl.PyReferenceExpressionImpl;
import io.github.zjay.plugin.quickrequest.generator.linemarker.PyLineMarkerProvider;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.file.FileUtil;
import io.github.zjay.plugin.quickrequest.util.python.FlaskMethods;

import java.util.*;

public class PythonRequestMappingContributor extends OtherRequestMappingByNameContributor{


    @Override
    List<OtherRequestEntity> getPsiElementSearchers(Project project) {
        return getResultList(project);
    }

    public static List<OtherRequestEntity> getResultList(Project project){
        List<OtherRequestEntity> resultList = new LinkedList<>();
        PsiManager psiManager = PsiManager.getInstance(project);
        Collection<VirtualFile> virtualFiles = FilenameIndex.getAllFilesByExt(project, "py", GlobalSearchScope.projectScope(project));
        try {
            virtualFiles.forEach(virtualFile -> {
                if(isFlaskAppFile(virtualFile, project)){
                    PsiFile file = psiManager.findFile(virtualFile);
                    PsiElement[] psiElements = PsiTreeUtil.collectElements(file, psiElement -> true);
                    for (PsiElement psiElement : psiElements) {
                        if(psiElement instanceof PyDecoratorList){
                            PyDecoratorList pyDecorators = (PyDecoratorList) psiElement;
                            for (PyDecorator pyDecorator : pyDecorators.getDecorators()) {
                                if(FlaskMethods.isExist(((PyDecoratorImpl)pyDecorator).getName())){
                                    PyArgumentList argumentList = pyDecorator.getArgumentList();
                                    if(argumentList == null){
                                        continue;
                                    }
                                    PyExpression[] arguments = argumentList.getArguments();
                                    for (PyExpression argument : arguments) {
                                        String url = PyLineMarkerProvider.getUrlFromDecorator(argument);
                                        if(url != null){
                                            PyCallExpressionImpl expression = (PyCallExpressionImpl)pyDecorator.getExpression();
                                            PyReferenceExpressionImpl callee = (PyReferenceExpressionImpl)expression.getCallee();
                                            PsiElement resolve = callee.getReference().resolve();
                                            String qualifiedName = null;
                                            if(resolve instanceof PyFunctionImpl){
                                                PyFunctionImpl reference = (PyFunctionImpl) callee.getReference().resolve();
                                                qualifiedName = reference.getQualifiedName();
                                            }else if (resolve instanceof PyTargetExpression){
                                                PyTargetExpression reference = (PyTargetExpression) callee.getReference().resolve();
                                                qualifiedName = reference.getQualifiedName();
                                            }
                                            if(qualifiedName != null && qualifiedName.startsWith("flask.sansio.scaffold.Scaffold.")){
                                                resultList.add(new OtherRequestEntity(pyDecorator, url,FlaskMethods.getMethodType(((PyDecoratorImpl)pyDecorator).getName())));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }catch (Exception e){

        }
        return resultList;
    }

    private static boolean isFlaskAppFile(VirtualFile child, Project project) {
        if (!child.isDirectory() && FileUtil.isProjectFile(project, child)) {
            CharSequence text = LoadTextUtil.loadText(child);
            if (text.toString().contains("Flask(__name__)")) {
                return true;
            }
        }

        return false;
    }


}
