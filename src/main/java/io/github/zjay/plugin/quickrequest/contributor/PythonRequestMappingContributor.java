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
import io.github.zjay.plugin.quickrequest.generator.linemarker.PyLineMarkerProvider;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.ReflectUtils;
import io.github.zjay.plugin.quickrequest.util.file.FileUtil;
import io.github.zjay.plugin.quickrequest.util.python.FlaskMethods;

import java.util.*;

public class PythonRequestMappingContributor extends OtherRequestMappingByNameContributor{


    @Override
    List<OtherRequestEntity> getPsiElementSearchers(Project project) {
        return getResultList(project);
    }

    public static List<OtherRequestEntity> getResultList(Project project){
        List<OtherRequestEntity> resultList = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(project);
        Collection<VirtualFile> virtualFiles = FilenameIndex.getAllFilesByExt(project, "py", GlobalSearchScope.projectScope(project));
        try {
            virtualFiles.forEach(virtualFile -> {
                if(isFlaskAppFile(virtualFile, project)){
                    PsiFile file = psiManager.findFile(virtualFile);
                    PsiElement[] psiElements = PsiTreeUtil.collectElements(file, psiElement -> true);
                    for (PsiElement psiElement : psiElements) {
                        if (!Objects.equals("com.jetbrains.python.psi.PyDecoratorList", psiElement.getClass().getCanonicalName())){
                            continue;
                        }
                        List<PsiElement> decorators = (List<PsiElement>)ReflectUtils.invokeMethod(psiElement, "getDecorators");
                        for (PsiElement pyDecorator : decorators) {
                            String name = (String)ReflectUtils.invokeMethod(pyDecorator, "getName");
                            if(FlaskMethods.isExist(name)){
                                PsiElement argumentList = (PsiElement)ReflectUtils.invokeMethod(pyDecorator, "getArgumentList");
                                if(argumentList == null){
                                    continue;
                                }
                                PsiElement expression = (PsiElement)ReflectUtils.invokeMethod(pyDecorator, "getExpression");
                                PsiElement callee = (PsiElement)ReflectUtils.invokeMethod(expression, "getCallee");
                                PsiElement resolve = callee.getReference().resolve();

                                PsiElement[] arguments = (PsiElement[])ReflectUtils.invokeMethod(argumentList, "getArguments");
                                for (PsiElement argument : arguments) {
                                    String url = PyLineMarkerProvider.getUrlFromDecorator(argument);
                                    if(url != null){
                                        String qualifiedName = null;
                                        String canonicalName = resolve.getClass().getCanonicalName();
                                        if(Objects.equals("com.jetbrains.python.psi.impl.PyFunctionImpl", canonicalName) || Objects.equals("com.jetbrains.python.psi.PyTargetExpression", canonicalName)){
                                            PsiElement reference =  callee.getReference().resolve();
                                            qualifiedName = (String) ReflectUtils.invokeMethod(reference, "getQualifiedName");
                                        }
                                        if(qualifiedName != null && qualifiedName.startsWith("flask.sansio.scaffold.Scaffold.")){
                                            resultList.add(new OtherRequestEntity(pyDecorator, url,FlaskMethods.getMethodType(name)));
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
