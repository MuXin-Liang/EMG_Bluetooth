package com.lambort.emg_bluetooth_openbci;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class DrawWave {
    private Random random = new Random();
    public boolean isDraw = true;

    // 获取MainActivity提供的SurfaceHolder 与 Paint对象
    private SurfaceHolder holder;
    private SurfaceView surfaceView;
    private Paint paint;

    // 要绘制的曲线的高度
    private int HEIGHT;
    // 要绘制的曲线的水平宽度
    private int WIDTH;
    // 离屏幕左边界的起始距离
    private final int X_OFFSET = 5;
    // 初始化X坐标
    private int cx = X_OFFSET;
    // 实际的Y轴的位置
    private int centerY ;
    // 上一个点的位置，根据点的位置画折线
    private int startX;
    private int startY;
    // 决定间隔
    private int x_interval = 2;
    // 最大值
    private int MaxVolt;

    private Timer timer = new Timer();
    private TimerTask task = null;
    private Context mActivity;

    //纵坐标最大值
    public int maxValue ;
    public Integer Y_values[] = new Integer[9] ;

    public void ResetValue(int maxVolt) {
        maxValue = maxVolt;
        //初始化坐标
        for (int i = 0; i < 9; i++) {
            Y_values[i] = maxValue * (8 - i)/ 8 ;
        }
    }

    public DrawWave(Context activity, SurfaceView showSurfaceView,int MaxValue){
        mActivity = activity;
        surfaceView = showSurfaceView;
        maxValue = MaxValue;
        x_interval = Save_Lab.x_interval;

        // 初始化SurfaceHolder对象
        holder = showSurfaceView.getHolder();
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);

    }

    public void startDraw(){
        new DrawThread().start();
    }

    private void InitData() {
        Resources resources = mActivity.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        //获取SurfaceView的高度
        HEIGHT = surfaceView.getHeight();
        //获取屏幕的宽度作为示波器的边长
        WIDTH = dm.widthPixels;
        //Y轴的中心就是高的一半
        centerY = HEIGHT;

        // 初始化点的位置
        startX = 0;
        startY = centerY;

    }

    public int cy = 0;
    double in;

    class DrawThread extends Thread {

        public void run() {
            InitData();
            drawBackGround(holder); //绘制背景
            if(task!= null){
                task.cancel();
            }
            task = new TimerTask() {
                @Override
                public void run() {
                    cy = (int) (in * centerY) + centerY;
                    if (isDraw) {
                        try {
                            Canvas canvas = holder.lockCanvas(new Rect(cx - x_interval, cy - HEIGHT / 2,
                                    cx + x_interval, cy + HEIGHT / 2));
                            // 根据Ｘ，Ｙ坐标画线
                            canvas.drawLine(startX, startY, cx, cy, paint);
                            // 提交修改
                            holder.unlockCanvasAndPost(canvas);

                            //结束点作为下一次折线的起始点
                            startX = cx;
                            startY = cy;
                            cx += x_interval;

                            // 超过指定宽度，刷新
                            if (cx > WIDTH) {
                                Refresh();
                            }
                        }
                        catch (Exception e){

                        }
                    }
                    else {
                        System.gc();
                        cancel();
                    }
                }
            };
            timer.schedule(task,0,1);

        }
    }

    private void drawBackGround(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // 绘制黑色背景
        canvas.drawColor(Color.BLACK);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setStrokeWidth(2);


        // 画网格8*8
        Paint mPaint = new Paint();
        mPaint.setColor(Color.GRAY);// 网格为黄色
        mPaint.setStrokeWidth(1);// 设置画笔粗细
        mPaint.setTextSize(20);//设置坐标文本大小
        mPaint.setTextAlign(Paint.Align.RIGHT);

        int oldY = 0;
        for (int i = 0; i <= 8; i++) {// 绘画横线
            canvas.drawLine(0, oldY, WIDTH, oldY, mPaint);
            canvas.drawText(Y_values[i].toString(),WIDTH,oldY+30,mPaint);
            oldY = oldY + HEIGHT/8;
        }

        int oldX = 0;
        Integer points_every_block = 0;
        DecimalFormat df = new DecimalFormat("0.0");
        mPaint.setTextAlign(Paint.Align.LEFT);
        while(oldX<WIDTH){// 绘画纵线
            canvas.drawLine(oldX, 0, oldX, HEIGHT, mPaint);
            canvas.drawText(df.format(1.0/ConfigActivity.fs*points_every_block*4)+"",oldX+5,centerY-5,mPaint);
            // canvas.drawText((points_every_block).toString(),oldX+5,centerY,mPaint);
            oldX = oldX + HEIGHT/8;
            points_every_block = points_every_block+HEIGHT/8/x_interval;
        }

        // 绘制坐标轴
        canvas.drawLine(X_OFFSET, centerY, WIDTH, centerY, p);
        canvas.drawLine(X_OFFSET, 0, X_OFFSET, HEIGHT, p);
        holder.unlockCanvasAndPost(canvas);
        holder.lockCanvas(new Rect(0, 0, 0, 0));
        holder.unlockCanvasAndPost(canvas);
    }



    public void Refresh(){
        drawBackGround(holder);
        startX = X_OFFSET;
        cx = startX+x_interval;
        startY = cy;

    }

}
