package com.example.intervaltimer;

import android.content.Context;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.math.BigInteger;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;

public class MainActivity extends FragmentActivity implements AmbientModeSupport.AmbientCallbackProvider, MessageClient.OnMessageReceivedListener {

    ImageView imageView;

    private static final String TYPE_KEY = "com.example.key.type";
    private static final String FINISHED_KEY = "/finished";
    private static final String PAUSED_KEY = "/paused";
    private static final String READY_KEY = "/ready";
    private static final String TAG = "MainActivity";
    private static final String VIBRATION_KEY = "/vibration";
    private static final String RESET_KEY = "/reset";
    private static final String WAIT_KEY = "/wait";

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout li= findViewById(R.id.watchBackground);
        li.setBackgroundResource(R.color.black);

        textView = findViewById(R.id.textView);
        textView.setText("Waiting...");

        // Enables Always-on
        AmbientModeSupport.attach(this);
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }

    private void updateWatchFace(BigInteger type) {
        LinearLayout li= findViewById(R.id.watchBackground);
        if(type.equals(BigInteger.valueOf(0))) {
            li.setBackgroundResource(R.color.black);
        } else {
            li.setBackgroundResource(R.color.green);
        }
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(TYPE_KEY)) {
            textView.setVisibility(View.INVISIBLE);
            BigInteger type = new BigInteger(1, messageEvent.getData());
            updateWatchFace(type);
        } else if (messageEvent.getPath().equals(FINISHED_KEY)) {
            textView.setText("Finished");
            textView.setVisibility(View.VISIBLE);
        } else if (messageEvent.getPath().equals(PAUSED_KEY)) {
            textView.setText("Paused");
            textView.setVisibility(View.VISIBLE);
        } else if (messageEvent.getPath().equals(READY_KEY)) {
            textView.setText("Ready");
            textView.setVisibility(View.VISIBLE);
            updateWatchFace(BigInteger.valueOf(0));
        } else if (messageEvent.getPath().equals(VIBRATION_KEY)) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(1000, DEFAULT_AMPLITUDE));
        } else if (messageEvent.getPath().equals(WAIT_KEY)) {
            textView.setText("Waiting...");
        }
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    /** Customizes appearance for Ambient mode. (We don't do anything minus default.) */
    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        /** Prepares the UI for ambient mode. */
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);
        }
        /**
         * Updates the display in ambient mode on the standard interval. Since we're using a custom
         * refresh cycle, this method does NOT update the data in the display. Rather, this method
         * simply updates the positioning of the data in the screen to avoid burn-in, if the display
         * requires it.
         */
        @Override
        public void onUpdateAmbient() {
            super.onUpdateAmbient();
        }

        /** Restores the UI to active (non-ambient) mode. */
        @Override
        public void onExitAmbient() {
            super.onExitAmbient();
        }
    }
}
