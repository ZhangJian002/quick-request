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

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class OtherRequestMappingByNameContributor implements ChooseByNameContributor {
    abstract List<OtherRequestEntity> getPsiElementSearchers(Project project);
    private List<RequestMappingItem> navigationItems = new ArrayList<>();
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        navigationItems = findRequestMappingItems(project);
        return navigationItems.stream().filter(q -> selectedModules.isEmpty() || selectedModules.contains(q.getModuleName()))
                .map(RequestMappingItem::getName).distinct().toArray(String[]::new);
    }

    // 增加一个内存中的过滤状态（由外部通过 Model 或 Contributor 实例设置）
    private Set<String> selectedModules = new HashSet<>();

    public void setSelectedModules(Set<String> modules) {
        this.selectedModules = modules;
    }


    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        return navigationItems.stream().filter(q -> q.getName().equals(name))
                .filter(q -> selectedModules.isEmpty() || selectedModules.contains(q.getModuleName())).toArray(RequestMappingItem[]::new);
    }

    private List<RequestMappingItem> findRequestMappingItems(Project project) {
        List<OtherRequestEntity> psiSearchers = getPsiElementSearchers(project);
        List<RequestMappingItem> resultList = new ArrayList<>();
        for (OtherRequestEntity psiSearcher : psiSearchers) {
            resultList.add(new RequestMappingItem(psiSearcher.getElement(),psiSearcher.getUrlPath(),psiSearcher.getMethod()));
        }
        return resultList;

    }

}
