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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.intellij.util.ui.ListTableModel;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.model.DataMapping;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.view.AbstractConfigurableView;
import io.github.zjay.plugin.quickrequest.view.sub.OtherConfigView;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class OtherConfigConfigurable extends AbstractConfigConfigurable {
    protected FastRequestConfiguration config;
    private final OtherConfigView view;

    public OtherConfigConfigurable() {
        config = FastRequestComponent.getInstance().getState();
        view = new OtherConfigView(config);
    }

    @Override
    public AbstractConfigurableView getView() {
        return view;
    }

    @Override
    public @Nullable
    JComponent createComponent() {
        return view.getComponent();
    }

    @Override
    public String getDisplayName() {
        return ResourceBundle.getBundle(Constant.I18N_PATH).getString("OtherConfig");
    }

    @Override
    public boolean isModified() {
        List<DataMapping> currentUrlReplaceMappingList = view.getViewUrlReplaceMappingList();
        List<DataMapping> urlReplaceMappingList = config.getUrlReplaceMappingList();
        if(!Objects.equals(view.getClickAndSend(), config.getClickAndSend())){
            return true;
        }
        if(!Objects.equals(view.getNeedInterface(), config.getNeedInterface())){
            return true;
        }
        if(!Objects.equals(view.getNoNeedAutoGenerateConfig(), config.getNoNeedAutoGenerateConfig())){
            return true;
        }
        if(!Objects.equals(view.getConnectionTimeoutText().getText(), config.getConnectionTimeout() + "")){
            return true;
        }
        if(!Objects.equals(view.getReadTimeoutText().getText(), config.getReadTimeout() + "")){
            return true;
        }
        if(!Objects.equals(view.getJmhConnectionTimeoutText().getText(), config.getJmhConnectionTimeout() + "")){
            return true;
        }
        if(!Objects.equals(view.getJmhReadTimeoutText().getText(), config.getJmhReadTimeout() + "")){
            return true;
        }
        if(!Objects.equals(view.getJmhWriteTimeoutText().getText(), config.getJmhWriteTimeout() + "")){
            return true;
        }
        if(!Objects.equals(view.getJmhThreadsText().getText(), config.getThreads() + "")){
            return true;
        }
        if(!Objects.equals(view.getJmhTestCountText().getText(), config.getTestCount() + "")){
            return true;
        }
        if(!Objects.equals(view.getGrpcurlPath(), config.getGrpcurlPath())){
            return true;
        }

        if(!Objects.equals(view.getNeedIdeaLog(), config.getNeedIdeaLog())){
            return true;
        }

        return !judgeEqual(currentUrlReplaceMappingList, urlReplaceMappingList);
    }

    @Override
    public void apply() {
        List<DataMapping> viewUrlReplaceMappingList = view.getViewUrlReplaceMappingList();
        List<DataMapping> changeUrlReplaceMappingList = JSONArray.parseArray(JSON.toJSONString(viewUrlReplaceMappingList), DataMapping.class);
        config.setUrlReplaceMappingList(changeUrlReplaceMappingList);
        config.setClickAndSend(view.getClickAndSend());
        config.setNeedInterface(view.getNeedInterface());
        config.setNoNeedAutoGenerateConfig(view.getNoNeedAutoGenerateConfig());
        config.setNeedIdeaLog(view.getNeedIdeaLog());
        config.setConnectionTimeout(view.getConnectionTimeout());
        config.setReadTimeout(view.getReadTimeout());
        config.setJmhConnectionTimeout(view.getJmhConnectionTimeout());
        config.setJmhReadTimeout(view.getJmhReadTimeout());
        config.setJmhWriteTimeout(view.getJmhWriteTimeout());
        config.setThreads(view.getThreads());
        config.setTestCount(view.getTestCount());
        config.setGrpcurlPath(view.getGrpcurlPath());
    }

    @Override
    public void reset() {
        super.reset();
        List<DataMapping> oldUrlReplaceMappingList = config.getUrlReplaceMappingList();
        List<DataMapping> oldUrlReplaceMappingListNew = JSONArray.parseArray(JSON.toJSONString(oldUrlReplaceMappingList), DataMapping.class);
        if(view.getUrlReplaceTable() != null){
            view.setViewUrlReplaceMappingList(oldUrlReplaceMappingListNew);
            view.getUrlReplaceTable().setModel(new ListTableModel<>(view.getColumnInfo(), oldUrlReplaceMappingListNew));
        }
        view.setReadTimeout(60);
        view.setConnectionTimeout(60);
        view.setClickAndSend(false);
        view.setNeedInterface(false);
        view.setNoNeedAutoGenerateConfig(false);
        view.setNeedIdeaLog(false);
        view.setJmhConnectionTimeout(60);
        view.setJmhReadTimeout(60);
        view.setJmhWriteTimeout(60);
        view.setThreads(50);
        view.setTestCount(5);
        view.setGrpcurlPath(null);
    }

    public boolean judgeEqual(List<DataMapping> list1, List<DataMapping> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list1.size(); i++) {
            DataMapping dataMapping1 = list1.get(i);
            DataMapping dataMapping2 = list2.get(i);
            if (!dataMapping1.getType().equals(dataMapping2.getType()) || !dataMapping1.getValue().equals(dataMapping2.getValue())) {
                return false;
            }
        }
        return true;
    }

}
