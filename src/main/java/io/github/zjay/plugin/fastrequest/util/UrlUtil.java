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

package io.github.zjay.plugin.fastrequest.util;


import org.apache.http.util.Asserts;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtil {

    public static final String CLASSPATH_URL_PREFIX = "classpath:";

    public static List<String> paramPathParam(String url) {
        List<String> result = new ArrayList<>();
        Pattern p = Pattern.compile("\\{([^}]*)}");
        Matcher m = p.matcher(url);
        while (m.find()) {
            result.add(m.group(1));
        }
        return result;
    }

    public static boolean isURL(String url) {
        try {
            url = (url.startsWith("http://") || url.startsWith("https://")) ? url : "http://" + url;
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isHttpURL(String url) {
        try {
            new URL(url).toURI();
            return  (url.startsWith("http://") || url.startsWith("https://"));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDubboURL(String url) {
        try {
            return !url.startsWith("http") && url.split(":").length > 1;
        } catch (Exception e) {
            return false;
        }
    }

    public static URL url(String url) {
        return url(url, null);
    }

    public static URL url(String url, URLStreamHandler handler) {
        Asserts.notNull(url, "URL");

        // 兼容Spring的ClassPath路径
        if (url.startsWith(CLASSPATH_URL_PREFIX)) {
            url = url.substring(CLASSPATH_URL_PREFIX.length());
            return getClassLoader().getResource(url);
        }

        try {
            return new URL(null, url, handler);
        } catch (MalformedURLException e) {
            // 尝试文件路径
            try {
                return new File(url).toURI().toURL();
            } catch (MalformedURLException ex2) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ClassLoader getClassLoader() {
        ClassLoader classLoader = getContextClassLoader();
        if (classLoader == null) {
            classLoader = UrlUtil.class.getClassLoader();
            if (null == classLoader) {
                classLoader = getSystemClassLoader();
            }
        }
        return classLoader;
    }

    public static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            // 绕开权限检查
            return AccessController.doPrivileged(
                    (PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader());
        }
    }

    public static ClassLoader getSystemClassLoader() {
        if (System.getSecurityManager() == null) {
            return ClassLoader.getSystemClassLoader();
        } else {
            // 绕开权限检查
            return AccessController.doPrivileged(
                    (PrivilegedAction<ClassLoader>) ClassLoader::getSystemClassLoader);
        }
    }
}
