package io.github.zjay.plugin.fastrequest.util;


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
                    contentType = "application/json";
                    break;
                case '<':
                    // XML请求体
                    contentType = "application/xml";
                    break;
                default:
                    // 请求体
                    contentType = "application/x-www-form-urlencoded";
                    break;
            }
        }
        return contentType;
    }
}
