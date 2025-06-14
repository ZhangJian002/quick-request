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

package io.github.zjay.plugin.quickrequest.model;

import com.google.common.collect.Lists;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.grpc.GrpcCurlUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 总配置
 *
 * @author Kings
 * @date 2021/05/22
 * @see Serializable
 */
public class FastRequestConfiguration implements Serializable {

    /**
     * 项目列表
     */
    private List<String> projectList = new ArrayList<>();
    /**
     * env列表
     */
    private List<String> envList = new ArrayList<>();
    /**
     * 数据集合
     */
    private List<NameGroup> dataList = new ArrayList<>();

    /**
     * 自定义数据映射
     */
    private List<DataMapping> customDataMappingList = new ArrayList<>();

    private List<String> ignoreDataMappingList = Constant.IGNORE_PARAM_PARSE_LIST;

    /**
     * 是否点击并请求
     */
    private Boolean clickAndSend = null;

    private Integer connectionTimeout = null;

    private Integer readTimeout = null;

    /**
     * 压测相关参数
     */
    private Integer jmhConnectionTimeout = null;
    private Integer jmhReadTimeout = null;
    private Integer jmhWriteTimeout = null;
    private Integer threads = null;
    private Integer testCount = null;

    /**
     * Apis中是否过滤接口方法
     */
    private Boolean needInterface = null;

    /**
     * 是否需要在启动时自动生成配置，仅对spring boot项目生效
     */
    private Boolean noNeedAutoGenerateConfig = null;

    /**
     * 是否需要记录请求日志（idea）
     */
    private Boolean needIdeaLog = null;

    /**
     * grpcurl的路径
     */
    private String grpcurlPath = null;

    public Boolean getNeedIdeaLog() {
        return needIdeaLog;
    }

    public void setNeedIdeaLog(Boolean needIdeaLog) {
        this.needIdeaLog = needIdeaLog;
    }

    public String getGrpcurlPath() {
        return grpcurlPath;
    }

    public String queryNotNullGrpcurlPath(){
        return grpcurlPath == null ? GrpcCurlUtils.GRPC_PATH_DEFAULT : grpcurlPath;
    }

    public void setGrpcurlPath(String grpcurlPath) {
        this.grpcurlPath = grpcurlPath;
    }

    public Boolean getNoNeedAutoGenerateConfig() {
        return noNeedAutoGenerateConfig;
    }

    public void setNoNeedAutoGenerateConfig(Boolean noNeedAutoGenerateConfig) {
        this.noNeedAutoGenerateConfig = noNeedAutoGenerateConfig;
    }

    public Boolean getNeedInterface() {
        return needInterface;
    }

    public void setNeedInterface(Boolean needInterface) {
        this.needInterface = needInterface;
    }

    /**
     * 默认的数据映射
     */
    private List<DataMapping> defaultDataMappingList = Lists.newArrayList(
            new DataMapping("byte", "1"),
            new DataMapping("java.lang.Byte", "1"),
            new DataMapping("short", "1"),
            new DataMapping("java.lang.Short", "1"),
            new DataMapping("int", "1"),
            new DataMapping("java.lang.Integer", "1"),
            new DataMapping("long", "1"),
            new DataMapping("java.lang.Long", "1"),
            new DataMapping("char", "a"),
            new DataMapping("java.lang.Character", "a"),
            new DataMapping("float", "1"),
            new DataMapping("java.lang.Float", "1"),
            new DataMapping("double", "1"),
            new DataMapping("java.lang.Double", "1"),
            new DataMapping("boolean", "true"),
            new DataMapping("java.lang.Boolean", "true"),
            new DataMapping("java.math.BigDecimal", "1")
    );

    /**
     * url替换规则
     */
    private List<DataMapping> urlReplaceMappingList = new ArrayList<>();


    private String enableEnv;

    private String enableProject;

    private String domain = "";

    private ParamGroup paramGroup = new ParamGroup();

    private int randomStringLength = 3;

    private long defaultGroupCount = 0;

    /**
     * String生成策略
     * name+random
     * random
     * none
     */
    private String randomStringStrategy = "name+random";

    /**
     * String生成器分隔符
     */
    private String randomStringDelimiter = "_";

    private List<DataMapping> headerList = new ArrayList<>();

    private List<HeaderGroup> headerGroupList = new ArrayList<>();

    private List<DataMapping> globalHeaderList = new ArrayList<>();

    public List<String> getProjectList() {
        return projectList;
    }

    public long getDefaultGroupCount() {
        return defaultGroupCount;
    }

    public void setDefaultGroupCount(long defaultGroupCount) {
        this.defaultGroupCount = defaultGroupCount;
    }

    public void setProjectList(List<String> projectList) {
        this.projectList = projectList;
    }

    public List<String> getEnvList() {
        return envList;
    }

    public void setEnvList(List<String> envList) {
        this.envList = envList;
    }

    public List<NameGroup> getDataList() {
        return dataList;
    }

    public void setDataList(List<NameGroup> dataList) {
        this.dataList = dataList;
    }

    public String getEnableEnv() {
        return enableEnv;
    }

    public void setEnableEnv(String enableEnv) {
        this.enableEnv = enableEnv;
    }

    public String getEnableProject() {
        return enableProject;
    }

    public void setEnableProject(String enableProject) {
        this.enableProject = enableProject;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<DataMapping> getDefaultDataMappingList() {
        return defaultDataMappingList;
    }

    public void setDefaultDataMappingList(List<DataMapping> defaultDataMappingList) {
        this.defaultDataMappingList = defaultDataMappingList;
    }

    public List<DataMapping> getCustomDataMappingList() {
        return customDataMappingList;
    }

    public void setCustomDataMappingList(List<DataMapping> customDataMappingList) {
        this.customDataMappingList = customDataMappingList;
    }

    public ParamGroup getParamGroup() {
        return paramGroup;
    }

    public void setParamGroup(ParamGroup paramGroup) {
        this.paramGroup = paramGroup;
    }

    public int getRandomStringLength() {
        return randomStringLength;
    }

    public void setRandomStringLength(int randomStringLength) {
        this.randomStringLength = randomStringLength;
    }

    public List<DataMapping> getHeaderList() {
        return headerList;
    }

    public void setHeaderList(List<DataMapping> headerList) {
        this.headerList = headerList;
    }

    public List<DataMapping> getUrlReplaceMappingList() {
        return urlReplaceMappingList;
    }

    public void setUrlReplaceMappingList(List<DataMapping> urlReplaceMappingList) {
        this.urlReplaceMappingList = urlReplaceMappingList;
    }

    public String getRandomStringStrategy() {
        return randomStringStrategy;
    }

    public void setRandomStringStrategy(String randomStringStrategy) {
        this.randomStringStrategy = randomStringStrategy;
    }

    public String getRandomStringDelimiter() {
        return randomStringDelimiter;
    }

    public void setRandomStringDelimiter(String randomStringDelimiter) {
        this.randomStringDelimiter = randomStringDelimiter;
    }

    public List<HeaderGroup> getHeaderGroupList() {
        headerGroupList.removeIf(Objects::isNull);
        return headerGroupList;
    }

    public void setHeaderGroupList(List<HeaderGroup> headerGroupList) {
        this.headerGroupList = headerGroupList;
    }

    public List<String> getIgnoreDataMappingList() {
        return ignoreDataMappingList;
    }

    public void setIgnoreDataMappingList(List<String> ignoreDataMappingList) {
        this.ignoreDataMappingList = ignoreDataMappingList;
    }

    public List<DataMapping> getGlobalHeaderList() {
        return globalHeaderList;
    }

    public void setGlobalHeaderList(List<DataMapping> globalHeaderList) {
        this.globalHeaderList = globalHeaderList;
    }

    public Boolean getClickAndSend() {
        return clickAndSend;
    }

    public void setClickAndSend(Boolean clickAndSend) {
        this.clickAndSend = clickAndSend;
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
}
