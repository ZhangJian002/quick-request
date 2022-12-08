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

import com.intellij.icons.AllIcons;
import zjay.icons.PluginIcons;

import javax.swing.*;

public class FrIconUtil {
    public static Icon getIconByMethodType(String methodType) {
        switch (methodType) {
            case "POST":
                return PluginIcons.ICON_POST;
            case "PUT":
                return PluginIcons.ICON_PUT;
            case "DELETE":
                return PluginIcons.ICON_DELETE;
            case "PATCH":
                return PluginIcons.ICON_PATCH;
            default:
                return PluginIcons.ICON_GET;
        }
    }

    public static Icon getIconByMethodAndClassType(String methodType, boolean isInterface) {
        if("GET".equals(methodType)){
            if(isInterface){
                return PluginIcons.ICON_GET_INTEFACE;
            }else {
                return PluginIcons.ICON_GET_CLASS;
            }
        }
        if("POST".equals(methodType)){
            if(isInterface){
                return PluginIcons.ICON_POST_INTEFACE;
            }else {
                return PluginIcons.ICON_POST_CLASS;
            }
        }
        if("PUT".equals(methodType)){
            if(isInterface){
                return PluginIcons.ICON_PUT_INTEFACE;
            }else {
                return PluginIcons.ICON_PUT_CLASS;
            }
        }
        if("DELETE".equals(methodType)){
            if(isInterface){
                return PluginIcons.ICON_DEL_INTEFACE;
            }else {
                return PluginIcons.ICON_DEL_CLASS;
            }
        }
        if(isInterface){
            return AllIcons.Nodes.Interface;
        }else {
            return AllIcons.Nodes.Class;
        }
    }
}
