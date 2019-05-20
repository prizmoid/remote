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
public class DashFragment extends Fragment {

    private static final String TAG = "DashFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private BtDeviceControlViewModel pageViewModel;
    private boolean mAddServer = false;

    public static DashFragment newInstance(int index) {
        DashFragment fragment = new DashFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        //pageViewModel.setIndex(index);
    }

    public String getUpdateCmd() {
        //collect date for saving
        return "";
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_dash, container, false);

        pageViewModel.getCurrentDevice().observe(this, new Observer<BtDevice>() {
            @Override
            public void onChanged(@Nullable BtDevice d) {
                Cmd c = d.getCommand("SERVER0");
                if (c == null) {
                    mAddServer = true;
                    return;
                }
                Attr a = c.getAttr("IP");
                ((TextView)root.findViewById(R.id.serverip_value)).setText(a.a_value);
                a = c.getAttr("PORT");
                ((TextView)root.findViewById(R.id.serverport_value)).setText(a.a_value);
                a = c.getAttr("LOGIN");
                ((TextView)root.findViewById(R.id.serverlogin_value)).setText(a.a_value);
                a = c.getAttr("PASS");
                ((TextView)root.findViewById(R.id.serverpass_value)).setText(a.a_value);
            }
        });
        return root;
    }
}