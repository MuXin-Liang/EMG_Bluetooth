package com.lambort.emg_bluetooth_openbci;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import android.os.Handler;

/**
 * Created by Joe on 2017/10/20.
 */

public class Bluetooth_Lab {
    private static final String serverUUID = "00001101-0000-1000-8000-00805F9B34FB";
    
    public static String Name="BtDeviceName";
    public static String Address="BtDeviceAddress";
    public static String Service="BtDeviceService";

    private BluetoothDevice mBtDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothSocket mBtSocket   = null;
    
    private ArrayList<HashMap<String,Object>> BtDevices_list;
    private Set<BluetoothDevice> mySet;
    private OutputStream outStream = null;
    private InputStream inStream  = null;

    private static Context mContext;
    private static Handler mHandler;
    private static Bluetooth_Lab mLab = null;

    
    private Bluetooth_Lab(Context appContext, Handler handler){
        mContext = appContext.getApplicationContext();
        mHandler = handler;
    }

    public static Bluetooth_Lab init(Context c,Handler handler){
        if (mLab==null){
            mLab=new Bluetooth_Lab( c.getApplicationContext(),handler) ;
        }
        mContext = c;
        mHandler = handler;
        return mLab;
    }
    
    public static Bluetooth_Lab get(){
        return mLab;
    }

    public BluetoothSocket getBtSocket(){
        return mBtSocket;
    }

    public boolean get_BtSocket_Status(){
        boolean retval = true;
        try {
            retval = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(android.bluetooth.BluetoothProfile.HEADSET)
                    != android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

        } catch (Exception exc) {
            // nothing to do
        }
        return retval;
    }

    // 1 开启蓝牙
    public void Bluetooth_on() {
        mBtAdapter = mBtAdapter.getDefaultAdapter();

        if ( mBtAdapter == null ) {
            showMessage("Bluetooth unused.");
            return;
        }
        if (!mBtAdapter.isEnabled()) {
            mBtAdapter.enable();
            showMessage("开启蓝牙中...");
        } else {
            showMessage("蓝牙已开启");
        }
    }
    
    // 2.获取已经配对的设备列表
    public void init_list(){
        HashMap<String,Object> map;
        BtDevices_list=new ArrayList<HashMap<String,Object>>();

        if (mBtAdapter != null) {
            //获得已配对设备列表
            mySet = mBtAdapter.getBondedDevices();
            if(mySet.size()>0) {
                for (BluetoothDevice mBtDevice : mySet) {
                    map = new HashMap<String, Object>();
                    map.put(Bluetooth_Lab.Name, mBtDevice.getName());
                    map.put(Bluetooth_Lab.Address, mBtDevice.getAddress());
                    map.put(Bluetooth_Lab.Service,mBtDevice);
                    BtDevices_list.add(map);
                }
            }
            else{
                showMessage("还没有已配对的远程蓝牙设备！");
            }
        }
    }

    public ArrayList<HashMap<String,Object>> getBtDevices_list(){
        return BtDevices_list;
    }

    // 3.连接
    public void BtConnect(String address) {
        mBtDevice = mBtAdapter.getRemoteDevice(address);

        if(mBtSocket!=null)
            try {
                mBtSocket.close();
            }
            catch (IOException e) {
                ;
            }

        if (mBtDevice != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //第一次采用UUID方式获取socket
                    try{
                        mBtAdapter.cancelDiscovery();
                        mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(UUID.fromString(serverUUID));
                    }
                    catch (IOException e){
                        Log.e("",e.getMessage());
                    }
                    //链接该socket
                    try{
                        mBtSocket.connect();
                        //showThreadMessage("连接成功！");
                    }
                    catch (IOException ex) {
                        showThreadMessage("连接失败...正在进行第二次连接");
                        //第一次链接socket失败，第二次采用反射方式获取socket
                        try {
                            Method m = mBtDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                            mBtSocket = (BluetoothSocket) m.invoke(mBtDevice, 0);
                        } catch (NoSuchMethodException e) {
                            showThreadMessage("连接创建失败！");
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        //获取socket进行第二次链接
                        if (!mBtSocket.isConnected()) {
                            try {
                                mBtSocket.connect();
                                showThreadMessage("连接成功!！");

                            } catch (IOException e) {
                                e.printStackTrace();
                                showThreadMessage("连接失败");
                                try {
                                    mBtSocket.close();
                                } catch (IOException e2) {
                                }
                            }
                        }
                        else {
                            showThreadMessage("已连接！");
                        }
                    }
                    /* I/O initialize */
                    try {
                        inStream  = mBtSocket.getInputStream();
                        outStream = mBtSocket.getOutputStream();
                        new readThread().start();
                        //showThreadMessage("线程创建成功");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


    }

    final byte delimiter = 10; //This is the ASCII code for a newline character
    // 4.开启接收数据的线程
    //读取数据线程，用下面的Handler传输数据
    private class readThread extends Thread {
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            int readBufferPosition = 0;

            while (true) {
                try {
                    bytes = inStream.available();
                    // Read from the InputStream
                    if(bytes > 0)
                    {
                        byte[] buf_data = new byte[bytes];
                        inStream.read(buf_data);
                        for(int i=0;i<bytes;i++) {
                            byte b = buf_data[i];
                            if (b == delimiter) {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(buffer, 0, encodedBytes, 0, encodedBytes.length);
                                //final String data = new String(encodedBytes, "US-ASCII");
                                final String data = new String(encodedBytes);
                                readBufferPosition = 0;

                                Message msg = new Message();
                                msg.obj = data;
                                msg.what = 0;
                                mHandler.sendMessage(msg);
                            }
                            else {
                                buffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {
                    showThreadMessage("读取失败");
                    try {
                        inStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    break;
                }
            }

        }
    }


    public void showThreadMessage(String str){
        Looper.prepare();
        Toast.makeText(mContext,str,Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    public void showMessage(String str){
        Toast.makeText(mContext,str,Toast.LENGTH_SHORT).show();
    }

}
