package com.prizmoid.setup_ble.ui.btdevicecontrol;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.app.ActionBar;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prizmoid.setup_ble.BluetoothLeService;
import com.prizmoid.setup_ble.R;
import com.prizmoid.setup_ble.model.BtDevice;
import com.prizmoid.setup_ble.ui.fragments.AboutFragment;
import com.prizmoid.setup_ble.ui.fragments.DashFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BtDeviceControlFragment extends Fragment {
    private static final String TAG = "BtDevControlFragment";
    private static final String LIST_NAME = "list_name";
    private static final String LIST_UUID = "3";
    private BtDeviceControlViewModel mViewModel;
    private boolean connected;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
    private ViewPager mViewPager;
    private int mCurrent = 0;

    public static BtDeviceControlFragment newInstance() {
        return new BtDeviceControlFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bt_device_control_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(BtDeviceControlViewModel.class);
       // mViewModel.readDeviceData();

        mCurrent = 2;
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getContext(), getChildFragmentManager());

        mViewPager = getView().findViewById(R.id.view_pager);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrent = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        ((TextView)getView().findViewById(R.id.title)).setTextAppearance(getContext(), R.style.style_device_name);
        ((TextView)getView().findViewById(R.id.title)).setText(mViewModel.mDeviceLiveData.getValue().name);
        TabLayout tabs = getView().findViewById(R.id.tabs);
        tabs.setupWithViewPager(mViewPager);
        FloatingActionButton fab = getView().findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrent == 0) {
                    Fragment f = ((SectionsPagerAdapter)mViewPager.getAdapter()).getItem(0);
                    String updateCmd = ((DashFragment) f).getUpdateCmd();
                }
                Snackbar.make(view, getResources().getText(R.string.action_save_full), Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_save, null).show();
            }
        });


        // TODO: Use the ViewModel
        mViewModel.mDeviceLiveData.observe(this, new Observer<BtDevice>() {
             @Override
              public void onChanged(@Nullable BtDevice data) {
                  // update ui.
                 Log.d(TAG, "Live data changed " + data);
                 mViewPager.setCurrentItem(mCurrent);
                 Fragment f = ((SectionsPagerAdapter)mViewPager.getAdapter()).getItem(mCurrent);
                 switch (mCurrent) {
                     case 0:
                         updateDataDash(f, data);
                         break;
                     case 1:
                         updateDataConfig(f, data);
                         break;
                     case 2:
                         updateDataAbout(f, data);
                         break;
                 }
              }
        });
        /*getView().findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.doAction();
            }
        });*/
    }

    private void updateDataAbout(Fragment f, BtDevice data) {
        //AboutFragment af = (AboutFragment) f;
       // ((TextView)af.getView().findViewById(R.id.section_label)).setText(data.getCommand("VER").getAttr("HW_VERSION").a_value);
        Log.d(TAG, "About: " + data.getCommand("VER").getAttr("HW_VERSION").a_value);
    }

    private void updateDataConfig(Fragment f, BtDevice data) {

    }

    private void updateDataDash(Fragment f, BtDevice data) {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    // Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device. This can be a
// result of read or notification operations.
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                connected = true;
//                updateConnectionState("connected");
//                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
//                updateConnectionState(R.string.disconnected);
//                invalidateOptionsMenu();
 //               clearUI();
            } else if (BluetoothLeService.
                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
 //               displayGattServices(bluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
   //             displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    //  populate the data structure that is bound to the
    // ExpandableListView on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().
                getString(R.string.unknown_service);
        String unknownCharaString = getResources().
                getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics =
                new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData =
                    new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, "00002a05-0000-1000-8000-00805f9b34fb");
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =
                        new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, "r");
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

    }
}
