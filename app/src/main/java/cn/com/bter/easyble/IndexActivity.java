package cn.com.bter.easyble;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by admin on 2017/10/25.
 */

public class IndexActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index_layout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        reuresPermission();
    }

    /***********************权限部分****************************/
    private static final int RC_STORAGE_AND_LOCATION = 0x121;

    private int count = 0;

    private void reuresPermission(){
        if(count < 2) {
            methodRequiresTwoPermission("必要的权限");
        }else if(count == 2){
            methodRequiresTwoPermission("最后问一次给不给权限");
        }else if(count > 2){
            finish();
        }
    }

    /**
     * 申请权限
     */
    @AfterPermissionGranted(RC_STORAGE_AND_LOCATION)
    private boolean methodRequiresTwoPermission(String title) {
        count++;
        String[] persissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (!EasyPermissions.hasPermissions(this, persissions)) {
            EasyPermissions.requestPermissions(this, title, RC_STORAGE_AND_LOCATION, persissions);
            return false;
        } else {
            //已经有权限
//            ToastUtils.showShortSafe("已经有权限");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startMainActivity();
                }
            },1000);
            return true;
        }
    }

    private void startMainActivity(){
        startActivity(new Intent(IndexActivity.this,MainActivity.class));
        finish();
    }

    /**
     * 权限结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        startMainActivity();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if(count > 2){
            finish();
            return;
        }
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this, "为了应用能正常运行请运行权限").build().show();
        }
        reuresPermission();
    }

    /***********************权限部分****************************/
}
