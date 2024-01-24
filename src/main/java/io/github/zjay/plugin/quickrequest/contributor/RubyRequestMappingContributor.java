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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.zjay.plugin.quickrequest.generator.linemarker.RubyLineMarkerProvider;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.ruby.RailsMethods;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.blocks.RCompoundStatementImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall.RCallImpl;
import java.util.LinkedList;
import java.util.List;

public class RubyRequestMappingContributor extends OtherRequestMappingByNameContributor{


    @Override
    List<OtherRequestEntity> getPsiElementSearchers(Project project) {
        return getResultList(project);
    }

    public static List<OtherRequestEntity> getResultList(Project project){
        List<OtherRequestEntity> resultList = new LinkedList<>();
        try {
            Class.forName("org.jetbrains.plugins.ruby.rails.model.RailsApp");
            PsiManager psiManager = PsiManager.getInstance(project);
            ModuleManager moduleManager = ModuleManager.getInstance(project);
            for (Module module : moduleManager.getModules()) {
                RailsApp railsApp = RailsApp.fromModule(module);
                if(railsApp != null){
                    railsApp.getRoutesFiles().allFiles().forEach(virtualFile -> {
                        if(project.getBasePath() != null && virtualFile.getPath().startsWith(project.getBasePath())){
                            PsiElement[] psiElements = PsiTreeUtil.collectElements(psiManager.findFile(virtualFile), psiElement -> true);
                            for (PsiElement psiElement : psiElements) {
                                if(psiElement instanceof RCallImpl){
                                    if(psiElement.getParent().getParent().getParent().getParent().getParent() instanceof RCompoundStatementImpl){
                                        RCompoundStatementImpl parent = (RCompoundStatementImpl) psiElement.getParent().getParent().getParent().getParent().getParent();
                                        if(parent.getText().contains("Rails.application.routes.draw")){
                                            RCallImpl rCall = (RCallImpl) psiElement;
                                            if(RailsMethods.isExist(rCall.getName())){
                                                String[] urlAndMethodType = RubyLineMarkerProvider.getUrlAndMethodType(rCall);
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
