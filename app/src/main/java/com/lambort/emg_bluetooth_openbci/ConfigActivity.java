package com.lambort.emg_bluetooth_openbci;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ConfigActivity extends Activity {
    public static String UserName = "";
    //信号最值
    public static int MaxValue = 1024;
    //FFT点数
    public static int FFTpoints = 128;
    //采样频率
    public static int fs = 250;
    //滑窗长度
    public static int window_len = 128;

    private Button bt_sure;
    private Button bt_report;

    private EditText et_username;
    private EditText et_fftpoints;
    private EditText et_maxvlaue;
    private EditText et_fs;
    private EditText et_window_len;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        et_fftpoints = (EditText)findViewById(R.id.et_fftpoints);
        et_maxvlaue = (EditText)findViewById(R.id.et_maxvalue);
        et_username = (EditText)findViewById(R.id.et_username);
        et_fs = (EditText)findViewById(R.id.et_fs);
        et_window_len = (EditText) findViewById(R.id.et_window_len);

        et_username.setText(UserName);
        et_fftpoints.setText(String.valueOf(FFTpoints));
        et_maxvlaue.setText(String.valueOf(MaxValue));
        et_fs.setText(String.valueOf(fs));
        et_window_len.setText(String.valueOf(window_len));

        bt_sure = (Button)findViewById(R.id.bt_config_sure);
        bt_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserName = et_username.getText().toString();
                FFTpoints = Integer.parseInt(et_fftpoints.getText().toString());
                MaxValue = Integer.parseInt(et_maxvlaue.getText().toString());
                window_len = Integer.parseInt(et_window_len.getText().toString());
                finish();
            }
        });

        bt_report = (Button)findViewById(R.id.bt_report);
        bt_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ConfigActivity.this,ReportActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        et_username.setText(UserName);
        et_fftpoints.setText(String.valueOf(FFTpoints));
        et_maxvlaue.setText(String.valueOf(MaxValue));
        et_fs.setText(String.valueOf(fs));
        et_window_len.setText(String.valueOf(window_len));
    }
}
