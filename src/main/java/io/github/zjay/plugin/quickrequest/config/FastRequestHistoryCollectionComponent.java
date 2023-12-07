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

package io.github.zjay.plugin.quickrequest.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.github.zjay.plugin.quickrequest.model.HistoryTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Objects;

@State(name = "quickRequestHisCollections", storages = {@Storage("quickRequestHisApi.xml")})
public class FastRequestHistoryCollectionComponent implements PersistentStateComponent<HistoryTable> {
    private HistoryTable config;

    @Override
    public @Nullable HistoryTable getState() {
        if(config == null){
            config = new HistoryTable();
            config.setList(new LinkedList<>());
            return config;
        }
        return config;
    }

    @Override
    public void loadState(@NotNull HistoryTable state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }

    public static FastRequestHistoryCollectionComponent getInstance(Project project) {
        return ApplicationManager.getApplication().getService(FastRequestHistoryCollectionComponent.class);
    }

}
