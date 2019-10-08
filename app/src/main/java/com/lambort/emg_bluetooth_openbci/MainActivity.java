package com.lambort.emg_bluetooth_openbci;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends Activity implements View.OnClickListener {

    private TextView tv_rec;
    private TextView tv_main_freq;
    private TextView tv_avg_energy;
    private TextView tv_time_count;

    private SurfaceView sfv_wave;
    private SurfaceView sfv_freq;
    private Button bt_bluetooth;
    private Button bt_Draw;
    private Button bt_save;
    private Button bt_time_count;
    private Button bt_config;
    private Button bt_save_coef;

    private Bluetooth_Lab mBtTool;
    private DrawWave mDrawWave;
    private DrawFreq mDrawFreq;

    //处理接受数据线程传来的数据的Handler
    private final Handler receiveHandler = new ReceiveHandler(this);

    Calendar calendar = Calendar.getInstance();
    ArrayList<Integer> rawdata_list = new ArrayList<Integer>();
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_rec = (TextView) findViewById(R.id.tv_rec);
        tv_main_freq = (TextView)findViewById(R.id.tv_main_freq);
        tv_time_count = (TextView)findViewById(R.id.tv_time_count);
        tv_avg_energy = (TextView)findViewById(R.id.tv_avg_energy);

        mBtTool = Bluetooth_Lab.init(this,receiveHandler);
        bt_bluetooth = (Button)findViewById(R.id.bt_bluetooth);
        sfv_wave = (SurfaceView)findViewById(R.id.sfv_wave);
        sfv_freq = (SurfaceView)findViewById(R.id.sfv_freq);
        mDrawWave = new DrawWave(this,sfv_wave,ConfigActivity.MaxValue);
        mDrawFreq = new DrawFreq(this,sfv_freq,receiveHandler,ConfigActivity.MaxValue,ConfigActivity.FFTpoints);

        bt_bluetooth = (Button)findViewById(R.id.bt_bluetooth);
        bt_bluetooth.setOnClickListener(this);
        bt_Draw = (Button)findViewById(R.id.bt_Draw);
        bt_Draw.setOnClickListener(this);
        bt_save = (Button)findViewById(R.id.bt_save);
        bt_save.setOnClickListener(this);
        bt_time_count = (Button)findViewById(R.id.bt_time_count);
        bt_time_count.setOnClickListener(this);
        bt_config = (Button)findViewById(R.id.bt_config);
        bt_config.setOnClickListener(this);
        bt_save_coef = (Button)findViewById(R.id.bt_save_coef);
        bt_save_coef.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBtTool = Bluetooth_Lab.init(this,receiveHandler);
        mDrawFreq.ResetValue(ConfigActivity.FFTpoints);
        mDrawWave.ResetValue(ConfigActivity.MaxValue);
        mDrawWave.isDraw = true;
        mDrawFreq.isDraw = true;
    }

    @Override
    protected void onPause() {
        mDrawWave.isDraw = false;
        mDrawFreq.isDraw = false;
        super.onPause();
    }

    private static int freqCursor = 0;


    static class ReceiveHandler extends Handler {
        WeakReference<MainActivity> mactivity;
        MainActivity mInstance;
        TextView tv_rec;

        public ReceiveHandler(MainActivity activity) {
            mactivity = new WeakReference<MainActivity>(activity);
            mInstance = mactivity.get();

        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                if (msg.what == 0) {
                    mInstance.count += 1;
                    //传入归一化数值，最大值手动设定。
                    mInstance.mDrawWave.in = -Double.valueOf((String) msg.obj) / (double) ConfigActivity.MaxValue;
                    mInstance.mDrawFreq.inBuf.add((Double.valueOf((String) msg.obj) / ConfigActivity.MaxValue));
                    tv_rec = mInstance.tv_rec;
                    tv_rec.setText((String) msg.obj);
                    mInstance.rawdata_list.add(Double.valueOf((String) msg.obj).intValue());

                    // 如果达到150000，自动保存并情况
                    mInstance.calendar = Calendar.getInstance();
                        if (mInstance.count % 150000 == 0 && mInstance.mBtTool.get_BtSocket_Status()) {
                            final String filename = String.format(mInstance.getResources().getString(R.string.auto_file_name),
                                    ConfigActivity.UserName,
                                    mInstance.calendar.get(Calendar.DAY_OF_YEAR), mInstance.calendar.get(Calendar.HOUR_OF_DAY),
                                    mInstance.calendar.get(Calendar.MINUTE),
                                    mInstance.calendar.get(Calendar.SECOND),
                                    mInstance.rawdata_list.size());
                        mInstance.saveFile(filename, mInstance.rawdata_list, 0);
                        Toast.makeText(mInstance, "正在自动保存...", Toast.LENGTH_LONG).show();
                        mInstance.rawdata_list.clear();
                    }
                }
                //计算震颤系数
                else if (msg.what == 1) {
                    DecimalFormat df = new DecimalFormat("0.0000");
                    Save_Lab.tremor_tmp = ((float) (Integer) msg.obj / ConfigActivity.FFTpoints * ConfigActivity.fs);
                    mInstance.tv_main_freq.setText("震颤系数: " + df.format(Save_Lab.tremor_tmp) + " Hz");
                } else if (msg.what == 2) {
                    float f = (float) (double) msg.obj;
                    Save_Lab.rigidity_tmp = f;
                    DecimalFormat df = new DecimalFormat("0.000");
                    mInstance.tv_avg_energy.setText("僵直系数: " + df.format((double) msg.obj) + "");
                }
            }catch (Exception e){
                ;
            }
        }
    }

    public void saveFile(String fileName, ArrayList<Integer> arrlist,int start) {
        // 创建String对象保存文件名路径
        try {
            // 创建指定路径的文件
            File file = new File(Environment.getExternalStorageDirectory(), fileName);
            // 如果文件不存在
            if (file.exists()) {
                // 创建新的空文件
                file.delete();
            }
            file.createNewFile();
            // 获取文件的输出流对象
            DataOutputStream os = new DataOutputStream(new FileOutputStream(file, true));

            for(int i = start;i<arrlist.size();i++) {
                os.writeShort(arrlist.get(i));
            }
            // 最后关闭文件输出流
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    long currentSecond =0 ;
    Handler mhandle;
    boolean isPause = false;
    int save_start = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_bluetooth:
                Intent intent = new Intent(MainActivity.this,DeviceListActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_config:
                mDrawWave.isDraw = false;
                mDrawFreq.isDraw = false;
                Intent intent2 = new Intent(MainActivity.this,ConfigActivity.class);
                startActivity(intent2);
                break;
            case R.id.bt_save_coef:
                int point_num = rawdata_list.size();
                if (point_num == 0){
                    break;
                }
                Save_Lab.get(MainActivity.this).save_New(
                        Save_Lab.tremor_tmp,Save_Lab.rigidity_tmp,Save_Lab.movement_tmp,
                        rawdata_list.subList(point_num>Save_Lab.points?point_num-Save_Lab.points:0,point_num));
                break;

            case R.id.bt_Draw:
                mDrawWave.isDraw = false;
                mDrawWave.isDraw = true;
                mDrawWave.startDraw();

                mDrawFreq.isDraw = false;
                mDrawFreq.isDraw = true;
                mDrawFreq.startDraw();
                break;
            case R.id.bt_save:
                if(((Button)v).getText().equals("开始保存") ) {
                    ((Button) v).setText("最终保存");
                    save_start = rawdata_list.size();
                }
                else{
                    ((Button)v).setText("开始保存");
                    CreateFileNameDialog(save_start);
                    save_start = 0;
                }
                break;
            case R.id.bt_time_count:
                if(((Button)v).getText().equals("开始计时") ){
                    ((Button)v).setText("停止计时");
                    mhandle = new Handler();
                    isPause = false;//是否暂停
                    currentSecond = 0;//当前毫秒数
                    Runnable timeRunable = new Runnable() {
                        @Override
                        public void run() {

                            currentSecond = currentSecond + 1000;
                            Save_Lab.movement_tmp = (float) currentSecond;
                            tv_time_count.setText("迟缓系数: "+getFormatHMS(currentSecond));
                            if (!isPause) {
                                //递归调用本runable对象，实现每隔一秒一次执行任务
                                mhandle.postDelayed(this, 100);
                            }
                            else {

                            }
                        }
                    };
                    timeRunable.run();

                }
                else{
                    ((Button)v).setText("开始计时");
                    isPause = true;
                }
                break;
        }
    }

    public static String getFormatHMS(long time){
        int ms  = (int)time%10000/10;
        time=time/10000;//总秒数
        int s= (int) (time%60);//秒
        int m= (int) (time/60);//分
        int h=(int) (time/3600);//秒
        return String.format("%02d:%02d:%02d:%03d",h,m,s,ms);
    }

    public void CreateFileNameDialog(final int savestart) {
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(20, 20, 30, 10);
        final EditText et1 = new EditText(this);
        et1.setLayoutParams(lp);
        et1.setHint("文件名");
        calendar = Calendar.getInstance();
        et1.setText(String.format(getResources().getString(R.string.file_name),
                ConfigActivity.UserName,
                calendar.get(Calendar.DAY_OF_YEAR), calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND),
                rawdata_list.size()-savestart));

        layout.addView(et1);
        new AlertDialog.Builder(this).setTitle("设置文件名")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(layout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input1 = et1.getText().toString();
                        if (input1.equals("")) {
                            Toast.makeText(getApplicationContext(), "不能为空" + input1, Toast.LENGTH_SHORT).show();
                        } else {
                            AlertDialog  new_dialog = new AlertDialog.Builder(MainActivity.this).setTitle("正在保存...").show();
                            saveFile(input1, rawdata_list,savestart);
                            rawdata_list.clear();
                            new_dialog.dismiss();
                            CreateDialog("保存完毕");
                        }
                    }
                })
                .setNegativeButton("取消",null)
                .show();
    }

    public AlertDialog CreateDialog(String str) {
        return new AlertDialog.Builder(this).setTitle(str)
                .show();
    }
}

