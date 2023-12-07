package io.github.zjay.plugin.quickrequest.dubbo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.github.zjay.plugin.quickrequest.model.ParamKeyValue;

import java.util.*;

public class DubboService {
    /**
     * dubo服务地址
     */
    private String serviceAddress;

    /**
     * dubbo服务名
     */
    private String serviceName;

    /**
     * 参数
     */
    private Param param;


    private InvokeService invokeService = new InvokeService();

    public DubboService(String serviceName, Param param){
        this.param = param;
        this.serviceName = serviceName;
    }

    public void setServiceAddress(String serviceAddress){
        this.serviceAddress = serviceAddress;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Param getParam(){
        return this.param;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * 发起dubbbo调用
     * @return
     */
    public Response invoke(){
        String[] response = invokeService.invoke(this);
        return new Response(response);
    }

    /**
     * 返回值处理相关
     */
    public static class Response{
        private String responseStr;

        private String resultStr;
        public Response(String[] response){
            responseStr = response[0];
            resultStr = response[1];
        }

        public String getResponseStr() {
            return responseStr;
        }

        public String getResultStr() {
            return resultStr;
        }
    }

    /**
     * 参数处理相关
     */
  public static class Param {
        private List<Object> paramList;

        private String requestEditorText;


        public Param(LinkedHashMap<String, Object> bodyParamMap) {
            initParam(bodyParamMap);
        }

        /**
         * 获取页面RequestEditPanel上展示的Text
         *
         * @return
         */
        public String getDefaultText() {
            String defaultText = JSON.toJSONString(paramList,SerializerFeature.PrettyFormat);
            defaultText = defaultText.replace("\t", "    ");
            return defaultText;
        }

        /**
         * 获取dubbo调用时需要的格式
         *
         * @return
         */
        public String getDubboParamString() {
            String dubboParam = getDefaultText();
            JSONArray arr = JSON.parseArray(dubboParam);
            dubboParam = JSON.toJSONString(arr);
            dubboParam = dubboParam.substring(1,dubboParam.length() - 1);
            return dubboParam;
        }

        public void setRequestEditorText(String requestEditorText) {
            this.requestEditorText = requestEditorText;
        }

        /**
         * 初始化参数
         *
         */
        private void initParam(LinkedHashMap<String, Object> linkedHashMap) {
            paramList = new LinkedList<>();
            linkedHashMap.forEach((key, value) -> {
                if(value instanceof ParamKeyValue){
                    ParamKeyValue value1 = (ParamKeyValue) value;
                    paramList.add(value1.getValue());
                }else {
                    paramList.add(value);
                }
            });
        }

    }

}
