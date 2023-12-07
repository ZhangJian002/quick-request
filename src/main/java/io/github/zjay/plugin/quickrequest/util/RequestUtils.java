package io.github.zjay.plugin.quickrequest.util;


import org.apache.commons.lang3.StringUtils;

public class RequestUtils {

    public static String get(String body) {
        String contentType = null;
        if (StringUtils.isNotBlank(body)) {
            char firstChar = body.charAt(0);
            switch (firstChar) {
                case '{':
                case '[':
                    // JSON请求体
                    contentType = "application/json;charset=UTF-8";
                    break;
                case '<':
                    // XML请求体
                    contentType = "application/xml;charset=UTF-8";
                    break;
                default:
                    // 请求体
                    contentType = "application/x-www-form-urlencoded;charset=UTF-8";
                    break;
            }
        }
        return contentType;
    }
}
