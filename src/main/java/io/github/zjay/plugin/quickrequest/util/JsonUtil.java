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

import org.apache.commons.lang3.StringUtils;

public class JsonUtil {
    public static boolean isJson(String str) {
        return isJsonObj(str) || isJsonArray(str);
    }

    public static boolean isJsonObj(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return isWrap(StringUtils.trim(str), '{', '}');
    }

    public static boolean isJsonArray(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return isWrap(StringUtils.trim(str), '[', ']');
    }

    public static boolean isWrap(CharSequence str, char prefixChar, char suffixChar) {
        if (null == str) {
            return false;
        }

        return str.charAt(0) == prefixChar && str.charAt(str.length() - 1) == suffixChar;
    }

}
