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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.zjay.plugin.quickrequest.generator.linemarker.RubyLineMarkerProvider;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.ReflectUtils;
import io.github.zjay.plugin.quickrequest.util.ruby.RailsMethods;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class RubyRequestMappingContributor extends OtherRequestMappingByNameContributor{


    @Override
    List<OtherRequestEntity> getPsiElementSearchers(Project project) {
        return getResultList(project);
    }

    public static List<OtherRequestEntity> getResultList(Project project){
        List<OtherRequestEntity> resultList = new LinkedList<>();
        try {
            Class<?> railsAppClass = Class.forName("org.jetbrains.plugins.ruby.rails.model.RailsApp");
            PsiManager psiManager = PsiManager.getInstance(project);
            ModuleManager moduleManager = ModuleManager.getInstance(project);
            for (Module module : moduleManager.getModules()) {
                Object railsApp = ReflectUtils.invokeStaticMethod(railsAppClass, "fromModule", Module.class, module);
                if(railsApp != null){
                    Object getRoutesFiles = ReflectUtils.invokeMethod(railsApp, "getRoutesFiles");
                    Stream<VirtualFile> allFilesStream = (Stream<VirtualFile>)ReflectUtils.invokeMethod(getRoutesFiles, "allFiles");
                    allFilesStream.forEach(virtualFile -> {
                        if(project.getBasePath() != null && virtualFile.getPath().startsWith(project.getBasePath())){
                            PsiElement[] psiElements = PsiTreeUtil.collectElements(psiManager.findFile(virtualFile), psiElement -> true);
                            for (PsiElement psiElement : psiElements) {
                                if(Objects.equals("org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall.RCallImpl", psiElement.getClass().getCanonicalName())){
                                    PsiElement element = psiElement.getParent().getParent().getParent().getParent().getParent();
                                    String canonicalName = element.getClass().getCanonicalName();
                                    if(Objects.equals("org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.blocks.RCompoundStatementImpl", canonicalName)){
                                        String text = (String)ReflectUtils.invokeMethod(element, "getText");
                                        if(text.contains("Rails.application.routes.draw")){
                                            String name = (String)ReflectUtils.invokeMethod(psiElement, "getName");
                                            if(RailsMethods.isExist(name)){
                                                String[] urlAndMethodType = RubyLineMarkerProvider.getUrlAndMethodType(psiElement);
                                                resultList.add(new OtherRequestEntity(psiElement, urlAndMethodType[0],urlAndMethodType[1]));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }catch (Exception e){

        }
        return resultList;
    }



}
