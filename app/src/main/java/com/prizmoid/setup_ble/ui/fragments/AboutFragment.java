package com.prizmoid.setup_ble.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prizmoid.setup_ble.R;
import com.prizmoid.setup_ble.model.Attr;
import com.prizmoid.setup_ble.model.BtDevice;
import com.prizmoid.setup_ble.model.Cmd;
import com.prizmoid.setup_ble.ui.btdevicecontrol.BtDeviceControlViewModel;


/**
 * A placeholder fragment containing a simple view.
 */
public class AboutFragment extends Fragment {
    private static final String TAG = "AboutFragment";

    private static final String ARG_SECTION_NUMBER = "section_number";

    private BtDeviceControlViewModel pageViewModel;

    public static AboutFragment newInstance(int index) {
        Log.v(TAG, "New instance of AboutFrag");
        AboutFragment fragment = new AboutFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate of AboutFrag");
        pageViewModel = ViewModelProviders.of(getActivity()).get(BtDeviceControlViewModel.class);
        if (pageViewModel != null) {
            pageViewModel.readDeviceData();
        } else  {
            Log.e(TAG, "model is null!");
        }
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
    }



    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_about, container, false);
        pageViewModel.getCurrentDevice().observe(this, new Observer<BtDevice>() {
            @Override
            public void onChanged(@Nullable BtDevice d) {
                Cmd c = d.getCommand("VER");
                Attr a = c.getAttr("HW_TYPE");
                ((TextView)root.findViewById(R.id.type_value)).setText(" : " + a.a_value);
                a = c.getAttr("HW_VERSION");
                ((TextView)root.findViewById(R.id.hw_value)).setText(" : " + a.a_value);
                a = c.getAttr("FW_VERSION");
                ((TextView)root.findViewById(R.id.fw_value)).setText(" : " + a.a_value);
                a = c.getAttr("FLEX_VERSION");
                ((TextView)root.findViewById(R.id.flx_value)).setText(" : " + a.a_value);
                a = c.getAttr("RELEASE_DATE");
                ((TextView)root.findViewById(R.id.rdate_value)).setText(" : " + a.a_value);

                c = d.getCommand("DEV");
                a = c.getAttr("DEV_NAME");
                ((TextView)root.findViewById(R.id.device_value)).setText(" : " + a.a_value);
                a = c.getAttr("DEV_ID");
                ((TextView)root.findViewById(R.id.devid_value)).setText(" : " + a.a_value);
                a = c.getAttr("DEV_SERIAL");
                ((TextView)root.findViewById(R.id.devserial_value)).setText(" : " + a.a_value);

                c = d.getCommand("BOOTVER");
                a = c.getAttr("BOOT_VERSION");
                ((TextView)root.findViewById(R.id.boot_value)).setText(" : " + a.a_value);

                c = d.getCommand("HWCFG");
                a = c.getAttr("SERIAL");
                ((TextView)root.findViewById(R.id.devserial_value)).setText(" : " + a.a_value);

                a = c.getAttr("HW_TYPE");
                ((TextView)root.findViewById(R.id.type_value)).setText(" : " + a.a_value);

            }
        });
        return root;
    }
}