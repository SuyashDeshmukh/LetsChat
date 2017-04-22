package edu.csulb.android.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Suyash on 21-Apr-17.
 */

public class LauncherActivity extends ActionBarActivity {

    Button bluetooth;
    Button wifi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_activity);
        bluetooth=(Button)findViewById(R.id.bluetooth);
        wifi=(Button)findViewById(R.id.wifidirect);

        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bluetooth = new Intent(LauncherActivity.this,MainActivity.class);
                startActivity(bluetooth);
            }
        });

        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent wifi = new Intent(LauncherActivity.this,WiFiDirectActivity.class);
                startActivity(wifi);
            }
        });

    }
}
