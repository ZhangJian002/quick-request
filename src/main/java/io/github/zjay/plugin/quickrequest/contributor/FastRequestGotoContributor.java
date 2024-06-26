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

import com.intellij.ide.actions.SearchEverywherePsiRenderer;
import com.intellij.ide.actions.searcheverywhere.*;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.ide.util.gotoByName.GotoFileModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiJavaFile;
import io.github.zjay.plugin.quickrequest.util.FrIconUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class FastRequestGotoContributor extends AbstractGotoSEContributor  {
    private Project myProject;
    private final RequestMappingModel requestMappingModel;

    private final GotoFileModel myModelForRenderer;

    protected FastRequestGotoContributor(@NotNull AnActionEvent event) {
        super(event);
        myProject = event.getProject();
        myModelForRenderer = new MyGotoFileModel(myProject);
        requestMappingModel = new RequestMappingModel(myProject, ExtensionPointName.<ChooseByNameContributor>create("QuickRequest.requestMappingContributor").getExtensionList());
    }

    private class MyGotoFileModel extends GotoFileModel{

        public MyGotoFileModel(@NotNull Project project) {
            super(project);
        }

        @Override
        public @NotNull String getNotFoundMessage() {
            return "Not found";
        }

    }


    public @NotNull ListCellRenderer<Object> getElementsRenderer() {
        return new SearchEverywherePsiRenderer(this) {
            @NotNull
            @Override
            public ItemMatchers getItemMatchers(@NotNull JList list, @NotNull Object value) {
                ItemMatchers defaultMatchers = super.getItemMatchers(list, value);
                if (!(value instanceof PsiFileSystemItem) || myModelForRenderer == null) {
                    return defaultMatchers;
                }

                return GotoFileModel.convertToFileItemMatchers(defaultMatchers, (PsiFileSystemItem)value, myModelForRenderer);
            }

            @Override
            protected Icon getIcon(PsiElement element) {
                if(element instanceof RequestMappingItem){
                    return getMethodIcon((RequestMappingItem)element);
                }
                return super.getIcon(element);
            }
        };
    }

    public @Nullable @Nls String getAdvertisement() {
        return null;
    }

    protected @NotNull FilteringGotoByModel<?> createModel(@NotNull Project project) {
        return requestMappingModel;
    }

    public @NotNull @Nls String getGroupName() {
        return "Apis";
    }

    public int getSortWeight() {
        return 1000;
    }

    public boolean showInFindResults() {
        return true;
    }

    public int getElementPriority(@NotNull Object element, @NotNull String searchPattern) {
        return super.getElementPriority(element, searchPattern) + 5;
    }

    private Icon getMethodIcon(RequestMappingItem requestMappingItem) {
        PsiFile containingFile = requestMappingItem.getPsiElement().getContainingFile();
        if(Objects.equals("com.intellij.psi.impl.source.PsiJavaFileImpl", containingFile.getClass().getCanonicalName())){
            PsiJavaFile psiJavaFile = (PsiJavaFile) containingFile;
            return FrIconUtil.getIconByMethodAndClassType(requestMappingItem.getRequestMethod(), psiJavaFile.getClasses()[0].isInterface());
        }else {
            return FrIconUtil.getIconByMethodType(requestMappingItem.getRequestMethod());
        }

    }

    static class Factory implements SearchEverywhereContributorFactory<Object>{

        public @NotNull SearchEverywhereContributor<Object> createContributor(@NotNull AnActionEvent initEvent) {
            return new FastRequestGotoContributor(initEvent);
        }


    }
}
