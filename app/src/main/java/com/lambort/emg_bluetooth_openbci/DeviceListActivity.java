package com.lambort.emg_bluetooth_openbci;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joe on 2019/1/10.
 */

public class DeviceListActivity extends Activity {

    private ListView list_device;
    private TextView tv_data;
    private Button btn_findmore;
    private SimpleAdapter mPairedDevicesArrayAdapter;
    //处理接受数据线程传来的数据的Handler
    private final Handler receiveHandler = new ReceiveHandler(this);
    private Bluetooth_Lab mBluetooth_Lab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);
        mBluetooth_Lab = Bluetooth_Lab.init(this,receiveHandler);

        mBluetooth_Lab.Bluetooth_on();
        mBluetooth_Lab.init_list();

        tv_data = (TextView)findViewById(R.id.tv_data);

        list_device=(ListView)findViewById(R.id.list_device);
        this.mPairedDevicesArrayAdapter = new SimpleAdapter(this,
                mBluetooth_Lab.getBtDevices_list(),
                R.layout.device_list_item,
                new String[]{Bluetooth_Lab.Name,Bluetooth_Lab.Address},
                new int[]{R.id.device_name,R.id.device_address});
        list_device.setAdapter(mPairedDevicesArrayAdapter);


        list_device.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Adapter adapter=parent.getAdapter();
                Map<String,Object> map=(HashMap<String,Object>)adapter.getItem(position);
                mBluetooth_Lab.BtConnect((String) map.get(mBluetooth_Lab.Address));
            }
        });


    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    static class ReceiveHandler extends Handler {
        WeakReference<DeviceListActivity> mactivity;
        DeviceListActivity mInstance;
        TextView tv_data;

        public ReceiveHandler(DeviceListActivity activity) {
            mactivity = new WeakReference<DeviceListActivity>(activity);
            mInstance = mactivity.get();
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                tv_data = mInstance.tv_data;
                tv_data.setText((String) msg.obj);
            }
            catch (Exception e){

            }
        }
    }


}
