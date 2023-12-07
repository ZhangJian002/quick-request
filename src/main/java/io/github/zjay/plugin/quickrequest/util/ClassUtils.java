package io.github.zjay.plugin.quickrequest.util;

public class ClassUtils {

    private static String apacheMath3 = "org.apache.commons.math3.stat.descriptive.StatisticalSummary";

    public static boolean existMath3Class(){
        try {
            Class.forName(apacheMath3);
            return true;
        } catch (ClassNotFoundException e) {
            //not exist
        }
        return false;
    }
}
