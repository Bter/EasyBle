package cn.com.bter.easyble.easyblelib.utils;

import android.util.Log;

/**
 * TAG 可为null或""，但此时无法打印出来
 * msg 不可为null会报异常，当为""时无法打印出来
 * Created by admin on 2017/10/19.
 */

public class LogUtil {
    private static final String TAG = LogUtil.class.getSimpleName();
    private static boolean isEnable = true;

    public static void enAble(){
        isEnable = true;
    }

    public static boolean isEnable(){
        return isEnable;
    }

    public static void disAble(){
        isEnable = false;
    }

    public static void i(String msg){
        i(TAG,msg);
    }
    public static void v(String msg){
        v(TAG,msg);
    }
    public static void d(String msg){
        d(TAG,msg);
    }
    public static void w(String msg){
        w(TAG,msg);
    }
    public static void e(String msg){
        e(TAG,msg);
    }

    public static void i(String tag,String msg){
        if(isEnable)
            Log.i(tag,""+msg);
    }
    public static void v(String tag,String msg){
        if(isEnable)
            Log.v(tag,""+msg);
    }
    public static void d(String tag,String msg){
        if(isEnable)
            Log.d(tag,""+msg);
    }
    public static void w(String tag,String msg){
        if(isEnable)
            Log.w(tag,""+msg);
    }
    public static void e(String tag,String msg){
        if(isEnable)
            Log.e(tag,""+msg);
    }
}
