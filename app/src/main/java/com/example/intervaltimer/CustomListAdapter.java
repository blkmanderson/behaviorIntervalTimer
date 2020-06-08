package com.example.intervaltimer;

        import android.util.Log;
        import android.widget.ArrayAdapter;
        import android.app.Activity;
        import android.view.View;
        import android.widget.TextView;
        import android.view.ViewGroup;
        import android.view.LayoutInflater;

        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.List;

public class CustomListAdapter extends ArrayAdapter {

    //to reference the Activity
    private final Activity context;

    //to store the list of countries
    private List<IntervalTest> tests;


    public CustomListAdapter(Activity context, List<IntervalTest> tests){

        super(context,R.layout.listview_row , tests);

        this.context=context;
        this.tests = tests;

    }

    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.listview_row, null,true);

        //this code gets references to objects in the listview_row.xml file
        TextView nameTextField = rowView.findViewById(R.id.nameTextView);
        TextView detailTextView = rowView.findViewById(R.id.detailTextView);

        //this code sets the values of the objects to values from the arrays
        String pattern = "MM.dd.YYYY";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        nameTextField.setText(tests.get(position).getName());
        detailTextView.setText(simpleDateFormat.format(tests.get(position).getDate()));

        return rowView;

    }

    public void updateList(List<IntervalTest> tests) {
        this.tests.clear();
        this.tests.addAll(tests);
        this.notifyDataSetChanged();
    }

}


