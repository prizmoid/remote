package com.prizmoid.setup_ble;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.prizmoid.setup_ble.ui.btdevicecontrol.BtDeviceControlFragment;
import com.prizmoid.setup_ble.ui.btdevicecontrol.BtDeviceControlViewModel;
import com.prizmoid.setup_ble.ui.fragments.SwipeRefreshListFragmentFragment;
import com.prizmoid.setup_ble.ui.fragments.WelcomeScreen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements WelcomeScreen.OnNavigateListener {

    private BtDeviceControlViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewModel = ViewModelProviders.of(this).get(BtDeviceControlViewModel.class);

        String strDate = "";
        try {
            SimpleDateFormat gmtDateTimeFormatter = new SimpleDateFormat("yyyyMMdd");
            Date timestamp = gmtDateTimeFormatter.parse(getText(R.string.release_date).toString());
            java.text.DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(getApplicationContext());
            strDate = dateFormat.format(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ((TextView)findViewById(R.id.textVersionName)).setText("v" +  BuildConfig.VERSION_NAME
                + ";  " + strDate);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            SwipeRefreshListFragmentFragment fragment = new SwipeRefreshListFragmentFragment();
            transaction.replace(R.id.content_fragment, fragment);
            transaction.commit();
            /* FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            WelcomeScreen fragment = new WelcomeScreen();
            fragment.setOnNavListener(this);
            transaction.replace(R.id.content_fragment, fragment);
            transaction.commit();*/
        }

    }

    @Override
    public void onNavigate(int action) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (action) {
            case 0:
                SwipeRefreshListFragmentFragment fragment1 = new SwipeRefreshListFragmentFragment();
                transaction.replace(R.id.content_fragment, fragment1);
                transaction.commit();
                break;
            case 1:
                BtDeviceControlFragment fragment2 = new BtDeviceControlFragment();
                transaction.replace(R.id.content_fragment, fragment2);
                transaction.commit();
            default:
                break;
        }
    }


}
