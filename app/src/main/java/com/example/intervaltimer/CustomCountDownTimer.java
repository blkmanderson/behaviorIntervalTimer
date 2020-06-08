package com.example.intervaltimer;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

public class CustomCountDownTimer extends CountDownTimer {

    CustomTimer customTimer;

    int index;
    TextView textField;
    int type;
    Context context;

    long timeRemaining;

    public CustomCountDownTimer(long duration, long interval, TextView textField, int startingIndex, int type, Context context, CustomTimer customTimer ) {
        super(duration, interval);

        this.textField = textField;
        this.index = startingIndex;
        this.context = context;
        this.timeRemaining = duration;
        this.textField.setText(lengthToString(duration));
        this.type = type;
        this.customTimer = customTimer;

        setTextColor();
    }

    @Override
    public void onTick(long l) {
        this.timeRemaining = l;
        this.textField.setText(lengthToString(l));

        if(type == 1) {
            customTimer.tick();
        }
    }

    @Override
    public void onFinish() {
        this.index ++;
        Log.d("Index",  String.valueOf(this.index));
        customTimer.startNextTimer(this.index, this.textField);
    }

    public void pause() {
        customTimer.timerPaused(index, textField, timeRemaining);
    }

    private void setTextColor() {
        switch (type) {
            case 1:
                this.textField.setTextColor(ContextCompat.getColor(context, R.color.green));
                break;
            case 0:
                this.textField.setTextColor(ContextCompat.getColor(context, R.color.black));
                break;
        }
    }

    private String lengthToString(long length) {
        long time = length;

        String hoursString;
        String minString;
        String secString;

        long hours = time / 3600000;
        time = (time - (hours * 3600000));

        long min = time / 60000;
        time = (time - (min * 60000));

        long sec = time / 1000;

        if(hours < 10) {
            hoursString = "0" + hours;
        } else {
            hoursString = String.valueOf(hours);
        }

        if(min < 10) {
            minString = "0" + min;
        } else {
            minString = String.valueOf(min);
        }

        if(sec < 10) {
            secString = "0" + sec;
        } else {
            secString = String.valueOf(sec);
        }


        return hoursString + ":" + minString + ":" + secString;
    }


}
