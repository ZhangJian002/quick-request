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

package io.github.zjay.plugin.quickrequest.view.sub;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.*;
import io.github.zjay.plugin.quickrequest.deprecated.MyComponentPanelBuilder;
import io.github.zjay.plugin.quickrequest.deprecated.MyPanelGridBuilder;
import io.github.zjay.plugin.quickrequest.model.DataMapping;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.util.MyResourceBundleUtil;
import io.github.zjay.plugin.quickrequest.view.AbstractConfigurableView;
import io.github.zjay.plugin.quickrequest.view.inner.UrlReplaceAddView;
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

    private Boolean needInterface = null;

    /**
     * 是否需要在启动时自动生成配置，仅对spring boot项目生效
     */
    private Boolean noNeedAutoGenerateConfig = null;

    private Integer connectionTimeout = null;

    private Integer readTimeout = null;
    private JBTable urlReplaceTable;
    private FastRequestConfiguration configOld;

    private JTextField connectionTimeoutText;
    private JTextField readTimeoutText;


    private Integer jmhConnectionTimeout = null;
    private Integer jmhReadTimeout = null;
    private Integer jmhWriteTimeout = null;
    private Integer threads = null;
    private Integer testCount = null;
    private JTextField jmhConnectionTimeoutText;
    private JTextField jmhReadTimeoutText;
    private JTextField jmhWriteTimeoutText;
    private JTextField jmhThreadsText;
    private JTextField jmhTestCountText;

    private JBCheckBox clickAndSendCheckBox ;
    private JBCheckBox needInterfaceCheckBox ;
    private JBCheckBox noNeedAutoGenerateConfigCheckBox;

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
        panel.add(createConnectionPanel(), gb.nextLine().fillCell().weighty(1.0));
        panel.add(createMyTablePanel(), gb.nextLine().fillCell().weighty(1.0));
        panel.add(createBasePanel(), gb.nextLine().fillCell().weighty(1.0));
        panel.add(createJmhPanel(), gb.nextLine().fillCell().weighty(1.0));
        return panel;
    }


    private JPanel createConnectionPanel() {
        connectionTimeout = config.getConnectionTimeout();
        readTimeout = config.getReadTimeout();
        connectionTimeoutText = new JTextField("30");
        readTimeoutText = new JTextField("30");
        if(connectionTimeout != null){
            connectionTimeoutText.setText(connectionTimeout + "");
        }
        if(readTimeout != null){
            readTimeoutText.setText(readTimeout + "");
        }
        connectionTimeoutText.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = connectionTimeoutText.getText();
                try {
                    connectionTimeout = Integer.parseInt(text);
                    if(connectionTimeout < 0){
                        throw new Exception("Positive integer required");
                    }
                    return true;
                } catch (Exception e) {
                    Messages.showMessageDialog("Positive integer required", "Error", Messages.getInformationIcon());
                    connectionTimeoutText.setText("30");
                    return false;
                }
            }
        });
        readTimeoutText.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = readTimeoutText.getText();
                try {
                    readTimeout = Integer.parseInt(text);
                    if(readTimeout < 0){
                        throw new Exception("Positive integer required");
                    }
                    return true;
                } catch (Exception e) {
                    Messages.showMessageDialog("Positive integer required", "Error", Messages.getInformationIcon());
                    readTimeoutText.setText("30");
                    return false;
                }
            }
        });
        JPanel connectionConfigPanel = new MyPanelGridBuilder()
                .add(new MyComponentPanelBuilder(connectionTimeoutText).withLabel("ConnectionTimeout"))
                .add(new MyComponentPanelBuilder(readTimeoutText).withLabel("ReadTimeout"))
                .createPanel();
        return connectionConfigPanel;
    }

    private JPanel createBasePanel() {
        this.clickAndSendCheckBox = createClickAndSendPanel();
        this.needInterfaceCheckBox = createNeedInterfacePanel();
        this.noNeedAutoGenerateConfigCheckBox = createNeedAutoGenerateConfigPanel();
        JPanel clickAndSendConfigPanel = new MyPanelGridBuilder()
                .add(new MyComponentPanelBuilder(clickAndSendCheckBox))
                .add(new MyComponentPanelBuilder(needInterfaceCheckBox))
                .add(new MyComponentPanelBuilder(noNeedAutoGenerateConfigCheckBox).withTooltip(MyResourceBundleUtil.getKey("NoNeedAutoGenerateConfigTip")))
                .createPanel();
        clickAndSendConfigPanel.setBorder(IdeBorderFactory.createTitledBorder(MyResourceBundleUtil.getKey("BaseConfig")));
        return clickAndSendConfigPanel;

    }

    private JBCheckBox createClickAndSendPanel() {
        Boolean clickAndSend = config.getClickAndSend();
        this.clickAndSend = clickAndSend;
        JBCheckBox completeCheckBox = new JBCheckBox(MyResourceBundleUtil.getKey("ClickAndSendConfig"), clickAndSend != null && clickAndSend);
        completeCheckBox.addItemListener(e -> {
            this.clickAndSend = e.getStateChange() == ItemEvent.SELECTED;
        });
        return completeCheckBox;
    }

    private JBCheckBox createNeedInterfacePanel() {
        Boolean needInterface = config.getNeedInterface();
        this.needInterface = needInterface;
        JBCheckBox completeCheckBox = new JBCheckBox(MyResourceBundleUtil.getKey("NeedInterfaceConfig"), needInterface != null && needInterface);
        completeCheckBox.addItemListener(e -> {
            this.needInterface = e.getStateChange() == ItemEvent.SELECTED;
        });
        return completeCheckBox;
    }

    private JBCheckBox createNeedAutoGenerateConfigPanel() {
        Boolean noNeedAutoGenerateConfig = config.getNoNeedAutoGenerateConfig();
        this.noNeedAutoGenerateConfig = noNeedAutoGenerateConfig;
        JBCheckBox completeCheckBox = new JBCheckBox(MyResourceBundleUtil.getKey("NoNeedAutoGenerateConfig"), noNeedAutoGenerateConfig != null && noNeedAutoGenerateConfig);
        completeCheckBox.addItemListener(e -> {
            this.noNeedAutoGenerateConfig = e.getStateChange() == ItemEvent.SELECTED;
        });
        return completeCheckBox;
    }

    private JPanel createJmhPanel() {
        jmhConnectionTimeout = config.getJmhConnectionTimeout();
        jmhReadTimeout = config.getJmhReadTimeout();
        jmhWriteTimeout = config.getJmhWriteTimeout();
        threads = config.getThreads();
        testCount = config.getTestCount();

        jmhConnectionTimeoutText = new JTextField("60");
        jmhReadTimeoutText = new JTextField("60");
        jmhWriteTimeoutText = new JTextField("60");
        jmhThreadsText = new JTextField("50");
        jmhTestCountText = new JTextField("5");
        if(jmhConnectionTimeout != null){
            jmhConnectionTimeoutText.setText(jmhConnectionTimeout + "");
        }
        if(jmhReadTimeout != null){
            jmhReadTimeoutText.setText(jmhReadTimeout + "");
        }
        if(jmhWriteTimeout != null){
            jmhWriteTimeoutText.setText(jmhWriteTimeout + "");
        }
        if(threads != null){
            jmhThreadsText.setText(threads + "");
        }
        if(testCount != null){
            jmhTestCountText.setText(testCount + "");
        }
        addInputVerifiers();
        JPanel jmhConfigPanel = new MyPanelGridBuilder()
                .add(new MyComponentPanelBuilder(jmhConnectionTimeoutText).withLabel("ConnectionTimeout").withTooltip(MyResourceBundleUtil.getKey("OkConnectionTimeout")))
                .add(new MyComponentPanelBuilder(jmhReadTimeoutText).withLabel("ReadTimeout").withTooltip(MyResourceBundleUtil.getKey("OkReadTimeout")))
                .add(new MyComponentPanelBuilder(jmhWriteTimeoutText).withLabel("WriteTimeout").withTooltip(MyResourceBundleUtil.getKey("OkWriteTimeout")))
                .add(new MyComponentPanelBuilder(jmhThreadsText).withLabel("Threads").withTooltip(MyResourceBundleUtil.getKey("JmhThreads")))
                .add(new MyComponentPanelBuilder(jmhTestCountText).withLabel("Test times").withTooltip(MyResourceBundleUtil.getKey("JmhTestTimes")))
                .createPanel();
        jmhConfigPanel.setBorder(IdeBorderFactory.createTitledBorder(MyResourceBundleUtil.getKey("PressureTestConfiguration")));
        return jmhConfigPanel;

    }

    private void addInputVerifiers() {
        jmhConnectionTimeoutText.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = jmhConnectionTimeoutText.getText();
                try {
                    jmhConnectionTimeout = Integer.parseInt(text);
                    if(jmhConnectionTimeout <= 0){
                        throw new Exception("Positive integer required");
                    }
                    return true;
                } catch (Exception e) {
                    Messages.showMessageDialog("Positive integer required", "Error", Messages.getInformationIcon());
                    jmhConnectionTimeoutText.setText("60");
                    return false;
                }
            }
        });
        jmhReadTimeoutText.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = jmhReadTimeoutText.getText();
                try {
                    jmhReadTimeout = Integer.parseInt(text);
                    if(jmhReadTimeout <= 0){
                        throw new Exception("Positive integer required");
                    }
                    return true;
                } catch (Exception e) {
                    Messages.showMessageDialog("Positive integer required", "Error", Messages.getInformationIcon());
                    jmhReadTimeoutText.setText("60");
                    return false;
                }
            }
        });
        jmhWriteTimeoutText.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = jmhWriteTimeoutText.getText();
                try {
                    jmhWriteTimeout = Integer.parseInt(text);
                    if(jmhWriteTimeout <= 0){
                        throw new Exception("Positive integer required");
                    }
                    return true;
                } catch (Exception e) {
                    Messages.showMessageDialog("Positive integer required", "Error", Messages.getInformationIcon());
                    jmhWriteTimeoutText.setText("60");
                    return false;
                }
            }
        });
        jmhThreadsText.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = jmhThreadsText.getText();
                try {
                    threads = Integer.parseInt(text);
                    if(threads <= 0){
                        throw new Exception("Positive integer required");
                    }
                    return true;
                } catch (Exception e) {
                    Messages.showMessageDialog("Positive integer required", "Error", Messages.getInformationIcon());
                    jmhThreadsText.setText("50");
                    return false;
                }
            }
        });
        jmhTestCountText.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = jmhTestCountText.getText();
                try {
                    testCount = Integer.parseInt(text);
                    if(testCount <= 0){
                        throw new Exception("Positive integer required");
                    }
                    return true;
                } catch (Exception e) {
                    Messages.showMessageDialog("Positive integer required", "Error", Messages.getInformationIcon());
                    jmhTestCountText.setText("5");
                    return false;
                }
            }
        });
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
        return JBUI.Panels.simplePanel(new MyComponentPanelBuilder(tablePanel)
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

    public Boolean getNeedInterface() {
        return needInterface;
    }

    public void setNeedInterface(Boolean needInterface) {
        this.needInterface = needInterface;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public JTextField getConnectionTimeoutText() {
        return connectionTimeoutText;
    }

    public void setConnectionTimeoutText(JTextField connectionTimeoutText) {
        this.connectionTimeoutText = connectionTimeoutText;
    }

    public JTextField getReadTimeoutText() {
        return readTimeoutText;
    }

    public void setReadTimeoutText(JTextField readTimeoutText) {
        this.readTimeoutText = readTimeoutText;
    }

    public JTextField getJmhConnectionTimeoutText() {
        return jmhConnectionTimeoutText;
    }

    public void setJmhConnectionTimeoutText(JTextField jmhConnectionTimeoutText) {
        this.jmhConnectionTimeoutText = jmhConnectionTimeoutText;
    }

    public JTextField getJmhReadTimeoutText() {
        return jmhReadTimeoutText;
    }

    public void setJmhReadTimeoutText(JTextField jmhReadTimeoutText) {
        this.jmhReadTimeoutText = jmhReadTimeoutText;
    }

    public JTextField getJmhWriteTimeoutText() {
        return jmhWriteTimeoutText;
    }

    public void setJmhWriteTimeoutText(JTextField jmhWriteTimeoutText) {
        this.jmhWriteTimeoutText = jmhWriteTimeoutText;
    }

    public JTextField getJmhThreadsText() {
        return jmhThreadsText;
    }

    public void setJmhThreadsText(JTextField jmhThreadsText) {
        this.jmhThreadsText = jmhThreadsText;
    }

    public JTextField getJmhTestCountText() {
        return jmhTestCountText;
    }

    public void setJmhTestCountText(JTextField jmhTestCountText) {
        this.jmhTestCountText = jmhTestCountText;
    }

    public Integer getJmhConnectionTimeout() {
        return jmhConnectionTimeout;
    }

    public void setJmhConnectionTimeout(Integer jmhConnectionTimeout) {
        this.jmhConnectionTimeout = jmhConnectionTimeout;
    }

    public Integer getJmhReadTimeout() {
        return jmhReadTimeout;
    }

    public void setJmhReadTimeout(Integer jmhReadTimeout) {
        this.jmhReadTimeout = jmhReadTimeout;
    }

    public Integer getJmhWriteTimeout() {
        return jmhWriteTimeout;
    }

    public void setJmhWriteTimeout(Integer jmhWriteTimeout) {
        this.jmhWriteTimeout = jmhWriteTimeout;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public Integer getTestCount() {
        return testCount;
    }

    public void setTestCount(Integer testCount) {
        this.testCount = testCount;
    }

    public Boolean getNoNeedAutoGenerateConfig() {
        return noNeedAutoGenerateConfig;
    }

    public void setNoNeedAutoGenerateConfig(Boolean noNeedAutoGenerateConfig) {
        this.noNeedAutoGenerateConfig = noNeedAutoGenerateConfig;
    }

    public JBCheckBox getClickAndSendCheckBox() {
        return clickAndSendCheckBox;
    }

    public JBCheckBox getNeedInterfaceCheckBox() {
        return needInterfaceCheckBox;
    }

    public JBCheckBox getNoNeedAutoGenerateConfigCheckBox() {
        return noNeedAutoGenerateConfigCheckBox;
    }
}
