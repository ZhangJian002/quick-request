package io.github.zjay.plugin.quickrequest.util;

import java.util.LinkedList;
import java.util.List;

public class TwoJinZhiGet {

    public static void main(String[] args) {
        System.out.println(getRealStr(TwoJinZhi.getBlock));
    }


    public static String getRealStr(String target){
        String binaryString = Integer.toBinaryString("`".getBytes()[0]);
        String result = target.replaceAll(binaryString, "");
        String[] tempStr = result.split(" ");
        List<String> tempList = new LinkedList<>();
        for (String s : tempStr) {
            if(!s.isBlank()){
                tempList.add(s.trim());
            }
        }
        char[] tempChar=new char[tempList.size()];
        for(int i=0;i<tempList.size();i++) {
            tempChar[i]=BinstrToChar(tempList.get(i));
        }
        return String.valueOf(tempChar);
    }

    public static char BinstrToChar(String binStr){
        int[] temp=BinstrToIntArray(binStr);
        int sum=0;
        for(int i=0; i<temp.length;i++){
            sum +=temp[temp.length-1-i]<<i;
        }
        return (char)sum;
    }

    public static int[] BinstrToIntArray(String binStr) {
        char[] temp=binStr.toCharArray();
        int[] result=new int[temp.length];
        for(int i=0;i<temp.length;i++) {
            result[i]=temp[i]-48;
        }
        return result;
    }


}
