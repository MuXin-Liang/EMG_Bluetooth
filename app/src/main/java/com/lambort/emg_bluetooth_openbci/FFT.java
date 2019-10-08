package com.lambort.emg_bluetooth_openbci;

import java.util.ArrayList;

public class FFT {

    private static double PI = 3.141592653;

    private static int FFT_N;

    public FFT(int fft_N){
        FFT_N = fft_N;
        SIN_TAB = new float[FFT_N/2];
        create_sin_tab(SIN_TAB);
    }

    public static class Compx {
        public float real;
        public float imag;
        public double abs() {
            return Math.sqrt(real*real+imag*imag);
        }
    };

    //private Compx[] S = new Compx[FFT_N];
    private float SIN_TAB[];

    /*******************************************************************
     函数原型：struct compx EE(struct compx b1,struct compx b2)
     函数功能：对两个复数进行乘法运算
     输入参数：两个以联合体定义的复数a,b
     输出参数：a和b的乘积，以联合体的形式输出
     *******************************************************************/
    private Compx EE(Compx a,Compx b)
    {
        Compx c = new Compx();
        c.real=a.real*b.real-a.imag*b.imag;
        c.imag=a.real*b.imag+a.imag*b.real;
        return(c);
    }
    /******************************************************************
     函数原型：void create_sin_tab(float *sin_t)
     函数功能：创建一个正弦采样表，采样点数与福利叶变换点数相同
     输入参数：*sin_t存放正弦表的数组指针
     输出参数：无
     ******************************************************************/
    private void create_sin_tab(float[] sin_t)
    {
        int i;
        for(i=0;i<FFT_N/2;i++)
            sin_t[i]=(float) Math.sin(2*PI*i/FFT_N);
    }
    /******************************************************************
     函数原型：void sin_tab(float pi)
     函数功能：采用查表的方法计算一个数的正弦值
     输入参数：pi 所要计算正弦值弧度值，范围0--2*PI，不满足时需要转换
     输出参数：输入值pi的正弦值
     ******************************************************************/
    private float sin_tab(float pi)
    {
        int n;
        float a = 0;
        n=(int)(pi*FFT_N/2/PI);

        if(n>=0&&n<FFT_N/2)
            a=SIN_TAB[n];
        else if(n>=FFT_N/2&&n<FFT_N)
            a=-SIN_TAB[n-FFT_N/2];
        return a;
    }
    /******************************************************************
     函数原型：void cos_tab(float pi)
     函数功能：采用查表的方法计算一个数的余弦值
     输入参数：pi 所要计算余弦值弧度值，范围0--2*PI，不满足时需要转换
     输出参数：输入值pi的余弦值
     ******************************************************************/
    private float cos_tab(float d)
    {
        float a,pi2;
        pi2=(float) (d+PI/2);
        if(pi2>2*PI)
            pi2-=2*PI;
        a=sin_tab(pi2);
        return a;
    }
    /*****************************************************************
     函数原型：void FFT(struct compx *xin,int N)
     函数功能：对输入的复数组进行快速傅里叶变换（FFT）
     输入参数：*xin复数结构体组的首地址指针，struct型
     输出参数：无
     *****************************************************************/
    public void FFT(Compx[] xin)
    {
        int f,m,nv2,nm1,i,k,l,j=0;
        Compx u,w,t;

        nv2=FFT_N/2;             //变址运算，即把自然顺序变成倒位序，采用雷德算法
        nm1=FFT_N-1;
        for(i=0;i<nm1;i++)
        {
            if(i<j)                    //如果i<j,即进行变址
            {
                t=xin[j];
                xin[j]=xin[i];
                xin[i]=t;
            }
            k=nv2;                    //求j的下一个倒位序
            while(k<=j)               //如果k<=j,表示j的最高位为1
            {
                j=j-k;                 //把最高位变成0
                k=k/2;                 //k/2，比较次高位，依次类推，逐个比较，直到某个位为0
            }
            j=j+k;                   //把0改为1
        }

        {
            int le,lei,ip;                            //FFT运算核，使用蝶形运算完成FFT运算
            f=FFT_N;
            for(l=1;(f=f/2)!=1;l++)              //计算l的值，即计算蝶形级数
                ;
            for(m=1;m<=l;m++)                   // 控制蝶形结级数
            {                               //m表示第m级蝶形，l为蝶形级总数l=log（2）N
                le=2<<(m-1);                    //le蝶形结距离，即第m级蝶形的蝶形结相距le点
                lei=le/2;                         //同一蝶形结中参加运算的两点的距离

                u = new Compx();
                u.real=(float) 1;                        //u为蝶形结运算系数，初始值为1
                u.imag=(float) 0;
                //w.real=cos(PI/lei);                  //不适用查表法计算sin值和cos值
                // w.imag=-sin(PI/lei);

                w = new Compx();
                w.real=cos_tab((float) (PI/lei));             //w为系数商，即当前系数与前一个系数的商
                w.imag=-sin_tab((float) (PI/lei));
                for(j=0;j<=lei-1;j++)              //控制计算不同种蝶形结，即计算系数不同的蝶形结
                {
                    for(i=j;i<=FFT_N-1;i=i+le)       //控制同一蝶形结运算，即计算系数相同蝶形结
                    {
                        ip=i+lei;                          //i，ip分别表示参加蝶形运算的两个节点
                        t=EE(xin[ip],u);                   //蝶形运算，详见公式
                        xin[ip].real=xin[i].real-t.real;
                        xin[ip].imag=xin[i].imag-t.imag;
                        xin[i].real=xin[i].real+t.real;
                        xin[i].imag=xin[i].imag+t.imag;
                    }
                    u=EE(u,w);                          //改变系数，进行下一个蝶形运算
                }
            }
        }
    }

    public double[] FFT(ArrayList<Double> x){
        Compx xin[] = new Compx[FFT_N];
        for(int i =0;i<FFT_N;i++)
        {
            xin[i] = new Compx();
            xin[i].real = x.get(i).floatValue();
            xin[i].imag = 0;
        }
        FFT(xin);
        double xout[] = new double[FFT_N];
        for(int i =0;i<FFT_N;i++)
            xout[i] = xin[i].abs();
        return xout;
    }
}

