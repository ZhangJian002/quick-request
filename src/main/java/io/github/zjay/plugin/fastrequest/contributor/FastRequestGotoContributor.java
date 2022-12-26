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

package io.github.zjay.plugin.fastrequest.contributor;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.searcheverywhere.*;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import io.github.zjay.plugin.fastrequest.idea.SearchEverywherePsiRenderer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FastRequestGotoContributor extends AbstractGotoSEContributor  {
    private Project myProject;
    private final RequestMappingModel requestMappingModel;

    protected FastRequestGotoContributor(@NotNull AnActionEvent event) {
        super(event);
        myProject = event.getProject();
        requestMappingModel = new RequestMappingModel(myProject, ExtensionPointName.<ChooseByNameContributor>create("QuickRequest.requestMappingContributor").getExtensionList());
    }


    public @NotNull ListCellRenderer<Object> getElementsRenderer() {
        return new SearchEverywherePsiRenderer(this){

            @Override
            protected DefaultListCellRenderer getRightCellRenderer(final Object value) {
                if(value instanceof RequestMappingItem){
                    RequestMappingItem item = (RequestMappingItem) value;
                    PsiElement psiElement = item.getPsiElement();
                    Module module = ModuleUtil.findModuleForPsiElement(psiElement);
                    if(module == null){
                        return super.getRightCellRenderer(value);
                    }


                    return new DefaultListCellRenderer(){
                        @Override
                        public Icon getIcon() {
                            return AllIcons.Nodes.Module;
                        }

                        @Override
                        public String getText() {
                            return module.getName();
                        }
                    };
                }
                return super.getRightCellRenderer(value);
            }

            /**
            protected @Nullable TextWithIcon getItemLocation(Object value) {
                if(value instanceof RequestMappingItem){
                    RequestMappingItem item = (RequestMappingItem) value;
                    PsiElement psiElement = item.getPsiElement();
                    Module module = ModuleUtil.findModuleForPsiElement(psiElement);
                    if(module == null){
                        return super.getItemLocation(value);
                    }
                    return new TextWithIcon(module.getName(), AllIcons.Nodes.Module);
                }
                return super.getItemLocation(value);
            }
//             */
        };
    }

    public @Nullable @Nls String getAdvertisement() {
        return "type [/url] to search";
    }

    protected @NotNull FilteringGotoByModel<?> createModel(@NotNull Project project) {
        return requestMappingModel;
    }

    public @NotNull @Nls String getGroupName() {
        return "Quick Request";
    }

    public int getSortWeight() {
        return 1000;
    }

    public boolean showInFindResults() {
        return false;
    }

    public int getElementPriority(@NotNull Object element, @NotNull String searchPattern) {
        return super.getElementPriority(element, searchPattern) + 5;
    }

    static class Factory implements SearchEverywhereContributorFactory<Object>{

        public @NotNull SearchEverywhereContributor<Object> createContributor(@NotNull AnActionEvent initEvent) {
            return new FastRequestGotoContributor(initEvent);
        }


    }
}
