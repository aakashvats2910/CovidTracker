package com.worldhelper.covidtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.worldhelper.covidtracker.capture.NearbyCollector;
import com.worldhelper.covidtracker.capture.NewLocationCapture;
import com.worldhelper.covidtracker.util.LocalVariables;

public class GoOutActivity extends AppCompatActivity {

    private Switch main_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_out);

        main_switch = findViewById(R.id.main_switch);

        String onOrOff = LocalVariables.getDefaults("onoroff", getApplicationContext());
//        if (onOrOff != null) {
//            if (onOrOff.equals("1")) main_switch.setChecked(true);
//            if (onOrOff.equals("0")) main_switch.setChecked(false);
//        }

        main_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (main_switch.isChecked()) {
                    Toast.makeText(getApplicationContext(), "ON" , Toast.LENGTH_SHORT).show();
                    startService();
                } else {
                    Toast.makeText(getApplicationContext(), "OFF" , Toast.LENGTH_SHORT).show();
                    stopService();
                }
            }
        });
    }

    public void startService() {
        NearbyCollector.startCollector();
        try {
            NewLocationCapture.deleteByQuery();
        } catch (Exception e) {
            System.out.println("()()()() ERROR : " + e.getMessage());
        }
        Intent serviceIntent = new Intent(this, LocationCollector.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        ContextCompat.startForegroundService(this, serviceIntent);
        LocalVariables.setDefaults("onoroff","1", getApplicationContext());
    }
    public void stopService() {
        NearbyCollector.stopCollector();
        try {
            NewLocationCapture.deletePreviousPosition();
        } catch (Exception e) {
            System.out.println("()()()() ERROR : " + e.getMessage());
        }
        Intent serviceIntent = new Intent(this, LocationCollector.class);
        stopService(serviceIntent);
        try {
            NewLocationCapture.stopLocationService();
        } catch (Exception e) {
            System.out.println("()()()() ERROR : " + e.getMessage());
        }
        LocalVariables.setDefaults("onoroff","0", getApplicationContext());
    }


}
