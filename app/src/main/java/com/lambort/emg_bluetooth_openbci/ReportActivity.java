package com.lambort.emg_bluetooth_openbci;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;

public class ReportActivity extends Activity {
    
    private EditText et_name;
    private EditText et_tremor_old;
    private EditText et_rigidity_old;
    private EditText et_movement_old;

    private EditText et_tremor_new;
    private EditText et_rigidity_new;
    private EditText et_movement_new;

    private TextView tv_tremor_better;
    private TextView tv_rigidity_better;
    private TextView tv_movement_better;

    private WaveView wv_before;
    private WaveView wv_after;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_report);
        et_name = (EditText)findViewById(R.id.et_name);
        et_tremor_old = (EditText)findViewById(R.id.et_tremor_old);
        et_rigidity_old = (EditText)findViewById(R.id.et_rigidity_old);
        et_movement_old = (EditText)findViewById(R.id.et_movement_old);
        et_tremor_new = (EditText)findViewById(R.id.et_tremor_new);
        et_rigidity_new = (EditText)findViewById(R.id.et_rigidity_new);
        et_movement_new = (EditText)findViewById(R.id.et_movement_new);
        tv_tremor_better = (TextView)findViewById(R.id.tv_tremor_better);
        tv_rigidity_better = (TextView)findViewById(R.id.tv_rigidity_better);
        tv_movement_better = (TextView)findViewById(R.id.tv_movement_better);
        wv_before = (WaveView)findViewById(R.id.wav_before);
        wv_after = (WaveView)findViewById(R.id.wv_after);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Save_Lab.get(ReportActivity.this).get_New();
        Save_Lab.get(ReportActivity.this).get_Old();
        et_name.setText(ConfigActivity.UserName);

        DecimalFormat df = new DecimalFormat("0.0000");
        et_tremor_old.setText(df.format(Save_Lab.tremor_old)+"");
        et_rigidity_old.setText(df.format(Save_Lab.rigidity_old)+"");
        et_movement_old.setText(df.format(Save_Lab.movement_old/10000)+"");

        et_tremor_new.setText(df.format(Save_Lab.tremor_new)+"");
        et_rigidity_new.setText(df.format(Save_Lab.rigidity_new)+"");
        et_movement_new.setText(df.format(Save_Lab.movement_new/10000)+"");

        df = new DecimalFormat("0.00");
        tv_tremor_better.setText("震颤:"+df.format((Save_Lab.tremor_new-Save_Lab.tremor_old)/Save_Lab.tremor_old*100)+"%");
        tv_rigidity_better.setText("僵直:"+df.format((Save_Lab.rigidity_new-Save_Lab.rigidity_old)/Save_Lab.rigidity_old*100)+"%");
        tv_movement_better.setText("运动:"+df.format((Save_Lab.movement_new-Save_Lab.movement_old)/Save_Lab.movement_old*100)+"%");

        wv_before.setData(Save_Lab.wave_old);
        wv_after.setData(Save_Lab.wave_new);
    }

}
