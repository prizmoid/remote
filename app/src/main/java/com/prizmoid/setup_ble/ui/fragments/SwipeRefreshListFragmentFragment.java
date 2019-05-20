/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prizmoid.setup_ble.ui.fragments;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;


import com.prizmoid.setup_ble.model.BtDevice;
import com.prizmoid.setup_ble.BtManager;
import com.prizmoid.setup_ble.MainActivity;
import com.prizmoid.setup_ble.R;
import com.prizmoid.setup_ble.ui.btdevicecontrol.BtDeviceControlViewModel;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;

/**
 * A sample which shows how to use {@link android.support.v4.widget.SwipeRefreshLayout} within a
 * {@link android.support.v4.app.ListFragment} to add the 'swipe-to-refresh' gesture to a
 * {@link android.widget.ListView}. This is provided through the provided re-usable
 * {@link SwipeRefreshListFragment} class.
 *
 * <p>To provide an accessible way to trigger the refresh, this app also provides a refresh
 * action item. This item should be displayed in the Action Bar's overflow item.
 *
 * <p>In this sample app, the refresh updates the ListView with a random set of new items.
 *
 * <p>This sample also provides the functionality to change the colors displayed in the
 * {@link android.support.v4.widget.SwipeRefreshLayout} through the options menu. This is meant to
 * showcase the use of color rather than being something that should be integrated into apps.
 */
public class SwipeRefreshListFragmentFragment extends SwipeRefreshListFragment implements BtManager.OnBtScanListener, BtDevice.OnBtDeviceConnected {

    private static final String TAG = SwipeRefreshListFragmentFragment.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int LIST_ITEM_COUNT = 1;
    private static final int REQUEST_ENABLE_BT = 4000;
    private boolean mBleAvailable = false;
    private  BtManager mBleScanner;
    private BtDeviceControlViewModel mViewModel;
    private BtDevice mBtDevice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(BtDeviceControlViewModel.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect trackers.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }
        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted," +
                            " this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    // BEGIN_INCLUDE (setup_views)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getContext(), "BLE is not available on this device", Toast.LENGTH_SHORT).show();
            mBleAvailable = false;
        }
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(getContext(), "BT is not available on this device", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        //mBleScanner = new BleBtManager(getContext(), this);
        mBleScanner = new BtManager(getContext(), this);
        List<String> list = new ArrayList(LIST_ITEM_COUNT);

        /**
         * Create an ArrayAdapter to contain the data for the ListView. Each item in the ListView
         * uses the system-defined simple_list_item_1 layout that contains one TextView.
         */
        ListAdapter adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                list);

        // Set the adapter between the ListView and its backing data.
        setListAdapter(adapter);
        // BEGIN_INCLUDE (setup_refreshlistener)
        /**
         * Implement {@link SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to
         * refresh" gesture, SwipeRefreshLayout invokes
         * {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}. In
         * {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that
         * refreshes the content. Call the same method in response to the Refresh action from the
         * action bar.
         */
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "onRefresh called from SwipeRefreshLayout");

                initiateRefresh();
            }
        });


        // END_INCLUDE (setup_refreshlistener)
        if (mBleScanner.checkBtAvailable()) {
            setRefreshing(true);
            initiateRefresh();
        } else {
            ((ArrayAdapter<String>) getListAdapter()).add(getString(R.string.enable_bt));
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    // END_INCLUDE (setup_views)

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (requestCode != RESULT_CANCELED) {
                setRefreshing(true);
                initiateRefresh();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        onOptionsItemSelected(menu.getItem(0).getSubMenu().getItem(1));

    }

    // BEGIN_INCLUDE (setup_refresh_menu_listener)

    /**
     * Respond to the user's selection of the Refresh action item. Start the SwipeRefreshLayout
     * progress bar, then initiate the background task that refreshes the content.
     *
     * <p>A color scheme menu item used for demonstrating the use of SwipeRefreshLayout's color
     * scheme functionality. This kind of menu item should not be incorporated into your app,
     * it just to demonstrate the use of color. Instead you should choose a color scheme based
     * off of your application's branding.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                Log.i(TAG, "Refresh menu item selected");

                // Start our refresh background task
                initiateRefresh();
                return true;

            case R.id.menu_color_scheme_1:
                Log.i(TAG, "setColorScheme #1");
                item.setChecked(true);
                //mBleScanner.writeToDevice("VER");
                // Change the colors displayed by the SwipeRefreshLayout by providing it with 4
                // color resource ids
                setColorScheme(R.color.color_scheme_1_1, R.color.color_scheme_1_2,
                        R.color.color_scheme_1_3, R.color.color_scheme_1_4);
                return true;

            case R.id.menu_color_scheme_2:
                //mBleScanner.writeToDevice("DATE");
                Log.i(TAG, "setColorScheme #2");
                item.setChecked(true);

                // Change the colors displayed by the SwipeRefreshLayout by providing it with 4
                // color resource ids
                setColorScheme(R.color.color_scheme_2_1, R.color.color_scheme_2_2,
                        R.color.color_scheme_2_3, R.color.color_scheme_2_4);
                return true;

            case R.id.menu_color_scheme_3:
                Log.i(TAG, "setColorScheme #3");
                item.setChecked(true);
                //mBleScanner.writeToDevice("REP");
                // Change the colors displayed by the SwipeRefreshLayout by providing it with 4
                // color resource ids
                setColorScheme(R.color.color_scheme_3_1, R.color.color_scheme_3_2,
                        R.color.color_scheme_3_3, R.color.color_scheme_3_4);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // END_INCLUDE (setup_refresh_menu_listener)

    // BEGIN_INCLUDE (initiate_refresh)

    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     */
    private void initiateRefresh() {
        Log.i(TAG, "initiateRefresh");
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
        adapter.clear();
        Toast.makeText(getContext(), getString(R.string.looking_ble), Toast.LENGTH_LONG).show();
        /**
         * Execute the background task, which uses {@link android.os.AsyncTask} to load the data.
         */
        //new DummyBackgroundTask().execute();
        mBleScanner.startScan(true, mBleAvailable);

    }
    // END_INCLUDE (initiate_refresh)

    // BEGIN_INCLUDE (refresh_complete)

    /**
     * When the AsyncTask finishes, it calls onRefreshComplete(), which updates the data in the
     * ListAdapter and turns off the progress bar.
     */
    private void onRefreshComplete(List<String> result) {
        Log.i(TAG, "onRefreshComplete");

        // Remove all items from the ListAdapter, and then replace them with the new items
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
        adapter.clear();
        if (result.size() > 0) {
            for (String cheese : result) {
                adapter.add(cheese);
            }
        } else {
            adapter.add(getString(R.string.not_found));
        }
        // Stop the refreshing indicator
        setRefreshing(false);
        if (adapter.getCount() == 1) {
            selectBtDevice(0);
        }
    }

    @Override
    public void onScanUpdate(final BtDevice bt) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
                String str = bt.name == null ? bt.address : bt.name;
                if (str == null) {
                    str = "unknown device";
                }
                adapter.add(str + getState(bt.device) );
            }
        });
        //leDeviceListAdapter.notifyDataSetChanged();
    }

    private String getState(BluetoothDevice device) {
        if (device != null) {
            switch (device.getBondState()) {
                case BluetoothDevice.BOND_BONDED:
                    return " - bonded";
                case BluetoothDevice.BOND_BONDING:
                    return " - bonding";
                case BluetoothDevice.BOND_NONE:
                    return " - not bonded";
            }
        }
        return "";
    }

    @Override
    public void onScanComplete(List<BtDevice> btDevices) {
        final List<String> list = new ArrayList<>(btDevices.size());
        for (BtDevice bt : btDevices) {
            String str =bt.name == null ? bt.address : bt.name;
            if (str == null) {
                str = "unknown device";
            }
            list.add(str  + getState(bt.device));
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onRefreshComplete(list);
            }
        });

    }

    @Override
    public void onBtDeviceConnected() {
        ((MainActivity) getActivity()).onNavigate(1);
    }
    // END_INCLUDE (refresh_complete)

    /**
     * Dummy {@link AsyncTask} which simulates a long running task to fetch new cheeses.
     */
    private class DummyBackgroundTask extends AsyncTask<Void, Void, List<String>> {

        static final int TASK_DURATION = 3 * 1000; // 3 seconds

        @Override
        protected List<String> doInBackground(Void... params) {
            // Sleep for a small amount of time to simulate a background-task
            try {
                Thread.sleep(TASK_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<String> list = new ArrayList(LIST_ITEM_COUNT);
            for (int i = 0; i < LIST_ITEM_COUNT; i++) {
                list.add("test " + ((int) (Math.random() * 100)));
            }
            // Return a new random list of cheeses
            return list;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);

            // Tell the Fragment that the refresh has completed
            onRefreshComplete(result);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBleScanner != null) {
            mBleScanner.startScan(false, mBleAvailable);
            mBleScanner.release();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        selectBtDevice(position);
    }

    private void selectBtDevice(int position) {
        setRefreshing(false);
        mBleScanner.startScan(false, mBleAvailable);
        mBtDevice = mBleScanner.getDevice(position);
        if (mBtDevice != null) {
            String str = mBtDevice.device != null ? mBtDevice.device.getAddress() : mBtDevice.address;
            Toast.makeText(getContext(), str + " " + position, Toast.LENGTH_SHORT).show();
            if (mViewModel.connect(mBtDevice, this)) {
                //TODO: open new fragment
                Log.d(TAG, "Connected to " + mBtDevice.toString());

            }
        }
    }
}
