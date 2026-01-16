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

import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.DefaultChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiElement;
import io.github.zjay.plugin.quickrequest.util.ReflectUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RequestMappingModel extends FilteringGotoByModel<String> {

    private final Set<String> selectedModuleNames = new HashSet<>();
    private final List<ChooseByNameContributor> contributors;


    protected RequestMappingModel(@NotNull Project project, @NotNull List<ChooseByNameContributor> contributors) {
        super(project, contributors);
        this.contributors = contributors;
    }

    public void setModuleSelected(com.intellij.openapi.module.Module module, boolean selected) {
        if (selected) selectedModuleNames.add(module.getName());
        else selectedModuleNames.remove(module.getName());

        for (ChooseByNameContributor contributor : contributors) {
            ReflectUtils.invokeMethod(contributor, "setSelectedModules", Set.class, new HashSet<>(selectedModuleNames));
        }
    }

    public void clearModuleSelected() {
        selectedModuleNames.clear();

        for (ChooseByNameContributor contributor : contributors) {
            ReflectUtils.invokeMethod(contributor, "setSelectedModules", Set.class, new HashSet<>(selectedModuleNames));
        }
    }

    public boolean isModuleSelected(com.intellij.openapi.module.Module module) {
        // 初始状态可以设为全选或全不选
        return selectedModuleNames.contains(module.getName());
    }

    @Override
    protected @Nullable String filterValueFor(NavigationItem item) {
        // 如果你的 item 是 RequestMappingItem，在这里返回它所属的模块名
        if (item instanceof RequestMappingItem) {
            com.intellij.openapi.module.Module module = ModuleUtil.findModuleForPsiElement(((RequestMappingItem) item).getPsiElement());
            return module != null ? module.getName() : null;
        }
        return null;
    }

    @Override
    public @NotNull ChooseByNameItemProvider getItemProvider(@Nullable PsiElement context) {
        return new DefaultChooseByNameItemProvider(context);
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getPromptText() {
        return "Enter mapping url";
    }

    @Override
    public @NotNull @NlsContexts.Label String getNotInMessage() {
        return "No matches found";
    }

    @Override
    public @NotNull @NlsContexts.Label String getNotFoundMessage() {
        return "Mapping not found";
    }

    @Override
    public @Nullable @NlsContexts.Label String getCheckBoxName() {
        return null;
    }

    @Override
    public boolean loadInitialCheckBoxState() {
        return false;
    }

    @Override
    public void saveInitialCheckBoxState(boolean state) {

    }

    @Override
    protected synchronized @Nullable Collection<String> getFilterItems() {
        return super.getFilterItems();
    }



    @Override
    public String[] getSeparators() {
        return new String[0];
    }

    @Override
    public @Nullable String getFullName(@NotNull Object element) {
        return getElementName(element);
    }

    @Override
    public boolean willOpenEditor() {
        return false;
    }
}
