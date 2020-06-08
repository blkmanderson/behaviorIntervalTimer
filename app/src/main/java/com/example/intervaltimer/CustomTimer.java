package com.example.intervaltimer;

import android.widget.TextView;

public interface CustomTimer {
    void startNextTimer(int i, TextView textView);
    void timerPaused(int i, TextView textView, long durationRemaining);
    void timerResumed();
    void tick();

}