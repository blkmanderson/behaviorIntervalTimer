package com.example.intervaltimer;

import android.graphics.Color;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.ItemTouchHelper.Callback.makeFlag;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {

    private List<Interval> data;

    private final StartDragListener mStartDragListener;

    private boolean editable;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        View rowView;
        ImageView imageView;
        ImageView lableImage;

        public MyViewHolder(View itemView) {
            super(itemView);

            rowView = itemView;
            mTitle = itemView.findViewById(R.id.txtTitle);
            imageView = itemView.findViewById(R.id.imageView);
            lableImage = itemView.findViewById(R.id.labelImg);
        }
    }

    public RecycleViewAdapter(List<Interval> data, StartDragListener startDragListener) {
        mStartDragListener = startDragListener;
        this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.mTitle.setText(lengthToString(data.get(position).getLength()));

        if(this.editable == true) {
          holder.imageView.setImageResource(R.drawable.ic_delete_24px);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_menu_24px);
        }
        if(data.get(position).getType() == 1) {
            holder.lableImage.setImageResource(R.drawable.ic_green_label);
        } else {
            holder.lableImage.setImageResource(R.drawable.ic_label_24px);
        }

        holder.imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("Adptr:", "Action_Down");
                        if(isEditable()) {
                            mStartDragListener.deleteInterval(position);
                            return true;
                        }
                        mStartDragListener.requestDrag(holder);
                        return true;
                    case MotionEvent.ACTION_UP:
                        Log.d("Adptr:", "Action_Up");
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        Log.d("Adptr:", "Action_Cancel");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("Adptr:", "Action_Move");
                        break;
                }

                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                data.get(i).setPlace(new Integer(i+1));
                data.get(i+1).setPlace(new Integer(i));

                Collections.swap(data, i, i + 1);

            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                data.get(i).setPlace(new Integer((i-1)));
                data.get(i - 1).setPlace(new Integer(i));

                Collections.swap(data, i, i - 1);

            }
        }
        notifyItemMoved(fromPosition, toPosition);
        mStartDragListener.endDrag();
    }

    @Override
    public void onRowSelected(MyViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.GRAY);

    }

    @Override
    public void onRowClear(MyViewHolder myViewHolder) {

        myViewHolder.rowView.setBackgroundColor(Color.WHITE);
    }

    public String lengthToString(long length) {
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

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }
}