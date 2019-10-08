package com.lambort.emg_bluetooth_openbci;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

public class DrawFreq {
    public ArrayList<Double> inBuf = new ArrayList<Double>();
    public boolean isDraw = true;

    //FFT的点数
    public int FFTpoints;
    public int MaxVolt;

    // 获取MainActivity提供的SurfaceHolder 与 Paint对象
    private SurfaceHolder holder;
    private SurfaceView surfaceView;
    private Paint paint;
    private Handler mHandler;

    // 要绘制的曲线的高度
    private int HEIGHT;
    // 要绘制的曲线的水平宽度
    private int WIDTH;
    // 离屏幕左边界的起始距离
    private final int X_OFFSET = 5;
    // 初始化X坐标
    private int cx = X_OFFSET;
    // 实际的Y轴的位置
    private int centerY;


    private Timer timer = new Timer();
    private TimerTask task = null;
    private Context mActivity;

    //纵坐标最大值
    public int maxValue;
    public Integer Y_values[] = new Integer[9];

    public DrawFreq(Context activity, SurfaceView showSurfaceView, Handler handler, int maxVolt, int fftpoints) {
        mActivity = activity;
        surfaceView = showSurfaceView;
        mHandler = handler;
        MaxVolt = maxVolt;
        FFTpoints = fftpoints;
        maxValue = FFTpoints / 2;
        for (int i = 0; i < 9; i++) {
            Y_values[i] = maxValue * (8 - i) / 8;
        }

        // 初始化SurfaceHolder对象
        holder = showSurfaceView.getHolder();
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);

    }

    public void ResetValue(int fft_points) {
        FFTpoints = fft_points;
        maxValue = FFTpoints / 2;
    }

    public void startDraw() {
        new DrawThread().start();
    }

    private void InitData() {
        Resources resources = mActivity.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        //获取SurfaceView的高度
        HEIGHT = surfaceView.getHeight();
        //获取屏幕的宽度作为示波器的边长
        WIDTH = dm.widthPixels;
        //Y轴的中心在底部
        centerY = HEIGHT;
    }

    public int cy = 0;
    double in;
    FFT fft;

    class DrawThread extends Thread {

        public void run() {
            InitData();
            drawBackGround(holder); //绘制背景
            fft = new FFT(FFTpoints);

            if (task != null) {
                task.cancel();
            }
            task = new TimerTask() {
                @Override
                public void run() {
                    if (inBuf.size() >= FFTpoints && isDraw) {
                        try {
                            double[] mBytes = fft.FFT(inBuf);
                            //求 主频 及 短时平均能量 并向Handler发送
                            double tmp_max = 0;
                            int main_freq = 0;
                            double avg_energy = 0;

                            for (int i = 0; i < mBytes.length; i++) {
                                avg_energy += inBuf.get(i) * inBuf.get(i) * ConfigActivity.MaxValue * ConfigActivity.MaxValue;
                                if (tmp_max < mBytes[i] && i < mBytes.length / 2 && i != 0) {
                                    tmp_max = mBytes[i];
                                    main_freq = i;
                                }
                            }
                            Message msg_main_freq = new Message();
                            msg_main_freq.what = 1;
                            msg_main_freq.obj = main_freq;
                            mHandler.sendMessage(msg_main_freq);

                            Message msg_avg_ene = new Message();
                            msg_avg_ene.what = 2;
                            msg_avg_ene.obj = avg_energy / mBytes.length;
                            mHandler.sendMessage(msg_avg_ene);

                            // 清空
                            drawBackGround(holder);
                            ClearDraw();
                            drawLines(holder, mBytes);
                            inBuf.clear();

                        } catch (Exception e) {
                            Log.e("?", "Error!");
                        }
                    } else if (isDraw == false) {
                        System.gc();
                        cancel();
                    }
                }
            };
            timer.schedule(task, 0, 2);

        }
    }

    private void drawLines(SurfaceHolder holder, double[] mBytes) {
        synchronized(holder) {
            Canvas canvas = holder.lockCanvas(new Rect(0, 0,
                    WIDTH, HEIGHT));
            paint.setStrokeWidth(WIDTH / FFTpoints);
            cx = WIDTH / 2;
            // 画 FFTpoints 条线
            for (int i = 0; i < FFTpoints / 2; i++) {
                cy = -(int) (mBytes[i] / maxValue * HEIGHT) + HEIGHT;
                // 根据Ｘ，Ｙ坐标画线
                canvas.drawLine(cx, HEIGHT, cx, cy, paint);
                cx += WIDTH / FFTpoints;
            }
            cx = WIDTH / 2 - WIDTH / FFTpoints;
            for (int i = FFTpoints - 1; i >= FFTpoints / 2; i--) {
                cy = -(int) (mBytes[i] / maxValue * HEIGHT) + HEIGHT;
                // 根据Ｘ，Ｙ坐标画线
                canvas.drawLine(cx, HEIGHT, cx, cy, paint);
                cx -= WIDTH / FFTpoints;
            }
            // 提交修改
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawBackGround(SurfaceHolder holder) {
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas(null);
            // 绘制黑色背景
            //canvas.drawColor(Color.BLACK);
            //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);

            // 画网格8*8
            Paint mPaint = new Paint();
            mPaint.setColor(Color.GRAY);// 网格为黄色
            mPaint.setStrokeWidth(1);// 设置画笔粗细
            mPaint.setTextSize(30);//设置坐标文本大小
            mPaint.setTextAlign(Paint.Align.RIGHT);
            int oldY = 0;
            for (int i = 0; i <= 8; i++) {// 绘画横线
                canvas.drawLine(0, oldY, WIDTH, oldY, mPaint);
                oldY = oldY + HEIGHT / 8;
            }
            int oldX = WIDTH / 2;
            while (oldX < WIDTH) {// 绘画纵线
                canvas.drawLine(oldX, 0, oldX, HEIGHT, mPaint);
                oldX = oldX + HEIGHT / 8;
            }
            oldX = WIDTH / 2;
            while (oldX > 0) {// 绘画纵线
                canvas.drawLine(oldX, 0, oldX, HEIGHT, mPaint);
                oldX = oldX - HEIGHT / 8;
            }

            // 绘制坐标轴
            Paint p = new Paint();
            p.setColor(Color.WHITE);
            p.setStrokeWidth(2);
            canvas.drawLine(X_OFFSET, HEIGHT, WIDTH, HEIGHT, p);
            canvas.drawLine(X_OFFSET, WIDTH / 2, X_OFFSET, HEIGHT, p);
        } catch (Exception e) {

        } finally {
            holder.unlockCanvasAndPost(canvas);
            holder.lockCanvas(new Rect(0, 0, 0, 0));
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void ClearDraw(){

        Canvas canvas = null;
        try{

            canvas = holder.lockCanvas(null);
            canvas.drawColor(Color.WHITE);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);

        }catch(Exception e){


        }finally{

            if(canvas != null){

                holder.unlockCanvasAndPost(canvas);

            }
        }
    }


}
