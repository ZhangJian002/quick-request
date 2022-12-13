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

package io.github.zjay.plugin.fastrequest.view.sub;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.*;
import io.github.zjay.plugin.fastrequest.model.DataMapping;
import io.github.zjay.plugin.fastrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.fastrequest.util.MyResourceBundleUtil;
import io.github.zjay.plugin.fastrequest.util.UrlUtil;
import io.github.zjay.plugin.fastrequest.view.AbstractConfigurableView;
import io.github.zjay.plugin.fastrequest.view.inner.UrlReplaceAddView;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OtherConfigView extends AbstractConfigurableView {

    private List<DataMapping> viewUrlReplaceMappingList = new LinkedList<>();

    private Boolean clickAndSend = null;
    private JBTable urlReplaceTable;
    private FastRequestConfiguration configOld;


    public OtherConfigView(FastRequestConfiguration config) {
        super(config);
        setLayout(new BorderLayout());
        add(createMainComponent());
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    private JComponent createMainComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBag gb = new GridBag()
                .setDefaultInsets(JBUI.insets(0, 0, 4, 10))
                .setDefaultWeightX(1)
                .setDefaultFill(GridBagConstraints.HORIZONTAL);
        panel.add(createMyTablePanel(), gb.nextLine().fillCell().weighty(1.0));
        panel.add(createBasePanel(), gb.nextLine().fillCell().weighty(1.0));
        return panel;
    }

    private JPanel createBasePanel() {
        Boolean clickAndSend = config.getClickAndSend();
        this.clickAndSend = clickAndSend;
        JPanel jPanel = new JPanel();
        JCheckBox completeCheckBox = new JCheckBox("点击生成请求按钮时是否直接发起", clickAndSend != null && clickAndSend);
        completeCheckBox.addItemListener(e -> {
            this.clickAndSend = e.getStateChange() == ItemEvent.SELECTED;
        });
        jPanel.add(completeCheckBox);
        return jPanel;
    }

    private JPanel createMyTablePanel() {
        FastRequestConfiguration configOld = JSONObject.parseObject(JSONObject.toJSONString(config), FastRequestConfiguration.class);
        viewUrlReplaceMappingList.addAll(configOld.getUrlReplaceMappingList());

        JBTable table = createTable();
        table.getEmptyText().setText("Target:${api-module}  replacement:base");
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(table, null);
        toolbarDecorator.setMoveDownAction(null);
        toolbarDecorator.setMoveUpAction(null);

        toolbarDecorator.setAddAction(event -> {
            UrlReplaceAddView dataMappingAddView = new UrlReplaceAddView();
            if (dataMappingAddView.showAndGet()) {
                DataMapping dataMapping = dataMappingAddView.getValue();
                if (viewUrlReplaceMappingList.stream().anyMatch(q -> dataMapping.getType().equals(q.getType()))) {
                    Messages.showMessageDialog("Target already exist", "Error", Messages.getInformationIcon());
                    return;
                }
                viewUrlReplaceMappingList.add(dataMapping);
                table.setModel(new ListTableModel<>(getColumnInfo(), viewUrlReplaceMappingList));
                setUrlReplaceTable(table);
            }
        }).setRemoveAction(event -> {
            int selectedRow = table.getSelectedRow();
            viewUrlReplaceMappingList.remove(selectedRow);
            table.setModel(new ListTableModel<>(getColumnInfo(), viewUrlReplaceMappingList));
            setUrlReplaceTable(table);
        }).setToolbarPosition(ActionToolbarPosition.TOP);
        JPanel tablePanel = toolbarDecorator.createPanel();
        return JBUI.Panels.simplePanel(UI.PanelFactory.panel(tablePanel)
                .withLabel(MyResourceBundleUtil.getKey("UrlReplaceConfig")).moveLabelOnTop()
                .withComment(MyResourceBundleUtil.getKey("OtherConfigTitle1") + " " + MyResourceBundleUtil.getKey("OtherConfigTitle2"), false).resizeY(true).createPanel());
    }

    public JBTable createTable() {
        ColumnInfo<Object, Object>[] columns = getColumnInfo();
        ListTableModel<DataMapping> model = new ListTableModel<>(columns, viewUrlReplaceMappingList);
        JBTable table = new JBTable(model) {
            @Override
            public Object getValueAt(int row, int column) {
                if (viewUrlReplaceMappingList.isEmpty()) {
                    return StringUtils.EMPTY;
                }
                DataMapping dataMapping = viewUrlReplaceMappingList.get(row);
                if (dataMapping == null) {
                    return StringUtils.EMPTY;
                }
                if (column == 0) {
                    return dataMapping.getType();
                } else {
                    return dataMapping.getValue();
                }
            }
        };
        table.setVisible(true);
        return table;
    }

    public ColumnInfo<Object, Object>[] getColumnInfo() {
        ArrayList<String> columnListName = Lists.newArrayList("Target", "Replacement");
        ColumnInfo<Object, Object>[] columnArray = new ColumnInfo[columnListName.size()];
        for (int i = 0; i < columnListName.size(); i++) {
            ColumnInfo<Object, Object> envColumn = new ColumnInfo<>(columnListName.get(i)) {
                @Override
                public @Nullable Object valueOf(Object o) {
                    return o;
                }
            };
            columnArray[i] = envColumn;
        }
        return columnArray;
    }

    public JBTable getUrlReplaceTable() {
        return urlReplaceTable;
    }

    public void setUrlReplaceTable(JBTable urlReplaceTable) {
        this.urlReplaceTable = urlReplaceTable;
    }

    public List<DataMapping> getViewUrlReplaceMappingList() {
        return viewUrlReplaceMappingList;
    }

    public void setViewUrlReplaceMappingList(List<DataMapping> viewUrlReplaceMappingList) {
        this.viewUrlReplaceMappingList = viewUrlReplaceMappingList;
    }

    public Boolean getClickAndSend() {
        return clickAndSend;
    }

    public void setClickAndSend(Boolean clickAndSend) {
        this.clickAndSend = clickAndSend;
    }
}
