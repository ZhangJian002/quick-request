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

package io.github.zjay.plugin.quickrequest.util;


import java.util.concurrent.ThreadLocalRandom;

public class StringUtils {

    public static final String BASE_NUMBER = "0123456789";
    /**
     * 用于随机选的字符
     */
    public static final String BASE_CHAR = "abcdefghijklmnopqrstuvwxyz";
    /**
     * 用于随机选的字符和数字
     */
    public static final String BASE_CHAR_NUMBER = BASE_CHAR + BASE_NUMBER;

    public static String randomString(String name, String delimiter, int length, String mode) {
        //不生成
        if ("none".equals(mode)) {
            return "";
        } else if ("name+random".equals(mode)) {
            //字段名+随机
            if(length < 1){
                return name;
            }
            return name + delimiter + randomString(BASE_CHAR_NUMBER, length);
        } else {
            //随机
            return randomString(BASE_CHAR_NUMBER, length);
        }
    }

    public static String randomString(String baseString, int length) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(baseString)) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }
        final StringBuilder sb = new StringBuilder(length);

        if (length < 1) {
            length = 1;
        }
        int baseLength = baseString.length();
        for (int i = 0; i < length; i++) {
            int number = ThreadLocalRandom.current().nextInt(baseLength);
            sb.append(baseString.charAt(number));
        }
        return sb.toString();
    }

    public static <T> T defaultIfNull(final T object, final T defaultValue) {
        return (null != object) ? object : defaultValue;
    }
}
