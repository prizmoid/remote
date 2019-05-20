package com.prizmoid.setup_ble.ui.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prizmoid.setup_ble.R;

public class WelcomeScreen extends Fragment {
    OnNavigateListener mCallback;

    public void setOnNavListener(OnNavigateListener activity) {
        mCallback = activity;
    }

    // Container Activity must implement this interface
    public interface OnNavigateListener {
        public void onNavigate(int action);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.welcome_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.buttonStartSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchResults();
            }
        });
    }

    void showSearchResults() {
        if (mCallback != null) {
            mCallback.onNavigate(0);
        }
    }
}
