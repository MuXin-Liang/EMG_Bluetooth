package com.lambort.emg_bluetooth_openbci;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //加载启动界面
        setContentView(R.layout.activity_launch);
        //初始化SaveLab
        Save_Lab.get(this);
        Integer time = 1000;    //设置等待时间，单位为毫秒
        Handler handler = new Handler();
        //当计时结束时，跳转至主界面
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LaunchActivity.this, MainActivity.class));
                LaunchActivity.this.finish();
            }
        }, time);
    }
}
