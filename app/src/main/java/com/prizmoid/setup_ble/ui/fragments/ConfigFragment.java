package com.prizmoid.setup_ble.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prizmoid.setup_ble.R;
import com.prizmoid.setup_ble.ui.btdevicecontrol.BtDeviceControlViewModel;


/**
 * A placeholder fragment containing a Settings view.
 */
public class ConfigFragment extends Fragment {
    private static final String TAG = "ConfigFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";

    private BtDeviceControlViewModel pageViewModel;

    public static ConfigFragment newInstance(int index) {
        ConfigFragment fragment = new ConfigFragment();
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
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_config, container, false);

        return root;
    }
}