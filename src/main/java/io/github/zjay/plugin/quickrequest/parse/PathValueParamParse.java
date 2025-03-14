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

package io.github.zjay.plugin.quickrequest.parse;

import com.intellij.psi.PsiClass;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.PsiUtil;
import io.github.zjay.plugin.quickrequest.model.DataMapping;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.model.ParamKeyValue;
import io.github.zjay.plugin.quickrequest.model.ParamNameType;
import io.github.zjay.plugin.quickrequest.util.KV;
import io.github.zjay.plugin.quickrequest.util.StringUtils;
import io.github.zjay.plugin.quickrequest.util.TypeUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class PathValueParamParse extends AbstractParamParse {
    @Override
    public LinkedHashMap<String, Object> parseParam(FastRequestConfiguration config, List<ParamNameType> paramNameTypeList, PsiClass numberClass) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        List<DataMapping> customDataMappingList = config.getCustomDataMappingList();
        List<DataMapping> defaultDataMappingList = config.getDefaultDataMappingList();
        List<ParamNameType> pathParamList = paramNameTypeList.stream().filter(q -> q.getParseType() == 1).collect(Collectors.toList());
        int randomStringLength = config.getRandomStringLength();
        String randomStringStrategy = config.getRandomStringStrategy();
        String randomStringDelimiter = config.getRandomStringDelimiter();
        for (ParamNameType paramNameType : pathParamList) {
            String name = paramNameType.getName();
            String type = paramNameType.getType();
            //random String
            if ("java.lang.String".equals(type)) {
                ParamKeyValue value = new ParamKeyValue(name, StringUtils.randomString(name,randomStringDelimiter,randomStringLength,randomStringStrategy), 2, TypeUtil.Type.String.name());
                result.put(name, value);
                continue;
            } else if (numberClass != null && (paramNameType.getPsiClass().isInheritor(numberClass, true) || paramNameType.getPsiClass() == numberClass)) {
                result.put(name, new ParamKeyValue(name, ThreadLocalRandom.current().nextInt(0, 101), 2, TypeUtil.Type.Number.name()));
                continue;
            }
            //pathValueParam 不支持自定义类解析 只能是基础类型
//            DataMapping dataMapping = customDataMappingList.stream().filter(q -> type.equals(q.getType())).findFirst().orElse(null);
//            if (dataMapping != null) {
//                Object value = dataMapping.getValue();
//                result.put(name, value);
//                continue;
//            }
            DataMapping dataMapping = defaultDataMappingList.stream().filter(q -> type.equals(q.getType())).findFirst().orElse(null);
            if (dataMapping != null) {
                Object value = dataMapping.getValue();
                String calcType = TypeUtil.calcType(type);
                result.put(name, new ParamKeyValue(name, value, 2, calcType));
            } else {
                //匹配不到默认匹配string

                PsiClass psiClass = PsiUtil.resolveClassInClassTypeOnly(PsiTypesUtil.getClassType(paramNameType.getPsiClass()).getDeepComponentType());
                boolean isEnum = psiClass != null && psiClass.isEnum();
                if (isEnum) {

                    KV kv = KV.getFields(paramNameType.getPsiClass(), numberClass);
                    Object enumParamKeyValue = kv.values().stream().findFirst().orElse(null);
                    if (enumParamKeyValue != null) {
                        result.put(name, new ParamKeyValue(name, ((ParamKeyValue) enumParamKeyValue).getValue(), 2, TypeUtil.Type.String.name()));
                    }
                    continue;
                }

                result.put(name, new ParamKeyValue(name, StringUtils.randomString(name, randomStringDelimiter, randomStringLength, randomStringStrategy), 2, TypeUtil.Type.String.name()));
            }
        }
        return result;
    }
}
