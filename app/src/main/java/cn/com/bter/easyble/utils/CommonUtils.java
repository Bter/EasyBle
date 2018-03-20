package cn.com.bter.easyble.utils;

import android.content.Context;

import java.text.DecimalFormat;

/**
 * Created by admin on 2017/5/30.
 */

public class CommonUtils {
    public final static String NUM_REX = "^\\d+$";
    private static final DecimalFormat nf = new DecimalFormat("#.#");

    /**
     * 格式化小数
     * 只保留一位小数
     * 四舍五入
     * 如果小数点后为0，则只取整数
     * @param formatValue
     * @param pointerLen 小数点长度（0-4）
     * @return
     */
    public static String formatDecima(double formatValue,int pointerLen){
        if(pointerLen < 0 || pointerLen > 4){
            pointerLen = 0;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("#");
        if(pointerLen > 0){
            sb.append(".");
        }
        for(int i = 0;i < pointerLen;i++){
            sb.append("#");
        }
        nf.applyPattern(sb.toString());
        return nf.format(formatValue);
    }

    /**
     * dp转px
     *
     * @param dpValue dp值
     * @return px值
     */
    public static int dp2px(Context context,float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public static String byte2HexString(byte value){
        int v = value & 0xFF;
        String str = Integer.toHexString(v).toUpperCase();
        if (str.length() < 2) {
            str = "0"+str;
        }
        return str;
    }

    public static String bytes2HexString(byte[] data,boolean hasSpace){
        StringBuilder builder = new StringBuilder();
        if(null != data){
            for(byte value : data){
                builder.append(byte2HexString(value));
                if(hasSpace) {
                    builder.append(" ");
                }
            }
        }
        return builder.toString();
    }

    public static String byte2BitString(byte value){
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            builder.append((value >> i) & 0x01).append(" ");
        }

        return builder.toString();
    }

    /**
     * 16进制Ascii字符串转byte数组
     * @param asciiStrs
     * @return
     */
    public static byte[] hexAsciiStrs2Bytes(String asciiStrs){//AA07030009100208BE
        if(null != asciiStrs) {
            asciiStrs = asciiStrs.replaceAll("\\s","");//可以替换大部分空白字符， 不限于空格
                                                        //\s 可以匹配空格、制表符、换页符等空白字符的其中任意一个

            if(asciiStrs.length() > 0) {
                int len = asciiStrs.length() / 2;
                if (len > 0) {
                    byte[] data = new byte[len];
                    for (int i = 0, j = 0; i < len && (j + 2) <= asciiStrs.length(); i++, j += 2) {
                        data[i] = hexAscii2Byte(asciiStrs.substring(j, j + 2));
                    }
                    return data;
                /*
                int len = s.length();
                byte[] data = new byte[len / 2];
                for (int i = 0; i < len; i += 2) {
                    data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                            + Character.digit(s.charAt(i+1), 16));
                }
                * */
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     *
     * 16进制Ascii转byte
     * @param ascii
     * @return
     */
    public static byte hexAscii2Byte(String ascii){
        byte value = 0;

        if(ascii != null){
            if(ascii.length() > 0) {
                value = (byte) Integer.parseInt(ascii, 16);
            }
        }

        return value;
    }

    /**
     * int到byte[]<br\>
     * Big endian <br\>
     * 认为第一个字节是最高位字节（按照从低地址到高地址的顺序存放数据的高位字节到低位字节）<br\>
     * @param i
     * @return
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }

    /**
     * byte[]转int
     * @param bytes
     * @return
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value= 0;
        //由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift= (4 - 1 - i) * 8;
            value +=(bytes[i] & 0x000000FF) << shift;//往高位游
        }
        return value;
    }

    public static String bytesToAscii(byte[] bytes){
        StringBuilder builder = new StringBuilder();
        String str = null;
        for (int i = 0; i < bytes.length; i++) {
            str = byteToAscii(bytes[i]);
            if(null != str) {
                builder.append(str);
            }
        }
        return builder.toString();
    }

    /**
     * byte转ascii
     * @param value
     * @return
     */
    public static String byteToAscii(byte value){
        String result = null;
        if(0 <= value && value <= (byte)0x7F){
            result = new String(new byte[]{value});
        }
        return result;
    }

    /**
     * 是否为空字符串
     * @param str
     * @return
     */
    public static boolean isEmptyString(String str){
        boolean result = false;

        if(str == null || str.trim().equals("")){
            result = true;
        }

        return result;
    }
}
