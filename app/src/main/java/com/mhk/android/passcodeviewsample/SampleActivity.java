package com.mhk.android.passcodeviewsample;

import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mhk.android.passcodeview.PasscodeView;

/**
 * Created by Mehedi Hasan Khan on 9/6/15.
 */
public class SampleActivity extends AppCompatActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_activity);

        final PasscodeView pcView = (PasscodeView) findViewById(R.id.passcode_view);

        pcView.postDelayed(new Runnable() {
            @Override
            public void run() {
                pcView.requestToShowKeyboard();
            }
        }, 400);

        pcView.setPasscodeEntryListener(new PasscodeView.PasscodeEntryListener() {
            @Override
            public void onPasscodeEntered(String passcode) {
                Toast.makeText(SampleActivity.this, "Passcode entered: " + passcode, Toast.LENGTH_SHORT).show();
            }
        });

        Log.d("SAMPLE", "Activity created");
    }
}
