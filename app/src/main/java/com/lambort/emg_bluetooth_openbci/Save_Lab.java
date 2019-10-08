package com.lambort.emg_bluetooth_openbci;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.List;

public class Save_Lab {
    public static final String TREMOR = "tremor"; //震颤
    public static final String RIGIDITY = "rigidity"; //僵直
    public static final String MOVEMENT = "movement"; //运动
    public static final String WAVE = "wave"; //波形
    public static final String Old_Share_Name = "Old";
    public static final String New_Share_Name = "New";

    // 老保存
    public static float tremor_old = 0;
    public static float rigidity_old = 0;
    public static float movement_old = 0;
    public static int[] wave_old;
    //新保存
    public static float tremor_new = 0;
    public static float rigidity_new = 0;
    public static float movement_new = 0;
    //暂存
    public static float tremor_tmp = 0;
    public static float rigidity_tmp = 0;
    public static float movement_tmp = 0;
    public static int[] wave_new;

    private static Context mContext;
    private static Save_Lab mSave_Lab = null;
    public static int WIDTH = 2;
    public static int x_interval = 2;
    public static int points = 1;

    private Save_Lab(Context context){
        mContext = context;
        WIDTH =  context.getResources().getDisplayMetrics().widthPixels;
        points = WIDTH/x_interval;
        wave_old = new int[points];
        wave_new = new int[points];
    }

    public static Save_Lab get(Context context){
        if(mSave_Lab == null){
            mSave_Lab = new Save_Lab(context);
        }
        mContext = context;
        return mSave_Lab;
    }

    public boolean isOldFirst(){
        SharedPreferences share=mContext.getSharedPreferences(Old_Share_Name,Activity.MODE_PRIVATE);
        return share.getBoolean("isFirst",true);
    }

    public boolean isNewFirst(){
        SharedPreferences share=mContext.getSharedPreferences(New_Share_Name,Activity.MODE_PRIVATE);
        return share.getBoolean("isFirst",true);
    }

    public void  save_New(float tremor, float rigidity, float movement, List<Integer> wave){
        // old没有，只放进old
        if(isOldFirst()){
            SharedPreferences sharedPreferences; //私有数据
            sharedPreferences = mContext.getSharedPreferences(Old_Share_Name, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
            editor.putFloat(TREMOR, tremor);
            editor.putFloat(RIGIDITY, rigidity);
            editor.putFloat(MOVEMENT, movement);

            JSONArray jsonArray = new JSONArray();
            for (int b : wave) {
                jsonArray.put(b);
            }
            editor.putString(WAVE,jsonArray.toString());

            editor.putBoolean("isFirst", false);
            editor.commit();//提交修改
        }
        //old 有，new没有,只放进new
        else if (isNewFirst()){
            SharedPreferences sharedPreferences; //私有数据
            sharedPreferences = mContext.getSharedPreferences(New_Share_Name, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器

            editor.putFloat(TREMOR, tremor);
            editor.putFloat(RIGIDITY, rigidity);
            editor.putFloat(MOVEMENT, movement);

            JSONArray jsonArray = new JSONArray();
            for (int b : wave) {
                jsonArray.put(b);
            }
            editor.putString(WAVE,jsonArray.toString());

            editor.putBoolean("isFirst", false);
            editor.commit();//提交修改
        }
        else{
            //读 new
            SharedPreferences share_new=mContext.getSharedPreferences(New_Share_Name,Activity.MODE_PRIVATE);

            //new 存入 old
            SharedPreferences share_old = mContext.getSharedPreferences(Old_Share_Name, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = share_old.edit();//获取编辑器
            editor.putFloat(TREMOR, share_new.getFloat(TREMOR,0));
            editor.putFloat(RIGIDITY, share_new.getFloat(RIGIDITY,0));
            editor.putFloat(MOVEMENT, share_new.getFloat(MOVEMENT,0));
            editor.putString(WAVE,share_new.getString(WAVE,""));
            editor.commit();//提交修改

            //存入new
            editor = share_new.edit();//获取编辑器
            editor.putFloat(TREMOR, tremor);
            editor.putFloat(RIGIDITY, rigidity);
            editor.putFloat(MOVEMENT, movement);
            JSONArray jsonArray = new JSONArray();
            for (int b : wave) {
                jsonArray.put(b);
            }
            editor.putString(WAVE,jsonArray.toString());
            editor.commit();//提交修改
        }
    }

    public void get_Old(){
        //判断是否已有数据
        SharedPreferences share=mContext.getSharedPreferences(Old_Share_Name,Activity.MODE_PRIVATE);
        tremor_old =share.getFloat(TREMOR,0);
        rigidity_old =share.getFloat(RIGIDITY,0);
        movement_old =share.getFloat(MOVEMENT,0);

        try {
            JSONArray jsonArray = new JSONArray(share.getString(WAVE, "[]"));
            for (int i = 0; i < points; i++) {
                wave_old[i] = jsonArray.getInt(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void get_New(){
        //判断是否已有数据
        SharedPreferences share=mContext.getSharedPreferences(New_Share_Name,Activity.MODE_PRIVATE);
        tremor_new =share.getFloat(TREMOR,0);
        rigidity_new =share.getFloat(RIGIDITY,0);
        movement_new =share.getFloat(MOVEMENT,0);
        try {
            JSONArray jsonArray = new JSONArray(share.getString(WAVE, "[]"));
            for (int i = 0; i < points; i++) {
                wave_new[i] = jsonArray.getInt(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
