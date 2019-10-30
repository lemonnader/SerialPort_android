package com.serialportlibrary.util;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Tyhj on 2017/3/10.
 */

public class ByteStringUtil {

    //16进制的string转byte数组
    public static byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++){
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte)Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    //byte数组转16进制数字的字符串
    public static String byteArrayToHexStr(byte[] byteArray) {

        if (byteArray == null){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<byteArray.length;i++) {
            int high = ((byteArray[i]>>4) & 0x0f);// 取高4位
            int low = byteArray[i] & 0x0f;  //取低4位
            sb.append(high>9?(char)((high-10)+'A'):(char)(high+'0'));
            sb.append(low>9?(char)((low-10)+'A'):(char)(low+'0'));
        }

        return sb.toString();
    }


    //byte数组转ASCII码
    public static String convertbyteToASCII(byte[] hex){


        try {
            return new String(hex, "ascii");
        }catch (Exception e){

        }
        return new String();

    }

    public static void main(String[] args){
        System.out.println(Arrays.toString(hexStrToByteArray("55AA0100010001")));
    }

}