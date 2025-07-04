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

package io.github.zjay.plugin.quickrequest.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import quickRequest.icons.PluginIcons;
import io.github.zjay.plugin.quickrequest.model.MethodType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Constant {
    public static final String I18N_PATH = "io/github/zjay/fastrequest/18n/fr";
    public static final String GRPCURL_URL = "https://github.com/fullstorydev/grpcurl";

    public static final String ROUTE = "11110000010010 11110000001111 11101011000001 11110000010100 11100000100101";

    public static final String MAIN = "11011011000001 11000011000001 11011100000001 11000001101110";

    public enum FrameworkType {
        DUBBO,
        SPRING,
        JAX_RS;
    }

    public enum JaxRsMappingConfig {
        PATH("javax.ws.rs.Path", "");
        private final String code;
        private final String methodType;

        public String getCode() {
            return code;
        }

        public String getMethodType() {
            return methodType;
        }

        JaxRsMappingConfig(String code, String methodType) {
            this.code = code;
            this.methodType = methodType;
        }
    }

    public enum DubboMethodConfig {
        DubboService("org.apache.dubbo.config.annotation.DubboService"),
        AliService("com.alibaba.dubbo.config.annotation.Service"),
        ApacheService("org.apache.dubbo.config.annotation.Service"),
        ;
        private final String code;
//        private final String methodType;

        public String getCode() {
            return code;
        }

//        public String getMethodType() {
//            return methodType;
//        }
        public static boolean exist(String target){
            for (DubboMethodConfig value : values()) {
                if(Objects.equals(value.code, target)){
                    return true;
                }
            }
            return false;
        }

        DubboMethodConfig(String code) {
            this.code = code;
//            this.methodType = methodType;
        }
    }

    public enum JaxRsMappingMethodConfig {
        GET("javax.ws.rs.GET", "GET"),
        POST("javax.ws.rs.POST", "POST"),
        DELETE("javax.ws.rs.DELETE", "DELETE"),
        PUT("javax.ws.rs.PUT", "PUT");
        private final String code;
        private final String methodType;

        public String getCode() {
            return code;
        }

        public String getMethodType() {
            return methodType;
        }

        JaxRsMappingMethodConfig(String code, String methodType) {
            this.code = code;
            this.methodType = methodType;
        }
    }

    public enum SpringMappingConfig {
        GET_MAPPING("org.springframework.web.bind.annotation.GetMapping", "GET"),
        POST_MAPPING("org.springframework.web.bind.annotation.PostMapping", "POST"),
        REQUEST_MAPPING("org.springframework.web.bind.annotation.RequestMapping", ""),
        DELETE_MAPPING("org.springframework.web.bind.annotation.DeleteMapping", "DELETE"),
        PUT_MAPPING("org.springframework.web.bind.annotation.PutMapping", "PUT"),
        PATCH_MAPPING("org.springframework.web.bind.annotation.PatchMapping", "PATCH");
        private final String code;
        private final String methodType;

        public String getCode() {
            return code;
        }

        public String getMethodType() {
            return methodType;
        }

        SpringMappingConfig(String code, String methodType) {
            this.code = code;
            this.methodType = methodType;
        }
    }

    public enum SpringControllerConfig {
        CONTROLLER("org.springframework.stereotype.Controller"),
        REST_CONTROLLER("org.springframework.web.bind.annotation.RestController");
        private final String code;

        public String getCode() {
            return code;
        }

        SpringControllerConfig(String code) {
            this.code = code;
        }
    }

    public enum JaxRsUrlParamConfig {
        PATH_PARAM("javax.ws.rs.PathParam", 1),
        QUERY_PARAM("javax.ws.rs.QueryParam", 2),
        FORM_PARAM("javax.ws.rs.FormParam", 2),
        BEAN_PARAM("javax.ws.rs.BeanParam", 2),
        HEADER_PARAM("javax.ws.rs.HeaderParam", 0),
        COOKIE_PARAM("javax.ws.rs.CookieParam", 0),
        MATRIX_PARAM("javax.ws.rs.MatrixParam", 0),
        ;
        private final String code;

        /**
         * 1- path url参数  2-url参数&拼接 3-body参数 0-不参与
         */

        private final Integer parseType;

        public String getCode() {
            return code;
        }

        public Integer getParseType() {
            return parseType;
        }

        JaxRsUrlParamConfig(String code, Integer parseType) {
            this.code = code;
            this.parseType = parseType;
        }
    }

    public enum SpringUrlParamConfig {
        PATH_VARIABLE("org.springframework.web.bind.annotation.PathVariable", 1),
        REQUEST_PARAM("org.springframework.web.bind.annotation.RequestParam", 2),
        REQUEST_BODY("org.springframework.web.bind.annotation.RequestBody", 3),
        MATRIX_VARIABLE("org.springframework.web.bind.annotation.MatrixVariable", 0),
        MODEL_ATTRIBUTE("org.springframework.web.bind.annotation.ModelAttribute", 0),
        REQUEST_HEADER("org.springframework.web.bind.annotation.RequestHeader", 0),
        REQUEST_PART("org.springframework.web.bind.annotation.RequestPart", 0),
        COOKIE_VALUE("org.springframework.web.bind.annotation.CookieValue", 0),
        SESSION_ATTRIBUTE("org.springframework.web.bind.annotation.SessionAttribute", 0),
        REQUEST_ATTRIBUTE("org.springframework.web.bind.annotation.RequestAttribute", 0);
        private final String code;

        /**
         * 1- path url参数  2-url参数&拼接 3-body参数 0-不参与
         */

        private final Integer parseType;

        public String getCode() {
            return code;
        }

        public Integer getParseType() {
            return parseType;
        }

        SpringUrlParamConfig(String code, Integer parseType) {
            this.code = code;
            this.parseType = parseType;
        }
    }

    public enum SpringParamTypeConfig {
        URL_PARAMS("URL Params"),
        JSON("JSON"),
        FORM_URL_ENCODED("Form URL-Encoded");

        private final String code;

        public String getCode() {
            return code;
        }

        SpringParamTypeConfig(String code) {
            this.code = code;
        }
    }

    public static class HttpStatusDesc {
        public static Map<Integer, String> STATUS_MAP
                = ImmutableMap.<Integer, String>builder()
                .put(0, "")
                .put(200, "OK")
                .put(201, "Created")
                .put(202, "Accepted")
                .put(203, "Non-Authoritative Information")
                .put(204, "No Content.")
                .put(205, "Reset Content")
                .put(206, "Partial Content")

                .put(300, "Multiple Choices")
                .put(301, "Moved Permanently")
                .put(302, "Temporary Redirect")
                .put(303, "See Other")
                .put(304, "Not Modified")
                .put(305, "Use Proxy")
                .put(307, "Temporary Redirect")
                .put(308, "Permanent Redirect")

                .put(400, "Bad Request")
                .put(401, "Unauthorized")
                .put(402, "Payment Required")
                .put(403, "Forbidden")
                .put(404, "Not Found")
                .put(405, "Method Not Allowed")
                .put(406, "Not Acceptable")
                .put(407, "Proxy Authentication Required")
                .put(408, "Request Time-Out")
                .put(409, "Conflict")
                .put(410, "Gone")
                .put(411, "Length Required")
                .put(412, "Precondition Failed")
                .put(413, "Request Entity Too Large")
                .put(414, "Request-URI Too Large")
                .put(415, "Unsupported Media Type")

                .put(500, "Internal Server Error")
                .put(501, "Not Implemented")
                .put(502, "Bad Gateway")
                .put(503, "Service Unavailable")
                .put(504, "Gateway Timeout")
                .put(505, "HTTP Version Not Supported")
                .build();
    }

    public static Key<Integer> KEY_QUICKREQUEST = Key.create("QuickRequest");

    public static List<String> IGNORE_PARAM_PARSE_LIST = Lists.newArrayList(
            "javax.servlet.http.HttpServletRequest",
            "javax.servlet.http.HttpServletResponse",
            "org.springframework.ui.ModelMap"
    );

    public static List<String> SUPPORTED_ANNOTATIONS = Lists.newArrayList(
            "GetMapping", "PostMapping", "RequestMapping", "DeleteMapping", "PutMapping", "PatchMapping",
            "GET", "POST", "DELETE", "PUT",
            "Service","DubboService"
    );

    public static List<String> SUPPORTED_METHODS = Lists.newArrayList(
            "get", "post", "put", "delete", "patch"
    );

    public static List<MethodType> METHOD_TYPE_LIST = Lists.newArrayList(
            new MethodType("GET", PluginIcons.ICON_GET),
            new MethodType("POST", PluginIcons.ICON_POST),
            new MethodType("PUT", PluginIcons.ICON_PUT),
            new MethodType("DELETE", PluginIcons.ICON_DELETE),
            new MethodType("PATCH", PluginIcons.ICON_PATCH),
            new MethodType("DUBBO", PluginIcons.ICON_DUBBO)
    );

    public enum AutoCompleteType{
        Header_Name,Header_value

    }

    public enum JetBrainsProductName{
        IntelliJ_IDEA("IntelliJ IDEA"),Aqua("Aqua"),
        Android_Studio("Android Studio"),Go_Land("GoLand"),
//        PhpStorm("PhpStorm")
        ;

         final String value;

        JetBrainsProductName(String value) {
            this.value = value;
        }

        public static boolean isExist(@NotNull String name){
            for (JetBrainsProductName value : values()) {
                if(name.startsWith(value.value)){
                    return true;
                }
            }
            return false;
        }

        public static boolean isButtonSupport(@NotNull String name){
            if(name.startsWith(IntelliJ_IDEA.value) || name.startsWith(Aqua.value) || name.startsWith(Android_Studio.value)){
                return true;
            }
            return false;
        }

        public String getValue() {
            return value;
        }
    }



}
