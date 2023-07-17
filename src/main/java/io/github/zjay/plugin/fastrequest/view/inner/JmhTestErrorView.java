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

package io.github.zjay.plugin.fastrequest.view.inner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListTableModel;
import io.github.zjay.plugin.fastrequest.model.DataMapping;
import io.github.zjay.plugin.fastrequest.model.HeaderGroup;
import io.github.zjay.plugin.fastrequest.util.MyResourceBundleUtil;
import io.github.zjay.plugin.fastrequest.view.component.MyLanguageTextField;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class JmhTestErrorView extends DialogWrapper {
    private final List<String> errorList;

    public JmhTestErrorView(List<String> errorList) {
        super(false);
        this.errorList = errorList;
        init();
        setTitle(MyResourceBundleUtil.getKey("JmhErrorDialog"));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        Map<String, Long> map = errorList.stream().collect(Collectors.groupingBy(x -> x, Collectors.counting()));
        Iterator<Map.Entry<String, Long>> iterator = map.entrySet().iterator();
        List<MyError> errors = new ArrayList<>();
        while (iterator.hasNext()){
            Map.Entry<String, Long> next = iterator.next();
            errors.add(new MyError(next.getKey(), next.getValue()));
        }
        ColumnInfo<Object, Object>[] columns = getColumns(Lists.newArrayList("Error Message", "Times"));

        ListTableModel<MyError> model = new ListTableModel<>(columns, errors);
        JBTable table = new JBTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Object getValueAt(int row, int column) {
                MyError error = errors.get(row);
                if(column == 0){
                    return error.getErrorMessage();
                }else if(column == 1){
                    return error.getTimes();
                }
                return super.getValueAt(row, column);
            }
        };
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.setRowHeight(35);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        decorator.setMoveUpAction(null).setMoveDownAction(null).setAddAction(null).setRemoveAction(null);
        decorator.setPreferredSize(new Dimension(500, -1));
        return JBUI.Panels.simplePanel()
                .withPreferredSize(500, 300)
                .addToCenter(decorator.createPanel());
    }

    private ColumnInfo<Object, Object>[] getColumns(List<String> titleList) {
        ColumnInfo<Object, Object>[] columns = new ColumnInfo[titleList.size()];
        for (int i = 0; i < titleList.size(); i++) {
            ColumnInfo<Object, Object> envColumn = new ColumnInfo<>(titleList.get(i)) {
                @Override
                public @Nullable Object valueOf(Object o) {
                    return o;
                }
            };

            columns[i] = envColumn;
        }
        return columns;
    }

    static class MyError{
        private String errorMessage;
        private Long times;
        public MyError(String errorMessage, Long times){
            this.errorMessage = errorMessage;
            this.times = times;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public Long getTimes() {
            return times;
        }

        public void setTimes(Long times) {
            this.times = times;
        }
    }

}
