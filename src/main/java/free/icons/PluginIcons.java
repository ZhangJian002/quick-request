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

package free.icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface PluginIcons {
    //light:#515151 dark:#BFBFBF
    Icon fastRequest = IconLoader.getIcon("/free.icon/fastRequest.svg", PluginIcons.class);
    Icon fastRequest_editor = IconLoader.getIcon("/free.icon/fastRequest_editor.svg", PluginIcons.class);
    Icon fastRequest_toolwindow = IconLoader.getIcon("/free.icon/fastRequest_toolwindow.svg", PluginIcons.class);

    Icon ICON_ARRAY = IconLoader.getIcon("/free.icon/array.svg", PluginIcons.class);
    Icon ICON_BOOLEAN = IconLoader.getIcon("/free.icon/boolean.svg", PluginIcons.class);
    Icon ICON_NUMBER = IconLoader.getIcon("/free.icon/number.svg", PluginIcons.class);
    Icon ICON_OBJECT = IconLoader.getIcon("/free.icon/object.svg", PluginIcons.class);
    Icon ICON_STRING = IconLoader.getIcon("/free.icon/string.svg", PluginIcons.class);
    Icon ICON_FILE = IconLoader.getIcon("/free.icon/file.svg", PluginIcons.class);

    Icon ICON_SEND = IconLoader.getIcon("/free.icon/send.svg", PluginIcons.class);
    Icon ICON_SEND_MINI = IconLoader.getIcon("/free.icon/send-mini.svg", PluginIcons.class);
    Icon ICON_SEND_DOWNLOAD = IconLoader.getIcon("/free.icon/send_download.svg", PluginIcons.class);

    Icon ICON_VISIBLE = IconLoader.getIcon("/free.icon/visible.svg", PluginIcons.class);
    Icon ICON_INVISIBLE = IconLoader.getIcon("/free.icon/invisible.svg", PluginIcons.class);

    Icon ICON_EXPAND = IconLoader.getIcon("/free.icon/expand.svg", PluginIcons.class);
    Icon ICON_COLLAPSE = IconLoader.getIcon("/free.icon/collapse.svg", PluginIcons.class);

    Icon ICON_NAVIGATE = IconLoader.getIcon("/free.icon/navigate.svg", PluginIcons.class);
    Icon ICON_CONFIG = IconLoader.getIcon("/free.icon/config.svg", PluginIcons.class);

    Icon ICON_SAVE = IconLoader.getIcon("/free.icon/save.svg", PluginIcons.class);
    Icon ICON_CURL = IconLoader.getIcon("/free.icon/curl.svg", PluginIcons.class);
    Icon ICON_DOC = IconLoader.getIcon("/free.icon/readme.svg", PluginIcons.class);
    Icon ICON_COFFEE = IconLoader.getIcon("/free.icon/coffee.svg", PluginIcons.class);
    Icon ICON_RETRY = IconLoader.getIcon("/free.icon/retry.svg", PluginIcons.class);
    Icon ICON_FILTER = IconLoader.getIcon("/free.icon/filter.svg", PluginIcons.class);

    Icon ICON_CONTEXT_HELP = IconLoader.getIcon("/free.icon/contextHelp.svg", PluginIcons.class);


    Icon ICON_GET = IconLoader.getIcon("/free.icon/get.svg", PluginIcons.class);
    Icon ICON_POST = IconLoader.getIcon("/free.icon/post.svg", PluginIcons.class);
    Icon ICON_PUT = IconLoader.getIcon("/free.icon/put.svg", PluginIcons.class);
    Icon ICON_DELETE = IconLoader.getIcon("/free.icon/delete.svg", PluginIcons.class);
    Icon ICON_PATCH = IconLoader.getIcon("/free.icon/patch.svg", PluginIcons.class);
    Icon ICON_CODE = IconLoader.getIcon("/free.icon/code.svg", PluginIcons.class);

    Icon ICON_GET_INTEFACE = IconLoader.getIcon("/free.icon/get-interface.svg", PluginIcons.class);
    Icon ICON_POST_INTEFACE = IconLoader.getIcon("/free.icon/post-interface.svg", PluginIcons.class);
    Icon ICON_PUT_INTEFACE = IconLoader.getIcon("/free.icon/put-interface.svg", PluginIcons.class);
    Icon ICON_DEL_INTEFACE = IconLoader.getIcon("/free.icon/del-interface.svg", PluginIcons.class);

    Icon ICON_GET_CLASS = IconLoader.getIcon("/free.icon/get-class.svg", PluginIcons.class);
    Icon ICON_POST_CLASS = IconLoader.getIcon("/free.icon/post-class.svg", PluginIcons.class);
    Icon ICON_PUT_CLASS = IconLoader.getIcon("/free.icon/put-class.svg", PluginIcons.class);
    Icon ICON_DEL_CLASS = IconLoader.getIcon("/free.icon/del-class.svg", PluginIcons.class);



    Icon NOTIFICATIONS_NEW = IconLoader.getIcon("/free.icon/notificationsNew.svg", PluginIcons.class);

    Icon ICON_POSTMAN = IconLoader.getIcon("/free.icon/postman.svg", PluginIcons.class);
    Icon ICON_LOCAL_SCOPE = IconLoader.getIcon("/free.icon/localScope.svg", PluginIcons.class);
    Icon ICON_LOCAL_SCOPE_LARGE = IconLoader.getIcon("/free.icon/localScope-large.svg", PluginIcons.class);

    Icon ICON_CLEAR = IconLoader.getIcon("/free.icon/clear.svg", PluginIcons.class);

    Icon ICON_REDO = IconLoader.getIcon("/free.icon/redo.svg", PluginIcons.class);

    Icon ICON_SYNC = IconLoader.getIcon("/free.icon/sync.svg", PluginIcons.class);
}