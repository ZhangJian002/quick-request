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

package io.github.zjay.plugin.quickrequest.view;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.actions.RevealFileAction;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.progress.util.ColorProgressBar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.*;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.dualView.TreeTableView;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import groovy.json.StringEscapeUtils;
import io.github.zjay.plugin.quickrequest.action.*;
import io.github.zjay.plugin.quickrequest.action.soft_wrap.BodyFormatAction;
import io.github.zjay.plugin.quickrequest.base.ParentAction;
import io.github.zjay.plugin.quickrequest.complete.GeneralTextAutoCompleteEditor;
import io.github.zjay.plugin.quickrequest.config.*;
import io.github.zjay.plugin.quickrequest.configurable.ConfigChangeNotifier;
import io.github.zjay.plugin.quickrequest.dubbo.DubboService;
import io.github.zjay.plugin.quickrequest.grpc.GrpcCurlUtils;
import io.github.zjay.plugin.quickrequest.grpc.GrpcRequest;
import io.github.zjay.plugin.quickrequest.jmh.JMHTest;
import io.github.zjay.plugin.quickrequest.model.*;
import io.github.zjay.plugin.quickrequest.util.*;
import io.github.zjay.plugin.quickrequest.util.file.FileUtil;
import io.github.zjay.plugin.quickrequest.util.http.BodyContentType;
import io.github.zjay.plugin.quickrequest.util.http.Header;
import io.github.zjay.plugin.quickrequest.util.http.UrlQuery;
import io.github.zjay.plugin.quickrequest.util.thead.GlobalThreadPool;
import io.github.zjay.plugin.quickrequest.view.component.CheckBoxHeader;
import io.github.zjay.plugin.quickrequest.view.component.MyLanguageTextField;
import io.github.zjay.plugin.quickrequest.view.component.MyParamCheckItemListener;
import io.github.zjay.plugin.quickrequest.view.inner.EnvAddView;
import io.github.zjay.plugin.quickrequest.view.inner.HeaderGroupView;
import io.github.zjay.plugin.quickrequest.view.inner.SupportView;
import io.github.zjay.plugin.quickrequest.view.ui.MethodFontListCellRenderer;
import okhttp3.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartPanel;
import quickRequest.icons.PluginIcons;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * FastRequest工具窗口
 *
 * @author Kings
 * @date 2021/06/02
 * @see SimpleToolWindowPanel
 */
public class FastRequestToolWindow extends SimpleToolWindowPanel {
    private static final Logger LOGGER = Logger.getInstance(CommonConfigView.class);
    public static final int JSON_TABLE_COLUMN_NAME_WIDTH = 200;
    public static final int JSON_TABLE_COLUMN_TYPE_WIDTH = 80;
    public static final String NO_ENV = "<No Env>";
    public static final String NO_PROJECT = "<No Project>";
    public static final int MAX_DATA_LENGTH = 5 * 1024 * 1024;

    private final AtomicReference<Future<?>> futureAtomicReference = new AtomicReference<>();

    private final Project myProject;

    public static Project project;
    private JPanel panel;
    private JComboBox<String> envComboBox;
    private JComboBox<String> projectComboBox;
    public JTextField urlTextField;
    private JComboBox<String> methodTypeComboBox;
    private JTextArea urlParamsTextArea;
    private JPanel rowParamsTextArea;
    private JTextArea urlEncodedTextArea;
    private JTabbedPane urlEncodedTabbedPane;
    private JTabbedPane urlParamsTabbedPane;
    private JPanel urlParamsTablePanel;
    private JPanel urlEncodedTablePanel;
    private JScrollPane urlParamsTextPanel;
    private JScrollPane urlEncodedTextPanel;
    private JPanel pathParamsPanel;
    private JPanel headerPanel;
    /**
     * tab：Headers、Path Param、URL params、Body ...
     */
    public JTabbedPane tabbedPane;
    private JTabbedPane responseTabbedPanel;
    private JScrollPane responseBodyScrollPanel;
    private JScrollPane responseInfoScrollPanel;
    //    private JComboBox<Integer> responseStatusComboBox;
    private JPanel responseInfoPanel;
    private JTabbedPane multipartTabbedPane;
    private JPanel multipartTablePanel;
    private JPanel jsonResponsePanel;
    private JButton manageConfigButton;
    private JPanel titlePanel;
    private JLabel warnLabel1;
    private JLabel warnLabel2;
    private JTabbedPane bodyTabbedPane;
    private JProgressBar requestProgressBar;
    private JPanel prettyJsonEditorPanel;
    private JPanel responseTextAreaPanel;
    private JCheckBox completeCheckBox;
    private JPanel chartPanel;
    private JPanel sendPanel;

    private MyLanguageTextField prettyJsonLanguageTextField;
    private MyLanguageTextField jsonParamsLanguageTextField;

    private JBPopupMenu tablePopupMenu;

    private JBPopupMenu headerPopupMenu;
    private JBTable urlParamsTable;
    private JBTable urlEncodedTable;
    private JBTable multipartTable;
    private JBTable pathParamsTable;

    private CheckBoxHeader urlParamsCheckBoxHeader;
    private CheckBoxHeader urlEncodedCheckBoxHeader;
    private CheckBoxHeader multipartCheckBoxHeader;
    private CheckBoxHeader pathParamsCheckBoxHeader;


    private JBTable responseInfoTable;
    private TreeTableView responseTable;
    private JBTable headerTable;
    private List<DataMapping> headerParamsKeyValueList;
    private List<ParamKeyValue> responseInfoParamsKeyValueList = new ArrayList<>();
    private List<ParamKeyValue> pathParamsKeyValueList = new ArrayList<>();
    private List<ParamKeyValue> urlParamsKeyValueList = new ArrayList<>();
    private List<ParamKeyValue> urlEncodedKeyValueList = new ArrayList<>();
    private List<ParamKeyValue> multipartKeyValueList = new ArrayList<>();
    private LinkedHashMap<String, Object> bodyParamMap;
    private AtomicBoolean urlEncodedParamChangeFlag;
    private AtomicBoolean urlParamsChangeFlag;
    private AtomicBoolean urlCompleteChangeFlag;
    private static final Map<Object, Icon> TYPE_ICONS = ImmutableMap.<Object, Icon>builder()
            .put(TypeUtil.Type.Object.name(), PluginIcons.ICON_OBJECT)
            .put(TypeUtil.Type.Array.name(), PluginIcons.ICON_ARRAY)
            .put(TypeUtil.Type.String.name(), PluginIcons.ICON_STRING)
            .put(TypeUtil.Type.Number.name(), PluginIcons.ICON_NUMBER)
            .put(TypeUtil.Type.Boolean.name(), PluginIcons.ICON_BOOLEAN)
            .put(TypeUtil.Type.File.name(), PluginIcons.ICON_FILE)
            .build();
    private ComboBox<String> typeJComboBox;
    private ComboBox<String> normalTypeJComboBox;

    @Override
    public void setRequestFocusEnabled(boolean requestFocusEnabled) {
        super.setRequestFocusEnabled(requestFocusEnabled);
    }

    public boolean sendButtonFlag = true;



    public void stopCellEditing() {
        if (this.headerTable.isEditing()) {
            this.headerTable.getCellEditor().stopCellEditing();
        }
        if (this.pathParamsTable.isEditing()) {
            this.pathParamsTable.getCellEditor().stopCellEditing();
        }
        if (this.urlParamsTable.isEditing()) {
            this.urlParamsTable.getCellEditor().stopCellEditing();
        }
        if (this.urlEncodedTable.isEditing()) {
            this.urlEncodedTable.getCellEditor().stopCellEditing();
        }
        if (this.multipartTable.isEditing()) {
            this.multipartTable.getCellEditor().stopCellEditing();
        }
    }

    private JTextField getKeyTextField(String text) {
        JTextField jTextField = new JTextField(text);
        jTextField.setText(text);
        return jTextField;
    }

    private ComboBox getTypeComboBox(String type) {
        ComboBox<String> typeJComboBox = new ComboBox<>();
        typeJComboBox.setRenderer(new IconListRenderer(TYPE_ICONS));
        typeJComboBox.addItem(TypeUtil.Type.Number.name());
        typeJComboBox.addItem(TypeUtil.Type.Array.name());
        typeJComboBox.addItem(TypeUtil.Type.String.name());
        typeJComboBox.addItem(TypeUtil.Type.Object.name());
        typeJComboBox.addItem(TypeUtil.Type.Boolean.name());
        if (type != null) {
            typeJComboBox.setSelectedItem(type);
        }
        return typeJComboBox;
    }

    private ComboBox getRootTypeComboBox(String type) {
        ComboBox<String> typeJComboBox = new ComboBox<>();
        typeJComboBox.setRenderer(new IconListRenderer(TYPE_ICONS));
        typeJComboBox.addItem(TypeUtil.Type.Array.name());
        typeJComboBox.addItem(TypeUtil.Type.Object.name());
        typeJComboBox.setSelectedItem(type);
        return typeJComboBox;
    }

    private ComboBox getNormalTypeComboBox(String type) {
        ComboBox<String> typeJComboBox = new ComboBox<>();
        typeJComboBox.setRenderer(new IconListRenderer(TYPE_ICONS));
        typeJComboBox.addItem(TypeUtil.Type.String.name());
        typeJComboBox.addItem(TypeUtil.Type.Number.name());
        typeJComboBox.addItem(TypeUtil.Type.Boolean.name());
        typeJComboBox.setSelectedItem(type);
        return typeJComboBox;
    }

    private void setTableButtons() {
        tablePopupMenu = new JBPopupMenu();
        JBMenuItem delMenItem = new JBMenuItem(" Delete Rows ");
        delMenItem.setIcon(AllIcons.General.Remove);
        delMenItem.addActionListener(evt -> {
            switch (tabbedPane.getSelectedIndex()) {
                case 0:
                    removeUrlParamsTableLines(headerTable, null, null, null, headerParamsKeyValueList);
                    break;
                case 1:
                    removeUrlParamsTableLines(pathParamsTable, pathParamsKeyValueList, null, null, null);
                    break;
                case 2:
                    removeUrlParamsTableLines(urlParamsTable, urlParamsKeyValueList, urlParamsTextArea, urlParamsChangeFlag, null);
                    break;
                case 3:
                    if (bodyTabbedPane.getSelectedIndex() == 1) {
                        removeUrlParamsTableLines(urlEncodedTable, urlEncodedKeyValueList, urlEncodedTextArea, urlEncodedParamChangeFlag, null);
                    } else if (bodyTabbedPane.getSelectedIndex() == 2) {
                        removeUrlParamsTableLines(multipartTable, multipartKeyValueList, null, null, null);
                    }
                    break;
            }
        });
        tablePopupMenu.add(delMenItem);
        JBMenuItem clearMenItem = new JBMenuItem(" Clear Rows ");
        clearMenItem.setIcon(PluginIcons.ICON_CLEAR);
        clearMenItem.addActionListener(evt -> {
            switch (tabbedPane.getSelectedIndex()) {
                case 0:
                    clearTableLines(headerTable, null, null, null, headerParamsKeyValueList);
                    break;
                case 1:
                    clearTableLines(pathParamsTable, pathParamsKeyValueList, null, null, null);
                    break;
                case 2:
                    clearTableLines(urlParamsTable, urlParamsKeyValueList, urlParamsTextArea, urlParamsChangeFlag, null);
                    break;
                case 3:
                    if (bodyTabbedPane.getSelectedIndex() == 1) {
                        clearTableLines(urlEncodedTable, urlEncodedKeyValueList, urlEncodedTextArea, urlEncodedParamChangeFlag, null);
                    } else if (bodyTabbedPane.getSelectedIndex() == 2) {
                        clearTableLines(multipartTable, multipartKeyValueList, null, null, null);
                    }
                    break;
            }
        });
        tablePopupMenu.add(clearMenItem);
    }

    private void setHeaderButtons() {
        headerPopupMenu = new JBPopupMenu();
        JBMenuItem localMenItem = new JBMenuItem(MyResourceBundleUtil.getKey("AddLocalHeader"));
        localMenItem.setIcon(PluginIcons.ICON_ADD_GREEN);
        localMenItem.addActionListener(evt -> {
            int[] selectedRows = responseTable.getSelectedRows();
            for (int selectedRow : selectedRows) {
                CustomNode node = (CustomNode) ((ListTreeTableModelOnColumns) responseTable.getTableModel()).getRowValue(selectedRow);
                String key = node.getKey();
                Object value = node.getValue();
                if(StringUtils.isBlank(key) || value == null ||StringUtils.isBlank(value.toString())){
                    continue;
                }
                DataMapping dataMapping = headerParamsKeyValueList.stream().filter(q -> q.getType().equals(key)).findFirst().orElse(null);
                if (dataMapping == null) {
                    DataMapping addOne = new DataMapping(key, value.toString());
                    headerParamsKeyValueList.add(addOne);
                } else {
                    dataMapping.setValue(value.toString());
                }
            }
            FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
            assert config != null;
            config.setHeaderList(headerParamsKeyValueList);
            saveAndChangeHeader();
            //refreshTable(headerTable);
            headerTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("", "Header Name", "Header Value")), headerParamsKeyValueList));
            tabbedPane.setSelectedIndex(0);
            headerTable.getColumnModel().getColumn(0).setMaxWidth(30);
            setHeaderTitle();
        });
        headerPopupMenu.add(localMenItem);
        JBMenuItem globalMenItem = new JBMenuItem(MyResourceBundleUtil.getKey("AddGlobalHeader"));
        globalMenItem.setIcon(PluginIcons.ICON_ADD_YELLOW);
        globalMenItem.addActionListener(evt -> {
            FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
            assert config != null;
            List<DataMapping> globalHeaderList = config.getGlobalHeaderList();
            int[] selectedRows = responseTable.getSelectedRows();
            for (int selectedRow : selectedRows) {
                CustomNode node = (CustomNode) ((ListTreeTableModelOnColumns) responseTable.getTableModel()).getRowValue(selectedRow);
                String key = node.getKey();
                Object value = node.getValue();
                if(StringUtils.isBlank(key) || value == null ||StringUtils.isBlank(value.toString())){
                    continue;
                }
                DataMapping dataMapping = globalHeaderList.stream().filter(q -> q.getType().equals(key)).findFirst().orElse(null);
                if (dataMapping == null) {
                    DataMapping addOne = new DataMapping(key, value.toString());
                    globalHeaderList.add(addOne);
                } else {
                    dataMapping.setValue(value.toString());
                }
            }
            config.setGlobalHeaderList(globalHeaderList);
        });
        headerPopupMenu.add(globalMenItem);
    }

    private ComboBox getNormalTypeAndFileComboBox(String type) {
        ComboBox<String> typeJComboBox = new ComboBox<>();
        typeJComboBox.setRenderer(new IconListRenderer(TYPE_ICONS));
        typeJComboBox.addItem(TypeUtil.Type.Number.name());
        typeJComboBox.addItem(TypeUtil.Type.String.name());
        typeJComboBox.addItem(TypeUtil.Type.Boolean.name());
        typeJComboBox.addItem(TypeUtil.Type.File.name());
        typeJComboBox.setSelectedItem(type);
        return typeJComboBox;
    }

    private void createUIComponents() {
        typeJComboBox = new ComboBox<>();
        typeJComboBox.setRenderer(new IconListRenderer(TYPE_ICONS));
        typeJComboBox.addItem(TypeUtil.Type.Number.name());
        typeJComboBox.addItem(TypeUtil.Type.Array.name());
        typeJComboBox.addItem(TypeUtil.Type.String.name());
        typeJComboBox.addItem(TypeUtil.Type.Object.name());
        typeJComboBox.addItem(TypeUtil.Type.Boolean.name());

        normalTypeJComboBox = new ComboBox<>();
        normalTypeJComboBox.setRenderer(new IconListRenderer(TYPE_ICONS));
        normalTypeJComboBox.addItem(TypeUtil.Type.Number.name());
        normalTypeJComboBox.addItem(TypeUtil.Type.String.name());
        normalTypeJComboBox.addItem(TypeUtil.Type.Boolean.name());

        urlEncodedParamChangeFlag = new AtomicBoolean(true);
        urlParamsChangeFlag = new AtomicBoolean(true);
        urlCompleteChangeFlag = new AtomicBoolean(false);

//        setTableButtons();
//        setHeaderButtons();

        renderingHeaderTablePanel();
        renderingUrlParamsTablePanel();
        renderingUrlEncodedPanel();
        renderingMultipartPanel();
        renderingPathParamsPanel();
        renderingResponseInfoPanel();
        renderingJsonResponsePanel();
        //table绑定事件
        bindTableOperations(headerTable);
        bindTableOperations(urlParamsTable);
        bindTableOperations(urlEncodedTable);
        bindTableOperations(multipartTable);
        bindTableOperations(pathParamsTable);
        bindHeaderOperations(responseTable);


        ActionLink managerConfigLink = new ActionLink("config", e -> {
            ShowSettingsUtil.getInstance().showSettingsDialog(myProject, "Quick Request");
        });
        managerConfigLink.setExternalLinkIcon();
        manageConfigButton = managerConfigLink;
        prettyJsonEditorPanel = new MyLanguageTextField(myProject, JsonLanguage.INSTANCE, JsonFileType.INSTANCE, true, true, 1);
        responseTextAreaPanel = new MyLanguageTextField(myProject, PlainTextLanguage.INSTANCE, PlainTextFileType.INSTANCE, true, false, 1);

        rowParamsTextArea = new MyLanguageTextField(myProject, JsonLanguage.INSTANCE, JsonFileType.INSTANCE, false, true, 1);

        //设置高度固定搜索框
        prettyJsonEditorPanel.setMinimumSize(new Dimension(-1, 120));
        prettyJsonEditorPanel.setPreferredSize(new Dimension(-1, 120));
        prettyJsonEditorPanel.setMaximumSize(new Dimension(-1, 1000));

        responseTextAreaPanel.setMinimumSize(new Dimension(-1, 120));
        responseTextAreaPanel.setPreferredSize(new Dimension(-1, 120));
        responseTextAreaPanel.setMaximumSize(new Dimension(-1, 1000));

        rowParamsTextArea.setMinimumSize(new Dimension(-1, 120));
        rowParamsTextArea.setPreferredSize(new Dimension(-1, 120));
        rowParamsTextArea.setMaximumSize(new Dimension(-1, 1000));

        chartPanel = new JPanel();
        sendPanel = new JPanel();
//        chartPanel.setLayout(new BorderLayout());
//        chartPanel.setPreferredSize(new Dimension(-1, 400));
//        chartPanel.setSize(-1, 400);


        //2020.3before
//        manageConfigButton = new JButton();
//        manageConfigButton.addActionListener(e->{
//            ShowSettingsUtil.getInstance().showSettingsDialog(myProject, "Quick Request");
//        });
    }

    private void $$$setupUI$$$() {
        createUIComponents();
    }

    public FastRequestToolWindow(ToolWindow toolWindow, Project project) {
        super(true, false);
        this.myProject = project;
        this.$$$setupUI$$$();

        DefaultActionGroup group = new DefaultActionGroup();
        GotoFastRequestAction gotoFastRequestAction = (GotoFastRequestAction) ActionManager.getInstance().getAction("quickRequest.gotoFastRequest");
        group.add(gotoFastRequestAction);
        group.add(new OpenConfigAction());
        group.addSeparator("  |  ");
        group.add(new FixPositionAction(myProject));
        group.add(new SaveRequestAction());
        if(ToolUtils.isSupportAction()){
            group.add(new RetryAction());
        }
        group.add(new CompleteUrlAction());
        group.add(new CopyCurlAction());
        group.add(new CleanAction());
        group.add(new CoffeeMeAction());
        group.add(new ParentAction(MyResourceBundleUtil.getKey("Question"), MyResourceBundleUtil.getKey("Question"), PluginIcons.ICON_QUESTION) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/ZhangJian002/quick-request/issues");
            }
        });
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, group, true);
        actionToolbar.setTargetComponent(panel);
        JComponent toolbarComponent = actionToolbar.getComponent();
        Border border = IdeBorderFactory.createBorder(SideBorder.BOTTOM);
        actionToolbar.getComponent().setBorder(border);
        setToolbar(toolbarComponent);


        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        FastRequestCurrentProjectConfiguration projectConfig = FastRequestCurrentProjectConfigComponent.getInstance(project).getState();
        assert projectConfig != null;

//        warnLabel1.setVisible(config.getEnvList().isEmpty() || config.getProjectList().isEmpty());
        manageConfigButton.setVisible(config.getEnvList().isEmpty() || config.getProjectList().isEmpty());
//        warnLabel2.setVisible(StringUtils.isBlank(getActiveDomain()));

        methodTypeComboBox.setRenderer(new MethodFontListCellRenderer());
        methodTypeComboBox.setFont(methodTypeComboBox.getFont().deriveFont(Font.BOLD));
        methodTypeComboBox.setForeground(MethodFontListCellRenderer.getColor(methodTypeComboBox.getSelectedItem().toString()));
        methodTypeComboBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                methodTypeComboBox.setForeground(MethodFontListCellRenderer.getColor(methodTypeComboBox.getSelectedItem().toString()));
                methodTypeComboBox.updateUI();
            }
        });

        //responseStatus ComboBox
        List<Integer> values = new ArrayList<>(Constant.HttpStatusDesc.STATUS_MAP.keySet());
        CollectionComboBoxModel<Integer> responseStatusComboBoxModel = new CollectionComboBoxModel<>(values);
//        responseStatusComboBox.setModel(responseStatusComboBoxModel);

        String activeEnv = getActiveEnv();
        String activeProject = getActiveProject();

        //env下拉列表
        ArrayList<String> envListClone = Lists.newArrayList(NO_ENV);
        envListClone.addAll(JSONObject.parseObject(JSONObject.toJSONString(config.getEnvList()), ArrayList.class));
        envListClone.add("Add Env");
        CollectionComboBoxModel<String> envModel = new CollectionComboBoxModel<>(envListClone);
        envComboBox.setModel(envModel);

        envComboBox.setRenderer(new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends String> list, String value, int index, boolean selected, boolean hasFocus) {
                if (value != null) {
                    append(value);
                    if ("Add Env".equals(value)) {
                        setIcon(AllIcons.General.Add);
                    } else if (NO_ENV.equals(value)) {
                        setIcon(AllIcons.General.BalloonError);
                    } else {
                        setIcon(AllIcons.Nodes.Enum);
                    }
                }
            }
        });

        envComboBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                Object selectEnv = envComboBox.getSelectedItem();
                if (selectEnv == null) {
                    return;
                }
                if ("Add Env".equals(envComboBox.getSelectedItem())) {
                    int idx = config.getEnvList().indexOf(activeEnv);
                    envComboBox.setSelectedIndex(Math.max(0, idx + 1));
                    envComboBox.hidePopup();
                    ShowSettingsUtil.getInstance().showSettingsDialog(myProject, "Quick Request");
                    return;
                }

                String env = selectEnv.toString();
                List<String> envList = config.getEnvList();
                if (!envList.contains(env)) {
                    //配置删除了当前的env则默认选中第一个env
                    if (envList.isEmpty()) {
                        //env被删除完了 补全域名开关自动关闭
                        config.setEnableEnv(null);
                        projectConfig.setEnableEnv(null);
                        config.setDomain(StringUtils.EMPTY);
                        projectConfig.setDomain(StringUtils.EMPTY);
                        envModel.setSelectedItem(NO_ENV);
                    } else {
                        if (NO_ENV.equals(env)) {
                            config.setEnableEnv(null);
                            projectConfig.setEnableEnv(null);
                            envModel.setSelectedItem(NO_ENV);
                        } else {
                            config.setEnableEnv(envList.get(0));
                            projectConfig.setEnableEnv(envList.get(0));
                            envModel.setSelectedItem(activeEnv);
                        }
                    }
                } else {
                    config.setEnableEnv(env);
                    projectConfig.setEnableEnv(env);
                }
                switchHeaderParam();
                //根据当前的env和project设置url
                setDomain(config);
            }
        });
        envModel.setSelectedItem(StringUtils.isBlank(activeEnv) ? NO_ENV : activeEnv);

        //project下拉列表
        ArrayList<String> projectListClone = Lists.newArrayList(NO_PROJECT);
        projectListClone.addAll(JSONObject.parseObject(JSONObject.toJSONString(config.getProjectList()), ArrayList.class));
        projectListClone.add("Add Project");
        CollectionComboBoxModel<String> projectModel = new CollectionComboBoxModel<>(projectListClone);
        projectComboBox.setModel(projectModel);
        projectComboBox.setRenderer(new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends String> list, String value, int index, boolean selected, boolean hasFocus) {
                if (value != null) {
                    append(value);
                    if ("Add Project".equals(value)) {
                        setIcon(AllIcons.General.Add);
                    } else if (NO_PROJECT.equals(value)) {
                        setIcon(AllIcons.General.BalloonError);
                    } else {
                        setIcon(AllIcons.Nodes.Property);
                    }
                }
            }
        });


        projectComboBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                Object selectProject = projectComboBox.getSelectedItem();
                if (selectProject == null) {
                    return;
                }
                if ("Add Project".equals(projectComboBox.getSelectedItem())) {
                    int idx = config.getProjectList().indexOf(activeProject);
                    projectComboBox.setSelectedIndex(Math.max(0, idx + 1));
                    projectComboBox.hidePopup();
                    ShowSettingsUtil.getInstance().showSettingsDialog(myProject, "Quick Request");
                    return;
                }
                String projectSelect = selectProject.toString();
                List<String> projectList = config.getProjectList();
                if (!projectList.contains(projectSelect)) {
                    //配置删除了当前的env则默认选中第一个env
                    if (projectList.isEmpty()) {
                        //project被删除完了 补全域名开关自动关闭
                        config.setEnableProject(null);
                        projectConfig.setEnableProject(null);
                        config.setDomain(StringUtils.EMPTY);
                        projectConfig.setDomain(null);
                        projectModel.setSelectedItem(NO_PROJECT);
                    } else {
                        if (NO_PROJECT.equals(projectSelect)) {
                            config.setEnableProject(null);
                            projectConfig.setEnableProject(null);
                            projectModel.setSelectedItem(NO_PROJECT);
                        } else {
                            config.setEnableProject(projectList.get(0));
                            projectConfig.setEnableProject(projectList.get(0));
                            projectModel.setSelectedItem(activeProject);
                        }
                    }
                } else {
                    config.setEnableProject(projectSelect);
                    projectConfig.setEnableProject(projectSelect);
                }
                switchHeaderParam();
                //根据当前的env和project设置url
                setDomain(config);
            }
        });
        projectModel.setSelectedItem(StringUtils.isBlank(activeProject) ? NO_PROJECT : activeProject);


        //更新域名
//        config.getParamGroup().setOriginUrl("");
        setDomain(config);

        //动态更新text中的内容
        urlEncodedTabbedPane.addChangeListener(changeEvent -> {
            if (urlEncodedTabbedPane.getSelectedIndex() == 0) {
                String paramStr = conventDataToString(urlEncodedKeyValueList);
                String currentUrlParamText = urlEncodedTextArea.getText();
                if (!paramStr.equals(currentUrlParamText)) {
                    List<ParamKeyValue> currentUrlParamsKeyValueList = new ArrayList<>();
                    if (StringUtils.isNoneBlank(currentUrlParamText)) {
                        String[] split = currentUrlParamText.split("&");
                        if (split.length > 0) {
                            for (String s : split) {
                                String[] kvArray = s.split("=");
                                if (kvArray.length <= 2) {
                                    String value = kvArray.length < 2 ? "" : kvArray[1].replace("\n", "");
                                    ParamKeyValue paramKeyValue = new ParamKeyValue(kvArray[0], value, 2, TypeUtil.calcTypeByStringValue(value));
                                    currentUrlParamsKeyValueList.add(paramKeyValue);
                                }
                            }
                        }
                    }
                    urlEncodedKeyValueList = currentUrlParamsKeyValueList;
                    //refreshTable(urlEncodedTable);
                    urlEncodedTable.setModel(new ListTableModel<>(getPathColumnInfo(), urlEncodedKeyValueList));
                    resizeTable(urlEncodedTable);
                    setCheckBoxHeader(urlEncodedTable, urlEncodedCheckBoxHeader);
                }
            }
        });

        //动态更新text中的内容
        urlParamsTabbedPane.addChangeListener(changeEvent -> {
            //table change 引发重新构建text
            if (urlParamsTabbedPane.getSelectedIndex() == 0) {
                String paramStr = conventDataToString(urlParamsKeyValueList);
                String currentUrlParamText = urlParamsTextArea.getText();
                if (!paramStr.equals(currentUrlParamText)) {
                    List<ParamKeyValue> currentUrlParamsKeyValueList = new ArrayList<>();
                    if (StringUtils.isNoneBlank(currentUrlParamText)) {
                        String[] split = currentUrlParamText.split("&");
                        for (String s : split) {
                            String[] kvArray = s.split("=");
                            if (kvArray.length <= 2) {
                                String value = kvArray.length < 2 ? "" : kvArray[1].replace("\n", "");
                                ParamKeyValue paramKeyValue = new ParamKeyValue(kvArray[0], value, 2, TypeUtil.calcTypeByStringValue(value));
                                currentUrlParamsKeyValueList.add(paramKeyValue);
                            }
                        }
                    }
                    urlParamsKeyValueList = currentUrlParamsKeyValueList;
                    //refreshTable(urlParamsTable);
                    urlParamsTable.setModel(new ListTableModel<>(getPathColumnInfo(), urlParamsKeyValueList));
                    resizeTable(urlParamsTable);
                    setCheckBoxHeader(urlParamsTable, urlParamsCheckBoxHeader);
                }
            }
        });

        //copy param
//        rowParamsTextArea.addMouseListener(copyMouseAdapter(rowParamsTextArea));
//        urlEncodedTextArea.addMouseListener(copyMouseAdapter(urlEncodedTextArea));
//        urlParamsTextArea.addMouseListener(copyMouseAdapter(urlParamsTextArea));
//        urlTextField.addMouseListener(copyMouseAdapterField(urlTextField));
//        headerParamsKeyValueList = config.getHeaderList();
        calcHeaderList();

//        sendRequestEvent();
        //send request
        //2秒内不允许狂点
        requestProgressBar.setIndeterminate(true);
        requestProgressBar.setVisible(false);
        //Send按钮
        ToolbarSendRequestAction toolbarSendRequestAction = (ToolbarSendRequestAction) ActionManager.getInstance().getAction("quickRequest.sendAction");
        ToolbarSendAndDownloadRequestAction sendAndDownloadRequestAction = (ToolbarSendAndDownloadRequestAction) ActionManager.getInstance().getAction("quickRequest.sendDownloadAction");
        ToolbarPressureRequestAction toolbarPressureRequestAction = (ToolbarPressureRequestAction) ActionManager.getInstance().getAction("quickRequest.pressureAction");

        DefaultActionGroup sendGroup = new DefaultActionGroup();
        sendGroup.add(toolbarSendRequestAction);
        sendGroup.add(sendAndDownloadRequestAction);
        if(ClassUtils.existMath3Class()){
            sendGroup.add(toolbarPressureRequestAction);
        }
        SplitButtonAction splitButtonAction = new SplitButtonAction(sendGroup);
        DefaultActionGroup sendAndStopGroup = new DefaultActionGroup();
        sendAndStopGroup.add(splitButtonAction);
        StopPositionAction stopPositionAction = new StopPositionAction();
        sendAndStopGroup.add(stopPositionAction);
        ActionToolbar actionToolbar1 = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, sendAndStopGroup, true);
        actionToolbar1.setTargetComponent(sendPanel);
        JComponent component = actionToolbar1.getComponent();
        sendPanel.add(component);
    }

    private void changeUrlParamsText() {
        String paramStr = conventDataToString(urlParamsKeyValueList);
        urlParamsTextArea.setText(paramStr);
        urlEncodedParamChangeFlag.set(false);
    }

    private void changeUrlEncodedParamsText() {
        String paramStr = conventDataToString(urlEncodedKeyValueList);
        urlEncodedTextArea.setText(paramStr);
        urlEncodedParamChangeFlag.set(false);
    }

    private String getCurlDataAndCopy() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        String methodType = (String) methodTypeComboBox.getSelectedItem();
        if(Objects.equals(methodType.toLowerCase(), "dubbo")){
            NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification("Dubbo doesn't support!", MessageType.ERROR).notify(myProject);
            return "";
        }
//        String domain = getActiveDomain();
        String url = getSendUrl();
        List<DataMapping> headerList = headerParamsKeyValueList;
        String urlParam = urlParamsTextArea.getText();
        String jsonParam = ((LanguageTextField) rowParamsTextArea).getText();
        String urlEncodedParam = urlEncodedTextArea.getText();

        if (StringUtils.isEmpty(url)) {
            Messages.showMessageDialog("Url not exist", "Error", Messages.getInformationIcon());
            return "";
        }
//        String url = domain + sendUrl;
        if (StringUtils.isNotEmpty(urlParam)) {
            String urlParamDeal = urlParam.lines().collect(Collectors.joining(""));
            url = url + "?" + urlParamDeal;
        }

        StringBuilder sb = new StringBuilder("curl -X ");
        sb.append("\"").append(methodType).append("\" ");
        sb.append("\"").append(url).append("\" \\\n");
        for (DataMapping header : headerList) {
            sb.append("-H '").append(header.getType()).append(": ").append(header.getValue()).append("' \\\n");
        }
        if (StringUtils.isNotEmpty(jsonParam) && !"{}".equals(jsonParam) && !"[]".equals(jsonParam)) {
            sb.append("-H '").append("Content-Type: application/json").append("' \\\n");
        }

        if (StringUtils.isNotEmpty(urlEncodedParam)) {
            String urlEncodedParamDeal = urlEncodedParam.lines().collect(Collectors.joining(""));
            sb.append("-d '").append(urlEncodedParamDeal).append("' \\\n");
        }
        if (StringUtils.isNotEmpty(jsonParam) && !"{}".equals(jsonParam) && !"[]".equals(jsonParam)) {
            String jsonParamDeal = jsonParam.lines().collect(Collectors.joining(""));
            sb.append("-d '").append(jsonParamDeal).append("' \\\n");
        }

        for (ParamKeyValue paramKeyValue : multipartKeyValueList) {
            if (!TypeUtil.Type.File.name().equals(paramKeyValue.getType())) {
                sb.append("-F \"").append(paramKeyValue.getKey()).append("=").append(paramKeyValue.getValue().toString()).append("\" \\\n");
            } else {
                sb.append("-F \"").append(paramKeyValue.getKey()).append("=").append("\" \\\n");
            }
        }
        String result = sb.toString();
        ToolUtil.setClipboardString(result);
        return result;
    }

    public boolean getSendButtonFlag() {
        return sendButtonFlag;
    }

    public boolean isRunning() {
        return futureAtomicReference.get() == null;
    }

    public void sendRequestEvent(boolean... conditions) {
        if (!sendButtonFlag || futureAtomicReference.get() != null) {
            return;
        }
        project = myProject;
        sendButtonFlag = false;
        //首先停止正在编辑中的table
        stopCellEditing();
        String methodType = (String) methodTypeComboBox.getSelectedItem();
        //请求进程条设置
        requestProgressBarSetting();
        if (Objects.equals(methodType, "DUBBO")) {
            dubboRequest(conditions);
        }else if (Objects.equals(methodType, "GRPC")) {
            grpcRequest(conditions);
        } else {
            //Restful Request
            restfulRequest(conditions);
        }
    }

    private void grpcRequest(boolean[] conditions) {
        try {
            String finalDomain;
            if (urlTextField.getText().contains(":")){
                finalDomain = urlTextField.getText();
            }else {
                finalDomain = getActiveDomain();
            }
            boolean isGrpcDomain = UrlUtil.isGrpcURL(finalDomain);
            if (!isGrpcDomain){
                setErrorInfo("gRPC");
                return;
            }
            FastRequestConfiguration configuration = FastRequestComponent.getInstance().getState();
            boolean existGrpcCurl = GrpcCurlUtils.existGrpcCurl(configuration.queryNotNullGrpcurlPath());
            if (!existGrpcCurl){
                Notification notification = NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification(MyResourceBundleUtil.getKey("GrpcurlNotExist"), MessageType.ERROR);
                notification.addAction(new NotificationAction(MyResourceBundleUtil.getKey("GrpcurlInstall")) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent anActionEvent, @NotNull Notification notification) {
                        BrowserUtil.browse(Constant.GRPCURL_URL);
                    }
                });
                notification.notify(project);
                sendButtonFlag = true;
                futureAtomicReference.set(null);
                requestProgressBar.setVisible(false);
            }else {
                futureAtomicReference.set(GlobalThreadPool.submit(() -> {
                    try {
                        GrpcRequest grpcRequest = getGrpcRequest(configuration);
                        long startTime = System.currentTimeMillis();
                        String[] result = GrpcCurlUtils.request(grpcRequest);
                        long endTime = System.currentTimeMillis();
                        grpcResponseHandler(result, startTime, endTime);
                    }catch (Exception e){
                        requestExceptionHandler(e);
                    } finally {
                        sendButtonFlag = true;
                        futureAtomicReference.set(null);
                        requestProgressBar.setVisible(false);
                    }
                }));
            }
        }catch (Exception e){
            requestExceptionHandler(e);
        }
    }

    private @NotNull GrpcRequest getGrpcRequest(FastRequestConfiguration configuration) {
        GrpcRequest grpcRequest = new GrpcRequest();
        String path = configuration.queryNotNullGrpcurlPath();
        grpcRequest.setGrpcurlPath(path);
        grpcRequest.setTls(false);
        grpcRequest.setProtoFile(configuration.getParamGroup().getPbFileName());
        grpcRequest.setProtoPath(configuration.getParamGroup().getPbImportPath());
        String body = ((MyLanguageTextField)rowParamsTextArea).getText();
        body = body.replaceAll("\n", "");
        grpcRequest.setData("\"" + StringEscapeUtils.escapeJava(body) +  "\"");
        if (urlTextField.getText().contains(":")){
            try {
                String[] split = urlTextField.getText().split(":");
                grpcRequest.setHost(split[0]);
                grpcRequest.setPort(Integer.parseInt(split[1].split("/")[0]));
            }catch (Exception e){
                NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification(MyResourceBundleUtil.getKey("CheckYourUrl"), MessageType.ERROR).notify(project);
                throw new RuntimeException(e);
            }
        }else {
            String[] domain = getActiveDomain().split(":");
            grpcRequest.setHost(domain[0]);
            grpcRequest.setPort(Integer.parseInt(domain[1]));
        }
        grpcRequest.setService(configuration.getParamGroup().getClassName());
        grpcRequest.setMethod(configuration.getParamGroup().getMethod());
        grpcRequest.setHeaderMap(getFinalHeader());
        return grpcRequest;
    }

    private void dubboRequest(boolean... conditions) {
        boolean fileMode = conditions[0];
        try {
            FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
            ParamGroup paramGroup = config.getParamGroup();
            String dubboServiceStr = String.format("%s.%s", paramGroup.getInterfaceName(), paramGroup.getMethod());
            String address;
            try {
                address = getDubboSendUrl().split("/")[0];
                if (StringUtils.isBlank(address)) {
                    address = "127.0.0.1:20880";
                }
            } catch (Exception e) {
                address = "127.0.0.1:20880";
            }
            if (!UrlUtil.isDubboURL(address)) {
                setErrorInfo("Dubbo");
                return;
            }
            String finalAddress = address;
            futureAtomicReference.set(GlobalThreadPool.submit(() -> {
                try {
                    LinkedHashMap<String, Object> bodyParamMapTemp = buildParamForDubbo();
                    DubboService.Param param = new DubboService.Param(bodyParamMapTemp);
                    DubboService dubboService = new DubboService(dubboServiceStr, param);
                    dubboService.setServiceAddress(finalAddress);
                    long start = System.currentTimeMillis();
                    DubboService.Response invokeRes = dubboService.invoke();
                    long end = System.currentTimeMillis();
                    //如果被外部中断，就不继续了
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    //结果处理
                    dubboResponseHandler(invokeRes, start, end, fileMode, paramGroup);
                } catch (Exception ee) {
                    //异常处理
                    exceptionHandlerForDubbo(ee);
                } finally {
                    sendButtonFlag = true;
                    futureAtomicReference.set(null);
                }
            }));
        } catch (Exception e) {
            requestExceptionHandler(e);
        }
    }

    private void setErrorInfo(String type) {
        ApplicationManager.getApplication().invokeLater(() -> {
            ((MyLanguageTextField) responseTextAreaPanel).setText("Correct url required." + type + " url should start with ip:port.");
            ((MyLanguageTextField) prettyJsonEditorPanel).setText("");
            tabbedPane.setSelectedIndex(4);
            responseTabbedPanel.setSelectedIndex(2);
            sendButtonFlag = true;
            futureAtomicReference.set(null);
            requestProgressBar.setVisible(false);
        });
    }

    private LinkedHashMap<String, Object> buildParamForDubbo() {
        LinkedHashMap<String, Object> bodyParamMapTemp = new LinkedHashMap<>();
        urlEncodedKeyValueList.stream().filter(ParamKeyValue::getEnabled).forEach(q -> {
            if (Objects.equals(q.getType(), "Object")) {
                bodyParamMapTemp.put(q.getKey(), JSONObject.parse(q.getValue().toString()));
            } else if (Objects.equals(q.getType(), "Array")) {
                bodyParamMapTemp.put(q.getKey(), JSONArray.parse(q.getValue().toString()));
            } else if (Objects.equals(q.getType(), "Number")) {
                bodyParamMapTemp.put(q.getKey(), Double.valueOf(q.getValue().toString()));
            } else if (Objects.equals(q.getType(), "Boolean")) {
                bodyParamMapTemp.put(q.getKey(), Boolean.valueOf(q.getValue().toString()));
            } else {
                bodyParamMapTemp.put(q.getKey(), q.getValue().toString());
            }
        });
        return bodyParamMapTemp;
    }

    private void restfulRequest(boolean... conditions) {
        try {
            //发起请求并处理返回结果
            sendAndHandleResponse(conditions);
        } catch (Exception exception) {
            requestExceptionHandler(exception);
        }
    }

    private void requestExceptionHandler(Exception exception) {
        ApplicationManager.getApplication().invokeLater(() -> {
            sendButtonFlag = true;
            futureAtomicReference.set(null);
            requestProgressBar.setVisible(false);
            String errorMsg = exception.getMessage();
            ((MyLanguageTextField) responseTextAreaPanel).setText(errorMsg);
            ((MyLanguageTextField) prettyJsonEditorPanel).setText("");
            responseInfoParamsKeyValueList = Lists.newArrayList(
                    new ParamKeyValue("Error", errorMsg)
            );
            //refreshTable(responseInfoTable);
            responseInfoTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("Name", "Value")), responseInfoParamsKeyValueList));
            responseInfoTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            responseInfoTable.getColumnModel().getColumn(0).setMaxWidth(150);
            CustomNode root = new CustomNode("Root", "");
            ((DefaultTreeModel) responseTable.getTableModel()).setRoot(root);
            tabbedPane.setSelectedIndex(4);
            responseTabbedPanel.setSelectedIndex(2);
        });
    }

    private void sendAndHandleResponse(boolean... conditions) {
        boolean fileMode = conditions[0];
        futureAtomicReference.set(GlobalThreadPool.submit(() -> {
            try {
                if (conditions.length > 1 && conditions[1]) {
                    JmhResultEntity jmhResultEntity = PressureUtils.jmhTest();
                    //如果被外部中断，就不继续了
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    chartPanel.removeAll();
                    ChartPanel chartPanel1 = JMHTest.pain(jmhResultEntity);
                    chartPanel1.setPreferredSize(new Dimension(chartPanel.getWidth(), 400));
                    chartPanel.add(chartPanel1, BorderLayout.CENTER);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        requestProgressBar.setVisible(false);
                        tabbedPane.setSelectedIndex(4);
                        responseTabbedPanel.setSelectedIndex(4);
                    });
                } else {
                    //新建、组装请求
                    Request request = buildOkHttpRequest();
                    if (request == null) return;
                    long start = System.currentTimeMillis();
                    Response response = OkHttp3Util.getSingleClientInstance().newCall(request).execute();
                    long end = System.currentTimeMillis();
                    //如果被外部中断，就不继续了
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    //结果处理
                    responseHandler(response, start, end, request, fileMode);
                }
            } catch (Exception ee) {
                //异常处理
                exceptionHandler(ee);
            } finally {
                sendButtonFlag = true;
                futureAtomicReference.set(null);
                requestProgressBar.setVisible(false);
            }
        }));
    }

    private void requestProgressBarSetting() {
        requestProgressBar.setVisible(true);
        requestProgressBar.setForeground(ColorProgressBar.GREEN);
    }

//    public static final Color BLUE = new JBColor(() -> {
//        UISettings settings = UISettings.getInstance();
//        return null == settings.getColorBlindness()
//                ? new JBColor(new Color(0x074BFA), new Color(0x0425F8))
//                : new JBColor(new Color(0x074BFA), new Color(0x074BFA));
//    });

//    public HttpRequest buildRequest() {
//        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
//        assert config != null;
//        String sendUrl = getSendUrl();
//
//        if (!UrlUtil.isURL(sendUrl)) {
//            ((MyLanguageTextField) prettyJsonEditorPanel).setText("");
//            ((MyLanguageTextField) responseTextAreaPanel).setText("Correct url required.Http url should start with http(s)://ip:port.");
//            tabbedPane.setSelectedIndex(4);
//            responseTabbedPanel.setSelectedIndex(2);
//            sendButtonFlag = true;
//            requestProgressBar.setVisible(false);
//            return null;
//        }
//        String methodType = (String) methodTypeComboBox.getSelectedItem();
//        HttpRequest request = HttpUtil.createRequest(Method.valueOf(methodType), sendUrl);
//        request.setMaxRedirectCount(10);
//        Integer connectionTimeout = config.getConnectionTimeout();
//        Integer readTimeout = config.getReadTimeout();
//        if (connectionTimeout != null && connectionTimeout != 0) {
//            request.setConnectionTimeout(connectionTimeout * 1000);
//        }
//        if (readTimeout != null && readTimeout != 0) {
//            request.setReadTimeout(readTimeout * 1000);
//        }
//        headerParamsKeyValueList = headerParamsKeyValueList == null ? new ArrayList<>() : headerParamsKeyValueList;
//        List<DataMapping> globalHeaderList = config.getGlobalHeaderList();
//        globalHeaderList = globalHeaderList == null ? new ArrayList<>() : globalHeaderList;
//        Map<String, List<String>> globalHeaderMap = globalHeaderList.stream().filter(DataMapping::getEnabled).collect(Collectors.toMap(DataMapping::getType, p -> Lists.newArrayList(p.getValue()), (existing, replacement) -> existing));
//        Map<String, List<String>> headerMap = headerParamsKeyValueList.stream().filter(DataMapping::getEnabled).collect(Collectors.toMap(DataMapping::getType, p -> Lists.newArrayList(p.getValue()), (existing, replacement) -> existing));
//        globalHeaderMap.putAll(headerMap);
//        request.header(globalHeaderMap);
//        Map<String, Object> multipartFormParam = multipartKeyValueList.stream().filter(ParamKeyValue::getEnabled)
//                .collect(HashMap::new, (m, v) -> {
//                    Object value = v.getValue();
//                    String key = v.getKey();
//                    if (TypeUtil.Type.File.name().equals(v.getType())) {
//                        if (value != null && !StringUtils.isBlank(value.toString())) {
//                            m.put(key, new File(value.toString()));
//                        } else {
//                            m.put(key, null);
//                        }
//                    } else {
//                        m.put(key, value);
//                    }
//                }, HashMap::putAll);
//
//        Map<String, Object> urlParam = urlParamsKeyValueList.stream().filter(ParamKeyValue::getEnabled).collect(Collectors.toMap(ParamKeyValue::getKey, ParamKeyValue::getValue, (existing, replacement) -> existing));
//        String jsonParam = ((LanguageTextField) rowParamsTextArea).getText();
//        StringBuilder urlEncodedParam = new StringBuilder("");
//        urlEncodedKeyValueList.stream().filter(ParamKeyValue::getEnabled).forEach(q -> {
//            urlEncodedParam.append(q.getKey()).append("=").append(q.getValue()).append("&");
//        });
//
//        boolean formFlag = true;
//        //json优先
//        if (StringUtils.isNotEmpty(urlEncodedParam)) {
//            request.body(StringUtils.removeEnd(urlEncodedParam.toString(), "&"));
//            formFlag = false;
//        }
//        if (StringUtils.isNotEmpty(jsonParam)) {
//            request.body(JSON.toJSONString(JSON.parse(jsonParam)));
//            formFlag = false;
//        }
//
//        if (!urlParam.isEmpty()) {
//            String queryParam = UrlQuery.of(urlParam).toString();
//            request.setUrl(request.getUrl() + "?" + URLEncoder.DEFAULT.encode(queryParam, StandardCharsets.UTF_8));
//        }
//        if (!multipartFormParam.isEmpty() && formFlag) {
//            request.form(multipartFormParam);
//        }
//        return request;
//    }

    private Map<String, String> getFinalHeader(){
        Map<String, String> resultMap = new HashMap<>();
        Map<String, List<String>> header = getHeader();
        header.forEach((key, values) -> {
            resultMap.put(key, values.get(0));
        });
        return resultMap;
    }

    private Map<String, List<String>> getHeader(){
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        headerParamsKeyValueList = headerParamsKeyValueList == null ? new ArrayList<>() : headerParamsKeyValueList;
        List<DataMapping> globalHeaderList = config.getGlobalHeaderList();
        globalHeaderList = globalHeaderList == null ? new ArrayList<>() : globalHeaderList;
        Map<String, List<String>> globalHeaderMap = globalHeaderList.stream().filter(DataMapping::getEnabled).collect(Collectors.toMap(DataMapping::getType, p -> Lists.newArrayList(p.getValue()), (existing, replacement) -> existing));
        Map<String, List<String>> headerMap = headerParamsKeyValueList.stream().filter(DataMapping::getEnabled).collect(Collectors.toMap(DataMapping::getType, p -> Lists.newArrayList(p.getValue()), (existing, replacement) -> existing));
        globalHeaderMap.putAll(headerMap);
        Map<String, List<String>> finalMap = new HashMap<>();
        globalHeaderMap.forEach((key, value) -> {
            if(StringUtils.isNotBlank(key) && !value.isEmpty() && StringUtils.isNotBlank(value.get(0))){
                finalMap.put(key, value);
            }
        });
        return finalMap;
    }
    public Request buildOkHttpRequest() {
        String sendUrl = getSendUrl();

        if (!UrlUtil.isURL(sendUrl)) {
            ApplicationManager.getApplication().invokeLater(() -> {
                ((MyLanguageTextField) prettyJsonEditorPanel).setText("");
                ((MyLanguageTextField) responseTextAreaPanel).setText("Correct url required.Http url should start with http(s)://ip:port.");
                tabbedPane.setSelectedIndex(4);
                responseTabbedPanel.setSelectedIndex(2);
                sendButtonFlag = true;
                requestProgressBar.setVisible(false);
            });
            return null;
        }
        String methodType = (String) methodTypeComboBox.getSelectedItem();
        assert methodType != null;
        RequestBody initBody = null;
        if(!Objects.equals(methodType.toLowerCase(), "get")){
            initBody = RequestBody.create("".getBytes());
        }
        Request.Builder request = new Request.Builder().method(methodType, initBody)
                .url(sendUrl);
        Map<String, List<String>> globalHeaderMap = getHeader();
        Iterator<Map.Entry<String, List<String>>> iterator = globalHeaderMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, List<String>> next = iterator.next();
            String headerName = next.getKey();
            List<String> value = next.getValue();
            if(StringUtils.isNotBlank(headerName) && StringUtils.isNotBlank(value.get(0))){
                request.header(headerName, value.get(0));
            }
        }
        Map<String, Object> multipartFormParam = multipartKeyValueList.stream().filter(ParamKeyValue::getEnabled)
                .collect(HashMap::new, (m, v) -> {
                    Object value = v.getValue();
                    String key = v.getKey();
                    if (TypeUtil.Type.File.name().equals(v.getType())) {
                        if (value != null && !StringUtils.isBlank(value.toString())) {
                            m.put(key, new File(value.toString()));
                        } else {
                            m.put(key, null);
                        }
                    } else {
                        m.put(key, value);
                    }
                }, HashMap::putAll);
        Map<String, Object> urlParam = urlParamsKeyValueList.stream().filter(ParamKeyValue::getEnabled).collect(Collectors.toMap(ParamKeyValue::getKey, ParamKeyValue::getValue, (existing, replacement) -> existing));
        String rowParams = ((LanguageTextField) rowParamsTextArea).getText();
        StringBuilder urlEncodedParam = new StringBuilder();
        urlEncodedKeyValueList.stream().filter(ParamKeyValue::getEnabled).forEach(q -> urlEncodedParam.append(q.getKey()).append("=").append(q.getValue()).append("&"));

        if (!urlParam.isEmpty()) {
            String queryParam = UrlQuery.of(urlParam).toString();
            request.url(sendUrl + "?" + queryParam);
        }
        if(Objects.equals(methodType.toLowerCase(), "get")){
            return request.build();
        }

        boolean formFlag = true;
        //json优先
        if (StringUtils.isNotEmpty(urlEncodedParam)) {
            String bodyStr;
            RequestBody body = RequestBody.create((bodyStr = StringUtils.removeEnd(urlEncodedParam.toString(), "&")),
                    MediaType.parse(RequestUtils.get(bodyStr)));
            request.method(methodType, body);
            formFlag = false;
        }
        if (StringUtils.isNotEmpty(rowParams)) {
            String contentType;
            if(CollectionUtils.isNotEmpty(globalHeaderMap.get("Content-Type"))){
                contentType = globalHeaderMap.get("Content-Type").get(0);
            }else {
                contentType = getTargetContentType();
            }
            RequestBody body = RequestBody.create(rowParams, MediaType.parse(contentType));
            request.method(methodType, body);
            formFlag = false;
        }

        if (!multipartFormParam.isEmpty() && formFlag) {
            MultipartBody.Builder multipartBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            Iterator<Map.Entry<String, Object>> iterator1 = multipartFormParam.entrySet().iterator();
            while (iterator1.hasNext()){
                Map.Entry<String, Object> next = iterator1.next();
                String key = next.getKey();
                Object value = next.getValue();
                if(value == null){
                    continue;
                }
                if(value instanceof File){
                    File file = (File)value;
                    multipartBody.addFormDataPart(key, file.getName(), RequestBody.create(file, MediaType.parse("application/octet-stream")));
                }else {
                    multipartBody.addFormDataPart(key, value.toString());
                }
            }
            request.method(methodType, multipartBody.build());
        }
        return request.build();
    }

    private String getTargetContentType() {
        BodyContentType bodyContentType = BodyContentType.valueOf(BodyFormatAction.chooseBodyType);
        return bodyContentType.getValue();
    }

    private String getSendUrl() {
        NameGroup defaultNameGroup = new NameGroup(StringUtils.EMPTY, new ArrayList<>());
        HostGroup defaultHostGroup = new HostGroup(StringUtils.EMPTY, StringUtils.EMPTY);
        String domain = FastRequestComponent.getInstance().getState().getDataList().stream().filter(n -> n.getName().equals(projectComboBox.getSelectedItem())).findFirst().orElse(defaultNameGroup)
                .getHostGroup().stream().filter(h -> h.getEnv().equals(envComboBox.getSelectedItem())).findFirst().orElse(defaultHostGroup).getUrl();
        String sendUrl;
        //考虑到可能人为修改url，就直接判断url是不是http或者houst:port型请求 不是再把前缀加上
        if (UrlUtil.isURL(urlTextField.getText())) {
            sendUrl = urlTextField.getText();
        } else {
            //如果不是url 就给加
            sendUrl = domain + urlTextField.getText();
        }
        return sendUrl;
    }

    private String getDubboSendUrl() {
        NameGroup defaultNameGroup = new NameGroup(StringUtils.EMPTY, new ArrayList<>());
        HostGroup defaultHostGroup = new HostGroup(StringUtils.EMPTY, StringUtils.EMPTY);
        String domain = FastRequestComponent.getInstance().getState().getDataList().stream().filter(n -> n.getName().equals(projectComboBox.getSelectedItem())).findFirst().orElse(defaultNameGroup)
                .getHostGroup().stream().filter(h -> h.getEnv().equals(envComboBox.getSelectedItem())).findFirst().orElse(defaultHostGroup).getUrl();
        String sendUrl;
        //考虑到可能人为修改url，就直接判断url是不是http请求 不是再把前缀加上
        if (UrlUtil.isDubboURL(urlTextField.getText())) {
            sendUrl = urlTextField.getText();
        } else {
            //如果不是url 就给加
            sendUrl = domain + urlTextField.getText();
        }
        return sendUrl;
    }

    private void exceptionHandler(Exception ee) {
        String errorMsg = ee.getMessage();
        ApplicationManager.getApplication().invokeLater(() -> {
            sendButtonFlag = true;
            futureAtomicReference.set(null);
            requestProgressBar.setVisible(false);
            tabbedPane.setSelectedIndex(4);
            responseTabbedPanel.setSelectedIndex(2);
            ((MyLanguageTextField) responseTextAreaPanel).setText(errorMsg);
            ((MyLanguageTextField) prettyJsonEditorPanel).setText("");
            responseInfoParamsKeyValueList = Lists.newArrayList(
                    new ParamKeyValue("Url", getSendUrl(), 2, TypeUtil.Type.String.name()),
                    new ParamKeyValue("Error", errorMsg)
            );
            //refreshTable(responseInfoTable);
            responseInfoTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("Name", "Value")), responseInfoParamsKeyValueList));
            responseInfoTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            responseInfoTable.getColumnModel().getColumn(0).setMaxWidth(150);
            CustomNode root = new CustomNode("Root", "");
            ((DefaultTreeModel) responseTable.getTableModel()).setRoot(root);
        });
    }

    private void exceptionHandlerForDubbo(Exception ee) {
        ApplicationManager.getApplication().invokeLater(() -> {
            sendButtonFlag = true;
            futureAtomicReference.set(null);
            requestProgressBar.setVisible(false);
            tabbedPane.setSelectedIndex(4);
            responseTabbedPanel.setSelectedIndex(2);
            String errorMsg = ee.getMessage();
            ((MyLanguageTextField) responseTextAreaPanel).setText(errorMsg);
            ((MyLanguageTextField) prettyJsonEditorPanel).setText("");
            responseInfoParamsKeyValueList = Lists.newArrayList(
                    new ParamKeyValue("Url", getDubboSendUrl(), 2, TypeUtil.Type.String.name()),
                    new ParamKeyValue("Error", errorMsg)
            );
            //refreshTable(responseInfoTable);
            responseInfoTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("Name", "Value")), responseInfoParamsKeyValueList));
            responseInfoTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            responseInfoTable.getColumnModel().getColumn(0).setMaxWidth(150);
            CustomNode root = new CustomNode("Root", "");
            ((DefaultTreeModel) responseTable.getTableModel()).setRoot(root);
        });

    }

    private void responseHandler(Response response, long start, long end, Request request, boolean fileMode) {
        ApplicationManager.getApplication().invokeLater(() -> {
            tabbedPane.setSelectedIndex(4);
            String duration = String.valueOf(end - start);
            requestProgressBar.setVisible(false);
            int status = response.code();
            String header = response.header(Header.CONTENT_DISPOSITION.getValue());
            boolean finalFileMode = fileMode || (StringUtils.isNotBlank(header) && header.contains("attachment"));
            //download file
            fileHandler(finalFileMode, status, response);
            //response渲染
            responsePageHandler(response, status, duration);
            ApplicationManager.getApplication().invokeLater(() -> {
                if (urlTextField.getText().isBlank() && getActiveDomain().isBlank()) {
                    return;
                }
                if(status >= 200 && status < 300){
                    //saveToHistory
                    saveTableRequest(1, request.method());
                }
            });

        });
    }

    private void dubboResponseHandler(DubboService.Response response, long start, long end, boolean fileMode, ParamGroup paramGroup) {
        ApplicationManager.getApplication().invokeLater(() -> {
            tabbedPane.setSelectedIndex(4);
            String duration = String.valueOf(end - start - 10);
            requestProgressBar.setVisible(false);
            //not a file
            resultHandler(false, response.getResultStr());
            //response渲染
            responseDubboPageHandler(response.getResponseStr(), duration);
            ApplicationManager.getApplication().invokeLater(() -> {
                if (urlTextField.getText().isBlank() && getActiveDomain().isBlank()) {
                    return;
                }
                //saveToHistory
                saveTableRequest(2, paramGroup.getMethodType());
            });

        });
    }

    private void grpcResponseHandler(String[] result, long start, long end) {
        ApplicationManager.getApplication().invokeLater(() -> {
            tabbedPane.setSelectedIndex(4);
            String duration = String.valueOf(end - start);
            requestProgressBar.setVisible(false);
            if(result[2].equals("0")){
                resultHandler(false, result[0]);
                //response渲染
                responseGrpcPageHandler(duration, true, result);
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (urlTextField.getText().isBlank() && getActiveDomain().isBlank()) {
                        return;
                    }
                    //saveToHistory
                    saveTableRequest(3, "GRPC");
                });
            }else {
                //response渲染
                responseGrpcPageHandler(duration,false, result);
                responseTabbedPanel.setSelectedIndex(3);
            }
        });
    }

    private void responseGrpcPageHandler(String duration, boolean success, String[] result) {
        String statusDis;
        if(success){
            statusDis = "<html><span style=\"color: #0CCD08;\">Success</span></html>";
        }else {
            statusDis = "<html><span style=\"color: #FB1A00;\">Failure</span></html>";
        }
        responseInfoParamsKeyValueList = Lists.newArrayList(
                new ParamKeyValue("Status", statusDis, 2, TypeUtil.Type.String.name()),
                new ParamKeyValue("API Call Script", result[1], 2, TypeUtil.Type.String.name())
        );
        if (!success){
            responseInfoParamsKeyValueList.add(new ParamKeyValue("Error Message", result[0], 2, TypeUtil.Type.String.name()));
        }
        responseInfoParamsKeyValueList.add(new ParamKeyValue("Time", duration + " ms", 2, TypeUtil.Type.String.name()));
        responseInfoParamsKeyValueList.add(new ParamKeyValue("Date", new Date()));
        responseInfoTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("Key", "Value")), responseInfoParamsKeyValueList));
        responseInfoTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        responseInfoTable.getColumnModel().getColumn(0).setMaxWidth(150);
    }

    private void pressureResponseHandler(PressureEntity pressureEntity) {
        ApplicationManager.getApplication().invokeLater(() -> {
            tabbedPane.setSelectedIndex(4);
            requestProgressBar.setVisible(false);
            //response渲染
            pressureResponsePageHandler(pressureEntity);
            responseTabbedPanel.setSelectedIndex(3);
        });
    }

    private void saveTableRequest(int type, String methodName) {
        HistoryTable historyTable = FastRequestHistoryCollectionComponent.getInstance(myProject).getState();
        LocalDateTime now = LocalDateTime.now();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间
        String formattedDateTime = now.format(formatter);
        HistoryTableData historyTableData = new HistoryTableData();
        if (type == 1) {
            historyTableData = new HistoryTableData(methodName, getSendUrl(), formattedDateTime);
            historyTableData.setHeaders(JSONArray.toJSONString(headerParamsKeyValueList));
            if (CollectionUtils.isNotEmpty(pathParamsKeyValueList)) {
                historyTableData.setPathParams(JSONArray.toJSONString(pathParamsKeyValueList));
            }
            if (CollectionUtils.isNotEmpty(urlParamsKeyValueList)) {
                historyTableData.setUrlParams(JSONArray.toJSONString(urlParamsKeyValueList));
            }
            if (CollectionUtils.isNotEmpty(urlEncodedKeyValueList)) {
                historyTableData.setUrlEncoded(JSONArray.toJSONString(urlEncodedKeyValueList));
            }
            historyTableData.setJsonParam(((LanguageTextField) rowParamsTextArea).getText());
            if (CollectionUtils.isNotEmpty(multipartKeyValueList)) {
                historyTableData.setMultipart(JSONArray.toJSONString(multipartKeyValueList));
            }
        } else if(type == 2) {
            historyTableData = new HistoryTableData(methodName, getDubboSendUrl(), formattedDateTime);
            if (CollectionUtils.isNotEmpty(urlEncodedKeyValueList)) {
                historyTableData.setUrlEncoded(JSONArray.toJSONString(urlEncodedKeyValueList));
            }
        } else if (type == 3) {
            historyTableData = new HistoryTableData(methodName, getDubboSendUrl(), formattedDateTime);
            historyTableData.setJsonParam(((LanguageTextField) rowParamsTextArea).getText());
            FastRequestConfiguration configuration = FastRequestComponent.getInstance().getState();
            historyTableData.setPbInfo(configuration.getParamGroup().getPbImportPath(), configuration.getParamGroup().getPbFileName());
        } else if (type == 4) {
            historyTableData = new HistoryTableData(methodName, getDubboSendUrl(), formattedDateTime);
            historyTableData.setJsonParam(((LanguageTextField) rowParamsTextArea).getText());
        }
        historyTable.getList().add(0, historyTableData);
    }

    private void responsePageHandler(Response response, int status, String duration) {
        String statusDis = status + " " + Constant.HttpStatusDesc.STATUS_MAP.get(status);
        if(status >= 200 && status < 300){
            statusDis = "<html><span style=\"color: #0CCD08;\">" + statusDis + "</span></html>";
        }else {
            statusDis = "<html><span style=\"color: #FB1A00;\">" + statusDis + "</span></html>";
        }
        responseInfoParamsKeyValueList = Lists.newArrayList(
                new ParamKeyValue("Status", statusDis),
//                new ParamKeyValue("Response Size", getResponseSize(length), 2, TypeUtil.Type.String.name()),
                new ParamKeyValue("Time", duration + " ms", 2, TypeUtil.Type.String.name()),
                new ParamKeyValue(Header.DATE.getValue(), response.header(Header.DATE.getValue())),
                new ParamKeyValue(Header.CONTENT_TYPE.getValue(), response.header(Header.CONTENT_TYPE.getValue()), 2, TypeUtil.Type.String.name())
        );
        if(StringUtils.isNotBlank(response.header(Header.HOST.getValue()))){
            responseInfoParamsKeyValueList.add(new ParamKeyValue(Header.HOST.getValue(), response.header(Header.HOST.getValue()), 2, TypeUtil.Type.String.name()));
        }
        if(StringUtils.isNotBlank(response.header(Header.CONTENT_LENGTH.getValue()))){
            responseInfoParamsKeyValueList.add(new ParamKeyValue(Header.CONTENT_LENGTH.getValue(), response.header(Header.CONTENT_LENGTH.getValue()), 2, TypeUtil.Type.String.name()));
        }
        if(StringUtils.isNotBlank(response.header(Header.TRANSFER_ENCODING.getValue()))){
            responseInfoParamsKeyValueList.add(new ParamKeyValue(Header.TRANSFER_ENCODING.getValue(), response.header(Header.TRANSFER_ENCODING.getValue()), 2, TypeUtil.Type.String.name()));
        }
        if(StringUtils.isNotBlank(response.header("Server"))){
            responseInfoParamsKeyValueList.add(new ParamKeyValue("Server", response.header("Server"), 2, TypeUtil.Type.String.name()));
        }
        if(StringUtils.isNotBlank(response.header(Header.CONTENT_ENCODING.getValue()))){
            responseInfoParamsKeyValueList.add(new ParamKeyValue(Header.CONTENT_ENCODING.getValue(), response.header(Header.CONTENT_ENCODING.getValue()), 2, TypeUtil.Type.String.name()));
        }
        if(StringUtils.isNotBlank(response.header("X-Powered-By"))){
            responseInfoParamsKeyValueList.add(new ParamKeyValue("X-Powered-By", response.header("X-Powered-By"), 2, TypeUtil.Type.String.name()));
        }
        if(StringUtils.isNotBlank(response.header(Header.SET_COOKIE.getValue()))){
            responseInfoParamsKeyValueList.add(new ParamKeyValue(Header.SET_COOKIE.getValue(), response.header(Header.SET_COOKIE.getValue()), 2, TypeUtil.Type.String.name()));
        }


        //refreshTable(responseInfoTable);
        responseInfoTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("Key", "Value")), responseInfoParamsKeyValueList));
        responseInfoTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        responseInfoTable.getColumnModel().getColumn(0).setMaxWidth(150);
//                        responseStatusComboBox.setSelectedItem(status);
//                        responseStatusComboBox.setBackground((status >= 200 && status < 300) ? MyColor.green : MyColor.red);
    }

    private String getResponseSize(long length) {
        String result = length + " B";
        if(length > 1024  * 1024){
            //M
            BigDecimal size = BigDecimal.valueOf(length).divide(new BigDecimal("1024 * 1024"), 2, RoundingMode.HALF_UP);
            result = size + " MB";
        }else if(length > 1024){
            //KB
            BigDecimal size = BigDecimal.valueOf(length).divide(new BigDecimal("1024"), 2, RoundingMode.HALF_UP);
            result = size + " KB";
        }
        return result;
    }

    private void responseDubboPageHandler(String message, String duration) {
        responseInfoParamsKeyValueList = Lists.newArrayList(
                new ParamKeyValue("Url", getDubboSendUrl(), 2, TypeUtil.Type.String.name()),
                new ParamKeyValue("Message", message, 2, TypeUtil.Type.String.name()),
                new ParamKeyValue("Time", duration + " ms", 2, TypeUtil.Type.String.name()),
                new ParamKeyValue("Date", new Date())
        );
        responseInfoTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("Key", "Value")), responseInfoParamsKeyValueList));
        responseInfoTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        responseInfoTable.getColumnModel().getColumn(0).setMaxWidth(150);
    }

    private void pressureResponsePageHandler(PressureEntity pressureEntity) {
        BigDecimal bigDecimal_100 = new BigDecimal(100);
        responseInfoParamsKeyValueList = Lists.newArrayList(
                new ParamKeyValue("Qps", pressureEntity.getQps(), 2, TypeUtil.Type.String.name()),
                new ParamKeyValue("Avg Time", pressureEntity.getAvgTime() + "ms", 2, TypeUtil.Type.String.name()),
                new ParamKeyValue("Max Time", pressureEntity.getMaxTime() + "ms", 2, TypeUtil.Type.String.name()),
                new ParamKeyValue("Min Time", pressureEntity.getMinTime() + "ms", 2, TypeUtil.Type.String.name()),
                new ParamKeyValue("All Time", pressureEntity.getAllTime() + "ms", 2, TypeUtil.Type.String.name()),
                new ParamKeyValue("Errors", pressureEntity.getErrors(), 2, TypeUtil.Type.String.name()),
                new ParamKeyValue("Error Rate", pressureEntity.getErrorRate().multiply(bigDecimal_100).setScale(1) + "%", 2, TypeUtil.Type.String.name()),
                new ParamKeyValue("Corrects", pressureEntity.getCorrects(), 2, TypeUtil.Type.String.name()),
                new ParamKeyValue("Correct Rate", pressureEntity.getCorrectRate().multiply(bigDecimal_100).setScale(1) + "%", 2, TypeUtil.Type.String.name())
        );
        //refreshTable(responseInfoTable);
        responseInfoTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("Name", "Value")), responseInfoParamsKeyValueList));
        responseInfoTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        responseInfoTable.getColumnModel().getColumn(0).setMaxWidth(150);
//                        responseStatusComboBox.setSelectedItem(status);
//                        responseStatusComboBox.setBackground((status >= 200 && status < 300) ? MyColor.green : MyColor.red);
    }

    private void resultHandler(boolean finalFileMode, String body) {
        if (!finalFileMode) {
            int bodyLength = body.getBytes(StandardCharsets.UTF_8).length;
            if (bodyLength > MAX_DATA_LENGTH) {
                ((MyLanguageTextField) responseTextAreaPanel).setText(body);
                ((MyLanguageTextField) prettyJsonEditorPanel).setText(body);
                refreshResponseTable("");
            } else {
                if (JsonUtil.isJson(body)) {
                    responseTabbedPanel.setSelectedIndex(1);
                    MyLanguageTextField prettyJsonEditor = (MyLanguageTextField) prettyJsonEditorPanel;
                    prettyJsonEditor.setText(body.isBlank() ? "" : body);
                    if(!(prettyJsonEditor.getFileType() instanceof JsonFileType)){
                        prettyJsonEditor.updateFileLanguage(JsonFileType.INSTANCE, body);
                        prettyJsonEditor.setLanguage(JsonLanguage.INSTANCE);
                    }
                    ((MyLanguageTextField) responseTextAreaPanel).setText(body);
                    refreshResponseTable(body);
                } else {
                    if(bodyLength <= 0){
                        responseTabbedPanel.setSelectedIndex(3);
                    }else {
                        responseTabbedPanel.setSelectedIndex(2);
                    }
//                    String subBody = body.substring(0, Math.min(body.length(), 32768));
//                    if (body.length() > 32768) {
//                        subBody += "\n\ntext too large only show 32768 characters\n.............";
//                    }
                    MyLanguageTextField prettyJsonEditor = (MyLanguageTextField) prettyJsonEditorPanel;
                    prettyJsonEditor.setText(body);
                    if (XmlUtil.isXml(body)){
                        prettyJsonEditor.updateFileLanguage(XmlFileType.INSTANCE, body);
                        prettyJsonEditor.setLanguage(XMLLanguage.INSTANCE);
                    }
                    ((MyLanguageTextField) responseTextAreaPanel).setText(body);
                    refreshResponseTable("");
                }
            }
//            try {
//                org.jsoup.nodes.Document document = Jsoup.parse(body, "", Parser.htmlParser());
//                // 判断是否成功解析为 HTML
//                JBCefBrowser browser = new JBCefBrowser();
//                browser.loadURL("http://www.baidu.com");
//            }catch (Exception e){
//                //ignore
//            }
        }
    }

    private void fileHandler(boolean finalFileMode, int status, Response response) {
        if (finalFileMode && status >= 200 && status < 300) {
            ((MyLanguageTextField) prettyJsonEditorPanel).setText("");
            ((MyLanguageTextField) responseTextAreaPanel).setText("");
            Task.Backgroundable task = new Task.Backgroundable(myProject, "Saving file...") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        sendButtonFlag = false;
                        InputStream inputStream = null;
                        FileOutputStream outputStream = null;
                        File finalFile = null;
                        try {
                            FileSaverDialog fd = FileChooserFactory.getInstance().createSaveFileDialog(new FileSaverDescriptor("Save As", ""), myProject);
                            File f = new File(myProject.getBasePath());
                            finalFile = FileUtil.completeFileNameFromHeader(f, response);
                            inputStream = response.body().byteStream();
                            outputStream = new FileOutputStream(finalFile);
                            // Buffer to read data from the input stream
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            // Read data from the input stream and write it to the file
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            outputStream.flush();
                            outputStream.close();
                            VirtualFileWrapper fileWrapper = fd.save(URLDecoder.decode(finalFile.getName(), StandardCharsets.UTF_8));
                            if (fileWrapper != null) {
                                File file = fileWrapper.getFile();
                                FileUtil.move(finalFile, file, true);
                                NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification("Success", MessageType.INFO)
                                        .addAction(new GotoFile(file))
                                        .notify(myProject);
                            }
                        } catch (Exception e) {
                            NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification("出现了个未知问题，请联系作者处理~", MessageType.ERROR).notify(myProject);
                        }finally {
                            try {
                                if(inputStream != null){
                                    inputStream.close();
                                }
                            } catch (IOException e) {
                            }
                            if(finalFile != null){
                                finalFile.delete();
                            }
                            sendButtonFlag = true;
                        }
                    });
                }
            };
            ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
        }else {
             String body = "";
            try {
                body = response.body().string();
            } catch (IOException ignored) {
            }
            resultHandler(finalFileMode, body);
        }
    }


    private void refreshTable(JBTable table) {
        SwingUtilities.invokeLater(table::updateUI);
    }

    /**
     * text鼠标右键拷贝至粘贴板
     *
     * @param textarea 文本区域
     * @return {@link MouseAdapter }
     * @author Kings
     * @date 2021/06/07
     */
    private MouseAdapter copyMouseAdapter(JTextArea textarea) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String text = textarea.getText();
                if (SwingUtilities.isRightMouseButton(e) && StringUtils.isNotEmpty(text)) {
                    ToolUtil.setClipboardString(text);
                }
            }
        };
    }

    private MouseAdapter copyMouseAdapterField(JTextField textField) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String text = textField.getText();
                if (SwingUtilities.isRightMouseButton(e) && StringUtils.isNotEmpty(text)) {
                    ToolUtil.setClipboardString(text);
                }
            }
        };
    }

    /**
     * 根据配置设置domain
     *
     * @param config 配置
     */
    private void setDomain(FastRequestConfiguration config) {
//        warnLabel1.setVisible(config.getEnvList().isEmpty() || config.getProjectList().isEmpty());
        manageConfigButton.setVisible(config.getEnvList().isEmpty() || config.getProjectList().isEmpty());

        FastRequestCurrentProjectConfiguration projectConfig = FastRequestCurrentProjectConfigComponent.getInstance(myProject).getState();
        assert projectConfig != null;

        String activeEnv = getActiveEnv();
        String activeProject = getActiveProject();
        if (StringUtils.isEmpty(activeEnv)) {
            config.setDomain(StringUtils.EMPTY);
            projectConfig.setDomain(StringUtils.EMPTY);
//            warnLabel2.setVisible(true);
            return;
        }
        if (StringUtils.isEmpty(activeProject)) {
            config.setDomain(StringUtils.EMPTY);
            projectConfig.setDomain(StringUtils.EMPTY);
//            warnLabel2.setVisible(true);
            return;
        }
        NameGroup defaultNameGroup = new NameGroup(StringUtils.EMPTY, new ArrayList<>());
        HostGroup defaultHostGroup = new HostGroup(StringUtils.EMPTY, StringUtils.EMPTY);
        String domain = config.getDataList().stream().filter(n -> activeProject.equals(n.getName())).findFirst().orElse(defaultNameGroup)
                .getHostGroup().stream().filter(h -> activeEnv.equals(h.getEnv())).findFirst().orElse(defaultHostGroup).getUrl();
        config.setDomain(domain);
        projectConfig.setDomain(domain);
        changeUrl();
    }


    /**
     * message事件:修改env和project动态修改ToolWindow中的内容
     *
     */
    public void changeEnvAndProject() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;

        String activeEnv = getActiveEnv();
        String activeProject = getActiveProject();

//        warnLabel1.setVisible(config.getEnvList().isEmpty() || config.getProjectList().isEmpty());
        manageConfigButton.setVisible(config.getEnvList().isEmpty() || config.getProjectList().isEmpty());
//        warnLabel2.setVisible(StringUtils.isBlank(getActiveDomain()));


        ArrayList<String> projectListClone = Lists.newArrayList(NO_PROJECT);
        projectListClone.addAll(JSONObject.parseObject(JSONObject.toJSONString(config.getProjectList()), ArrayList.class));
        projectListClone.add("Add Project");
        CollectionComboBoxModel<String> projectModel = new CollectionComboBoxModel<>(projectListClone);
        projectComboBox.setModel(projectModel);


        ArrayList<String> envListClone = Lists.newArrayList(NO_ENV);
        envListClone.addAll(JSONObject.parseObject(JSONObject.toJSONString(config.getEnvList()), ArrayList.class));
        envListClone.add("Add Env");
        CollectionComboBoxModel<String> envModel = new CollectionComboBoxModel<>(envListClone);
        envComboBox.setModel(envModel);

        int idxProject = StringUtils.isBlank(activeProject) ? -1 : config.getProjectList().indexOf(activeProject);
        int idxEnv = StringUtils.isBlank(activeEnv) ? -1 : config.getEnvList().indexOf(activeEnv);
        projectComboBox.setSelectedIndex(Math.max(0, idxProject + 1));
        envComboBox.setSelectedIndex(Math.max(0, idxEnv + 1));
        setDomain(config);
    }

    /**
     * @param detail
     * @param flag   当前的url是否是当前的项目的
     */
    public void refreshByCollection(CollectionConfiguration.CollectionDetail detail, boolean flag) {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        multipartKeyValueList = new ArrayList<>();
        urlParamsTextArea.setText("");
        ((LanguageTextField) rowParamsTextArea).setText("");
        urlEncodedTextArea.setText("");
        ParamGroupCollection paramGroup = detail.getParamGroup();
        //更新相关信息
        assert config != null;
        ParamGroup paramGroupConfig = config.getParamGroup();
        paramGroupConfig.setOriginUrl(paramGroup.getOriginUrl());
        paramGroupConfig.setClassName(paramGroup.getClassName());
        paramGroupConfig.setMethod(paramGroup.getMethod());
        paramGroupConfig.setMethodDescription(detail.getName());

        String pathParamsKeyValueListJson = paramGroup.getPathParamsKeyValueListJson();
        String urlParamsKeyValueListJson = paramGroup.getUrlParamsKeyValueListJson();
        String urlParamsKeyValueListText = paramGroup.getUrlParamsKeyValueListText();
        String bodyKeyValueListJson = paramGroup.getBodyKeyValueListJson();
        String urlEncodedKeyValueListJson = paramGroup.getUrlEncodedKeyValueListJson();
        String urlEncodedKeyValueListText = paramGroup.getUrlEncodedKeyValueListText();
        String multipartKeyValueListJson = paramGroup.getMultipartKeyValueListJson();

        pathParamsKeyValueList = JSON.parseObject(pathParamsKeyValueListJson, new TypeReference<List<ParamKeyValue>>() {
        });
        urlParamsKeyValueList = JSON.parseObject(urlParamsKeyValueListJson, new TypeReference<List<ParamKeyValue>>() {
        });
        urlEncodedKeyValueList = JSON.parseObject(urlEncodedKeyValueListJson, new TypeReference<List<ParamKeyValue>>() {
        });
        multipartKeyValueList = JSON.parseObject(multipartKeyValueListJson, new TypeReference<List<ParamKeyValue>>() {
        });

        String methodType = paramGroup.getMethodType();

//        methodTypeComboBox.setFont(Font.BOLD);
//        methodTypeComboBox.setForeground(JBColor.BLUE);

        //method
        methodTypeComboBox.setSelectedItem(methodType);

        //headers默认取最新的
        calcHeaderList();

        if ("GET".equals(methodType)) {
            urlParamsTextArea.setText(urlParamsKeyValueListText);
            if (pathParamsKeyValueList.isEmpty()) {
                tabbedPane.setSelectedIndex(2);
            } else {
                tabbedPane.setSelectedIndex(1);
            }
            urlParamsTabbedPane.setSelectedIndex(0);
            //get请求urlencoded param参数为空
            urlEncodedKeyValueList = new ArrayList<>();
            urlEncodedTextArea.setText("");
            ((LanguageTextField) rowParamsTextArea).setText("");
        } else {
            //body param
            if (!bodyKeyValueListJson.isBlank()) {
                //json
                ((LanguageTextField) rowParamsTextArea).setText(bodyKeyValueListJson);
                tabbedPane.setSelectedIndex(3);
                bodyTabbedPane.setSelectedIndex(0);
                urlEncodedTextArea.setText("");
                urlEncodedKeyValueList = new ArrayList<>();
            } else {
                boolean isMultipart = multipartKeyValueList.stream().anyMatch(q -> TypeUtil.Type.File.name().equals(q.getType()));
                if (isMultipart) {
                    tabbedPane.setSelectedIndex(3);
                    bodyTabbedPane.setSelectedIndex(2);
                    urlEncodedTextArea.setText("");
                    urlEncodedKeyValueList = new ArrayList<>();
                } else {
                    //urlencoded
                    urlEncodedTextArea.setText(urlEncodedKeyValueListText);
                    tabbedPane.setSelectedIndex(3);
                    bodyTabbedPane.setSelectedIndex(1);
                }
                //json设置为空
                ((LanguageTextField) rowParamsTextArea).setText("");
                //如果是非get请求则request Param为空转到url Encoded参数下
                urlParamsKeyValueList = new ArrayList<>();
                urlParamsTextArea.setText("");
            }
        }

        //刷新table
        pathParamsTable.setModel(new ListTableModel<>(getPathColumnInfo(), pathParamsKeyValueList));
        resizeTable(pathParamsTable);
        setCheckBoxHeader(pathParamsTable, pathParamsCheckBoxHeader);

        urlParamsTable.setModel(new ListTableModel<>(getPathColumnInfo(), urlParamsKeyValueList));
        resizeTable(urlParamsTable);
        setCheckBoxHeader(urlParamsTable, urlParamsCheckBoxHeader);

        urlEncodedTable.setModel(new ListTableModel<>(getPathColumnInfo(), urlEncodedKeyValueList));
        resizeTable(urlEncodedTable);
        setCheckBoxHeader(urlEncodedTable, urlEncodedCheckBoxHeader);

        multipartTable.setModel(new ListTableModel<>(getPathColumnInfo(), multipartKeyValueList));
        resizeTable(multipartTable);
        setCheckBoxHeader(multipartTable, multipartCheckBoxHeader);
        //默认不刷第一个url 这里与complete冲突
//        urlTextField.setText(url);
        if (flag) {
            changeUrl();
        } else {
            String url = paramGroup.getUrl();
            if (!UrlUtil.isHttpURL(url)) {
                urlTextField.setText(detail.getDomain() + url);
            } else {
                urlTextField.setText(url);
            }
        }

    }

    /**
     * @param data
     * @param flag 当前的url是否是当前的项目的
     */
    public void refreshByHisCollection(HistoryTableData data, boolean flag) {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        multipartKeyValueList = new ArrayList<>();
        urlParamsTextArea.setText("");
        ((LanguageTextField) rowParamsTextArea).setText("");
        urlEncodedTextArea.setText("");
        //更新相关信息

        String pathParamsKeyValueListJson = data.getPathParams();
        String urlParamsKeyValueListJson = data.getUrlParams();
//        String urlParamsKeyValueListText = data.getUrlParams();
        String bodyKeyValueListJson = data.getJsonParam();
        String urlEncodedKeyValueListJson = data.getUrlEncoded();
//        String urlEncodedKeyValueListText = data.getUrlEncoded();
        String multipartKeyValueListJson = data.getMultipart();
        String headers = data.getHeaders();
        if (StringUtils.isNotBlank(headers)) {
            headerParamsKeyValueList = JSON.parseArray(headers, DataMapping.class);
        } else {
            headerParamsKeyValueList = new ArrayList<>();
        }
        try {
            if (data.getType().equals("GRPC")){
                String[] url = data.getUrl().split("/");
                config.getParamGroup().setClassName(url[url.length-2]);
                config.getParamGroup().setMethod(url[url.length-1]);
                config.getParamGroup().setPbInfo(data.getPbImportPath(), data.getPbFileName());
            }
        }catch (Exception e){

        }
        if (StringUtils.isNotBlank(pathParamsKeyValueListJson)) {
            pathParamsKeyValueList = JSON.parseObject(pathParamsKeyValueListJson, new TypeReference<List<ParamKeyValue>>() {
            });
        } else {
            pathParamsKeyValueList = new ArrayList<>();
        }
        if (StringUtils.isNotBlank(urlParamsKeyValueListJson)) {
            urlParamsKeyValueList = JSON.parseObject(urlParamsKeyValueListJson, new TypeReference<List<ParamKeyValue>>() {
            });
        } else {
            urlParamsKeyValueList = new ArrayList<>();
        }
        if (StringUtils.isNotBlank(urlEncodedKeyValueListJson)) {
            urlEncodedKeyValueList = JSON.parseObject(urlEncodedKeyValueListJson, new TypeReference<List<ParamKeyValue>>() {
            });
        } else {
            urlEncodedKeyValueList = new ArrayList<>();
        }
        if (StringUtils.isNotBlank(multipartKeyValueListJson)) {
            multipartKeyValueList = JSON.parseObject(multipartKeyValueListJson, new TypeReference<List<ParamKeyValue>>() {
            });
        } else {
            multipartKeyValueList = new ArrayList<>();
        }

        String methodType = data.getType();

//        methodTypeComboBox.setBackground(buildMethodColor(methodType));

        //method
        methodTypeComboBox.setSelectedItem(methodType);

        //headers默认取最新的
        calcHeaderList();

        if ("GET".equals(methodType)) {
//            urlParamsTextArea.setText(urlParamsKeyValueListText);
            List<String> urlParamsList = urlParamsKeyValueList.stream().filter(ParamKeyValue::getEnabled).map(x -> x.getKey() + "=" + x.getValue()).collect(Collectors.toList());
            urlParamsTextArea.setText(StringUtils.join(urlParamsList, "\n&"));
            if (pathParamsKeyValueList.isEmpty()) {
                tabbedPane.setSelectedIndex(2);
            } else {
                tabbedPane.setSelectedIndex(1);
            }
            urlParamsTabbedPane.setSelectedIndex(0);
            //get请求urlencoded param参数为空
            urlEncodedKeyValueList = new ArrayList<>();
            urlEncodedTextArea.setText("");
            ((LanguageTextField) rowParamsTextArea).setText("");
        } else {
            //body param
            if (StringUtils.isNotBlank(bodyKeyValueListJson)) {
                //json
                ((LanguageTextField) rowParamsTextArea).setText(bodyKeyValueListJson);
                tabbedPane.setSelectedIndex(3);
                bodyTabbedPane.setSelectedIndex(0);
                urlEncodedTextArea.setText("");
                urlEncodedKeyValueList = new ArrayList<>();
            } else {
                boolean isMultipart = multipartKeyValueList.stream().anyMatch(q -> TypeUtil.Type.File.name().equals(q.getType()));
                if (isMultipart) {
                    tabbedPane.setSelectedIndex(3);
                    bodyTabbedPane.setSelectedIndex(2);
                    urlEncodedTextArea.setText("");
                    urlEncodedKeyValueList = new ArrayList<>();
                } else {
                    //urlencoded
//                    urlEncodedTextArea.setText(urlEncodedKeyValueListText);
                    List<String> urlEncodedList = urlEncodedKeyValueList.stream().filter(ParamKeyValue::getEnabled).map(x -> x.getKey() + "=" + x.getValue()).collect(Collectors.toList());
                    urlEncodedTextArea.setText(StringUtils.join(urlEncodedList, "\n&"));
                    tabbedPane.setSelectedIndex(3);
                    bodyTabbedPane.setSelectedIndex(1);
                }
                //json设置为空
                ((LanguageTextField) rowParamsTextArea).setText("");
                //如果是非get请求则request Param为空转到url Encoded参数下
                urlParamsKeyValueList = new ArrayList<>();
                urlParamsTextArea.setText("");
            }
        }

        //刷新table
        headerTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("", "Header Name", "Header Value")), headerParamsKeyValueList));
        resizeHeaderTable(headerTable);

        pathParamsTable.setModel(new ListTableModel<>(getPathColumnInfo(), pathParamsKeyValueList));
        resizeTable(pathParamsTable);
        setCheckBoxHeader(pathParamsTable, pathParamsCheckBoxHeader);

        urlParamsTable.setModel(new ListTableModel<>(getPathColumnInfo(), urlParamsKeyValueList));
        resizeTable(urlParamsTable);
        setCheckBoxHeader(urlParamsTable, urlParamsCheckBoxHeader);

        urlEncodedTable.setModel(new ListTableModel<>(getPathColumnInfo(), urlEncodedKeyValueList));
        resizeTable(urlEncodedTable);
        setCheckBoxHeader(urlEncodedTable, urlEncodedCheckBoxHeader);

        multipartTable.setModel(new ListTableModel<>(getPathColumnInfo(), multipartKeyValueList));
        resizeTable(multipartTable);
        setCheckBoxHeader(multipartTable, multipartCheckBoxHeader);
        //默认不刷第一个url 这里与complete冲突
//        urlTextField.setText(url);
        if (flag) {
            changeUrl();
        } else {
            String url = data.getUrl();
            urlTextField.setText(url);
        }

    }

    private void setCheckBoxHeader(JTable table, CheckBoxHeader header) {
        TableColumn checkBoxColumn = table.getColumnModel().getColumn(0);
        checkBoxColumn.setHeaderRenderer(header);
    }

    /**
     * message事件:action操作生成数据,修改ToolWindow中的内容
     *
     * @author Kings
     * @date 2021/06/02
     */
    public void refresh(boolean regenerate) {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        CollectionConfiguration collectionConfiguration = FastRequestCollectionComponent.getInstance(myProject).getState();
        assert collectionConfiguration != null;

        ParamGroup paramGroup = config.getParamGroup();
//        String mid = "id_" + paramGroup.getClassName() + "." + paramGroup.getMethod();
//        CollectionConfiguration.CollectionDetail detail = filterById(mid, collectionConfiguration.getDetail());
//        if (detail != null && !regenerate) {
//            refreshByCollection(detail, true);
//            return;
//        }

        //reset value
        multipartKeyValueList = new ArrayList<>();
        urlParamsTextArea.setText("");
        ((LanguageTextField) rowParamsTextArea).setText("");
        urlEncodedTextArea.setText("");


        LinkedHashMap<String, Object> pathParamMap = paramGroup.getPathParamMap();
        LinkedHashMap<String, Object> requestParamMap = paramGroup.getRequestParamMap();
        bodyParamMap = paramGroup.getBodyParamMap() == null ? new LinkedHashMap<>() : paramGroup.getBodyParamMap();
        String methodType = paramGroup.getMethodType();
        Integer type = paramGroup.getType();

//        methodTypeComboBox.setBackground(buildMethodColor(methodType));

        //method
        methodTypeComboBox.setSelectedItem(methodType);
//        if(Objects.equals(methodType, "Dubbo")){
//            return;
//        }


        //request param
        String requestParamStr = conventDataToString(conventMapToList(requestParamMap));

        //默认urlParam是允许的即使是post json形式
        urlParamsKeyValueList = conventMapToList(requestParamMap);
        urlParamsTextArea.setText(requestParamStr);
        pathParamsKeyValueList = conventMapToList(pathParamMap);
        calcHeaderList();

        if ("GET".equals(methodType)) {
            if (pathParamsKeyValueList.isEmpty()) {
                tabbedPane.setSelectedIndex(2);
                urlParamsTabbedPane.setSelectedIndex(0);
            } else {
                tabbedPane.setSelectedIndex(1);
            }
            //get请求urlencoded param参数为空
            urlEncodedKeyValueList = new ArrayList<>();
            urlEncodedTextArea.setText("");
            ((LanguageTextField) rowParamsTextArea).setText("");
        } else {
            //body param(form和body只能存在其一)
            if (!bodyParamMap.isEmpty()) {
                //json
                tabbedPane.setSelectedIndex(3);
                bodyTabbedPane.setSelectedIndex(0);
                ((LanguageTextField) rowParamsTextArea).setText(type == null || type == 0 ? bodyParamMapToJson() : JSONObject.toJSONString(bodyParamMap));
                //body去除form参数
                urlEncodedTextArea.setText("");
                urlEncodedKeyValueList = new ArrayList<>();
            } else {
                urlEncodedKeyValueList = conventMapToList(requestParamMap);
                boolean isMultipart = urlEncodedKeyValueList.stream().anyMatch(q -> TypeUtil.Type.File.name().equals(q.getType()));
                if (isMultipart) {
                    tabbedPane.setSelectedIndex(3);
                    bodyTabbedPane.setSelectedIndex(2);
                    multipartKeyValueList = new ArrayList<>(urlEncodedKeyValueList);
                    urlEncodedTextArea.setText("");
                    urlEncodedKeyValueList = new ArrayList<>();
                } else {
                    //urlencoded
                    urlEncodedTextArea.setText(requestParamStr);
                    tabbedPane.setSelectedIndex(3);
                    bodyTabbedPane.setSelectedIndex(1);
                    urlEncodedTabbedPane.setSelectedIndex(0);
                    urlEncodedKeyValueList = conventMapToList(requestParamMap);
                }
                //json设置为空(form去除body参数)
                ((LanguageTextField) rowParamsTextArea).setText("");
                //如果是非get请求则request Param为空转到url Encoded参数下
                urlParamsKeyValueList = new ArrayList<>();
                urlParamsTextArea.setText("");
            }
        }
        //刷新table
        pathParamsTable.setModel(new ListTableModel<>(getPathColumnInfo(), pathParamsKeyValueList));
        resizeTable(pathParamsTable);
        setCheckBoxHeader(pathParamsTable, pathParamsCheckBoxHeader);

        urlParamsTable.setModel(new ListTableModel<>(getPathColumnInfo(), urlParamsKeyValueList));
        resizeTable(urlParamsTable);
        setCheckBoxHeader(urlParamsTable, urlParamsCheckBoxHeader);

        urlEncodedTable.setModel(new ListTableModel<>(getPathColumnInfo(), urlEncodedKeyValueList));
        resizeTable(urlEncodedTable);
        setCheckBoxHeader(urlEncodedTable, urlEncodedCheckBoxHeader);

        multipartTable.setModel(new ListTableModel<>(getPathColumnInfo(), multipartKeyValueList));
        resizeTable(multipartTable);
        setCheckBoxHeader(multipartTable, multipartCheckBoxHeader);

        setDomain(config);
    }

    public void resizeTable(JBTable table) {
        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(1).setMinWidth(100);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.setRowHeight(35);
        table.getColumnModel().getColumn(2).setPreferredWidth((int) Math.round(table.getWidth() * 0.3));
        table.getColumnModel().getColumn(3).setPreferredWidth((int) Math.round(table.getWidth() * 0.55));
    }

    public void resizeHeaderTable(JBTable table) {
        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.setRowHeight(35);
    }


    private void changeUrl() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        ParamGroup paramGroup = config.getParamGroup();
        String originUrl = paramGroup.getOriginUrl();
        if (StringUtils.isBlank(originUrl)) {
            return;
        }

        String url = buildPathParamUrl(originUrl);
        url = ((url.startsWith("/") || "".equals(url)) ? "" : "/") + url;
        if (!UrlUtil.isHttpURL(url) && urlCompleteChangeFlag.get()) {
            urlTextField.setText(getActiveDomain() + url);
            paramGroup.setUrl(getActiveDomain() + url);
        } else {
            urlTextField.setText(url);
            paramGroup.setUrl(url);
        }
        if (StringUtils.isNotBlank(paramGroup.getMethodType())) {
            methodTypeComboBox.setSelectedItem(paramGroup.getMethodType());
        }
    }

    private String buildPathParamUrl(String url) {
        List<String> paramNameList = UrlUtil.paramPathParam(url);
        if (paramNameList.isEmpty()) {
            return url;
        }
        for (ParamKeyValue paramKeyValue : pathParamsKeyValueList) {
            if (paramKeyValue.getEnabled()) {
                String paramName = paramKeyValue.getKey();
                String paramNameWithSymbol = "{" + paramName + "}";
                url = url.replace(paramNameWithSymbol, paramKeyValue.getValue().toString());
            }
        }
        return url;
    }

    private void renderingHeaderTablePanel() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        headerTable = createHeaderTable();
        headerTable.getEmptyText().setText("No header params");
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(headerTable);
        toolbarDecorator.setMoveDownAction(null);
        toolbarDecorator.setMoveUpAction(null);
        toolbarDecorator.setActionGroup(new FastRequestCollectionToolWindow.MyActionGroup(() -> new ClearAction()));
        toolbarDecorator.setAddAction(anActionButton -> {
                    if (headerParamsKeyValueList == null) {
                        headerParamsKeyValueList = new ArrayList<>();
                    }
                    int selectedRow = headerTable.getSelectedRow();
                    selectedRow = Math.min(selectedRow, headerParamsKeyValueList.size() - 1);
                    if (selectedRow == -1) {
                        headerParamsKeyValueList.add(new DataMapping("", ""));
                    } else {
                        headerParamsKeyValueList.add(selectedRow + 1, new DataMapping("", ""));
                    }
                    //refreshTable(headerTable);
                    headerTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("", "Header Name", "Header Value")), headerParamsKeyValueList));
                    headerTable.getColumnModel().getColumn(0).setMaxWidth(30);
                    setHeaderTitle();
                }
        ).setRemoveAction(anActionButton -> {
            removeUrlParamsTableLines(headerTable, null, null, null, headerParamsKeyValueList);
        }).setToolbarPosition(ActionToolbarPosition.TOP);
        toolbarDecorator.setActionGroup(new FastRequestCollectionToolWindow.MyActionGroup(() -> new ParentAction(MyResourceBundleUtil.getKey("header.group.manage"), "", PluginIcons.ICON_GROUP) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int idx = -1;
                List<HeaderGroup> headerGroupList = config.getHeaderGroupList();
                HeaderGroup currentHeaderGroup = headerGroupList.stream().filter(q -> Objects.equals(getActiveProject(), q.getProjectName())).findFirst().orElse(null);
                if (currentHeaderGroup != null) {
                    idx = headerGroupList.indexOf(currentHeaderGroup);
                }
                stopCellEditing();
                HeaderGroupView dialog = new HeaderGroupView(myProject, currentHeaderGroup, getActiveProject(), getActiveEnv(), config.getEnvList());
                if (dialog.showAndGet()) {
                    HeaderGroup viewHeaderGroup = dialog.changeAndGet();
                    if (idx == -1) {
                        headerGroupList.add(viewHeaderGroup);
                    } else {
                        headerGroupList.set(idx, viewHeaderGroup);
                    }
                    switchHeaderParam();
                }
            }

        }));
        headerPanel = toolbarDecorator.createPanel();
    }

    private void renderingResponseInfoPanel() {
        responseInfoTable = createResponseInfoTable();
        responseInfoTable.getEmptyText().setText("No info");
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(responseInfoTable);
        toolbarDecorator.setMoveDownAction(null);
        toolbarDecorator.setMoveUpAction(null);
        toolbarDecorator.setAddAction(null);
        toolbarDecorator.setRemoveAction(null);
        responseInfoPanel = toolbarDecorator.createPanel();
    }

    private void renderingJsonResponsePanel() {
        responseTable = createJsonResponseTable();
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(responseTable);
        toolbarDecorator.setMoveDownAction(null);
        toolbarDecorator.setMoveUpAction(null);
//        toolbarDecorator.setAddActionName("Add to Headers").setAddIcon(PluginIcons.ICON_ADD_GREEN).setAddAction(anActionButton -> {
//                //弹出菜单
//                Rectangle bounds = anActionButton.getContextComponent().getBounds();
//                headerPopupMenu.show(jsonResponsePanel, bounds.x, bounds.y);
//            }
//        );
        toolbarDecorator.setRemoveAction(null);
        toolbarDecorator.setAddAction(null);

//        toolbarDecorator.setAddActionUpdater(e -> {
//            int selectedRow = responseTable.getSelectedRow();
//            ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) responseTable.getTableModel();
//            CustomNode node = (CustomNode) myModel.getRowValue(selectedRow);
//            return node != null && node.isLeaf() && selectedRow != 0;
//        }).setToolbarPosition(ActionToolbarPosition.TOP);
        jsonResponsePanel = toolbarDecorator.createPanel();
    }

    /**
     * 渲染UrlParams table面板
     *
     * @author Kings
     * @date 2021/06/02
     */
    private void renderingUrlParamsTablePanel() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        ParamGroup paramGroup = config.getParamGroup();
        String methodType = paramGroup.getMethodType();
        if (!"GET".equals(methodType)) {
            urlParamsKeyValueList = new ArrayList<>();
        }
        urlParamsTable = createUrlParamsKeyValueTable();
        urlParamsTable.getEmptyText().setText("No params");
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(urlParamsTable);
        toolbarDecorator.setMoveDownAction(null);
        toolbarDecorator.setMoveUpAction(null);
        toolbarDecorator.setActionGroup(new FastRequestCollectionToolWindow.MyActionGroup(() -> new ClearAction()));

        toolbarDecorator.setAddAction(anActionButton -> {
                    int selectedRow = urlParamsTable.getSelectedRow();
                    selectedRow = urlParamsKeyValueList.isEmpty() ? -1 : selectedRow;
                    selectedRow = Math.min(selectedRow, urlParamsKeyValueList.size() - 1);
                    if (selectedRow == -1) {
                        urlParamsKeyValueList.add(new ParamKeyValue("", "", 2, TypeUtil.Type.String.name()));
                    } else {
                        urlParamsKeyValueList.add(selectedRow + 1, new ParamKeyValue("", "", 2, TypeUtil.Type.String.name()));
                    }
                    refreshTable(urlParamsTable);
                    //urlParamsTable.setModel(new ListTableModel<>(getPathColumnInfo(), urlParamsKeyValueList));
                    resizeTable(urlParamsTable);
                }
        ).setRemoveAction(anActionButton -> {
            removeUrlParamsTableLines(urlParamsTable, urlParamsKeyValueList, urlParamsTextArea, urlParamsChangeFlag, null);
        }).setToolbarPosition(ActionToolbarPosition.TOP);
        urlParamsTablePanel = toolbarDecorator.createPanel();
    }

    class ClearAction extends ParentAction {
        public ClearAction() {
            super("Clear", "", PluginIcons.ICON_CLEAR);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            int selectedIndex = tabbedPane.getSelectedIndex();
            switch (selectedIndex) {
                case 0:
                    headerParamsKeyValueList.forEach(dataMapping -> {
                        dataMapping.setType("");
                        dataMapping.setValue("");
                    });
                    headerTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("", "Header Name", "Header Value")), headerParamsKeyValueList));
                    headerTable.getColumnModel().getColumn(0).setMaxWidth(30);
                    saveAndChangeHeader();
                    switchHeaderParam();
                    break;
                case 1:
                    pathParamsKeyValueList.forEach(paramKeyValue -> {
                        paramKeyValue.setKey("");
                        paramKeyValue.setValue("");
                    });
                    refreshTable(pathParamsTable);
                    resizeTable(pathParamsTable);
                    break;
                case 2:
                    urlParamsKeyValueList.forEach(paramKeyValue -> {
                        paramKeyValue.setKey("");
                        paramKeyValue.setValue("");
                    });
                    refreshTable(urlParamsTable);
                    resizeTable(urlParamsTable);
                    changeUrlParamsText();
                    break;
                case 3:
                    if (bodyTabbedPane.getSelectedIndex() == 1) {
                        urlEncodedKeyValueList.forEach(paramKeyValue -> {
                            paramKeyValue.setKey("");
                            paramKeyValue.setValue("");
                        });
                        refreshTable(urlEncodedTable);
                        resizeTable(urlEncodedTable);
                        changeUrlEncodedParamsText();
                    } else if (bodyTabbedPane.getSelectedIndex() == 2) {
                        multipartKeyValueList.forEach(paramKeyValue -> {
                            paramKeyValue.setKey("");
                            paramKeyValue.setValue("");
                        });
                        refreshTable(multipartTable);
                        resizeTable(multipartTable);
                    }
                    break;
            }

        }
    }

    private void removeUrlParamsTableLines(JBTable targetTable, List<ParamKeyValue> paramsKeyValueList,
                                           JTextArea targetArea, AtomicBoolean flag, List<DataMapping> headerParamsList) {
        int[] selectedIndices = targetTable.getSelectionModel().getSelectedIndices();
        List<Integer> indexes = Arrays.stream(selectedIndices).boxed().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(paramsKeyValueList)) {
            indexes.removeIf(q -> q > paramsKeyValueList.size() - 1);
            indexes.stream().mapToInt(i -> i).forEach(paramsKeyValueList::remove);
            refreshTable(targetTable);
            resizeTable(targetTable);
        }
        if (CollectionUtils.isNotEmpty(headerParamsList)) {
            indexes.removeIf(q -> q > headerParamsList.size() - 1);
            indexes.stream().mapToInt(i -> i).forEach(headerParamsList::remove);
            headerTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("", "Header Name", "Header Value")), headerParamsKeyValueList));
            headerTable.getColumnModel().getColumn(0).setMaxWidth(30);
            saveAndChangeHeader();
            switchHeaderParam();
        }
        targetTable.getSelectionModel().clearSelection();
        if (selectedIndices[0] > 0) {
            int row = selectedIndices[0] - 1;
            targetTable.getSelectionModel().setSelectionInterval(row, row);
            targetTable.setRowSelectionInterval(row, row);
        } else {
            if (targetTable.getRowCount() > 0) {
                targetTable.getSelectionModel().setSelectionInterval(0, 0);
                targetTable.setRowSelectionInterval(0, 0);
            }
        }
        if (targetArea != null) {
            String paramStr = conventDataToString(paramsKeyValueList);
            targetArea.setText(paramStr);
            flag.set(false);
        }
    }

    private void clearTableLines(JBTable targetTable, List<ParamKeyValue> paramsKeyValueList,
                                 JTextArea targetArea, AtomicBoolean flag, List<DataMapping> headerParamsList) {
        int[] selectedIndices = targetTable.getSelectionModel().getSelectedIndices();
        List<Integer> indexes = Arrays.stream(selectedIndices).boxed().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(paramsKeyValueList)) {
            indexes.removeIf(q -> q > paramsKeyValueList.size() - 1);
            indexes.stream().mapToInt(i -> i).forEach(index -> {
                paramsKeyValueList.get(index).setKey("");
                paramsKeyValueList.get(index).setValue("");
            });
            refreshTable(targetTable);
            resizeTable(targetTable);
        }
        if (CollectionUtils.isNotEmpty(headerParamsList)) {
            indexes.removeIf(q -> q > headerParamsList.size() - 1);
            indexes.stream().mapToInt(i -> i).forEach(index -> {
                headerParamsList.get(index).setType("");
                headerParamsList.get(index).setValue("");
            });
            headerTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("", "Header Name", "Header Value")), headerParamsKeyValueList));
            headerTable.getColumnModel().getColumn(0).setMaxWidth(30);
            saveAndChangeHeader();
            switchHeaderParam();
        }
        if (targetArea != null) {
            String paramStr = conventDataToString(paramsKeyValueList);
            targetArea.setText(paramStr);
            flag.set(false);
        }
    }

    public void refreshResponseTable(String body) {
        CustomNode root = new CustomNode("Root", "");
        if (StringUtils.isBlank(body)) {
            ((DefaultTreeModel) responseTable.getTableModel()).setRoot(root);
            return;
        }
        try {
            if (body.startsWith("{")) {
                convertJsonObjectToNode(root, JSONObject.parseObject(body));
                ((DefaultTreeModel) responseTable.getTableModel()).setRoot(root);
            } else {
                convertJsonArrayToNode("index ", JSONObject.parseArray(body), root);
                ((DefaultTreeModel) responseTable.getTableModel()).setRoot(root);
            }

            expandAll(responseTable.getTree(), new TreePath(root), true);
        }catch (Exception e){
            ((DefaultTreeModel) responseTable.getTableModel()).setRoot(root);
        }
        responseTable.updateUI();
    }

    private void renderingPathParamsPanel() {
        pathParamsTable = createPathParamKeyValueTable();
        pathParamsTable.getEmptyText().setText("No params");
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(pathParamsTable);
        toolbarDecorator.setMoveDownAction(null);
        toolbarDecorator.setMoveUpAction(null);
        toolbarDecorator.setActionGroup(new FastRequestCollectionToolWindow.MyActionGroup(() -> new ClearAction()));

        toolbarDecorator.setAddAction(anActionButton -> {
                    int selectedRow = pathParamsTable.getSelectedRow();
                    selectedRow = Math.min(selectedRow, pathParamsKeyValueList.size() - 1);
                    if (selectedRow == -1) {
                        pathParamsKeyValueList.add(new ParamKeyValue("", "", 2, TypeUtil.Type.String.name()));
                    } else {
                        pathParamsKeyValueList.add(selectedRow + 1, new ParamKeyValue("", "", 2, TypeUtil.Type.String.name()));
                    }
                    refreshTable(pathParamsTable);
                    //pathParamsTable.setModel(new ListTableModel<>(getPathColumnInfo(), pathParamsKeyValueList));
                    resizeTable(pathParamsTable);
                    changeUrl();
                }
        ).setRemoveAction(anActionButton -> {
            removeUrlParamsTableLines(pathParamsTable, pathParamsKeyValueList, null, null, null);
        }).setToolbarPosition(ActionToolbarPosition.TOP);
        pathParamsPanel = toolbarDecorator.createPanel();
    }

    /**
     * 渲染UrlEncoded table面板
     *
     * @author Kings
     * @date 2021/06/02f
     */
    private void renderingUrlEncodedPanel() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        ParamGroup paramGroup = config.getParamGroup();
        LinkedHashMap<String, Object> bodyParamMap = paramGroup.getBodyParamMap();
        String methodType = paramGroup.getMethodType();
        if (!"GET".equals(methodType)) {
            //body param
            Object bodyParam = bodyParamMap.values().stream().findFirst().orElse("");
            if ("".equals(bodyParam)) {
                //json形式 urlencoded 值为空
                urlEncodedKeyValueList = new ArrayList<>();
            }
        } else {
            //get urlencoded 值为空
            urlEncodedKeyValueList = new ArrayList<>();
        }
        urlEncodedTable = createUrlEncodedKeyValueTable();
        urlEncodedTable.getEmptyText().setText("No params");
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(urlEncodedTable);
        toolbarDecorator.setMoveDownAction(null);
        toolbarDecorator.setMoveUpAction(null);
        toolbarDecorator.setActionGroup(new FastRequestCollectionToolWindow.MyActionGroup(() -> new ClearAction()));


        toolbarDecorator.setAddAction(anActionButton -> {
                    int selectedRow = urlEncodedTable.getSelectedRow();
                    selectedRow = Math.min(selectedRow, urlEncodedKeyValueList.size() - 1);
                    if (selectedRow == -1) {
                        urlEncodedKeyValueList.add(new ParamKeyValue("", "", 2, TypeUtil.Type.String.name()));
                    } else {
                        urlEncodedKeyValueList.add(selectedRow + 1, new ParamKeyValue("", "", 2, TypeUtil.Type.String.name()));
                    }
                    refreshTable(urlEncodedTable);
                    //urlEncodedTable.setModel(new ListTableModel<>(getPathColumnInfo(), urlEncodedKeyValueList));
                    resizeTable(urlEncodedTable);
                }
        ).setRemoveAction(anActionButton -> {
            removeUrlParamsTableLines(urlEncodedTable, urlEncodedKeyValueList, urlEncodedTextArea, urlEncodedParamChangeFlag, null);
        }).setToolbarPosition(ActionToolbarPosition.TOP);
        urlEncodedTablePanel = toolbarDecorator.createPanel();
    }

    public void renderingMultipartPanel() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        ParamGroup paramGroup = config.getParamGroup();
        LinkedHashMap<String, Object> bodyParamMap = paramGroup.getBodyParamMap();
        String methodType = paramGroup.getMethodType();
        if (!"GET".equals(methodType)) {
            //body param
            Object bodyParam = bodyParamMap.values().stream().findFirst().orElse("");
            if ("".equals(bodyParam)) {
                //json形式 urlencoded 值为空
                multipartKeyValueList = new ArrayList<>();
            }
        } else {
            //get urlencoded 值为空
            multipartKeyValueList = new ArrayList<>();
        }
        multipartTable = createMultipartKeyValueTable();
        multipartTable.getEmptyText().setText("No params");
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(multipartTable);
        toolbarDecorator.setMoveDownAction(null);
        toolbarDecorator.setMoveUpAction(null);
        toolbarDecorator.setActionGroup(new FastRequestCollectionToolWindow.MyActionGroup(() -> new ClearAction()));


        toolbarDecorator.setAddAction(anActionButton -> {
                    int selectedRow = multipartTable.getSelectedRow();
                    selectedRow = Math.min(selectedRow, multipartKeyValueList.size() - 1);
                    if (selectedRow == -1) {
                        multipartKeyValueList.add(new ParamKeyValue("", "", 2, TypeUtil.Type.String.name()));
                    } else {
                        multipartKeyValueList.add(selectedRow + 1, new ParamKeyValue("", "", 2, TypeUtil.Type.String.name()));
                    }
                    refreshTable(multipartTable);
                    //multipartTable.setModel(new ListTableModel<>(getPathColumnInfo(), multipartKeyValueList));
                    resizeTable(multipartTable);
                }
        ).setRemoveAction(anActionButton -> {
            removeUrlParamsTableLines(multipartTable, multipartKeyValueList, null, null, null);
        }).setToolbarPosition(ActionToolbarPosition.TOP);
        multipartTablePanel = toolbarDecorator.createPanel();
    }

    /**
     * LinkedHashMap数据转ParamKeyValue集合
     *
     * @param paramLinkedMap 参数与地图
     * @return {@link List<ParamKeyValue> }
     * @author Kings
     * @date 2021/06/02
     */
    private List<ParamKeyValue> conventMapToList(LinkedHashMap<String, ?> paramLinkedMap) {
        List<ParamKeyValue> paramKeyValueList = new ArrayList<>();
        paramLinkedMap.forEach((key, value) -> {
            if (value instanceof ParamKeyValue){
                ParamKeyValue paramKeyValue = (ParamKeyValue) value;
                if (paramKeyValue.getCustomFlag() == 1) {
                    KV<String, ParamKeyValue> data = (KV<String, ParamKeyValue>) paramKeyValue.getValue();
                    //kv转成普通类型
                    List<ParamKeyValue> list = new ArrayList<>();
                    convertToParamKeyValueList("", data, list);
                    paramKeyValueList.addAll(list);
                } else {
                    paramKeyValueList.add(paramKeyValue);
                }
            }else {
                paramKeyValueList.add(new ParamKeyValue(key, "", 2, "String"));
            }

        });
        return paramKeyValueList;
    }

    private List<ParamKeyValue> conventPathParamsToList(LinkedHashMap<String, Object> paramLinkedMap) {
        List<ParamKeyValue> paramKeyValueList = new ArrayList<>();
        paramLinkedMap.forEach((key, value) -> paramKeyValueList.add((ParamKeyValue) value));
        return paramKeyValueList;
    }

    /**
     * requestParam urlEncodedParam 转 text
     * 每个参数换行处理
     *
     * @param paramKeyValueList 参数键值列表
     * @return {@link String }
     * @author Kings
     * @date 2021/06/02
     */
    private String conventDataToString(List<ParamKeyValue> paramKeyValueList) {
        StringBuilder sb = new StringBuilder();
        paramKeyValueList.forEach(paramKeyValue -> {
            Object value = paramKeyValue.getValue();
            value = paramKeyValue.getEnabled() ? value : "";
            if (paramKeyValue.getCustomFlag() == 2) {
                //基本类型映射  key=value
                sb.append(paramKeyValue.getKey()).append("=").append(value).append("\n&");
            } else {
                //对象 直接拼上value
                sb.append(value).append("\n&");
            }
        });
        return StringUtils.removeEnd(sb.toString(), "\n&");
    }


    private ColumnInfo<Object, Object>[] getPathColumnInfo() {
        ColumnInfo<Object, Object>[] columnArray = new ColumnInfo[4];
        List<String> titleList = Lists.newArrayList("", "Type", "Key", "Value");
        for (int i = 0; i < titleList.size(); i++) {
            ColumnInfo<Object, Object> envColumn = new ColumnInfo<>(titleList.get(i)) {
                @Override
                public @Nullable Object valueOf(Object o) {
                    return o;
                }
            };
            columnArray[i] = envColumn;
        }
        return columnArray;
    }


    /**
     * table列信息
     *
     * @return {@link ColumnInfo<Object, Object>[] }
     * @author Kings
     * @date 2021/06/02
     */
    private ColumnInfo<Object, Object>[] getColumnInfo() {
        ColumnInfo<Object, Object>[] columnArray = new ColumnInfo[2];
        List<String> titleList = Lists.newArrayList("Key", "Value");
        for (int i = 0; i < titleList.size(); i++) {
            ColumnInfo<Object, Object> envColumn = new ColumnInfo<>(titleList.get(i)) {
                @Override
                public @Nullable Object valueOf(Object o) {
                    return o;
                }
            };

            columnArray[i] = envColumn;
        }
        return columnArray;
    }


    private TreeTableView createJsonResponseTable() {
        //初始化为空
        CustomNode root = new CustomNode("Root", "");
        convertToNode(true, root, new LinkedHashMap<>());
        ColumnInfo[] columnInfo = new ColumnInfo[]{
                new TreeColumnInfo("Name") {
                    @Override
                    public int getWidth(JTable table) {
                        return JSON_TABLE_COLUMN_NAME_WIDTH;
                    }

                },   // <-- This is important!
                new ColumnInfo("Value") {
                    @Nullable
                    @Override
                    public Object valueOf(Object o) {
                        if (o instanceof CustomNode) {
                            return ((CustomNode) o).getValue();
                        } else return o;
                    }
                }
        };

        ListTreeTableModelOnColumns model = new ListTreeTableModelOnColumns(root, columnInfo);
        TreeTableView table = new TreeTableView(model) {
            @Override
            public void setTreeCellRenderer(TreeCellRenderer renderer) {
                super.setTreeCellRenderer(new Renderer());
            }

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
//                if (row != 0 && column == 1) {
//                    return new MyWrapCellRenderer();
//                }
                return super.getCellRenderer(row, column);
            }


            @Override
            public TableCellEditor getCellEditor(int row, int column) {
//                if (row != 0 && column == 1) {
//                    return new MyWrapCellEditor();
//                }
                return super.getCellEditor(row, column);
            }

            @Override
            public Object getValueAt(int row, int column) {
                ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) getTableModel();
                CustomNode node = (CustomNode) myModel.getRowValue(row);
                if (column == 0) {
                    return node.getKey();
                } else {
                    return node.getValue();
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
//                ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) getTableModel();
//                CustomNode node = (CustomNode) myModel.getRowValue(row);
//                return row != 0 && column == 1 && (!TypeUtil.Type.Object.name().equals(node.getType()) && !TypeUtil.Type.Array.name().equals(node.getType()));
            }

        };
        table.setRootVisible(true);
        table.setVisible(true);
        table.setCellSelectionEnabled(true);
        table.setRowHeight(25);
        return table;
    }

    /**
     * 解析数据,异常返回默认值
     *
     * @param type 类型
     * @return {@link Object }
     * @author Kings
     * @date 2021/06/09
     */
    private Object convertCellData(Object toBeConvert, String type) {
        Object defaultValue = null;
        try {

            switch (type) {
                case "Number":
                    defaultValue = 1;
                    break;
                case "Boolean":
                    defaultValue = true;
                    break;
                default:
                    defaultValue = "";
                    break;
            }
            if (TypeUtil.Type.Number.name().equals(type)) {
                return Integer.parseInt(toBeConvert.toString());
            } else if (TypeUtil.Type.Boolean.name().equals(type)) {
                return Boolean.parseBoolean(toBeConvert.toString());
            } else {
                return String.valueOf(toBeConvert);
            }
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void jsonTableNodeToJson(CustomNode firstNode, JSONObject jsonObject) {
        Iterator<TreeNode> treeNodeIterator = firstNode.children().asIterator();
        while (treeNodeIterator.hasNext()) {
            CustomNode node = (CustomNode) treeNodeIterator.next();
            String key = node.getKey();
            String type = node.getType();
            Object value = node.getValue();
            if (TypeUtil.Type.Object.name().equals(type)) {
                if (node.getChildCount() == 0) {
                    continue;
                }
                if (key.contains("index ")) {
                    JSONObject jsonObjectChild = new JSONObject(new LinkedHashMap<>());
                    jsonTableNodeToJson(node, jsonObjectChild);
                    jsonObject.putAll(jsonObjectChild);
                } else {
                    JSONObject jsonObjectChild = new JSONObject(new LinkedHashMap<>());
                    jsonTableNodeToJson(node, jsonObjectChild);
                    jsonObject.put(key, jsonObjectChild);
                }
            } else if (TypeUtil.Type.Array.name().equals(type)) {
                if (node.getChildCount() == 0) {
                    continue;
                }
                JSONArray jsonArrayChild = new JSONArray();
                jsonTableNodeToJsonArray(jsonArrayChild, node);
                jsonObject.put(key, jsonArrayChild);
            } else {
                jsonObject.put(key, convertCellData(value, type));
            }
        }
    }

    private void jsonTableNodeToJsonArray(JSONArray jsonArrayChild, CustomNode nodeHasChild) {
        Iterator<TreeNode> treeNodeIterator = nodeHasChild.children().asIterator();
        while (treeNodeIterator.hasNext()) {
            CustomNode node = (CustomNode) treeNodeIterator.next();
            String key = node.getKey();
            String type = node.getType();
            Object value = node.getValue();
            if (TypeUtil.Type.Object.name().equals(type)) {
                if (node.getChildCount() == 0) {
                    continue;
                }
                JSONObject jsonObjectChild = new JSONObject(new LinkedHashMap<>());
                jsonTableNodeToJson(node, jsonObjectChild);
                jsonArrayChild.add(jsonObjectChild);
            } else if (TypeUtil.Type.Array.name().equals(type)) {
                if (node.getChildCount() == 0) {
                    continue;
                }

                JSONArray jsonArrayChildChild = new JSONArray();
                jsonTableNodeToJsonArray(jsonArrayChildChild, node);
                jsonArrayChild.add(jsonArrayChildChild);
            } else {
                jsonArrayChild.add(convertCellData(value, type));
            }
        }
    }


    private String bodyParamMapToJson() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        convertToMap(bodyParamMap, map, false);
        if (Objects.equals("DUBBO", methodTypeComboBox.getSelectedItem())) {
            return JSON.toJSONString(map, true);
        } else {
            return JSON.toJSONString(map.get(map.keySet().stream().findFirst().orElse("")), true);
        }
    }


    class Renderer extends ColoredTreeCellRenderer {
        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            //解决TreeTable key加上>
            CustomNode node = (CustomNode) value;
            append(node.getKey());
            setToolTipText(node.getComment());
        }
    }

    class IconListRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;
        private Map<Object, Icon> icons = null;

        public IconListRenderer(Map<Object, Icon> icons) {
            this.icons = icons;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//            Icon icon = icons.get(value);
            JLabel picture = new JLabel(value.toString(), null, JLabel.LEFT);
//            if (index != -1) {
//                //下拉才显示值
//                picture.setText(value.toString());
//            }
            picture.setHorizontalAlignment(JLabel.LEFT);
            return picture;
        }


    }

    private static void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    private ColumnInfo[] jsonColumnInfo() {
        ColumnInfo value = new ColumnInfo("Value") {
            @Nullable
            @Override
            public Object valueOf(Object o) {
                if (o instanceof CustomNode) {
                    return ((CustomNode) o).getValue();
                } else return o;
            }
        };
        ColumnInfo type = new ColumnInfo("Type") {
            @Nullable
            @Override
            public Object valueOf(Object o) {
                if (o instanceof CustomNode) {
                    return ((CustomNode) o).getType();
                } else return o;
            }

            @Override
            public int getWidth(JTable table) {
                return JSON_TABLE_COLUMN_TYPE_WIDTH;
            }
        };

        ColumnInfo[] columnInfo = new ColumnInfo[]{
                new TreeColumnInfo("Name") {

                    @Override
                    public int getWidth(JTable table) {
                        return JSON_TABLE_COLUMN_NAME_WIDTH;
                    }

                },   // <-- This is important!
                type,
                value
        };
        return columnInfo;
    }

    private CustomNode convertJsonObjectToNode(CustomNode node, JSONObject jsonObject) {
        LinkedHashMap<String, Object> linkedHashMap = JSON.parseObject(JSON.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue), new TypeReference<>() {
        });
        Set<String> keys = linkedHashMap.keySet();
        keys.forEach(key -> {
//            node.setKey(key);
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                JSONObject valueJsonObject = (JSONObject) value;
                CustomNode customNode = new CustomNode(key, null, TypeUtil.Type.Object.name());
                node.add(convertJsonObjectToNode(customNode, valueJsonObject));
            } else if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                if (jsonArray.size() == 0) {
                    return;
                }
                CustomNode nodeArray = new CustomNode(key, "", TypeUtil.Type.Array.name());
                convertJsonArrayToNode("index ", jsonArray, nodeArray);
                node.add(nodeArray);
            } else {
                node.add(new CustomNode(key, value == null ? "null" : value));
            }
        });
        return node;
    }

    private void convertJsonArrayToNode(String key, JSONArray jsonArray, CustomNode node) {
        AtomicInteger idx = new AtomicInteger();
        jsonArray.forEach(json -> {
            CustomNode nodeArray = new CustomNode(key + (idx.get()), null);
            if (json instanceof JSONObject) {
                JSONObject valueJsonObject = (JSONObject) json;
                nodeArray.setType(TypeUtil.Type.Object.name());
                node.add(convertJsonObjectToNode(nodeArray, valueJsonObject));
            } else if (json instanceof JSONArray) {
                JSONArray tmpJsonArray = (JSONArray) json;
                if (tmpJsonArray.size() == 0) {
                    return;
                }
                CustomNode nodeArrayIn = new CustomNode(key, "");
                convertJsonArrayToNode("index ", tmpJsonArray, nodeArrayIn);
                nodeArray.setType(TypeUtil.Type.Array.name());
                nodeArray.add(nodeArrayIn);
                node.add(nodeArray);
            } else {
                node.add(new CustomNode(key + (idx.get()), json));
            }
            idx.getAndIncrement();
        });
    }

    /**
     * json数据转化为map用于text展示(递归遍历)
     *
     * @param data   数据
     * @param result 结果
     * @return
     * @author Kings
     * @date 2021/06/07
     */
    private void convertToMap(LinkedHashMap<String, Object> data, LinkedHashMap<String, Object> result, boolean isRoot) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            ParamKeyValue value = (ParamKeyValue) entry.getValue();
            String type = value.getType();
            Object dataValue = value.getValue();
            if (TypeUtil.Type.Object.name().equals(type)) {
                LinkedHashMap<String, Object> objectLinkedHashMap = new LinkedHashMap<>();
                LinkedHashMap<String, Object> kv = (LinkedHashMap<String, Object>) dataValue;
                if (kv != null) {
                    if (isRoot) {
                        LinkedHashMap<String, Object> rootMap = new LinkedHashMap<>();
                        convertToMap(kv, rootMap, false);
                        result.putAll(rootMap);
                    } else {
                        convertToMap(kv, objectLinkedHashMap, false);
                        result.put(key, objectLinkedHashMap);
                    }
                }
            } else if (TypeUtil.Type.Array.name().equals(type)) {
                if (dataValue instanceof KV) {
                    List<Object> list = new ArrayList<>();
                    LinkedHashMap<String, Object> arrayMap = new LinkedHashMap<>();
                    convertToMap((LinkedHashMap<String, Object>) dataValue, arrayMap, false);
                    list.add(arrayMap);
                    result.put(key, list);
                } else {
                    ArrayList<ParamKeyValue> dataList = (ArrayList<ParamKeyValue>) dataValue;
                    if (dataList.size() == 0) {
                        continue;
                    }
                    LinkedHashMap<String, Object> arrayMap = new LinkedHashMap<>();
                    List<Object> list = convertArrayToMap(dataList, arrayMap);
                    result.put(key, list);
                }
            } else {
                result.put(key, dataValue);
            }
        }

    }

    /**
     * json数据 list转化为map用于text展示(递归遍历)
     *
     * @param data   数据
     * @param result 结果
     * @return
     * @author Kings
     * @date 2021/06/07
     */
    private List<Object> convertArrayToMap(ArrayList<?> data, LinkedHashMap<String, Object> result) {
        List<Object> list = new ArrayList<>();
        for (Object o : data) {
            if (o instanceof ParamKeyValue) {
                list.add(((ParamKeyValue) o).getValue());
            } else {
                KV<String, ParamKeyValue> kv = (KV<String, ParamKeyValue>) o;
                kv.keySet().forEach(k -> {
                    ParamKeyValue paramKeyValue = kv.get(k);
                    String type = paramKeyValue.getType();
                    Object value = paramKeyValue.getValue();
                    if (TypeUtil.Type.Object.name().equals(type)) {
                        LinkedHashMap<String, Object> objectLinkedHashMap = new LinkedHashMap<>();
                        LinkedHashMap<String, Object> kvValue = (KV<String, Object>) value;
                        if (kvValue != null) {
                            convertToMap(kvValue, objectLinkedHashMap, false);
                            result.put(k, objectLinkedHashMap);
                        }
                    } else if (TypeUtil.Type.Array.name().equals(type)) {
                        ArrayList<KV<String, ParamKeyValue>> dataList = (ArrayList<KV<String, ParamKeyValue>>) value;
                        if (dataList.size() != 0) {
                            LinkedHashMap<String, Object> arrayDataList = new LinkedHashMap<>();
                            List<Object> l = convertArrayToMap(dataList, arrayDataList);
                            result.put(k, l);
                        }
                    } else {
                        result.put(k, value);
                    }
                });
                list.add(result);
            }
        }
        return list;
    }


    private void convertToParamKeyValueList(String prefixKey, KV<String, ParamKeyValue> data, List<ParamKeyValue> list) {
        for (Map.Entry<String, ParamKeyValue> entry : data.entrySet()) {
            String key = entry.getKey();
            ParamKeyValue value = entry.getValue();
            String type = value.getType();
            Object dataValue = value.getValue();
            String comment = value.getComment();
            if (TypeUtil.Type.Object.name().equals(type)) {
                List<ParamKeyValue> childObject = new ArrayList<>();
                convertToParamKeyValueList(prefixKey + key + ".", (KV<String, ParamKeyValue>) dataValue, childObject);
                list.addAll(childObject);
            } else if (TypeUtil.Type.Array.name().equals(type)) {
                ArrayList childList = (ArrayList) value.getValue();
                if (list.size() == 0) {
                    continue;
                }
                convertArrayToParamKeyValueList(prefixKey + key, childList, list);
            } else {
                list.add(new ParamKeyValue(prefixKey + key, dataValue, 2, type, comment));
            }
        }
    }

    private void convertArrayToParamKeyValueList(String key, ArrayList childList, List<ParamKeyValue> list) {
        for (int i = 0; i < childList.size(); i++) {
            String arrayKey = key + "[" + i + "]";
            Object o = childList.get(i);
            if (o instanceof ParamKeyValue) {
                //非对象进入
                ParamKeyValue paramKeyValue = (ParamKeyValue) o;
                paramKeyValue.setKey(key + "[0]");
                list.add(paramKeyValue);
            } else {
                KV<String, ParamKeyValue> kv = (KV<String, ParamKeyValue>) o;
                kv.forEach((k, v) -> {
                    ParamKeyValue value = kv.get(k);
                    Object dataValue = value.getValue();
                    String type = value.getType();
                    String comment = value.getComment();
                    if (TypeUtil.Type.Object.name().equals(type)) {
                        convertToParamKeyValueList(arrayKey + ".", (KV<String, ParamKeyValue>) dataValue, list);
                    } else if (TypeUtil.Type.Array.name().equals(type)) {
                        ArrayList<KV<String, ParamKeyValue>> childArrayList = (ArrayList<KV<String, ParamKeyValue>>) value.getValue();
                        if (childArrayList.size() != 0) {
                            convertArrayToParamKeyValueList(key + "." + arrayKey, childArrayList, list);
                        }
                    } else {
                        list.add(new ParamKeyValue(arrayKey + "." + k, dataValue, 2, type, comment));
                    }
                });
            }
        }
    }


    /**
     * json数据转树节点
     *
     * @param node 节点
     * @param data 数据
     * @return {@link CustomNode }
     * @author Kings
     * @date 2021/06/07
     */
    private CustomNode convertToNode(boolean isRoot, CustomNode node, LinkedHashMap<String, Object> data) {
        Set<String> keys = data.keySet();
        keys.forEach(key -> {
//            node.setKey(key);
            ParamKeyValue value = (ParamKeyValue) data.get(key);
            String type = value.getType();
            String comment = value.getComment();
            if (TypeUtil.Type.Object.name().equals(type)) {
                KV valueJsonObject = (KV) value.getValue();
                if (valueJsonObject == null) {
                    CustomNode nodeObject = new CustomNode(key, null, TypeUtil.Type.Object.name(), comment);
                    node.add(nodeObject);
                    return;
                }
                if (isRoot) {
                    convertToNode(false, node, valueJsonObject);
                } else {
                    CustomNode customNode = new CustomNode(key, null, type, comment);
                    node.add(convertToNode(false, customNode, valueJsonObject));
                }
            } else if (TypeUtil.Type.Array.name().equals(type)) {
                Object valueChild = value.getValue();
                if (valueChild instanceof KV) {
                    CustomNode addNode;
                    if (isRoot) {
                        addNode = new CustomNode("index 0", null, TypeUtil.Type.Object.name());
                    } else {
                        addNode = node;
                    }

                    KV k = (KV) valueChild;
                    Object o = k.entrySet().stream().findFirst().get();
                    if (o instanceof ArrayList) {
                        KV<String, ArrayList<ParamKeyValue>> listKV = k;
                        for (Map.Entry<String, ArrayList<ParamKeyValue>> entry : listKV.entrySet()) {
                            ArrayList<ParamKeyValue> basicTypeValue = entry.getValue();
                            for (ParamKeyValue paramKeyValue : basicTypeValue) {
                                CustomNode customNode = new CustomNode("", paramKeyValue.getValue(), paramKeyValue.getType(), comment);
                                addNode.add(customNode);
                            }
                        }
                    } else {
                        //参数直接传BeanName []
                        for (Map.Entry<String, ParamKeyValue> entry : ((KV<String, ParamKeyValue>) k).entrySet()) {
                            ParamKeyValue paramKeyValue = entry.getValue();
                            String childType = paramKeyValue.getType();
                            String childKey = paramKeyValue.getKey();
                            Object childValue = paramKeyValue.getValue();
                            String childComment = paramKeyValue.getComment();
                            if (TypeUtil.Type.Object.name().equals(childType)) {
                                CustomNode customNode = new CustomNode(childKey, null, childType, childComment);
                                addNode.add(convertToNode(false, customNode, (KV) childValue));
                            } else if (TypeUtil.Type.Array.name().equals(childType)) {
                                convertArrayToNode(false, childKey, childComment, (ArrayList) childValue, addNode);
                            } else {
                                CustomNode customNode = new CustomNode(childKey, childValue, childType, childComment);
                                addNode.add(customNode);
                            }
                        }
                    }
                    if (isRoot) {
                        node.add(addNode);
                    }
                } else {
                    ArrayList list = (ArrayList) valueChild;
                    if (list.size() == 0) {
                        CustomNode nodeArray = new CustomNode(key, null, TypeUtil.Type.Array.name(), comment);
                        node.add(nodeArray);
                        return;
                    }

                    convertArrayToNode(isRoot, key, comment, list, node);
                }
            } else {
                node.add(new CustomNode(key, value.getValue(), type, comment));
            }
        });
        return node;
    }


    /**
     * json数据中list转树节点
     *
     * @param key      关键
     * @param dataList 数据列表
     * @param node     节点
     * @author Kings
     * @date 2021/06/07
     */
    private void convertArrayToNode(boolean isRoot, String key, String comment, ArrayList dataList, CustomNode node) {
        CustomNode addNode;
        if (isRoot) {
            addNode = node;
        } else {
            addNode = new CustomNode(key, null, TypeUtil.Type.Array.name(), comment);
        }
        for (int j = 0; j < dataList.size(); j++) {
            Object o = dataList.get(j);
            if (o instanceof ParamKeyValue) {
                //非对象进入
                ParamKeyValue paramKeyValue = (ParamKeyValue) o;
                CustomNode nodeArrayIndex = new CustomNode("index " + j, paramKeyValue.getValue(), paramKeyValue.getType());
                addNode.add(nodeArrayIndex);
            } else {
                //对象进入
                KV<String, ParamKeyValue> kv = (KV<String, ParamKeyValue>) dataList.get(j);
                CustomNode nodeArrayIndex = new CustomNode("index " + j, null, TypeUtil.Type.Object.name());
                kv.entrySet().forEach(inKv -> {
                    String inKey = inKv.getKey();
                    ParamKeyValue value = kv.get(inKey);
                    String type = value.getType();
                    String commentChild = value.getComment();
                    if (TypeUtil.Type.Object.name().equals(type)) {
                        KV valueKvObject = (KV) value.getValue();
                        if (valueKvObject == null) {
                            return;
                        }
                        CustomNode customNode = new CustomNode(inKey, null, type, commentChild);
                        nodeArrayIndex.add(convertToNode(false, customNode, valueKvObject));
                    } else if (TypeUtil.Type.Array.name().equals(type)) {
                        ArrayList<KV<String, ParamKeyValue>> list = (ArrayList<KV<String, ParamKeyValue>>) value.getValue();
                        if (list.size() == 0) {
                            return;
                        }
                        for (int i = 0; i < list.size(); i++) {
                            convertArrayToNode(false, inKey, commentChild, list, nodeArrayIndex);
                        }
                    } else {
                        nodeArrayIndex.add(new CustomNode(inKey, value.getValue(), type, commentChild));
                    }
                });
                addNode.add(nodeArrayIndex);
            }
        }
        if (!isRoot) {
            node.add(addNode);
        }

    }

    /**
     * 自定义节点 json树节点
     *
     * @author Kings
     * @date 2021/06/07
     * @see DefaultMutableTreeNode
     */
    private class CustomNode extends DefaultMutableTreeNode {
        private String key;
        private Object value;
        private String type;
        private String comment;

        public CustomNode() {
        }

        public CustomNode(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public CustomNode(String key, Object value, String type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }

        public CustomNode(String key, Object value, String type, String comment) {
            this.key = key;
            this.value = value;
            this.type = type;
            this.comment = comment;
        }


        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    /**
     * 创建urlEncoded table
     *
     * @return {@link JBTable }
     * @author Kings
     * @date 2021/06/02
     */
    private JBTable createPathParamKeyValueTable() {
        ColumnInfo<Object, Object>[] columns = getPathColumnInfo();
        if (pathParamsKeyValueList == null) {
            pathParamsKeyValueList = new ArrayList<>();
        }
        ListTableModel<ParamKeyValue> model = new ListTableModel<>(columns, pathParamsKeyValueList);
        JBTable table = new JBTable(model) {

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == 0) {
                    boolean enable = (boolean) getValueAt(row, column);
                    return new DefaultCellEditor(new JCheckBox("", enable));
                }
                if (column == 1) {
                    String type = (String) getValueAt(row, column);
                    return new DefaultCellEditor(getNormalTypeComboBox(type));
                }
                return super.getCellEditor(row, column);
            }

            @Override
            public @NotNull Component prepareRenderer(@NotNull TableCellRenderer renderer, int row, int column) {
                if (column == 0) {
                    ParamKeyValue paramKeyValue = pathParamsKeyValueList.get(row);
                    boolean enabled = paramKeyValue.getEnabled();
                    return new JCheckBox("", enabled);
                } else if (column == 1) {
                    ParamKeyValue paramKeyValue = pathParamsKeyValueList.get(row);
                    String type = paramKeyValue.getType();
                    return getNormalTypeComboBox(type);
                }
                return super.prepareRenderer(renderer, row, column);
            }


            @Override
            public Object getValueAt(int row, int column) {
                if (pathParamsKeyValueList.isEmpty()) {
                    return StringUtils.EMPTY;
                }
                ParamKeyValue keyValue = pathParamsKeyValueList.get(row);
                if (keyValue == null) {
                    return StringUtils.EMPTY;
                }
                if (column == 0) {
                    return keyValue.getEnabled();
                } else if (column == 1) {
                    return keyValue.getType();
                } else if (column == 2) {
                    return keyValue.getKey();
                } else {
                    return keyValue.getValue();
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 0) {
                    boolean changeFlag = false;
                    String value = aValue.toString();
                    ParamKeyValue paramKeyValue = pathParamsKeyValueList.get(row);
                    paramKeyValue.setEnabled(Boolean.parseBoolean(value));
                    if (!paramKeyValue.getValue().equals(value)) {
                        changeFlag = true;
                    }
                    if (changeFlag) {
                        changeUrl();
                    }
                }
                if (column == 1) {
                    ParamKeyValue paramKeyValue = pathParamsKeyValueList.get(row);
                    paramKeyValue.setType(aValue.toString());
                }
                if (column == 2) {
                    boolean changeFlag = false;
                    ParamKeyValue paramKeyValue = pathParamsKeyValueList.get(row);
                    String value = aValue.toString();
                    if (!paramKeyValue.getValue().equals(value)) {
                        changeFlag = true;
                    }
                    paramKeyValue.setKey(aValue.toString());
                    if (changeFlag) {
                        changeUrl();
                    }
                }
                if (column == 3) {
                    boolean changeFlag = false;
                    String value = aValue.toString();
                    ParamKeyValue paramKeyValue = pathParamsKeyValueList.get(row);
                    if (!paramKeyValue.getValue().equals(value)) {
                        changeFlag = true;
                    }
                    paramKeyValue.setValue(value);
                    if (changeFlag) {
                        changeUrl();
                    }
                }

            }
        };
        table.setVisible(true);
        TableColumn checkBoxColumn = table.getColumnModel().getColumn(0);
        checkBoxColumn.setMaxWidth(30);
        table.getColumnModel().getColumn(1).setMinWidth(100);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.setRowHeight(35);
        pathParamsCheckBoxHeader = new CheckBoxHeader(new MyParamCheckItemListener(table));
        return table;
    }

    /**
     * 创建urlEncoded table
     *
     * @return {@link JBTable }
     * @author Kings
     * @date 2021/06/02
     */
    public JBTable createUrlEncodedKeyValueTable() {
        ColumnInfo<Object, Object>[] columns = getPathColumnInfo();
        if (urlEncodedKeyValueList == null) {
            urlEncodedKeyValueList = new ArrayList<>();
        }
        ListTableModel<ParamKeyValue> model = new ListTableModel<>(columns, urlEncodedKeyValueList);
        JBTable table = new JBTable(model) {

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == 0) {
                    boolean enable = (boolean) getValueAt(row, column);
                    return new DefaultCellEditor(new JCheckBox("", enable));
                } else if (column == 1) {
                    String type = (String) getValueAt(row, column);
                    return new DefaultCellEditor(getNormalTypeComboBox(type));
                }
                return super.getCellEditor(row, column);
            }

            @Override
            public @NotNull Component prepareRenderer(@NotNull TableCellRenderer renderer, int row, int column) {
                if (column == 0) {
                    ParamKeyValue paramKeyValue = urlEncodedKeyValueList.get(row);
                    boolean enabled = paramKeyValue.getEnabled();
                    return new JCheckBox("", enabled);
                } else if (column == 1) {
                    ParamKeyValue paramKeyValue = urlEncodedKeyValueList.get(row);
                    String type = paramKeyValue.getType();
                    return getNormalTypeComboBox(type);
                }
//                else if (column == 2) {
//                    ParamKeyValue paramKeyValue = urlEncodedKeyValueList.get(row);
//                    JTextField textField = new JTextField();
//                    textField.setText(getValueAt(row, column).toString());
//                    textField.setToolTipText(paramKeyValue.getComment());
//                    textField.setOpaque(false);
//                    return textField;
//                }
                return super.prepareRenderer(renderer, row, column);
            }

            @Override
            public Object getValueAt(int row, int column) {
                if (urlEncodedKeyValueList.isEmpty()) {
                    return StringUtils.EMPTY;
                }
                ParamKeyValue keyValue = urlEncodedKeyValueList.get(row);
                if (keyValue == null) {
                    return StringUtils.EMPTY;
                }
                if (column == 0) {
                    return keyValue.getEnabled();
                } else if (column == 1) {
                    return keyValue.getType();
                } else if (column == 2) {
                    return keyValue.getKey();
                } else {
                    return keyValue.getValue();
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 0) {
                    ParamKeyValue paramKeyValue = urlEncodedKeyValueList.get(row);
                    paramKeyValue.setEnabled(Boolean.parseBoolean(aValue.toString()));
                } else if (column == 1) {
                    ParamKeyValue paramKeyValue = urlEncodedKeyValueList.get(row);
                    paramKeyValue.setType(aValue.toString());
                } else if (column == 2) {
                    ParamKeyValue paramKeyValue = urlEncodedKeyValueList.get(row);
                    paramKeyValue.setKey(aValue.toString());
                    String value = aValue.toString();
                    if (!paramKeyValue.getValue().equals(value)) {
                        urlEncodedParamChangeFlag.set(true);
                    }
                } else if (column == 3) {
                    ParamKeyValue paramKeyValue = urlEncodedKeyValueList.get(row);
                    if (!paramKeyValue.getValue().equals(aValue)) {
                        urlEncodedParamChangeFlag.set(true);
                    }
                    paramKeyValue.setValue(aValue);
                }
                changeUrlEncodedParamsText();
            }
        };
        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(1).setMinWidth(100);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.setRowHeight(35);
        table.setVisible(true);
        urlEncodedCheckBoxHeader = new CheckBoxHeader(new MyParamCheckItemListener(table));
        return table;
    }

    class FileChooseCellEditor extends AbstractTableCellEditor {
        private TextFieldWithBrowseButton textFieldWithBrowseButton;

        public FileChooseCellEditor(TextFieldWithBrowseButton textFieldWithBrowseButton) {
            this.textFieldWithBrowseButton = textFieldWithBrowseButton;
        }

        @Override
        public Object getCellEditorValue() {
            return textFieldWithBrowseButton;
        }

        @Override
        public Component getTableCellEditorComponent(JTable jTable, Object o, boolean b, int i, int i1) {
            return textFieldWithBrowseButton;
        }
    }

    public JBTable createMultipartKeyValueTable() {
        ColumnInfo<Object, Object>[] columns = getPathColumnInfo();
        if (multipartKeyValueList == null) {
            multipartKeyValueList = new ArrayList<>();
        }
        ListTableModel<ParamKeyValue> model = new ListTableModel<>(columns, multipartKeyValueList);
        JBTable table = new JBTable(model) {

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == 0) {
                    boolean enable = (boolean) getValueAt(row, column);
                    return new DefaultCellEditor(new JCheckBox("", enable));
                }
                if (column == 1) {
                    String type = (String) getValueAt(row, column);
                    return new DefaultCellEditor(getNormalTypeAndFileComboBox(type));
                }
                if (column == 3) {
                    String type = (String) getValueAt(row, 1);
                    String value = (String) getValueAt(row, 2);
                    if (TypeUtil.Type.File.name().equals(type)) {
                        VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFileDescriptor(), myProject, LocalFileSystem.getInstance().findFileByIoFile(new File(value)));
                        String path = virtualFile == null ? value : virtualFile.getCanonicalPath();
                        TextFieldWithBrowseButton textFieldWithBrowseButton = new TextFieldWithBrowseButton(new JTextField(path));
                        return new FileChooseCellEditor(textFieldWithBrowseButton);
                    }
                }
                return super.getCellEditor(row, column);
            }

            @Override
            public @NotNull Component prepareRenderer(@NotNull TableCellRenderer renderer, int row, int column) {
                if (column == 0) {
                    ParamKeyValue paramKeyValue = multipartKeyValueList.get(row);
                    boolean enabled = paramKeyValue.getEnabled();
                    return new JCheckBox("", enabled);
                } else if (column == 1) {
                    ParamKeyValue paramKeyValue = multipartKeyValueList.get(row);
                    String type = paramKeyValue.getType();
                    return getNormalTypeAndFileComboBox(type);
//                }
//                else if (column == 2) {
//                    ParamKeyValue paramKeyValue = multipartKeyValueList.get(row);
//                    JTextField textField = new JTextField();
//                    textField.setText(getValueAt(row, column).toString());
//                    textField.setToolTipText(paramKeyValue.getComment());
//                    textField.setOpaque(false);
//                    return textField;
                } else if (column == 3) {
                    ParamKeyValue paramKeyValue = multipartKeyValueList.get(row);
                    String type = paramKeyValue.getType();
                    if (TypeUtil.Type.File.name().equals(type)) {
                        return new TextFieldWithBrowseButton(new JTextField(paramKeyValue.getValue().toString()));
                    } else {
                        return super.prepareRenderer(renderer, row, column);
                    }

                }
                return super.prepareRenderer(renderer, row, column);
            }

            @Override
            public Object getValueAt(int row, int column) {
                if (multipartKeyValueList.isEmpty()) {
                    return StringUtils.EMPTY;
                }
                ParamKeyValue keyValue = multipartKeyValueList.get(row);
                if (keyValue == null) {
                    return StringUtils.EMPTY;
                }
                if (column == 0) {
                    return keyValue.getEnabled();
                } else if (column == 1) {
                    return keyValue.getType();
                } else if (column == 2) {
                    return keyValue.getKey();
                } else {
                    return keyValue.getValue();
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 0) {
                    ParamKeyValue paramKeyValue = multipartKeyValueList.get(row);
                    paramKeyValue.setEnabled(Boolean.parseBoolean(aValue.toString()));
                } else if (column == 1) {
                    ParamKeyValue paramKeyValue = multipartKeyValueList.get(row);
                    paramKeyValue.setType(aValue.toString());
                } else if (column == 2) {
                    ParamKeyValue paramKeyValue = multipartKeyValueList.get(row);
                    paramKeyValue.setKey(aValue.toString());
                } else if (column == 3) {
                    String value = aValue.toString();
                    ParamKeyValue paramKeyValue = multipartKeyValueList.get(row);
                    if (TypeUtil.Type.File.name().equals(paramKeyValue.getType())) {
                        paramKeyValue.setValue(((TextFieldWithBrowseButton) aValue).getText());
                    } else {
                        paramKeyValue.setValue(value);
                    }

                }
            }
        };
        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(1).setMinWidth(100);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.setRowHeight(35);
        table.setVisible(true);
        multipartCheckBoxHeader = new CheckBoxHeader(new MyParamCheckItemListener(table));
        return table;
    }

    private ColumnInfo<Object, Object>[] getColumns(List<String> titleList) {
        ColumnInfo<Object, Object>[] columns = new ColumnInfo[titleList.size()];
        for (int i = 0; i < titleList.size(); i++) {
            ColumnInfo<Object, Object> envColumn = new ColumnInfo<>(titleList.get(i)) {
                @Override
                public @Nullable Object valueOf(Object o) {
                    return o;
                }
            };

            columns[i] = envColumn;
        }
        return columns;
    }


    private JBTable createHeaderTable() {
        ColumnInfo<Object, Object>[] columns = getColumns(Lists.newArrayList("", "Header Name", "Header Value"));
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        calcHeaderList();
        if (headerParamsKeyValueList == null) {
            headerParamsKeyValueList = new ArrayList<>();
        }
        ListTableModel<DataMapping> model = new ListTableModel<>(columns, headerParamsKeyValueList);
        JBTable table = new JBTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }

            @Override
            public @NotNull Component prepareRenderer(@NotNull TableCellRenderer renderer, int row, int column) {
                if(headerParamsKeyValueList.isEmpty()){
                    return super.prepareRenderer(renderer, row, column);
                }
                if (column == 0) {
                    DataMapping dataMapping = headerParamsKeyValueList.get(row);
                    boolean enabled = dataMapping.getEnabled();
                    return new JCheckBox("", enabled);
                }
                return super.prepareRenderer(renderer, row, column);
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if(headerParamsKeyValueList.isEmpty()){
                    return super.getCellEditor(row, column);
                }
                if (column == 0) {
                    boolean enable = (boolean) getValueAt(row, column);
                    return new DefaultCellEditor(new JCheckBox("", enable));
                }else if(column == 1){
                    return new GeneralTextAutoCompleteEditor(myProject, Constant.AutoCompleteType.Header_Name);
                } else if (column == 2) {
                    return new GeneralTextAutoCompleteEditor(myProject, Constant.AutoCompleteType.Header_value);
                }
                return super.getCellEditor(row, column);
            }

            @Override
            public Object getValueAt(int row, int column) {
                if (headerParamsKeyValueList.isEmpty()) {
                    return StringUtils.EMPTY;
                }
                DataMapping dataMapping = headerParamsKeyValueList.get(row);
                if (dataMapping == null) {
                    return StringUtils.EMPTY;
                }
                if (column == 0) {
                    return dataMapping.getEnabled();
                } else if (column == 1) {
                    return dataMapping.getType();
                } else {
                    return dataMapping.getValue();
                }
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 0) {
                    DataMapping dataMapping = headerParamsKeyValueList.get(row);
                    dataMapping.setEnabled(Boolean.parseBoolean(aValue.toString()));
                } else if (column == 1) {
                    DataMapping dataMapping = headerParamsKeyValueList.get(row);
                    dataMapping.setType(aValue.toString());
                } else if (column == 2) {
                    DataMapping dataMapping = headerParamsKeyValueList.get(row);
                    dataMapping.setValue(aValue.toString());
                }
                saveAndChangeHeader();
                setHeaderTitle();
//                config.setHeaderList(headerParamsKeyValueList);
            }


        };
        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.setRowHeight(23);
        table.setVisible(true);
        return table;
    }




    private JBTable createResponseInfoTable() {
        ColumnInfo<Object, Object>[] columns = getColumns(Lists.newArrayList("Key", "Value"));
        if (responseInfoParamsKeyValueList == null) {
            responseInfoParamsKeyValueList = new ArrayList<>();
        }
        ListTableModel<ParamKeyValue> model = new ListTableModel<>(columns, responseInfoParamsKeyValueList);
        JBTable table = new JBTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                //默认只允许修改value不允许修改key
                return false;
            }

            @Override
            public Object getValueAt(int row, int column) {
                if (responseInfoParamsKeyValueList.isEmpty()) {
                    return StringUtils.EMPTY;
                }
                ParamKeyValue keyValue = responseInfoParamsKeyValueList.get(row);
                if (keyValue == null) {
                    return StringUtils.EMPTY;
                }
                if (column == 0) {
                    return keyValue.getKey();
                } else {
                    return keyValue.getValue();
                }
            }
        };
        table.setRowHeight(35);
        table.setVisible(true);
        table.setCellSelectionEnabled(true);
        return table;
    }

    /**
     * 创建urlParams table
     *
     * @return {@link JBTable }
     * @author Kings
     * @date 2021/06/02
     */
    public JBTable createUrlParamsKeyValueTable() {
        ColumnInfo<Object, Object>[] columns = getPathColumnInfo();
        if (urlParamsKeyValueList == null) {
            urlParamsKeyValueList = new ArrayList<>();
        }
        ListTableModel<ParamKeyValue> model = new ListTableModel<>(columns, urlParamsKeyValueList);
        JBTable table = new JBTable(model) {
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == 0) {
                    boolean enable = (boolean) getValueAt(row, column);
                    return new DefaultCellEditor(new JCheckBox("", enable));
                } else if (column == 1) {
                    String type = (String) getValueAt(row, column);
                    return new DefaultCellEditor(getNormalTypeComboBox(type));
                }
                return super.getCellEditor(row, column);
            }

            @Override
            public @NotNull Component prepareRenderer(@NotNull TableCellRenderer renderer, int row, int column) {
                if (column == 0) {
                    ParamKeyValue paramKeyValue = urlParamsKeyValueList.get(row);
                    boolean enabled = paramKeyValue.getEnabled();
                    return new JCheckBox("", enabled);
                } else if (column == 1) {
                    ParamKeyValue paramKeyValue = urlParamsKeyValueList.get(row);
                    String type = paramKeyValue.getType();
                    return getNormalTypeComboBox(type);
                }
//                else if (column == 2) {
//                    ParamKeyValue paramKeyValue = urlParamsKeyValueList.get(row);
//                    JTextField textField = new JTextField();
//                    textField.setText(getValueAt(row, column).toString());
//                    textField.setToolTipText(paramKeyValue.getComment());
//                    textField.setOpaque(false);
//                    return textField;
//                }
                return super.prepareRenderer(renderer, row, column);
            }

            @Override
            public Object getValueAt(int row, int column) {
                if (urlParamsKeyValueList.isEmpty()) {
                    return StringUtils.EMPTY;
                }
                ParamKeyValue keyValue = urlParamsKeyValueList.get(row);
                if (keyValue == null) {
                    return StringUtils.EMPTY;
                }
                if (column == 0) {
                    return keyValue.getEnabled();
                } else if (column == 1) {
                    return keyValue.getType();
                } else if (column == 2) {
                    return keyValue.getKey();
                } else {
                    return keyValue.getValue();
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 0) {
                    ParamKeyValue paramKeyValue = urlParamsKeyValueList.get(row);
                    paramKeyValue.setEnabled(Boolean.parseBoolean(aValue.toString()));
                }
                if (column == 1) {
                    ParamKeyValue paramKeyValue = urlParamsKeyValueList.get(row);
                    paramKeyValue.setType(aValue.toString());
                }
                if (column == 2) {
                    ParamKeyValue paramKeyValue = urlParamsKeyValueList.get(row);
                    paramKeyValue.setKey(aValue.toString());
                    String value = aValue.toString();
                    if (!paramKeyValue.getValue().equals(value)) {
                        urlParamsChangeFlag.set(true);
                    }
                }
                if (column == 3) {
                    String value = aValue.toString();
                    ParamKeyValue paramKeyValue = urlParamsKeyValueList.get(row);
                    if (!paramKeyValue.getValue().equals(value)) {
                        urlParamsChangeFlag.set(true);
                    }
                    paramKeyValue.setValue(value);
                }
                changeUrlParamsText();
            }

        };
        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(1).setMinWidth(100);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.setRowHeight(35);
        table.setVisible(true);
        urlParamsCheckBoxHeader = new CheckBoxHeader(new MyParamCheckItemListener(table));
        return table;
    }

    private void bindTableOperations(JBTable table) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                    //通过点击位置找到点击为表格中的行
                    int focusedRowIndex = table.rowAtPoint(evt.getPoint());
                    if (focusedRowIndex == -1) {
                        return;
                    }
                    if (table.getSelectedRows().length <= 1) {
                        //将表格所选项设为当前右键点击的行
                        table.setRowSelectionInterval(focusedRowIndex, focusedRowIndex);
                    }
                    setTableButtons();
                    //弹出菜单
                    tablePopupMenu.show(table, evt.getX(), evt.getY());
                }
            }
        });
    }

    private void bindHeaderOperations(JBTable table) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                    //通过点击位置找到点击为表格中的行
                    int focusedRowIndex = table.rowAtPoint(evt.getPoint());
                    if (focusedRowIndex == -1) {
                        return;
                    }
                    if (table.getSelectedRows().length <= 1) {
                        //将表格所选项设为当前右键点击的行
                        table.setRowSelectionInterval(focusedRowIndex, focusedRowIndex);
                    }
                    //弹出菜单
                    setHeaderButtons();
                    headerPopupMenu.show(table, evt.getX(), evt.getY());
                }
            }
        });
    }

    /*****getter setter*****/

    public JComponent getContent() {
        return panel;
    }

    public JPanel getPanel() {
        return panel;
    }


    private CollectionConfiguration.CollectionDetail filterById(String id, CollectionConfiguration.CollectionDetail detail) {
        if (detail.getId().equals(id)) {
            return detail;
        }
        for (CollectionConfiguration.CollectionDetail d : detail.getChildList()) {
            CollectionConfiguration.CollectionDetail filterResult = filterById(id, d);
            if (filterResult != null) {
                return filterResult;
            }
        }
        return null;
    }

    private CollectionConfiguration.CollectionDetail filterClassGroupByName(String name, CollectionConfiguration.CollectionDetail detail) {
        if (detail.getName().equals(name) && detail.getType() == 1) {
            return detail;
        }
        for (CollectionConfiguration.CollectionDetail d : detail.getChildList()) {
            CollectionConfiguration.CollectionDetail filterResult = filterClassGroupByName(name, d);
            if (filterResult != null) {
                return filterResult;
            }
        }
        return null;
    }


    private final class StopPositionAction extends ParentAction {
        public StopPositionAction() {
            super("Stop", "Stop", AllIcons.Actions.Suspend);

        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(futureAtomicReference.get() != null);
        }


        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if (futureAtomicReference.get() != null) {
                futureAtomicReference.get().cancel(true);
                if (futureAtomicReference.get().isCancelled()) {
                    FastRequestToolWindow fastRequestToolWindow = ToolWindowUtil.getFastRequestToolWindow(myProject);
                    if (fastRequestToolWindow != null) {
                        fastRequestToolWindow.sendButtonFlag = true;
                        fastRequestToolWindow.requestProgressBar.setVisible(false);
                        futureAtomicReference.set(null);
                    }
                }
            }
        }
    }



    private final class SaveRequestAction extends ParentAction {
        public SaveRequestAction() {
            super(MyResourceBundleUtil.getKey("SaveRequest"), MyResourceBundleUtil.getKey("SaveRequest"), PluginIcons.ICON_SAVE);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            stopCellEditing();
            if (getActiveDomain().isBlank()) {
                Messages.showMessageDialog(MyResourceBundleUtil.getKey("msg_currentDomain_null"), "Error", Messages.getInformationIcon());
                return;
            }
            if (urlTextField.getText().isBlank()) {
                Messages.showMessageDialog(MyResourceBundleUtil.getKey("msg_UrlNull"), "Error", Messages.getInformationIcon());
                return;
            }
            CollectionConfiguration.CollectionDetail saved = getFromSaved();
            String name = "";
            if (saved != null){
                name = saved.getName();
            }
            EnvAddView envAddView = new EnvAddView(MyResourceBundleUtil.getKey("InputRequestName"), MyResourceBundleUtil.getKey("PleaseModify"));
            envAddView.setText(name);
            if (envAddView.showAndGet()) {
                name = envAddView.getText();
            }else {
                return;
            }
            //保存请求
            saveTreeRequest(name);

            //send message to change param
            MessageBus messageBus = myProject.getMessageBus();
            messageBus.connect();
            ConfigChangeNotifier configChangeNotifier = messageBus.syncPublisher(ConfigChangeNotifier.ADD_REQUEST_TOPIC);
            configChangeNotifier.configChanged(true, myProject.getName());
            //兼容性处理code
            NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification("Success", MessageType.INFO).notify(myProject);
            // 2020.3 before
            //new NotificationGroup("quickRequestWindowNotificationGroup", NotificationDisplayType.TOOL_WINDOW, true).createNotification("Success", NotificationType.INFORMATION).notify(myProject);
        }
    }

    private CollectionConfiguration.CollectionDetail getFromSaved(){
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        CollectionConfiguration collectionConfiguration = FastRequestCollectionComponent.getInstance(myProject).getState();
        assert collectionConfiguration != null;
        ParamGroup paramGroup = config.getParamGroup();
        String id = "id_" + paramGroup.getClassName() + "." + paramGroup.getMethod();
        return filterById(id, collectionConfiguration.getDetail());
    }

    private void saveTreeRequest(String name) {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        ParamGroup paramGroup = config.getParamGroup();
        CollectionConfiguration collectionConfiguration = FastRequestCollectionComponent.getInstance(myProject).getState();
        assert collectionConfiguration != null;
        if(StringUtils.isBlank(paramGroup.getClassName()) || StringUtils.isBlank(paramGroup.getMethod())){
            paramGroup.setClassName("temp");
            paramGroup.setMethod(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
            paramGroup.setOriginUrl(urlTextField.getText());
            paramGroup.setType(-1);
        }

        CollectionConfiguration.CollectionDetail collectionDetail;
        String id = "id_" + paramGroup.getClassName() + "." + paramGroup.getMethod();
        collectionDetail = filterById(id, collectionConfiguration.getDetail());
        boolean insertFlag = collectionDetail == null;
        if (insertFlag) {
            //插入
            collectionDetail = new CollectionConfiguration.CollectionDetail();
            String mid = "id_" + paramGroup.getClassName() + "." + paramGroup.getMethod();
            collectionDetail.setId(mid);
        }
        ParamGroupCollection paramGroupCollection = new ParamGroupCollection();
        collectionDetail.setEnableEnv(getActiveEnv());
        collectionDetail.setEnableProject(getActiveProject());
        collectionDetail.setDomain(getActiveDomain());
        collectionDetail.setType(2);
        collectionDetail.setName(name);
        if(StringUtils.isBlank(paramGroup.getMethodDescription())){
            collectionDetail.setName(name);
            config.setDefaultGroupCount(config.getDefaultGroupCount() + 1);
        }else {
            collectionDetail.setName(name);
        }
        paramGroupCollection.setOriginUrl(paramGroup.getOriginUrl());
        paramGroupCollection.setUrl(urlTextField.getText());
        paramGroupCollection.setMethodType((String) methodTypeComboBox.getSelectedItem());
        paramGroupCollection.setMethodDescription(StringUtils.isBlank(paramGroup.getMethodDescription()) ? paramGroup.getMethod() + "_req" : paramGroup.getMethodDescription());
        paramGroupCollection.setClassName(paramGroup.getClassName());
        paramGroupCollection.setMethod(paramGroup.getMethod());
        paramGroupCollection.setType(paramGroup.getType());
        paramGroupCollection.setPathParamsKeyValueListJson(JSON.toJSONString(pathParamsKeyValueList));
        paramGroupCollection.setUrlParamsKeyValueListJson(JSON.toJSONString(urlParamsKeyValueList));
        paramGroupCollection.setUrlParamsKeyValueListText(urlParamsTextArea.getText());
        paramGroupCollection.setUrlEncodedKeyValueListJson(JSON.toJSONString(urlEncodedKeyValueList));
        paramGroupCollection.setUrlEncodedKeyValueListText(urlEncodedTextArea.getText());
        paramGroupCollection.setBodyKeyValueListJson(((LanguageTextField) rowParamsTextArea).getText());
        paramGroupCollection.setMultipartKeyValueListJson(JSON.toJSONString(multipartKeyValueList));
        collectionDetail.setParamGroup(paramGroupCollection);
        collectionDetail.setHeaderList(headerParamsKeyValueList);
        String apiClassName = "temp";
        if(!Objects.equals(paramGroup.getClassName(), "temp")){
            apiClassName = paramGroup.getClassName().substring(paramGroup.getClassName().lastIndexOf(".") + 1);
        }
        CollectionConfiguration.CollectionDetail classNameGroup = filterClassGroupByName(apiClassName, collectionConfiguration.getDetail());
        if (insertFlag) {
            String module = paramGroup.getModule();
            CollectionConfiguration.CollectionDetail root = collectionConfiguration.getDetail();
            List<CollectionConfiguration.CollectionDetail> rootChildren = root.getChildList();
            CollectionConfiguration.CollectionDetail defaultGroup;
            if (CollectionUtils.isEmpty(rootChildren)) {
                defaultGroup = new CollectionConfiguration.CollectionDetail();
                defaultGroup.setType(1);
                defaultGroup.setId("11");
                defaultGroup.setName("Default Group");
                rootChildren.add(defaultGroup);
            } else {
                defaultGroup = rootChildren.get(0);
            }
            CollectionConfiguration.CollectionDetail group;
            if (module == null || module.isEmpty()) {
                group = defaultGroup;
            } else {
                group = rootChildren.stream().filter(q -> module.equals(q.getName())).findFirst().orElse(null);
                if (group == null) {
                    group = new CollectionConfiguration.CollectionDetail();
                    group.setId(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
                    group.setName(module);
                    group.setType(1);
                    rootChildren.add(group);
                }
            }
            //classGroup
            if (classNameGroup == null) {
                CollectionConfiguration.CollectionDetail groupDetail = new CollectionConfiguration.CollectionDetail();
                groupDetail.setId(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
                groupDetail.setName(apiClassName);
                groupDetail.setType(1);
                groupDetail.setChildList(Lists.newArrayList(collectionDetail));
                List<CollectionConfiguration.CollectionDetail> childList = group.getChildList();
                childList.add(groupDetail);
                group.setChildList(childList);
            } else {
                classNameGroup.getChildList().add(collectionDetail);
            }
        }
    }

    private final class CopyCurlAction extends ParentAction {
        public CopyCurlAction() {
            super("Copy as CURL", "Copy as CURL", PluginIcons.ICON_CURL);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            String curlData = getCurlDataAndCopy();
            //兼容性处理code
            if (StringUtils.isNoneBlank(curlData)) {
                NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification("Copy success", MessageType.INFO).notify(myProject);
            }
            // 2020.3 before
            //new NotificationGroup("quickRequestWindowNotificationGroup", NotificationDisplayType.TOOL_WINDOW, true).createNotification("Success", NotificationType.INFORMATION).notify(myProject);
        }
    }

    private final class CleanAction extends ParentAction {
        public CleanAction() {
            super("Clear[Help you create a new request instead of modifying a saved or historical one.]", "Clear[Help you create a new request instead of modifying a saved or historical one.]", PluginIcons.ICON_CLEAR);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            FastRequestConfiguration state = FastRequestComponent.getInstance().getState();
            state.getParamGroup().clear();
            urlTextField.setText("");
        }
    }

    private static final class ShareAction extends ParentAction {
        public ShareAction() {
            super(MyResourceBundleUtil.getKey("StarDocument"), MyResourceBundleUtil.getKey("StarDocument"), PluginIcons.ICON_DOC);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {

        }
    }

    private static final class CoffeeMeAction extends ParentAction {
        public CoffeeMeAction() {
            super(MyResourceBundleUtil.getKey("CoffeeMe"), MyResourceBundleUtil.getKey("CoffeeMe"), AllIcons.Ide.Gift);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            SupportView supportView = new SupportView();
            supportView.show();
        }
    }

    private final class CompleteUrlAction extends ParentAction {
        public CompleteUrlAction() {
            super("Complete", "Complete", PluginIcons.ICON_COMPLETE);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            String activeDomain = getActiveDomain();
            if (!urlTextField.getText().startsWith(activeDomain)) {
                urlCompleteChangeFlag.set(true);
                if (Objects.equals(methodTypeComboBox.getSelectedItem(), "DUBBO")) {
                    if (!UrlUtil.isDubboURL(urlTextField.getText())) {
                        urlTextField.setText(activeDomain + urlTextField.getText());
                    }
                } else {
                    if (!UrlUtil.isHttpURL(urlTextField.getText())) {
                        urlTextField.setText(activeDomain + urlTextField.getText());
                    }
                }
            } else {
                urlCompleteChangeFlag.set(false);
                urlTextField.setText(urlTextField.getText().replace(activeDomain, ""));
            }
        }
    }

    private class GotoFile extends ParentAction {
        private File file;

        public GotoFile(File file) {
            super(file.getName());
            this.file = file;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            RevealFileAction.openFile(file);
        }
    }

    private void calcHeaderList() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        List<HeaderGroup> headerGroupList = config.getHeaderGroupList();
        if (CollectionUtils.isEmpty(headerGroupList)) {
            headerParamsKeyValueList  = new ArrayList<>();
            return;
        }
        String enableEnv = getActiveEnv();
        String enableProject = getActiveProject();
        if (StringUtils.isBlank(enableEnv) || StringUtils.isBlank(enableProject)) {
            headerParamsKeyValueList  = new ArrayList<>();
            return;
        }
        HeaderGroup headerGroup = headerGroupList.stream().filter(q -> enableProject.equals(q.getProjectName())).findFirst().orElse(null);
        if (headerGroup == null) {
            headerParamsKeyValueList  = new ArrayList<>();
            return;
        }
        Map<String, LinkedHashMap<String, String>> envMap = headerGroup.getEnvMap();
        if (envMap == null || envMap.isEmpty()) {
            headerParamsKeyValueList  = new ArrayList<>();
            return;
        }
        List<DataMapping> headerList = new ArrayList<>();
        LinkedHashMap<String, String> headerKeyValues = envMap.get(enableEnv);
        if (headerKeyValues == null || headerKeyValues.isEmpty()) {
            headerParamsKeyValueList  = new ArrayList<>();
            return;
        }
        for (Map.Entry<String, String> entry : headerKeyValues.entrySet()) {
            headerList.add(new DataMapping(entry.getKey(), entry.getValue(), true));
        }
        if (CollectionUtils.isEmpty(headerParamsKeyValueList)){
            headerParamsKeyValueList = headerList;
        }
        setHeaderTitle();
    }

    private void setHeaderTitle(){
        if(tabbedPane != null && !headerParamsKeyValueList.isEmpty()){
            long headCount = headerParamsKeyValueList.stream().filter(x -> x.getEnabled() != null && x.getEnabled() && StringUtils.isNotBlank(x.getType()) && StringUtils.isNotBlank(x.getValue())).count();
            if(headCount == 0){
                tabbedPane.setTitleAt(0, "Headers");
            }else {
                tabbedPane.setTitleAt(0, "Headers(" + headCount + ")");
            }
        }
    }

    private void switchHeaderParam() {
        calcHeaderList();
        refreshHeader();
    }

    private void refreshHeader() {
        headerTable.setModel(new ListTableModel<>(getColumns(Lists.newArrayList("", "Header Name", "Header Value")), headerParamsKeyValueList));
        headerTable.getColumnModel().getColumn(0).setMaxWidth(30);
    }

    private String getActiveEnv() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;

        FastRequestCurrentProjectConfiguration projectConfig = FastRequestCurrentProjectConfigComponent.getInstance(myProject).getState();
        assert projectConfig != null;

        String projectEnableEnv = projectConfig.getEnableEnv();
        String globalEnableEnv = config.getEnableEnv();
        return StringUtils.isNoneBlank(projectEnableEnv) ? projectEnableEnv : globalEnableEnv;
    }

    private String getActiveDomain() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;

        FastRequestCurrentProjectConfiguration projectConfig = FastRequestCurrentProjectConfigComponent.getInstance(myProject).getState();
        assert projectConfig != null;

        String projectEnableDomain = projectConfig.getDomain();
        String globalEnableDomain = config.getDomain();
        return StringUtils.isNoneBlank(projectEnableDomain) ? projectEnableDomain : globalEnableDomain;
    }

    private String getActiveProject() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;

        FastRequestCurrentProjectConfiguration projectConfig = FastRequestCurrentProjectConfigComponent.getInstance(myProject).getState();
        assert projectConfig != null;

        String projectEnableProject = projectConfig.getEnableProject();
        String globalEnableProject = config.getEnableProject();
        return StringUtils.isNoneBlank(projectEnableProject) ? projectEnableProject : globalEnableProject;
    }

    private void saveAndChangeHeader() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        String enableEnv = getActiveEnv();
        String enableProject = getActiveProject();
        if (StringUtils.isBlank(enableEnv) || StringUtils.isBlank(enableProject)) {
            return;
        }

        List<HeaderGroup> headerGroupList = config.getHeaderGroupList();
        HeaderGroup headerGroup;
        LinkedHashMap<String, String> keyValueMap = headerParamsKeyValueList.stream().filter(DataMapping::getEnabled)
                .collect(Collectors.toMap(DataMapping::getType, DataMapping::getValue, (oldOne, newOne) -> newOne, LinkedHashMap::new));
        if (headerGroupList == null || headerGroupList.isEmpty()) {
            headerGroupList = new ArrayList<>();
            LinkedHashMap<String, LinkedHashMap<String, String>> envMap = Maps.newLinkedHashMap();
            envMap.put(enableEnv, keyValueMap);
            headerGroupList.add(new HeaderGroup(enableProject, envMap));
            config.setHeaderGroupList(headerGroupList);
            return;
        }
        if ((headerGroup = headerGroupList.stream().filter(q -> enableProject.equals(q.getProjectName())).findFirst().orElse(null)) == null) {
            LinkedHashMap<String, LinkedHashMap<String, String>> envMap = Maps.newLinkedHashMap();
            envMap.put(enableEnv, keyValueMap);
            headerGroupList.add(new HeaderGroup(enableProject, envMap));
            config.setHeaderGroupList(headerGroupList);
            return;
        }
        Map<String, LinkedHashMap<String, String>> envMap = headerGroup.getEnvMap();
        if (envMap == null || envMap.isEmpty()) {
            envMap = new LinkedHashMap<>();
            envMap.put(enableEnv, keyValueMap);
            headerGroup.setEnvMap(envMap);
            return;
        }
        envMap.put(enableEnv, keyValueMap);
        headerGroup.setEnvMap(envMap);
    }

    public List<DataMapping> getHeaderParamsKeyValueList() {
        return headerParamsKeyValueList;
    }


    public void setBodyFormat(BodyContentType bodyContentType){
        MyLanguageTextField myLanguageTextField = (MyLanguageTextField) this.rowParamsTextArea;
        Language language;
        FileType fileType;
        switch (bodyContentType){
            case JSON:
                language = JsonLanguage.INSTANCE;
                fileType = JsonFileType.INSTANCE;
                break;
            case XML:
                language = XMLLanguage.INSTANCE;
                fileType = XmlFileType.INSTANCE;
                break;
            case HTML:
                language = HTMLLanguage.INSTANCE;
                fileType = HtmlFileType.INSTANCE;
                break;
            default:
                language = PlainTextLanguage.INSTANCE;
                fileType = PlainTextFileType.INSTANCE;
                break;
        }
        String text = myLanguageTextField.getText();
        myLanguageTextField.updateFileLanguage(fileType, text);
        myLanguageTextField.setLanguage(language);


    }
}
