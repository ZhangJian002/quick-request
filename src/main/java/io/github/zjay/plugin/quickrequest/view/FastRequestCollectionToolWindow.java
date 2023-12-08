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
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.ide.ExporterToTextFile;
import com.intellij.ide.HelpTooltip;
import com.intellij.ide.plugins.newui.ListPluginComponent;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.*;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.content.Content;
import com.intellij.ui.dualView.TreeTableView;
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.BooleanFunction;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.StatusText;
import io.github.zjay.plugin.quickrequest.config.*;
import io.github.zjay.plugin.quickrequest.deprecated.AddAnActionFunction;
import io.github.zjay.plugin.quickrequest.view.ui.MethodFontTableCellRenderer;
import quickRequest.icons.PluginIcons;
import io.github.zjay.plugin.quickrequest.config.configurable.ConfigChangeNotifier;
import io.github.zjay.plugin.quickrequest.idea.ExportToFileUtil;
import io.github.zjay.plugin.quickrequest.util.*;
import io.github.zjay.plugin.quickrequest.view.component.CollectionNodeSelection;
import io.github.zjay.plugin.quickrequest.view.model.CollectionCustomNode;
import io.github.zjay.plugin.quickrequest.model.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FastRequestCollectionToolWindow extends SimpleToolWindowPanel {

    private Project myProject;
    private JPanel panel;
    private JPanel collectionPanel;
    private JLabel helpLabel;
    private JPanel searchPanel;
    private JTabbedPane apiTab;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel searchPanel2;
    private JPanel collectionPanel2;
    private TreeTableView collectionTable;

    private JBTable collectionTable2;

    private List<HistoryTableData> historyTableDataList;

    private CollectionConfiguration.CollectionDetail rootDetail;

    private DefaultActionGroup myInstalledSearchGroup;

    private DefaultActionGroup myInstalledSearchGroup2;
    private Consumer<SearchOptionAction> mySearchCallback;

    private Consumer<SearchOption2Action> mySearchCallback2;
    private SearchTextField jbSearchPanelText;

    private SearchTextField jbSearchPanelText2;
    private Tree tree;


    public FastRequestCollectionToolWindow(Project project, ToolWindow toolWindow) {
        super(true, false);
        this.setContent(panel);
        this.myProject = project;

        helpLabel.setIcon(PluginIcons.ICON_CONTEXT_HELP);
        new HelpTooltip().setDescription(MyResourceBundleUtil.getKey("CollectionSearchHelp")).installOn(helpLabel);

        refresh();
        refresh2();
        apiTab.setIconAt(0, PluginIcons.ICON_HISTORY);
        apiTab.setIconAt(1, PluginIcons.ICON_SAVE);

        myInstalledSearchGroup = new DefaultActionGroup();
        for (SearchTypeEnum option : SearchTypeEnum.values()) {
            if (option.name().startsWith("separator")) {
                myInstalledSearchGroup.addSeparator("  " + option.name().split("_")[1]);
            } else {
                myInstalledSearchGroup.add(new SearchOptionAction(option));
            }
        }

        myInstalledSearchGroup2 = new DefaultActionGroup();
        for (SearchTypeEnum option : SearchTypeEnum.values()) {
            if(option == SearchTypeEnum.name || option == SearchTypeEnum.url || option == SearchTypeEnum.separator_Method){
                continue;
            }
            myInstalledSearchGroup2.add(new SearchOption2Action(option));
        }

        mySearchCallback = updateAction -> {
            String query = jbSearchPanelText.getText();
            String rule = "";
            if (updateAction.myState) {
                rule = updateAction.getQuery();
            } else {
                query = query.replace(updateAction.getQuery(), "");
            }
            jbSearchPanelText.setText(rule + query);
            filterRequest();
        };
        mySearchCallback2 = updateAction -> {
            String query = jbSearchPanelText2.getText();
            String rule = "";
            if (updateAction.myState) {
                rule = updateAction.getQuery();
            } else {
                query = query.replace(updateAction.getQuery(), "");
            }
            jbSearchPanelText2.setText(rule + query);
            filterRequest2();
        };
    }

    private void createUIComponents() {
        renderingCollectionTablePanel();
        renderingCollectionTablePanel2();
        searchPanel = new SearchTextField(true);
        searchPanel.setFocusable(false);
        jbSearchPanelText = (SearchTextField) this.searchPanel;
        JBTextField searchTextField = jbSearchPanelText.getTextEditor();
        searchTextField.putClientProperty("StatusVisibleFunction", (BooleanFunction<JBTextField>) field -> field.getText().isEmpty());
        StatusText emptyText = searchTextField.getEmptyText();
        emptyText.appendText("Search by name or url ->", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, ListPluginComponent.GRAY_COLOR));
        searchTextField.putClientProperty("search.extension", ExtendableTextComponent.Extension
                .create(AllIcons.Actions.More, AllIcons.Actions.More, "Search options",
                        () -> showRightBottomPopup(searchTextField, "By", myInstalledSearchGroup)));
        searchTextField.putClientProperty("JTextField.variant", null);
        searchTextField.putClientProperty("JTextField.variant", "search");
        searchTextField.getDocument().addDocumentListener(new DelayedDocumentListener(1));
        setSearchPanel2();
    }

    private void setSearchPanel2() {
        searchPanel2 = new SearchTextField(true);
        searchPanel2.setFocusable(false);
        jbSearchPanelText2 = (SearchTextField) this.searchPanel2;
        JBTextField searchTextField2 = jbSearchPanelText2.getTextEditor();
        searchTextField2.putClientProperty("StatusVisibleFunction", (BooleanFunction<JBTextField>) field -> field.getText().isEmpty());
        StatusText emptyText2 = searchTextField2.getEmptyText();
        emptyText2.appendText("Search by url", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, ListPluginComponent.GRAY_COLOR));
//        searchTextField2.putClientProperty("search.extension", ExtendableTextComponent.Extension
//                .create(AllIcons.Actions.More, AllIcons.Actions.More, "Search options",
//                        () -> showRightBottomPopup(searchTextField2, "By", myInstalledSearchGroup2)));
        searchTextField2.putClientProperty("JTextField.variant", null);
        searchTextField2.putClientProperty("JTextField.variant", "search");
        searchTextField2.getDocument().addDocumentListener(new DelayedDocumentListener(2));
    }

    private class DelayedDocumentListener implements DocumentListener {

        private final Timer timer;

        public DelayedDocumentListener(Integer type) {
            if(type == 1){
                timer = new Timer(10, e -> filterRequest());
            }else {
                timer = new Timer(10, e -> filterRequest2());
            }
            timer.setRepeats(false);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            timer.restart();
        }


    }

    private static class GroupByActionGroup extends DefaultActionGroup implements CheckedActionGroup {
    }

    private static void showRightBottomPopup(@NotNull Component component, @NotNull @Nls String title, @NotNull ActionGroup group) {
        DefaultActionGroup actions = new GroupByActionGroup();
        actions.addSeparator(title);
        actions.addAll(group);

        DataContext context = DataManager.getInstance().getDataContext(component);

        JBPopup popup = new PopupFactoryImpl.ActionGroupPopup(null, actions, context, false, false, false, true, null, -1, null, null);
        popup.addListener(new JBPopupListener() {
            @Override
            public void beforeShown(@NotNull LightweightWindowEvent event) {
                Point location = component.getLocationOnScreen();
                Dimension size = popup.getSize();
                popup.setLocation(new Point(location.x + component.getWidth() - size.width, location.y + component.getHeight()));
            }
        });
        popup.show(component);
    }

    public void refresh() {
        CollectionConfiguration config = FastRequestCollectionComponent.getInstance(myProject).getState();
        assert config != null;
        rootDetail = config.getDetail();
        CollectionCustomNode root = convertToNode(rootDetail);
        ((DefaultTreeModel) collectionTable.getTableModel()).setRoot(root);
        tree = collectionTable.getTree();
        SwingUtil.expandAll(tree, new TreePath(root), true);

        TableColumnModel columnModel = collectionTable.getColumnModel();

        columnModel.getColumn(2).setMaxWidth(80);
        columnModel.getColumn(2).setPreferredWidth(80);
    }

    public void refresh2() {
        HistoryTable historyTable = FastRequestHistoryCollectionComponent.getInstance(myProject).getState();
        assert historyTable != null;
        List<HistoryTableData> list = historyTable.getList();
        historyTableDataList = list;
        ListTableModel model = (ListTableModel)collectionTable2.getModel();
        model.setItems(list);
    }

    private void filterRequest(String query, String rule) {
        CollectionConfiguration config = FastRequestCollectionComponent.getInstance(myProject).getState();
        assert config != null;
        CollectionConfiguration.CollectionDetail detail = config.getDetail();
        CollectionCustomNode root = (CollectionCustomNode) collectionTable.getTableModel().getRoot();


        if (StringUtils.isBlank(query)) {
            root = convertToNode(rootDetail);
            ((DefaultTreeModel) collectionTable.getTableModel()).setRoot(root);
        } else {
            CollectionCustomNode node = new CollectionCustomNode("0", "Root", 1);
            convertToNode(node, rootDetail.getChildList());
            filterNode(node, query, rule);
            ((DefaultTreeModel) collectionTable.getTableModel()).setRoot(node);
            SwingUtil.expandAll(collectionTable.getTree(), new TreePath(node), true);
        }
        SwingUtil.expandAll(collectionTable.getTree(), new TreePath(root), true);
    }

    private static Map<String, String> getQuery(String search) {
        String[] split = search.split("\\|");
        StringBuilder rule = new StringBuilder();
        String query = "";
        for (String s : split) {
            s = s.trim();
            if (SearchTypeEnum.fromValue(s) != null && search.indexOf("|", search.indexOf(s) + 1) != -1) {
                rule.append(s).append(",");
            } else {
                query = s;
            }
        }
        return ImmutableMap.<String, String>builder().put("query", query).put("rule", rule.toString()).build();
    }

    private void filterRequest() {
        CollectionConfiguration config = FastRequestCollectionComponent.getInstance(myProject).getState();
        assert config != null;
        String search = ((SearchTextField) searchPanel).getText();
        CollectionCustomNode root = (CollectionCustomNode) collectionTable.getTableModel().getRoot();
        if (StringUtils.isBlank(search)) {
            root = convertToNode(rootDetail);
            checkRule("");
            ((DefaultTreeModel) collectionTable.getTableModel()).setRoot(root);
        } else {
            Map<String, String> queryMap = getQuery(search);
            String query = queryMap.get("query");
            String rule = queryMap.get("rule");
            //选择或去除rule
            checkRule(rule);
            CollectionCustomNode node = new CollectionCustomNode("0", "Root", 1);
            convertToNode(node, rootDetail.getChildList());
            filterNode(node, query, rule);
            ((DefaultTreeModel) collectionTable.getTableModel()).setRoot(node);
            SwingUtil.expandAll(collectionTable.getTree(), new TreePath(node), true);
        }
        SwingUtil.expandAll(collectionTable.getTree(), new TreePath(root), true);
    }

    private void filterRequest2() {
        String search = ((SearchTextField) searchPanel2).getText();
        TableRowSorter<TableModel> rowSorter = (TableRowSorter)collectionTable2.getRowSorter();
        rowSorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                int rowIndex = entry.getIdentifier();
                HistoryTableData historyTableData = (HistoryTableData) entry.getModel().getValueAt(rowIndex, 0);
                if(StringUtils.isNotBlank(search) && !historyTableData.getUrl().toLowerCase().contains(search.toLowerCase())){
                    return false;
                }
                return true;
            }
        });
        collectionTable2.repaint();
    }

    private class HighlightCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // 调用父类的渲染方法，以保留原有的样式
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            // 根据匹配条件设置高亮样式
            String searchKeyword = ((SearchTextField) searchPanel2).getText(); // 替换成你的匹配关键字
            String cellValue = String.valueOf(value);
            JLabel jLabel = (JLabel) component;
            int width = table.getColumnModel().getColumn(column).getWidth();
            String realValue = cellValue;
            if(cellValue.length() * 7.5 > width){
                realValue = realValue.substring(0, (int)(width/7.5)) + "...";
            }
            jLabel.setToolTipText(cellValue);
            if(cellValue.toLowerCase().contains(searchKeyword.toLowerCase())){
                jLabel.setText("<html>" + realValue.replaceAll("(?i)" + searchKeyword, "<span style='color: #ffffff; background-color: #007acc;'>$0</span>")  + "</html>");
            }
            return component;
        }
    }

    private void checkRule(String rule) {
        AnAction[] children = myInstalledSearchGroup.getChildren(null);
        for (AnAction anAction : children) {
            if (anAction instanceof Separator) {
                continue;
            }
            SearchOptionAction child = (SearchOptionAction) anAction;
            if (rule.contains(child.myOption.name())) {
                child.myState = true;
            } else {
                child.myState = false;
            }
        }
    }

    private void checkRule2(String rule) {
        AnAction[] children = myInstalledSearchGroup2.getChildren(null);
        for (AnAction anAction : children) {
            if (anAction instanceof Separator) {
                continue;
            }
            SearchOption2Action child = (SearchOption2Action) anAction;
            if (rule.contains(child.myOption.name())) {
                child.myState = true;
            } else {
                child.myState = false;
            }
        }
    }

    private boolean filterNode(CollectionCustomNode node, String search, String rule) {
        if (node.isRoot()) {
            ArrayList<CollectionCustomNode> nodeList = (ArrayList<CollectionCustomNode>) IteratorUtils.toList(node.children().asIterator());
            for (CollectionCustomNode n : nodeList) {
                filterNode(n, search, rule);
            }
        } else {
            if (node.getChildCount() == 0) {
                boolean methodTypeSearchFlag = rule.contains(SearchTypeEnum.get.name()) || rule.contains(SearchTypeEnum.post.name()) ||
                        rule.contains(SearchTypeEnum.put.name()) || rule.contains(SearchTypeEnum.delete.name()) || rule.contains(SearchTypeEnum.patch.name());

                String targetText = "";

                if (rule.contains(SearchTypeEnum.name.name())) {
                    String name = node.getName();
                    targetText += name == null ? "" : name;
                }
                if (rule.contains(SearchTypeEnum.url.name())) {
                    String url = node.getUrl();
                    targetText += url == null ? "" : url;
                }
                if (methodTypeSearchFlag) {
                    String methodType = node.getDetail().getParamGroup().getMethodType();
                    if (methodType == null || !rule.contains(methodType.toLowerCase())) {
                        node.removeFromParent();
                        return true;
                    }
                }
                if (rule.isBlank() || (targetText.isBlank() && methodTypeSearchFlag)) {
                    targetText = node.getSearchText();
                }
                if (!targetText.toLowerCase().contains(search.toLowerCase())) {
                    node.removeFromParent();
                    return true;
                } else {
                    return false;
                }

            } else {
                ArrayList<CollectionCustomNode> nodeList = (ArrayList<CollectionCustomNode>) IteratorUtils.toList(node.children().asIterator());
                boolean removeGroup = true;
                for (CollectionCustomNode n : nodeList) {
                    removeGroup &= filterNode(n, search, rule);
                }
                if (removeGroup) {
                    node.removeFromParent();
                }
            }
        }
        return false;
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

    private CollectionConfiguration.CollectionDetail filterById(String id, List<CollectionConfiguration.CollectionDetail> collectionDetailList) {
        for (CollectionConfiguration.CollectionDetail detail : collectionDetailList) {
            if (detail.getId().equals(id)) {
                return detail;
            }
            List<CollectionConfiguration.CollectionDetail> childList = detail.getChildList();
            if (!childList.isEmpty()) {
                CollectionConfiguration.CollectionDetail detail1 = filterById(id, childList);
                if (detail1 != null) {
                    return detail1;
                }
            }
        }
        return null;
    }

    private void renderingCollectionTablePanel() {
        collectionTable = createCollectionTable();
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(collectionTable);
        toolbarDecorator.setMoveDownAction(null);
        toolbarDecorator.setMoveUpAction(null);
        toolbarDecorator.setRemoveActionUpdater(e -> {
            int selectedRow = collectionTable.getSelectedRow();
            CollectionCustomNode root = (CollectionCustomNode) ((ListTreeTableModelOnColumns) collectionTable.getTableModel()).getRowValue(selectedRow);
            return !"0".equals(root.getId()) && !"1".equals(root.getId());
        });
        toolbarDecorator.setAddActionName("Add Group").setAddAction(e -> {
            int selectedRow = collectionTable.getSelectedRow();
            if(selectedRow <= 0){
                return;
            }
            String id = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            CollectionCustomNode addNode = null;
            CollectionConfiguration.CollectionDetail detail = null;
            CollectionCustomNode node = (CollectionCustomNode) ((ListTreeTableModelOnColumns) collectionTable.getTableModel()).getRowValue(selectedRow);
            if (node.getType() == 1) {
                String idGroup = node.getId();
                CollectionConfiguration.CollectionDetail detailGroup = filterById(idGroup, rootDetail);
                if (detailGroup != null) {
                    List<CollectionConfiguration.CollectionDetail> childList = detailGroup.getChildList();
                    long count = childList.stream().filter(q -> q.getType() == 1).count();
                    addNode = new CollectionCustomNode(id, "Group " + (count + 1), 1);
                    detail = new CollectionConfiguration.CollectionDetail(id, "Group " + (count + 1), 1);
                    childList.add(detail);
                    detailGroup.setChildList(childList);
                    node.insert(addNode, 0);
                }
            } else {

                CollectionCustomNode parent = (CollectionCustomNode) node.getParent();
                String idParent = parent.getId();
                CollectionConfiguration.CollectionDetail detailGroup = filterById(idParent, rootDetail);
                if (detailGroup != null) {
                    List<CollectionConfiguration.CollectionDetail> childList = detailGroup.getChildList();
                    long count = childList.stream().filter(q -> q.getType() == 1).count();
                    addNode = new CollectionCustomNode(id, "Group " + (count + 1), 1);
                    detail = new CollectionConfiguration.CollectionDetail(id, "Group " + (count + 1), 1);
                    childList.add(detail);
                    detailGroup.setChildList(childList);
                    parent.insert(addNode, 0);
                }
            }
            collectionTable.setRowSelectionInterval(selectedRow, selectedRow);
            refreshTable();

        });
        toolbarDecorator.setRemoveAction(e -> {
            int i = Messages.showOkCancelDialog("Delete it(contains children)?", "Delete", "Delete", "Cancel", Messages.getQuestionIcon());
            if (i == 0) {
                int selectedRow = collectionTable.getSelectedRow();
                CollectionCustomNode node = (CollectionCustomNode) ((ListTreeTableModelOnColumns) collectionTable.getTableModel()).getRowValue(selectedRow);
                CollectionCustomNode parent = (CollectionCustomNode) node.getParent();
                parent.remove(node);
                if(selectedRow > 1){
                    collectionTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
                }else {
                    collectionTable.setRowSelectionInterval(0, 0);
                }
                refreshTable();
                CollectionConfiguration.CollectionDetail parentDetail = filterById(parent.getId(), rootDetail);
                parentDetail.getChildList().removeIf(q -> q.getId().equals(node.getId()));
            }
        });

        /**
        toolbarDecorator.addExtraActions(new ToolbarDecorator.ElementActionButton(MyResourceBundleUtil.getKey("button.addModuleGroup"), AllIcons.Nodes.ModuleGroup) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                CollectionCustomNode root = (CollectionCustomNode) ((ListTreeTableModelOnColumns) collectionTable.getTableModel()).getRowValue(0);
                ArrayList<CollectionCustomNode> nodeList = (ArrayList<CollectionCustomNode>) IteratorUtils.toList(root.children().asIterator());
                List<String> existList = nodeList.stream().map(CollectionCustomNode::getName).collect(Collectors.toList());
                ListAndSelectModule dialog = new ListAndSelectModule(myProject, existList);
                if (dialog.showAndGet()) {
                    String moduleName = dialog.getValue();
                    String id = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
                    CollectionCustomNode addNode = new CollectionCustomNode(id, moduleName, 1);
                    CollectionConfiguration.CollectionDetail detail = new CollectionConfiguration.CollectionDetail(id, moduleName, 1);
                    root.insert(addNode, 1);
                    rootDetail.getChildList().add(detail);
                    refreshTable();
                }
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        });
         */
        toolbarDecorator.setActionGroup(new MyActionGroup(()->new AnAction("Expand All", "", AllIcons.Actions.Expandall) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                collectionTable.setRootVisible(false);
                SwingUtil.expandAll(tree,new TreePath(tree.getModel().getRoot()),true);
            }

        }));

        toolbarDecorator.setActionGroup(new MyActionGroup(()->new AnAction("Collapse All", "", AllIcons.Actions.Collapseall) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                collectionTable.setRootVisible(true);
                SwingUtil.expandAll(tree,new TreePath(tree.getModel().getRoot()),false);
            }
        }));
        toolbarDecorator.setActionGroup(new MyActionGroup(() -> new AnAction("Refresh", "", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                refresh();
            }
        }));
        MyActionGroup myActionGroup = new MyActionGroup(() -> new AnAction(MyResourceBundleUtil.getKey("button.exportToPostman"), "", PluginIcons.ICON_POSTMAN) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {


                List<DataMapping> headerParamsKeyValueList;
                FastRequestToolWindow fastRequestToolWindow = ToolWindowUtil.getFastRequestToolWindow(myProject);
                if(fastRequestToolWindow == null){
                    headerParamsKeyValueList = new ArrayList<>();
                } else {
                    headerParamsKeyValueList = fastRequestToolWindow.getHeaderParamsKeyValueList();
                }
                FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
                assert config != null;
                List<DataMapping> globalHeaderList = config.getGlobalHeaderList();
                List<DataMapping> globalHeaderListNew = JSONArray.parseArray(JSON.toJSONString(globalHeaderList), DataMapping.class);
                globalHeaderListNew.removeIf(q->headerParamsKeyValueList.stream().anyMatch(p->p.getType().equals(q.getType())));
                headerParamsKeyValueList.addAll(globalHeaderListNew);

                PostmanCollection postmanCollection = PostmanExportUtil.getPostmanCollection(headerParamsKeyValueList,rootDetail,myProject.getName());
                ExporterToTextFile exporterToTextFile = new ExporterToTextFile(){

                    @Override
                    public @NotNull String getReportText() {
                        return JSON.toJSONString(postmanCollection, SerializerFeature.DisableCircularReferenceDetect);
                    }

                    @Override
                    public @NotNull String getDefaultFilePath() {
                        VirtualFile virtualFile = ProjectUtil.guessProjectDir(myProject);
                        if(virtualFile != null){
                            return virtualFile.getPath() + File.separator + "QuickRequest.postman_collection.json";
                        }
                        return "";
                    }

                    @Override
                    public boolean canExport() {
                        return true;
                    }
                };
                ExportToFileUtil.chooseFileAndExport(myProject,exporterToTextFile);
            }
        });

        toolbarDecorator.setActionGroup(myActionGroup);
        toolbarDecorator.setToolbarPosition(ActionToolbarPosition.TOP);
        collectionPanel = toolbarDecorator.createPanel();
    }

    private void renderingCollectionTablePanel2() {
        collectionTable2 = createCollectionTable2();
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(collectionTable2);
        toolbarDecorator.setAddAction(null);
        toolbarDecorator.setMoveDownAction(null);
        toolbarDecorator.setMoveUpAction(null);
        toolbarDecorator.setRemoveAction(e -> {
            int[] selectedIndices = collectionTable2.getSelectionModel().getSelectedIndices();
            ListTableModel model = (ListTableModel)collectionTable2.getModel();
            List<Integer> indexes = Arrays.stream(selectedIndices).boxed().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            indexes.forEach(model::removeRow);
            refreshTable2();
        });
        MyActionGroup myActionGroup = new MyActionGroup(() -> new AnAction("Refresh", "", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                refresh2();
            }
        });
        myActionGroup.add( new AnAction("Delete All", "", PluginIcons.ICON_DELETE_STH) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                if(historyTableDataList.isEmpty()){
                    return;
                }
                int i = Messages.showOkCancelDialog("Are you sure to <b>delete all</b> ?", "Delete", "Delete", "Cancel", Messages.getQuestionIcon());
                if(i != 0){
                    return;
                }
                historyTableDataList.clear();
                refresh2();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(!historyTableDataList.isEmpty());
            }
        });
        toolbarDecorator.setActionGroup(myActionGroup);
        toolbarDecorator.setToolbarPosition(ActionToolbarPosition.TOP);
        collectionPanel2 = toolbarDecorator.createPanel();
    }

    /**
     * 这个类 完全是为了兼容新版本做的，因为新版本addExtraActions 和 addExtraAction方法都已经标记为过期
     */
    public static class MyActionGroup extends ActionGroup{

        List<AnAction> allActions = new LinkedList<>();

        public MyActionGroup(){

        }

        public MyActionGroup(AddAnActionFunction anActionFunction){
            add(anActionFunction.initAnAction());
        }

        public void add(AnAction anAction){
            allActions.add(anAction);
        }

        public boolean isNotEmpty(){
            return !allActions.isEmpty();
        }

        @Override
        public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
            return allActions.toArray(new AnAction[allActions.size()]);
        }
    }

    private TreeTableView createCollectionTable() {
        //初始化为空
        CollectionCustomNode root = new CollectionCustomNode("0", "Root", 1);
        convertToNode(root, new ArrayList<>());
        ColumnInfo[] columnInfo = new ColumnInfo[]{
                new TreeColumnInfo("Api Name") {

                },   // <-- This is important!
                new ColumnInfo("Url") {
                    @Nullable
                    @Override
                    public Object valueOf(Object o) {
                        if (o instanceof CollectionCustomNode) {
                            return ((CollectionCustomNode) o).getUrl();
                        } else {
                            return o;
                        }
                    }
                },
                new ColumnInfo("Operation") {
                    @Nullable
                    @Override
                    public Object valueOf(Object o) {
                        return null;
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
            public Object getValueAt(int row, int column) {
                ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) getTableModel();
                CollectionCustomNode node = (CollectionCustomNode) myModel.getRowValue(row);
                if (column == 0) {
                    return node.getName();
                } else if (column == 1) {
                    return node.getUrl();
                } else {
                    return null;
                }
            }

            @Override
            public @NotNull Component prepareRenderer(@NotNull TableCellRenderer renderer, int row, int column) {
                ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) getTableModel();
                CollectionCustomNode node = (CollectionCustomNode) myModel.getRowValue(row);
                if (node.getType() != 1 && column == 2) {
                    renderer = new ButtonRenderer();
                }
                return super.prepareRenderer(renderer, row, column);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) getTableModel();
                CollectionCustomNode node = (CollectionCustomNode) myModel.getRowValue(row);
                return column == 0 || (column == 2 && node.getType() != 1);
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == 0) {
//                    ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) getTableModel();
                    String name = (String) getValueAt(row, column);
                    return new DefaultCellEditor(new JTextField(name));
                } else if (column == 2) {
                    ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) getTableModel();
                    CollectionCustomNode node = (CollectionCustomNode) myModel.getRowValue(row);
                    if (node.getType() != 1) {
                        return new ButtonEditor(new JCheckBox());
                    }
                }
                return super.getCellEditor(row, column);
            }

            @Override
            public void setValueAt(Object v, int row, int column) {
                if (column == 0) {
                    ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) getTableModel();
                    CollectionCustomNode node = (CollectionCustomNode) myModel.getRowValue(row);
                    CollectionConfiguration.CollectionDetail detail = filterById(node.getId(), rootDetail);
                    if (detail != null && !v.toString().isBlank()) {
                        detail.setName(v.toString());
                        node.setName(v.toString());
                        refreshTable();
                    }
                } else {
                    super.setValueAt(v, row, column);
                }
            }
        };

        table.setDragEnabled(true);
        table.setDropMode(DropMode.ON_OR_INSERT_ROWS);
        table.setRowSelectionAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setTransferHandler(new TransferHelper());
        table.setRootVisible(false);
        table.setVisible(true);
        table.getColumnModel().getColumn(1).setMinWidth(120);
        table.getColumnModel().getColumn(2).setMinWidth(90);
        table.getColumnModel().getColumn(2).setMaxWidth(90);
        table.setRowHeight(25);
//        table.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent event) {
//                if (event.getClickCount() == 2 && event.getButton() == MouseEvent.BUTTON1) {
//                    navigate(table);
//                }
//            }
//        });
//        table.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                super.keyPressed(e);
//                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//                    navigate(table);
//                }
//            }
//        });

        return table;
    }

    private JBTable createCollectionTable2() {
        ColumnInfo<Object, Object>[] columns = getHistoryColumnInfo();
        HistoryTable historyTable = FastRequestHistoryCollectionComponent.getInstance(myProject).getState();
        assert historyTable != null;
        List<HistoryTableData> list = historyTable.getList();
        historyTableDataList = list;
        ListTableModel<HistoryTableData> model = new ListTableModel<>(columns, list);
        JBTable jbTable = new JBTable(model) {

            @Override
            public @NotNull Component prepareRenderer(@NotNull TableCellRenderer renderer, int row, int column) {
                if (column == 3) {
                    renderer = new TableButtonRenderer();
                }else if(column == 1){
                    renderer = new HighlightCellRenderer();
                }else if(column == 0){
                    renderer = new MethodFontTableCellRenderer();
                }
                return super.prepareRenderer(renderer, row, column);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                if(column == 3){
                    return true;
                }
                return false;
            }

            @Override
            public Object getValueAt(int row, int column) {
                if (historyTableDataList.isEmpty()) {
                    return StringUtils.EMPTY;
                }
                HistoryTableData historyTableData = historyTableDataList.get(row);
                if (historyTableData == null) {
                    return StringUtils.EMPTY;
                }
                if (column == 0) {
                    return historyTableData.getType();
                } else if (column == 1) {
                    return historyTableData.getUrl();
                } else if (column == 2) {
                    return historyTableData.getTime();
                }
                return null;
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if(column == 3){
                    return new TableButtonEditor(new JCheckBox());
                }
                return super.getCellEditor(row, column);
            }
        };
        jbTable.getColumnModel().getColumn(0).setMinWidth(100);
        jbTable.getColumnModel().getColumn(0).setMaxWidth(100);
        jbTable.getColumnModel().getColumn(2).setMinWidth(150);
        jbTable.getColumnModel().getColumn(2).setMaxWidth(150);
        jbTable.getColumnModel().getColumn(3).setMinWidth(120);
        jbTable.getColumnModel().getColumn(3).setMaxWidth(120);
        jbTable.setRowHeight(30);
        return jbTable;
    }

    private ColumnInfo<Object, Object>[] getHistoryColumnInfo() {
        ColumnInfo<Object, Object>[] columnArray = new ColumnInfo[4];
        List<String> titleList = Lists.newArrayList("Method","Url", "Time", "Operation");
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

    private void navigate(TreeTableView table){
        int row = table.getSelectedRow();
        ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) collectionTable.getTableModel();
        Object rowValue = myModel.getRowValue(row);
        CollectionCustomNode node;
        if(rowValue !=null && (node = (CollectionCustomNode) rowValue).getType() == 2){
            load(node, false);
        }
    }

    class ButtonRenderer extends JBPanel implements TableCellRenderer {
        public ButtonRenderer() {

        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return renderButtons(this, row);
        }

    }

    public JBPanel renderButtons(JBPanel jbPanel, int row){
        BorderLayout borderLayout = new BorderLayout();
        jbPanel.setLayout(borderLayout);
        Dimension dimension = new Dimension(45, 20);
        JButton jButton = new JButton();
        jButton.setPreferredSize(dimension);
        jButton.setIcon(PluginIcons.ICON_LOCAL_SCOPE);
        JBColor jbColor = new JBColor(JBColor.WHITE, new Color(60, 63, 65));
        jButton.setBackground(jbColor);
        jButton.addActionListener(e-> {
            ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) collectionTable.getTableModel();
            CollectionCustomNode node = (CollectionCustomNode) myModel.getRowValue(row);
            load(node, false);
        });
        JButton jButton1 = new JButton();
        jButton1.setPreferredSize(dimension);
        jButton1.setIcon(PluginIcons.ICON_SEND_MINI);
        jButton1.setBackground(jbColor);
        jButton1.addActionListener(e-> {
            ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) collectionTable.getTableModel();
            CollectionCustomNode node = (CollectionCustomNode) myModel.getRowValue(row);
            load(node, true);
        });
        jbPanel.add(jButton, BorderLayout.WEST);
        jbPanel.add(jButton1, BorderLayout.EAST);
        return jbPanel;
    }

    class ButtonEditor extends DefaultCellEditor {
        private JBPanel jbPanel;
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            jbPanel = new JBPanel();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            return renderButtons(jbPanel, row);
        }
    }


    class TableButtonRenderer extends JBPanel implements TableCellRenderer {
        public TableButtonRenderer() {

        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return renderTableButtons(this, row);
        }

    }

    public JBPanel renderTableButtons(JBPanel jbPanel, int row){
        BorderLayout borderLayout = new BorderLayout();
        jbPanel.setLayout(borderLayout);
        Dimension dimension = new Dimension(60, 20);
        JButton jButton = new JButton();
        jButton.setPreferredSize(dimension);
        jButton.setIcon(PluginIcons.ICON_LOCAL_SCOPE);
        JBColor jbColor = new JBColor(JBColor.WHITE, new Color(60, 63, 65));
        jButton.setBackground(jbColor);
        jButton.addActionListener(e-> {
            ListTableModel model = (ListTableModel)collectionTable2.getModel();
            HistoryTableData historyTableData = (HistoryTableData)model.getRowValue(row);
            loadDataFromHistory(historyTableData, false);
        });
        JButton jButton1 = new JButton();
        jButton1.setPreferredSize(dimension);
        jButton1.setIcon(PluginIcons.ICON_SEND_MINI);
        jButton1.setBackground(jbColor);
        jButton1.addActionListener(e-> {
            ListTableModel model = (ListTableModel)collectionTable2.getModel();
            HistoryTableData historyTableData = (HistoryTableData)model.getRowValue(row);
            loadDataFromHistory(historyTableData, true);
        });
        jbPanel.add(jButton, BorderLayout.WEST);
        jbPanel.add(jButton1, BorderLayout.EAST);
        return jbPanel;
    }

    class TableButtonEditor extends DefaultCellEditor {
        private JBPanel jbPanel;
        public TableButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            jbPanel = new JBPanel();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            return renderTableButtons(jbPanel, row);
        }
    }


    class TransferHelper extends TransferHandler {
        public int getSourceActions(JComponent c) {
            return DnDConstants.ACTION_MOVE;
        }


        public Transferable createTransferable(JComponent comp) {
            JTable table = (JTable) comp;
            int row = table.getSelectedRow();
            ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) collectionTable.getTableModel();
            CollectionCustomNode node = (CollectionCustomNode) myModel.getRowValue(row);
            node.setRow(row);
            CollectionNodeSelection transferable = new CollectionNodeSelection(node);
            return transferable;
        }

        public boolean canImport(TransferSupport support) {
            return true;
        }

        public boolean importData(TransferSupport support) {
            JTable table = (JTable) support.getComponent();
            JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
            Point dp = dl.getDropPoint();
            int row = table.rowAtPoint(dp);
            int offset = row == -1 ? 0 : dl.getRow() - row;
            if (row == -1) {
                row = dl.getRow() - 1;
            }
            Transferable t = support.getTransferable();
            try {
                CollectionCustomNode add = (CollectionCustomNode) t.getTransferData(CollectionNodeSelection.CELL_DATA_FLAVOR);
                //从老节点删除
                ListTreeTableModelOnColumns myModel = (ListTreeTableModelOnColumns) collectionTable.getTableModel();
                CollectionCustomNode old = (CollectionCustomNode) myModel.getRowValue(add.getRow());
                CollectionCustomNode oldParent = (CollectionCustomNode) old.getParent();
                String sourceParentId = oldParent.getId();
                int oldIndex = oldParent.getIndex(old);
                //新节点增加
                CollectionCustomNode toAdd = (CollectionCustomNode) myModel.getRowValue(row);
                String targetId;
                int position = 0;
                if (toAdd.getType() == 1) {
                    old.removeFromParent();
                    targetId = toAdd.getId();
                    toAdd.insert(add, 0);
                } else {
                    CollectionCustomNode parent = (CollectionCustomNode) toAdd.getParent();
                    position = parent.getIndex(toAdd);

                    if (sourceParentId.equals(parent.getId())) {
                        if (position == oldIndex) {
                            return false;
                        } else if (position > oldIndex) {
                            old.removeFromParent();
                        } else {
                            old.removeFromParent();
                            position += offset;
                        }
                    } else {
                        old.removeFromParent();
                        position += offset;
                    }

                    parent.insert(add, position);
                    targetId = parent.getId();
                }
                TreeTableTree tree = collectionTable.getTree();
                for (TreeNode treeNode : add.getPath()) {
                    tree.expandPath(new TreePath(((CollectionCustomNode) treeNode).getPath()));
                }
                refreshTable();
                moveData(sourceParentId, add.getId(), targetId, position);
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    class Renderer extends ColoredTreeCellRenderer {
        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            //解决TreeTable key加上>
            CollectionCustomNode node = (CollectionCustomNode) value;
            append(node.getName());
            if (node.getType() == 2) {
                setIcon(FrIconUtil.getIconByMethodType(node.getDetail().getParamGroup().getMethodType()));
            }else if(node.getType() == 1 && !node.isRoot()){
                if(node.getChildCount() == 0){
                    return;
                }
                TreeNode firstChild = node.getFirstChild();
                if(firstChild != null){
                    CollectionCustomNode firstChildNode = (CollectionCustomNode) firstChild;
                    if(firstChildNode.getType() == 1){
                        setIcon(AllIcons.Nodes.Module);
                    }else {
                        setIcon(AllIcons.Nodes.Package);
                    }
                }
            }
        }
    }

    private CollectionCustomNode convertToNode(CollectionConfiguration.CollectionDetail detail) {
        String name = detail.getName();
        CollectionCustomNode node = new CollectionCustomNode(name, detail.getType(), detail.getParamGroup().getUrl());
        node.setId(detail.getId());
        node.setSearchText("<" + detail.getName() + detail.getDescription() + detail.getParamGroup().getUrl() + ">");
        node.setDetail(detail);
        List<CollectionConfiguration.CollectionDetail> child = detail.getChildList();
        if (CollectionUtils.isNotEmpty(child)) {
            CollectionConfiguration.CollectionDetail defaultGroup = null;
            for (CollectionConfiguration.CollectionDetail d : child) {
                if(Objects.equals("1", d.getId())){
                    defaultGroup = d;
                    continue;
                }
                CollectionCustomNode nodeIn = convertToNode(d);
                node.add(nodeIn);
            }
            if(defaultGroup != null){
                detail.getChildList().remove(defaultGroup);
            }
        }
        return node;
    }


    private void convertToNode(CollectionCustomNode node, List<CollectionConfiguration.CollectionDetail> collectionDetailList) {
        for (CollectionConfiguration.CollectionDetail c : collectionDetailList) {
            String name = c.getName();
            CollectionCustomNode nodeObject = new CollectionCustomNode(name, c.getType(), c.getParamGroup().getUrl());
            nodeObject.setId(c.getId());
            nodeObject.setDetail(c);
            List<CollectionConfiguration.CollectionDetail> childList = c.getChildList();
            if (CollectionUtils.isNotEmpty(childList)) {
                convertToNode(nodeObject, childList);
            }
            nodeObject.setSearchText("<" + c.getName() + c.getDescription() + c.getParamGroup().getUrl() + ">");
            node.add(nodeObject);
        }
    }

    private void moveData(String sourceParentId, String sourceId, String targetId, Integer position) {
        CollectionConfiguration.CollectionDetail sourceParent = filterById(sourceParentId, rootDetail);
        CollectionConfiguration.CollectionDetail from = filterById(sourceId, rootDetail);
        CollectionConfiguration.CollectionDetail to = filterById(targetId, rootDetail);

        sourceParent.getChildList().remove(from);
        to.getChildList().add(position, from);
    }

    private void load(CollectionCustomNode node, boolean sendFlag) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                boolean flag = false;
                //定位方法
                CollectionConfiguration collectionConfiguration = FastRequestCollectionComponent.getInstance(myProject).getState();
                assert collectionConfiguration != null;
                CollectionConfiguration.CollectionDetail detail = filterById(node.getId(), collectionConfiguration.getDetail());
                if (detail == null) {
                    return;
                }
                ParamGroupCollection paramGroup = detail.getParamGroup();
                if(paramGroup.getType() == null || paramGroup.getType() == 0){
                    String className = paramGroup.getClassName();
                    String methodName = paramGroup.getMethod();
                    PsiClass psiClass = null;
                    try {
                        psiClass = JavaPsiFacade.getInstance(myProject).findClass(className, GlobalSearchScope.projectScope(myProject));
                    } catch (IndexNotReadyException e) {
                        NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification("Index should be ready first", MessageType.INFO).notify(myProject);
                    }
                    //used to navigate
                    if (psiClass != null) {
                        PsiElement[] psiClassMethodsByName = psiClass.findMethodsByName(methodName, true);
                        if (psiClassMethodsByName.length > 0) {
                            ApplicationManager.getApplication().invokeLater(() -> {
                                PsiNavigateUtil.navigate(psiClassMethodsByName[0]);
                            });
                            flag = true;
                        } else {
                            NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification("Method not found", MessageType.INFO).notify(myProject);
                        }
                    }
                }
                loadAncChangeTab(flag, detail, sendFlag);
            });
        });

//
//        Task.Backgroundable task = new Task.Backgroundable(myProject, "") {
//≤
//            @Override
//            public void run(@NotNull ProgressIndicator indicator) {
//
//            }
//        };
//        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
    }

    private void loadDataFromHistory(HistoryTableData data, boolean sendFlag) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                loadFromHistoryAndChangeTab(false, data, sendFlag);
            });
        });

//
//        Task.Backgroundable task = new Task.Backgroundable(myProject, "") {
//≤
//            @Override
//            public void run(@NotNull ProgressIndicator indicator) {
//
//            }
//        };
//        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
    }

    private void loadAncChangeTab(boolean flag, CollectionConfiguration.CollectionDetail detail, boolean sendFlag) {
        //change data
        ApplicationManager.getApplication().invokeLater(() -> {
            //切换tab
            ToolWindow fastRequestToolWindow = ToolWindowManager.getInstance(myProject).getToolWindow("Quick Request");
            Content content = fastRequestToolWindow.getContentManager().getContent(0);
            assert content != null;
            fastRequestToolWindow.getContentManager().setSelectedContent(content);

            MessageBus messageBus = myProject.getMessageBus();
            messageBus.connect();
            ConfigChangeNotifier configChangeNotifier = messageBus.syncPublisher(ConfigChangeNotifier.LOAD_REQUEST);
            configChangeNotifier.loadRequest(detail, myProject.getName(), sendFlag, flag);
        });
    }

    private void loadFromHistoryAndChangeTab(boolean flag, HistoryTableData data, boolean sendFlag) {
        //change data
        ApplicationManager.getApplication().invokeLater(() -> {
            //切换tab
            ToolWindow fastRequestToolWindow = ToolWindowManager.getInstance(myProject).getToolWindow("Quick Request");
            Content content = fastRequestToolWindow.getContentManager().getContent(0);
            assert content != null;
            fastRequestToolWindow.getContentManager().setSelectedContent(content);

            MessageBus messageBus = myProject.getMessageBus();
            messageBus.connect();
            ConfigChangeNotifier configChangeNotifier = messageBus.syncPublisher(ConfigChangeNotifier.LOAD_REQUEST_HISTORY);
            configChangeNotifier.loadRequestHistory(data, myProject.getName(), sendFlag, flag);
        });
    }

    private void refreshTable() {
        SwingUtilities.invokeLater(() -> collectionTable.updateUI());
    }

    private void refreshTable2() {
        SwingUtilities.invokeLater(() -> collectionTable2.updateUI());
    }

    private enum SearchTypeEnum {
        name,
        url,
        separator_Method,
        get,
        post,
        put,
        delete,
        patch;

        public static SearchTypeEnum fromValue(String name) {
            for (SearchTypeEnum v : values()) {
                if (v.name().equals(name)) {
                    return v;
                }
            }
            return null;
        }
    }

    private final class SearchOptionAction extends ToggleAction implements DumbAware {
        private final SearchTypeEnum myOption;
        private boolean myState;

        private SearchOptionAction(@NotNull SearchTypeEnum option) {
            super(option.name());
            myOption = option;
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return myState;
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            myState = state;
            mySearchCallback.accept(this);
        }

        @NotNull
        public String getQuery() {
            return StringUtil.decapitalize(myOption.name() + "|");
        }
    }

    private final class SearchOption2Action extends ToggleAction implements DumbAware {
        private final SearchTypeEnum myOption;
        private boolean myState;

        private SearchOption2Action(@NotNull SearchTypeEnum option) {
            super(option.name());
            myOption = option;
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return myState;
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            myState = state;
            mySearchCallback2.accept(this);
        }

        @NotNull
        public String getQuery() {
            return StringUtil.decapitalize(myOption.name() + "|");
        }
    }
}
