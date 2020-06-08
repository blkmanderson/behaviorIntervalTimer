package com.example.intervaltimer;

import androidx.recyclerview.widget.RecyclerView;

public interface StartDragListener {
    void requestDrag(RecyclerView.ViewHolder viewHolder);
    void endDrag();
    void deleteInterval(int i);
}
