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

import com.intellij.icons.AllIcons;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.actions.searcheverywhere.PersistentSearchEverywhereContributorFilter;
import com.intellij.ide.plugins.newui.ListPluginComponent;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.*;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.util.BooleanFunction;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.ui.StatusText;
import io.github.zjay.plugin.quickrequest.action.CheckBoxFilterAction;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.configurable.FastRequestSearchEverywhereConfiguration;
import io.github.zjay.plugin.quickrequest.model.ApiService;
import io.github.zjay.plugin.quickrequest.model.MethodType;
import io.github.zjay.plugin.quickrequest.view.component.tree.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AllApisNavToolWindow extends SimpleToolWindowPanel implements Disposable {
    private final Project myProject;
    private JPanel panel;

    private SearchTextField searchPanel;

    private ApiTree apiTree;
    private ToolWindow toolWindow;
    private List<ApiService> allApiList;

    private AtomicBoolean refresh = new AtomicBoolean(true);
    //    PersistentSearchEverywhereContributorFilter<String> methodTypeFilter = createMethodTypeFilter();
    private CheckBoxFilterAction.Filter<String> moduleFilter;
    private CheckBoxFilterAction.Filter<String> methodTypeFilter;

    public AllApisNavToolWindow(Project project, ToolWindow toolWindow) {
        super(false, false);
        this.myProject = project;
        this.toolWindow = toolWindow;
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        this.setContent(panel);
        setLayout(new BorderLayout());
        apiTree = new ApiTree();
        initActionBar();

        apiTree.setCellRenderer(new MyCellRenderer());
        apiTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    navigateToMethod();
                }
            }
        });
        apiTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && event.getButton() == MouseEvent.BUTTON1) {
                    navigateToMethod();
                }
            }
        });
        searchPanel = new SearchTextField(true);
        searchPanel.setFocusable(false);
        JBTextField searchTextField2 = searchPanel.getTextEditor();
        searchTextField2.putClientProperty("StatusVisibleFunction", (BooleanFunction<JBTextField>) field -> field.getText().isEmpty());
        StatusText emptyText2 = searchTextField2.getEmptyText();
        emptyText2.appendText("Search by url or method name", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, ListPluginComponent.GRAY_COLOR));
//        searchTextField2.putClientProperty("search.extension", ExtendableTextComponent.Extension
//                .create(AllIcons.Actions.More, AllIcons.Actions.More, "Search options",
//                        () -> showRightBottomPopup(searchTextField2, "By", myInstalledSearchGroup2)));
        searchTextField2.putClientProperty("JTextField.variant", null);
        searchTextField2.putClientProperty("JTextField.variant", "search");
        searchTextField2.getDocument().addDocumentListener(new DelayedDocumentListener());
        panel.add(searchPanel, BorderLayout.NORTH);
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(apiTree);
        panel.add(scrollPane);
        Disposer.register(myProject, this);
    }

    public class DelayedDocumentListener implements DocumentListener {

        private final Timer timer;

        public DelayedDocumentListener() {
            timer = new Timer(2, e -> filterRequest());
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

    private void filterRequest() {
        if (allApiList == null) {
            return;
        }
        List<String> selectMethodType = methodTypeFilter.getSelectedElementList();
        List<ApiService> filterList = allApiList.stream().filter(q -> moduleFilter.getSelectedElementList().contains(q.getModuleName())).collect(Collectors.toList());
        List<ApiService> allApiFilterList = new LinkedList<>();
        filterList.stream().forEach(apiService -> {
            List<ApiService.ApiMethod> methods = apiService.getApiMethodList().stream().filter(x ->
                    (x.getName().toLowerCase().contains(searchPanel.getText().toLowerCase())
                            || x.getUrl().toLowerCase().contains(searchPanel.getText().toLowerCase()))
                            && selectMethodType.contains(x.getMethodType())
            ).collect(Collectors.toList());
            if(methods.isEmpty()){
                return;
            }
            ApiService item = new ApiService();
            item.setLanguage(apiService.getLanguage());
            item.setClassName(apiService.getClassName());
            item.setModuleName(apiService.getModuleName());
            item.setPackageName(apiService.getPackageName());
            item.setApiMethodList(methods);
            allApiFilterList.add(item);
        });
        List<ApiService.ApiMethod> filterMethodList = new LinkedList<>();
        allApiFilterList.stream().map(ApiService::getApiMethodList).filter(CollectionUtils::isNotEmpty).forEach(filterMethodList::addAll);
        long count = filterMethodList.stream().filter(q -> selectMethodType.contains(q.getMethodType())).count();
        RootNode root = new RootNode(count + " apis") {
        };

        NodeUtil.convertToRoot(root, NodeUtil.convertToMap(allApiFilterList), selectMethodType);
        ApplicationManager.getApplication().invokeLater(() -> {
            apiTree.setModel(new DefaultTreeModel(root));
            apiTree.expandAll();
        });
    }

    private void navigateToMethod() {
        if (!apiTree.isEnabled()) {
            return;
        }
        Object component = apiTree.getLastSelectedPathComponent();
        if (!(component instanceof MethodNode)) {
            return;
        }
        MethodNode methodNode = (MethodNode) component;
        PsiNavigateUtil.navigate(methodNode.getSource().getPsiMethod());
    }

    public void refreshFilterModule(List<String> selectModule, List<String> selectMethodType) {
        DumbService.getInstance(myProject).smartInvokeLater(() -> {
            Task.Backgroundable task = new Task.Backgroundable(myProject, "Reload apis...") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(false);
                    ApplicationManager.getApplication().runReadAction(() -> {
                        if (allApiList == null) {
                            return;
                        }
                        if(StringUtils.isNotBlank(searchPanel.getText())){
                            filterRequest();
                        }else {
                            List<ApiService> filterList = allApiList.stream().filter(q -> selectModule.contains(q.getModuleName())).collect(Collectors.toList());
                            indicator.setText("Rendering");
                            List<ApiService.ApiMethod> filterMethodList = new ArrayList<>();
                            filterList.stream().map(ApiService::getApiMethodList).filter(CollectionUtils::isNotEmpty).forEach(filterMethodList::addAll);
                            long count = filterMethodList.stream().filter(q -> selectMethodType.contains(q.getMethodType())).count();
                            RootNode root = new RootNode(count + " apis") {
                            };

                            NodeUtil.convertToRoot(root, NodeUtil.convertToMap(filterList), selectMethodType);
                            apiTree.setModel(new DefaultTreeModel(root));
                        }
                    });
                }
            };
            ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
        });
    }

    private static class MyCellRenderer extends ColoredTreeCellRenderer {

        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object target, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            BaseNode node = null;
            if (target instanceof BaseNode) {
                node = (BaseNode<?>) target;
            }
            if (node == null) {
                return;
            }
            append(node.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            setIcon(node.getIcon(true));
            if (target instanceof MethodNode) {
                setToolTipText(((MethodNode) target).getToolTipText());
            }

            SpeedSearchUtil.applySpeedSearchHighlighting(this, this, false, true);
        }
    }

    private void renderData(Project project) {
        refresh.set(false);
        DumbService.getInstance(project).smartInvokeLater(() -> rendingTree(null));
    }

    public static PersistentSearchEverywhereContributorFilter<String> createMethodTypeFilter() {
        List<String> methodNameList = Constant.METHOD_TYPE_LIST.stream().map(MethodType::getName).collect(Collectors.toList());
        Map<String, Icon> iconMap = Constant.METHOD_TYPE_LIST.stream().collect(Collectors.toMap(MethodType::getName, MethodType::getIcon));
        return new PersistentSearchEverywhereContributorFilter<>(
                methodNameList,
                FastRequestSearchEverywhereConfiguration.getInstance(),
                methodName -> methodName, iconMap::get);
    }

    private void rendingTree(List<String> moduleNameList) {
        Task.Backgroundable task = new Task.Backgroundable(myProject, "Reload apis...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                ApplicationManager.getApplication().runReadAction(() -> {
                    try {
                        addAllApis(moduleNameList);
                        indicator.setText("Rendering");
                        if(StringUtils.isNotBlank(searchPanel.getText())){
                            filterRequest();
                        }else {
                            List<String> selectMethodType = methodTypeFilter.getSelectedElementList();
                            List<ApiService.ApiMethod> filterMethodList = new ArrayList<>();
                            allApiList.stream().map(ApiService::getApiMethodList).forEach(filterMethodList::addAll);
                            long count = filterMethodList.stream().filter(q -> selectMethodType.contains(q.getMethodType())).count();
                            RootNode root = new RootNode(count + " apis");
                            NodeUtil.convertToRoot(root, NodeUtil.convertToMap(
                                    allApiList.stream().filter(q->CollectionUtils.isNotEmpty(q.getApiMethodList())).collect(Collectors.toList())
                            ), methodTypeFilter.getSelectedElementList());
                            ApplicationManager.getApplication().invokeLater(() -> apiTree.setModel(new DefaultTreeModel(root)));
                        }
                        NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification("Reload apis complete", MessageType.INFO)
                                .notify(myProject);

                    }finally {
                        refresh.set(true);
                    }
                });
            }
        };
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
    }

    private void addAllApis(List<String> moduleNameList) {
        addJavaApis(moduleNameList);
        addPhpApis(moduleNameList);
        addGoApis(moduleNameList);
        addPythonApis(moduleNameList);
    }

    private void addJavaApis(List<String> moduleNameList) {
        allApiList = NodeUtil.getJavaApis(moduleNameList, myProject);
    }

    private void addPhpApis(List<String> moduleNameList) {
        List<ApiService> apiServiceList = NodeUtil.getPhpApis(moduleNameList, myProject);
        judgeAndSet(apiServiceList);
    }

    private void addGoApis(List<String> moduleNameList) {
        List<ApiService> apiServiceList = NodeUtil.getGoApis(moduleNameList, myProject);
        judgeAndSet(apiServiceList);
    }

    private void addPythonApis(List<String> moduleNameList) {
        List<ApiService> apiServiceList = NodeUtil.getPythonApis(moduleNameList, myProject);
        judgeAndSet(apiServiceList);
    }

    private void judgeAndSet(List<ApiService> apiServiceList) {
        if(CollectionUtils.isNotEmpty(allApiList)){
            allApiList.addAll(apiServiceList);
        }else {
            allApiList = apiServiceList;
        }
    }

    private void initActionBar() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RefreshApiAction());
        group.add(CommonActionsManager.getInstance().createExpandAllAction(apiTree, apiTree));
        group.add(CommonActionsManager.getInstance().createCollapseAllAction(apiTree, apiTree));

        Module[] modules = ModuleManager.getInstance(myProject).getModules();
        List<String> moduleList = Arrays.stream(modules).map(Module::getName).sorted().collect(Collectors.toList());
        moduleFilter = new CheckBoxFilterAction.Filter<>(moduleList, module -> module, module -> null, FastRequestSearchEverywhereConfiguration.getInstance());
        group.add(new CheckBoxFilterAction<>("Filter Module", "Filter module", AllIcons.Actions.GroupByModule, moduleFilter, this::refresh));


        List<String> methodNameList = Constant.METHOD_TYPE_LIST.stream().map(MethodType::getName).collect(Collectors.toList());
        Map<String, Icon> iconMap = Constant.METHOD_TYPE_LIST.stream().collect(Collectors.toMap(MethodType::getName, MethodType::getIcon));
        methodTypeFilter = new CheckBoxFilterAction.Filter<>(methodNameList, methodName -> methodName, iconMap::get, FastRequestSearchEverywhereConfiguration.getInstance());
        group.add(new CheckBoxFilterAction<>("Filter Method", "Filter Method", AllIcons.Actions.GroupByMethod, methodTypeFilter, this::refresh));


        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, group, false);
        actionToolbar.setTargetComponent(panel);
        JComponent toolbarComponent = actionToolbar.getComponent();
        Border border = IdeBorderFactory.createBorder(SideBorder.BOTTOM);
        actionToolbar.getComponent().setBorder(border);
        setToolbar(toolbarComponent);
    }

    private void refresh() {
        List<String> moduleList = moduleFilter.getSelectedElementList();
        List<String> methodList = methodTypeFilter.getSelectedElementList();
        refreshFilterModule(moduleList, methodList);
    }


    private final class RefreshApiAction extends AnAction {
        public RefreshApiAction() {
            super("Refresh", "Refresh", AllIcons.Actions.Refresh);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            renderData(myProject);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(refresh.get());
        }
    }

    @Override
    public void dispose() {

    }
}
