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

package io.github.zjay.plugin.fastrequest.parse;

import com.alibaba.fastjson.JSONObject;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import io.github.zjay.plugin.fastrequest.dubbo.SupportType;
import io.github.zjay.plugin.fastrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.fastrequest.model.ParamKeyValue;
import io.github.zjay.plugin.fastrequest.util.TypeUtil;

import java.util.*;
import java.util.stream.Collectors;

public class DubboParamParse  {

    public LinkedHashMap<String, Object> parseParam(FastRequestConfiguration config, PsiParameterList paramNameTypeList) {
        Map map = new HashMap<>();
        LinkedHashMap<String, Object> targetMap = new LinkedHashMap<>();
        for (PsiParameter parameter : paramNameTypeList.getParameters()) {
            SupportType supportType = SupportType.touch(parameter);
            Object value = supportType.getRandomValue(parameter, map);
            TypeUtil.Type type;
            switch (supportType){
                case LIST:
                    type = TypeUtil.Type.Array;
                    break;
                case DATE:
                case STRING:
                    type = TypeUtil.Type.String;
                    break;
                case BOOLEAN:
                    type = TypeUtil.Type.Boolean;
                    break;
                case INTEGER:
                case CHAR:
                case FLOAT:
                    type = TypeUtil.Type.Number;
                    break;
                default:
                    type = TypeUtil.Type.Object;
                    break;
            }
            if(type != TypeUtil.Type.String){
                value = JSONObject.toJSONString(value);
            }
            targetMap.put(parameter.getName(), new ParamKeyValue(parameter.getName(), value, 2, type.name()));
        }
        return targetMap;
    }
}
