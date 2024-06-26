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

package io.github.zjay.plugin.quickrequest.configurable;

import com.intellij.util.messages.Topic;
import io.github.zjay.plugin.quickrequest.model.CollectionConfiguration;
import io.github.zjay.plugin.quickrequest.model.HistoryTableData;

public interface ConfigChangeNotifier {

    Topic<ConfigChangeNotifier> PARAM_CHANGE_TOPIC = Topic.create("param change", ConfigChangeNotifier.class);
    Topic<ConfigChangeNotifier> ENV_PROJECT_CHANGE_TOPIC = Topic.create("env project change", ConfigChangeNotifier.class);

    Topic<ConfigChangeNotifier> ADD_REQUEST_TOPIC = Topic.create("add request", ConfigChangeNotifier.class);

    Topic<ConfigChangeNotifier> LOAD_REQUEST = Topic.create("load request", ConfigChangeNotifier.class);

    Topic<ConfigChangeNotifier> LOAD_REQUEST_HISTORY = Topic.create("load request history", ConfigChangeNotifier.class);

    Topic<ConfigChangeNotifier> FILTER_MODULE = Topic.create("filter module", ConfigChangeNotifier.class);

    default void configChanged(boolean active, String projectName) {

    }

    default void loadRequest(CollectionConfiguration.CollectionDetail detail, String projectName, boolean sendFlag, boolean flag) {

    }

    default void loadRequestHistory(HistoryTableData data, String projectName, boolean sendFlag, boolean flag) {

    }

}