package io.github.zjay.plugin.quickrequest.listener;

import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;
import com.intellij.util.messages.MessageBus;
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.config.FastRequestCurrentProjectConfigComponent;
import io.github.zjay.plugin.quickrequest.configurable.ConfigChangeNotifier;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.model.FastRequestCurrentProjectConfiguration;
import io.github.zjay.plugin.quickrequest.model.HostGroup;
import io.github.zjay.plugin.quickrequest.model.NameGroup;
import io.github.zjay.plugin.quickrequest.util.MyResourceBundleUtil;
import io.github.zjay.plugin.quickrequest.util.ReflectUtils;
import io.github.zjay.plugin.quickrequest.util.spring.SpringBootUtils;
import io.github.zjay.plugin.quickrequest.view.inner.EnvAddView;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SpringBootListener implements ProcessListener {

    private static final Map<Key<?>, ProcessEntity> processMap  = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    private final Project project;

    private final ModuleBasedConfiguration<?,?> configuration;

    private static final String urlTemplate = "http://localhost:portpath";

    static {
        //help GC. cache expire.
        scheduler.scheduleAtFixedRate(()->{
            List<Key<?>> needRemoveList = new ArrayList<>();
            processMap.forEach((key, processEntity) -> {
                if (System.currentTimeMillis() >= processEntity.expireTime){
                    needRemoveList.add(key);
                }
            });
            for (Key<?> key : needRemoveList) {
                processMap.remove(key);
            }
        }, 0, 1,  TimeUnit.MINUTES);

    }

    public SpringBootListener(Project project, ModuleBasedConfiguration<?,?> configuration) {
        this.project = project;
        this.configuration = configuration;
    }

    @Override
    public void startNotified(@NotNull ProcessEvent processEvent) {

    }

    @Override
    public void processTerminated(@NotNull ProcessEvent processEvent) {
    }

    @Override
    public void onTextAvailable(@NotNull ProcessEvent processEvent, @NotNull Key key) {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        if (config == null)
            return;
        if(config.getNoNeedAutoGenerateConfig() != null && config.getNoNeedAutoGenerateConfig()){
            return;
        }
        if(!(processEvent.getSource() instanceof ProcessHandler)){
            return;
        }
        try {
            String text = processEvent.getText();
            String profileFromText = SpringBootUtils.getProfileFromText(text);
            //要么是刚启动的，要么是启动一阵子且有类似profile格式的内容输出，这个情况舍去
            if (StringUtils.isNotBlank(profileFromText) && !processMap.containsKey(key)){
                processMap.put(key, new ProcessEntity(profileFromText));
                return;
            }
            String[] portAndContext = SpringBootUtils.getPortAndContextFromText(text);
            if (portAndContext != null && processMap.containsKey(key)){
                ProcessEntity processEntity = processMap.get(key);
                processEntity.port = Integer.parseInt(portAndContext[0]);
                processEntity.contextPath = portAndContext[1];
                //解析到这里，代表这些基本信息都有了，开始检查配置，有的就更新，没有的就新增
                if(!config.getEnvList().contains(processEntity.profile)){
                    config.getEnvList().add(processEntity.profile);
                }
                String name = configuration.getConfigurationModule().getModuleName();
                if (StringUtils.isBlank(name))
                    name = project.getName();
                if(!config.getProjectList().contains(name)){
                    config.getProjectList().add(name);
                }
                List<NameGroup> dataList = config.getDataList();
                String finalName = name;
                List<NameGroup> collect = dataList.stream().filter(x -> Objects.equals(finalName, x.getName())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)){
                    List<HostGroup> existHost = collect.get(0).getHostGroup().stream().filter(x -> Objects.equals(x.getEnv(), processEntity.profile)).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(existHost)){
                        HostGroup hostGroup = existHost.get(0);
                        if(StringUtils.isBlank(hostGroup.getUrl())){
                            hostGroup.setUrl(urlTemplate.replace("port", portAndContext[0]).replace("path", processEntity.contextPath));
                            notifyAndModify(hostGroup, name);
                        }
                    }else {
                        HostGroup hostGroup = new HostGroup(processEntity.profile, urlTemplate.replace("port", portAndContext[0]).replace("path", processEntity.contextPath));
                        collect.get(0).getHostGroup().add(hostGroup);
                        notifyAndModify(hostGroup, name);
                    }
                }else {
                    List<HostGroup> hostGroupList = new ArrayList<>();
                    HostGroup hostGroup = new HostGroup(processEntity.profile, urlTemplate.replace("port", portAndContext[0]).replace("path", processEntity.contextPath));
                    hostGroupList.add(hostGroup);
                    dataList.add(new NameGroup(name, hostGroupList));
                    notifyAndModify(hostGroup, name);
                }
                //修改当前项目配置
                FastRequestCurrentProjectConfiguration projectConfig = FastRequestCurrentProjectConfigComponent.getInstance(project).getState();
                if (projectConfig != null){
                    projectConfig.setEnableProject(name);
                    projectConfig.setEnableEnv(processEntity.profile);
                    projectConfig.setDomain("");
                }
                MessageBus messageBus = project.getMessageBus();
                messageBus.connect();
                ConfigChangeNotifier configChangeNotifier = messageBus.syncPublisher(ConfigChangeNotifier.ENV_PROJECT_CHANGE_TOPIC);
                configChangeNotifier.configChanged(true, project.getName());
                processMap.remove(key);
            }
        }catch (Exception e){
            processMap.remove(key);
        }
    }

//    public void springBootAPi(){
//        Object field = ReflectUtils.getParentField(processEvent.getSource(), "myListeners");
//        Object springBootConfig = null;
//        if (field != null){
//            try {
//                List<ProcessListener> processListeners = (List<ProcessListener>) field;
//                for (ProcessListener processListener : processListeners) {
//                    Object configuration = ReflectUtils.getParentField(processListener, "val$configuration");
//                    if (configuration != null && Objects.equals(configuration.getClass().getCanonicalName(), "com.intellij.spring.boot.run.SpringBootApplicationRunConfiguration")) {
//                        springBootConfig = configuration;
//                    }
//                }
//            }catch (Exception e){
//                return;
//            }
//        }
//        if (springBootConfig == null)
//            return;
//Object nameObj = ReflectUtils.invokeMethod(springBootConfig, "getModule");
//    }

    private void notifyAndModify(HostGroup hostGroup, String name) {
        String title = name + "/" + hostGroup.getEnv();
        String tip = MyResourceBundleUtil.getKey("createProjectAndEnvSuccess").replace("${env}", title);
        Notification quickRequestWindowNotificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification(tip, MessageType.INFO);
        quickRequestWindowNotificationGroup.addAction(new NotificationAction(MyResourceBundleUtil.getKey("modify")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent, @NotNull Notification notification) {
                EnvAddView envAddView = new EnvAddView(MyResourceBundleUtil.getKey("modify")  + ": "+ title, MyResourceBundleUtil.getKey("PleaseModify"));
                envAddView.setText(hostGroup.getUrl());
                if (envAddView.showAndGet()) {
                    String text = envAddView.getText();
                    hostGroup.setUrl(text);
                    MessageBus messageBus = project.getMessageBus();
                    messageBus.connect();
                    ConfigChangeNotifier configChangeNotifier = messageBus.syncPublisher(ConfigChangeNotifier.ENV_PROJECT_CHANGE_TOPIC);
                    configChangeNotifier.configChanged(true, project.getName());
                    notification.expire();
                }
            }
        });
        quickRequestWindowNotificationGroup.notify(project);
    }

    private static class ProcessEntity{
        public int port;
        public String profile;
        public String contextPath;

        public Long expireTime;

        public ProcessEntity(String profile){
            this.profile = profile;
            //10 min expire
            this.expireTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10);
        }


    }
}
