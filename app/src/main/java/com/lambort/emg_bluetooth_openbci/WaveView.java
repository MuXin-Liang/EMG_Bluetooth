package com.lambort.emg_bluetooth_openbci;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class WaveView extends View {
    private Canvas mCanvas;
    private Paint paint;
    // 要绘制的曲线的高度
    private int HEIGHT;
    // 要绘制的曲线的水平宽度
    private int WIDTH;
    // 离屏幕左边界的起始距离
    private final int X_OFFSET = 5;
    // 实际的Y轴的位置
    private int centerY;
    // 上一个点的位置，根据点的位置画折线
    private int startX;
    private int startY;
    // 初始化X坐标
    private int cx = X_OFFSET;
    public int cy = 0;
    // 决定间隔
    public static int x_interval = 2;
    private Context mActivity;
    private int[] draw_data_list = null;

    public WaveView(Context context){
        super(context);
        mActivity = context;
        Log.e("1","1");
    }

    public WaveView(Context context,AttributeSet attrs){
        super(context,attrs);
        mActivity = context;
        Log.e("2","2");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int mWidth = 0;
        int mHeight = 0;
        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        }else if(widthMode == MeasureSpec.AT_MOST){
            throw new IllegalArgumentException("width must be EXACTLY,you should set like android:width=\"200dp\"");
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        }else if(widthMeasureSpec == MeasureSpec.AT_MOST){

            throw new IllegalArgumentException("height must be EXACTLY,you should set like android:height=\"200dp\"");
        }
        HEIGHT = mHeight;
        //获取屏幕的宽度作为示波器的边长
        WIDTH = Save_Lab.WIDTH;

        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("onDraw","onDraw");
        drawBackGround(canvas);
        drawWave(canvas);

    }

    public void setData(int[] data_list){
        draw_data_list = data_list;
    }

    public void drawWave(Canvas canvas){
        if(draw_data_list == null){
            throw new IllegalArgumentException("drawData not init!");
        }
        int maxValue = 1;
        for (int i = 0; i < draw_data_list.length; i++) {
            if (maxValue < draw_data_list[i])
                maxValue = draw_data_list[i];
        }
        int minValue = maxValue;
        for (int i = 0; i < draw_data_list.length; i++) {
            if (minValue > draw_data_list[i])
                minValue = draw_data_list[i];
        }

        startX = X_OFFSET;
        startY = HEIGHT;
        cx = startX + x_interval;
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);

        for (int i = 0; i < draw_data_list.length; i++) {
            Log.e(""+i,cy+"");
            cy = -(int) (((double)draw_data_list[i]-minValue) / (maxValue-minValue) * HEIGHT) + HEIGHT;
            // 根据Ｘ，Ｙ坐标画线
            canvas.drawLine(startX, startY, cx, cy, paint);
            //结束点作为下一次折线的起始点
            startX = cx;
            startY = cy;
            cx += x_interval;
        }
    }

    private void drawBackGround(Canvas canvas) {
        // 绘制黑色背景
        canvas.drawColor(Color.BLACK);

        // 画网格8*8
        Paint mPaint = new Paint();
        mPaint.setColor(Color.GRAY);// 网格为黄色
        mPaint.setStrokeWidth(1);// 设置画笔粗细

        int oldY = 0;
        for (int i = 0; i <= 8; i++) {// 绘画横线
            canvas.drawLine(0, oldY, WIDTH, oldY, mPaint);
            oldY = oldY + HEIGHT / 8;
        }

        int oldX = 0;
        while (oldX < WIDTH) {// 绘画纵线
            canvas.drawLine(oldX, 0, oldX, HEIGHT, mPaint);
            oldX = oldX + HEIGHT / 8;
        }

        // 绘制坐标轴
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setStrokeWidth(2);

        canvas.drawLine(X_OFFSET, centerY, WIDTH, centerY, p);
        canvas.drawLine(X_OFFSET, 0, X_OFFSET, HEIGHT, p);
    }
}
