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

import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.zjay.plugin.quickrequest.generator.linemarker.RustLineMarkerProvider;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;

import java.util.*;

public class RustRequestMappingContributor extends OtherRequestMappingByNameContributor {


    @Override
    List<OtherRequestEntity> getPsiElementSearchers(Project project) {
        return getResultList(project);
    }

    public static List<OtherRequestEntity> getResultList(Project project){
        List<OtherRequestEntity> resultList = new LinkedList<>();
        try {
            PsiManager psiManager = PsiManager.getInstance(project);
            Collection<VirtualFile> virtualFiles = FilenameIndex.getAllFilesByExt(project, "rs", GlobalSearchScope.projectScope(project));
            try {
                virtualFiles.forEach(virtualFile -> {
                    PsiFile file = psiManager.findFile(virtualFile);
                    PsiElement[] psiElements = PsiTreeUtil.collectElements(file, psiElement -> true);
                    for (PsiElement psiElement : psiElements) {
                        if(Objects.equals(psiElement.getClass().getCanonicalName(), "org.rust.lang.core.psi.impl.RsOuterAttrImpl")){
                            String[] urlAndMethodType = RustLineMarkerProvider.getUrlAndMethodType(psiElement);
                            if(urlAndMethodType != null){
                                resultList.add(new OtherRequestEntity(psiElement, urlAndMethodType[2], urlAndMethodType[1]));
                            }
                        }
                    }
                });
            }catch (Exception e){
            }
        }catch (Exception e){

        }
        return resultList;
    }



}
