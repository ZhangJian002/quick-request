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
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import io.github.zjay.plugin.fastrequest.model.HeaderGroup;
import io.github.zjay.plugin.fastrequest.util.MyResourceBundleUtil;
import io.github.zjay.plugin.fastrequest.view.component.MyLanguageTextField;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SyncView extends DialogWrapper {
    private Map<String, String> valueMap = new HashMap<>();

    private JTextField gitUrlText;

    private JTextField userNameText;

    private JPasswordField passwordText;

    private static final String GIT_URL= "git_url";
    private static final String USER_NAME= "user_name";
    private static final String PASSWORD= "password";

    public SyncView() {
        super(false);
        init();
        setTitle(MyResourceBundleUtil.getKey("header.group.manage"));
    }

    public Map<String, String> changeAndGet() {
        valueMap.put(GIT_URL, gitUrlText.getText());
        valueMap.put(USER_NAME, userNameText.getText());
        valueMap.put(PASSWORD, Arrays.toString(passwordText.getPassword()));
        return this.valueMap;
    }


    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        JLabel gitUrl = new JLabel("Repository");

        JLabel userName = new JLabel(MyResourceBundleUtil.getKey("UserName"));

        JLabel password = new JLabel(MyResourceBundleUtil.getKey("Password"));

        gitUrlText = new JTextField();

        userNameText = new JTextField();

        passwordText = new JPasswordField();

        gitUrlText.setPreferredSize(new Dimension(550, 30));

        userNameText.setPreferredSize(new Dimension(550, 30));

        passwordText.setPreferredSize(new Dimension(550, 30));

        constraints.gridx = 0;

        constraints.gridy = 0;

        panel.add(gitUrl, constraints);

        constraints.gridx = 1;

        panel.add(gitUrlText, constraints);

        constraints.gridx = 0;

        constraints.gridy = 1;

        panel.add(userName, constraints);

        constraints.gridx = 1;

        panel.add(userNameText, constraints);

        constraints.gridx = 0;

        constraints.gridy = 2;

        panel.add(password, constraints);

        constraints.gridx = 1;

        panel.add(passwordText, constraints);

//        constraints.gridx = 0;
//
//        constraints.gridy = 2;
//
//        constraints.gridwidth = 2;
//
//        constraints.fill = GridBagConstraints.BOTH;
//        panel.setPreferredSize(new Dimension(400, 300));




        return JBUI.Panels.simplePanel()
                .withPreferredSize(600, 400)
                .addToCenter(panel);
    }

    protected ValidationInfo doValidate() {
        if(StringUtils.isEmpty(gitUrlText.getText())){
            return new ValidationInfo("Please enter repository name");
        }
        if(StringUtils.isBlank(userNameText.getText())){
            return new ValidationInfo("Please enter your user name");
        }
        if(passwordText.getPassword().length == 0){
            return new ValidationInfo("Please enter your password");
        }
        return super.doValidate();
    }

}
