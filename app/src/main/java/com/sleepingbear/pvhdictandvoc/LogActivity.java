package com.sleepingbear.pvhdictandvoc;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        Bundle b = getIntent().getExtras();
        if ( "F1".equals(b.getString("CODE")) ) {
            try {
                FileInputStream fis = getApplicationContext().openFileInput(CommConstants.infoFileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader buffreader = new BufferedReader(isr);

                String readString = buffreader.readLine();
                String log = "";
                while (readString != null) {
                    log += readString + "\n";
                    readString = buffreader.readLine();
                }
                ((TextView) findViewById(R.id.my_a_log_tv1)).setText(log);

                isr.close();
                fis.close();
            } catch (Exception e) {
            }
        }
    }
}
