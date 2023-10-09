package io.github.zjay.plugin.fastrequest.dubbo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiClassImplUtil;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import io.github.zjay.plugin.fastrequest.util.random.RandomStringUtils;
import io.github.zjay.plugin.fastrequest.util.random.RandomUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public enum SupportType {


    BOOLEAN {
        @Override
        Boolean getRandomValue(PsiVariable psiVariable) {
            return RandomUtils.nextBoolean();
        }

        @Override
        Boolean getValue(PsiVariable psiVariable, Map<String, String> defaultValueMap) {
            String defaultValue = defaultValueMap.get(psiVariable.getName());
            if(defaultValue != null){
                return Boolean.valueOf(defaultValue);
            }
            return getRandomValue(psiVariable);
        }
    },
    CHAR {
        @Override
        Character getRandomValue(PsiVariable psiVariable) {
            return RandomStringUtils.randomAlphabetic(1).charAt(0);
        }

        @Override
        Character getValue(PsiVariable psiVariable, Map<String, String> defaultValueMap) {
            String defaultValue = defaultValueMap.get(psiVariable.getName());
            if(defaultValue != null){
                return defaultValue.charAt(0);
            }
            return getRandomValue(psiVariable);
        }
    },
    INTEGER {
        @Override
        Integer getRandomValue(PsiVariable psiVariable) {
            return RandomUtils.nextInt(10000);
        }

        @Override
        Integer getValue(PsiVariable psiVariable, Map<String, String> defaultValueMap) {
            String defaultValue = defaultValueMap.get(psiVariable.getName());
            try {
                return Integer.valueOf(defaultValue);
            }catch (Exception e){
                return getRandomValue(psiVariable);
            }
        }
    },
    FLOAT {
        @Override
        Float getRandomValue(PsiVariable psiVariable) {
            return RandomUtils.nextFloat();
        }

        @Override
        Float getValue(PsiVariable psiVariable, Map<String, String> defaultValueMap) {
            String defaultValue = defaultValueMap.get(psiVariable.getName());
            try {
                return Float.valueOf(defaultValue);
            }catch (Exception e){
                return getRandomValue(psiVariable);
            }
        }
    },
    STRING {
        @Override
        String getRandomValue(PsiVariable psiVariable) {
            return RandomStringUtils.randomAlphabetic(10);
        }

        @Override
        String getValue(PsiVariable psiVariable, Map<String, String> defaultValueMap) {
            String defaultValue = defaultValueMap.get(psiVariable.getName());
            if(defaultValue != null){
                return defaultValue;
            }
            return getRandomValue(psiVariable);
        }
    },
    LIST {
        @Override
        List getRandomValue(PsiVariable psiVariable) {
            return new ArrayList(0);
        }

        @Override
        List getValue(PsiVariable psiVariable, Map<String, String> defaultValueMap) {
            String defaultValue = defaultValueMap.get(psiVariable.getName());
            if(defaultValue != null){
                try{
                    return JSON.parseArray(defaultValue);
                }catch (Exception e){}
            }
            return getRandomValue(psiVariable);
        }
    },
    MAP {
        @Override
        Map getRandomValue(PsiVariable psiVariable) {
            return new HashMap(0);
        }

        @Override
        Map getValue(PsiVariable psiVariable, Map<String, String> defaultValueMap) {
            String defaultValue = defaultValueMap.get(psiVariable.getName());
            if(defaultValue != null){
                try{
                    return JSON.parseObject(defaultValue);
                }catch (Exception e){}
            }
            return getRandomValue(psiVariable);
        }
    },
    DATE {
        @Override
        Date getRandomValue(PsiVariable psiVariable) {
            return new Date();
        }

        @Override
        Date getValue(PsiVariable psiVariable, Map<String, String> defaultValueMap) {
            String defaultValue = defaultValueMap.get(psiVariable.getName());
            if(defaultValue != null){
                try{
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return simpleDateFormat.parse(defaultValue);
                }catch (Exception e){}
            }
            return getRandomValue(psiVariable);
        }
    },

    OTHER {
        @Override
        JSONObject getRandomValue(PsiVariable psiVariable) {
            PsiClass psiClass = JavaPsiFacade.getInstance(psiVariable.getProject()).findClass(psiVariable.getType().getCanonicalText(), new ProjectAndLibrariesScope(psiVariable.getProject()));
            return this.obj2Map(psiClass);
        }

        @Override
        Map getValue(PsiVariable psiVariable, Map<String, String> defaultValueMap) {
            String defaultValue = defaultValueMap.get(psiVariable.getName());
            JSONObject docValue = null;
            if(defaultValue != null){
                try{
                    docValue = JSON.parseObject(defaultValue);
                }catch (Exception e){}
            }

            JSONObject randomValue = getRandomValue(psiVariable);
            return mergeJson(randomValue, docValue).getInnerMap();
        }

        public JSONObject mergeJson(JSONObject object1,JSONObject object2){
            if (object1 == null &&object2 == null){
                return null;
            }
            if (object1 == null){
                return object2;
            }
            if (object2 == null){
                return object1;
            }
            Iterator iterator = object2.keySet().iterator();
            while (iterator.hasNext()){
                String key = (String) iterator.next();
                Object value2 = object2.get(key);
                if (object1.containsKey(key)){
                    Object value1 = object1.get(key);

                    if (value1 instanceof JSONObject && value2 instanceof JSONObject){
                        object1.put(key,mergeJson((JSONObject) value1, (JSONObject) value2));
                    }else {
                        object1.put(key,value2);
                    }
                }else {
                    object1.put(key,value2);
                }
            }
            return object1;
        }

        private JSONObject obj2Map(PsiClass psiClass) {
            PsiField[] allField = PsiClassImplUtil.getAllFields(psiClass);
            JSONObject result = new JSONObject(allField.length);

            for (PsiField psiField : allField) {
                if(psiField.getModifierList().hasModifierProperty("static")
                        || psiField.getModifierList().hasModifierProperty("final")
                        ){
                    //TODO
                    continue;
                }
                SupportType supportType = SupportType.touch(psiField);

                if (supportType == SupportType.OTHER) {
                    PsiClass subPsiClass = JavaPsiFacade.getInstance(psiClass.getProject()).findClass(psiField.getType().getCanonicalText(), new ProjectAndLibrariesScope(psiClass.getProject()));
                    if(subPsiClass != null && psiClass != subPsiClass){
                        result.put(psiField.getName(), obj2Map(subPsiClass));
                    }
                } else {
                    result.put(psiField.getName(), supportType.getRandomValue(psiField));
                }

            }
            return result;
        }
    },

    ENUM {
        @Override
        String getRandomValue(PsiVariable psiVariable) {
            return ((PsiClassReferenceType)psiVariable.getType()).rawType().resolve().getFields()[0].getName();
        }

        @Override
        String getValue(PsiVariable psiVariable, Map<String, String> defaultValueMap) {
            String defaultValue = defaultValueMap.get(psiVariable.getName());
            if(defaultValue != null){
                return defaultValue;
            }
            return getRandomValue(psiVariable);
        }
    }
    ;

    abstract Object getRandomValue(PsiVariable psiVariable);

    abstract Object getValue(PsiVariable psiVariable, Map<String, String> defaultValueMap);

    public static SupportType touch(PsiVariable parameter) {
        PsiType type = parameter.getType();
        String typeStr = parameter.getType().getCanonicalText();

        if ("boolean".equals(typeStr) || "java.lang.Boolean".equals(typeStr)) {
            return SupportType.BOOLEAN;
        }

        if ("char".equals(typeStr) || "java.lang.Character".equals(typeStr)) {
            return SupportType.CHAR;
        }

        if ("byte".equals(typeStr) || "java.lang.Byte".equals(typeStr)
                || "int".equals(typeStr) || "java.lang.Integer".equals(typeStr)
                || "long".equals(typeStr) || "java.lang.Long".equals(typeStr)
                || "short".equals(typeStr) || "java.lang.Short".equals(typeStr)
                || type.equalsToText(Integer.class.getCanonicalName())
                || type.equalsToText(Long.class.getCanonicalName())
                || type.equalsToText(Short.class.getCanonicalName())
                || type.equalsToText(Byte.class.getCanonicalName())) {
            return SupportType.INTEGER;
        }

        if ("double".equals(typeStr) || "java.lang.Double".equals(typeStr)
                || "float".equals(typeStr) || "java.lang.Float".equals(typeStr)
                || type.equalsToText(Double.class.getCanonicalName())
                || type.equalsToText(Float.class.getCanonicalName())) {
            return SupportType.FLOAT;
        }

        if (type.equalsToText(String.class.getCanonicalName())) {
            return SupportType.STRING;
        }

        if (type.equalsToText(Date.class.getCanonicalName())) {
            return SupportType.DATE;
        }

        if (type.getCanonicalText().startsWith(List.class.getCanonicalName())
                || type instanceof PsiArrayType) {
            return SupportType.LIST;
        }

        if (type.getCanonicalText().startsWith(Map.class.getCanonicalName())) {
            return SupportType.MAP;
        }

        if (type.getSuperTypes().length > 0 && type.getSuperTypes()[0].getCanonicalText().contains(Enum.class.getCanonicalName())){
            return SupportType.ENUM;
        }

        return SupportType.OTHER;
    }

    public  Object getRandomValue(PsiVariable psiVariable, Map<String, String> defaultValueMap){
        return getValue(psiVariable, defaultValueMap);
    }
}
