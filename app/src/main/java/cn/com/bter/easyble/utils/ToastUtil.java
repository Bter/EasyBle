package cn.com.bter.easyble.utils;

import android.content.Context;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.widget.Toast;

public class ToastUtil {
    private static Toast mToast;
    public static void init(Context context){
        if(mToast == null && context != null){
            mToast = Toast.makeText(context.getApplicationContext(),"",Toast.LENGTH_SHORT);
        }
    }

    public static void show(@StringRes int resId){
        if(resId != -1){
            mToast.setText(resId);
            mToast.show();
        }
    }

    public static void show(String content){
        if(!TextUtils.isEmpty(content)){
            mToast.setText(content);
            mToast.show();
        }
    }
}
