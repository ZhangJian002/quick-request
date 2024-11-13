package io.github.zjay.plugin.quickrequest.util.spring;


import java.util.Objects;

public class SpringBootUtils {

    public static String getProfileFromText(String text){
        text = removeQuotes(text);
        String[] split = text.split(" ");
        int resultIndex = -1;
        for (int i = 0; i < split.length; i++){
            if (Objects.equals(split[i], "active:")){
                resultIndex = i+1;
            }
        }
        if (resultIndex != -1 && resultIndex < split.length){
            return split[resultIndex].trim();
        }
        return "";
    }

    public static String[] getPortAndContextFromText(String text){
        text = removeQuotes(text);
        String[] split = text.split(" ");
        int resultIndex = -1;
        for (int i = 0; i < split.length; i++){
            if (Objects.equals(split[i], "port(s):") && i >= 2 && Objects.equals(split[i-1], "on") && Objects.equals(split[i-2], "started")){
                resultIndex = i+1;
            }
        }
        if (resultIndex != -1 && resultIndex < split.length){
            return new String[]{split[resultIndex].trim(), getContextFromText(split)};
        }
        return null;
    }

    public static String getContextFromText(String[] split){
        int resultIndex = -1;
        for (int i = 0; i < split.length; i++){
            if (i < split.length - 2 && Objects.equals(split[i], "context") && Objects.equals(split[i+1], "path")){
                resultIndex = i+2;
            }
        }
        if (resultIndex != -1 && resultIndex < split.length){
            return split[resultIndex].trim();
        }
        return "";
    }

    public static String removeQuotes(String text){
        return text.replaceAll("[\"']", "");
    }
}
