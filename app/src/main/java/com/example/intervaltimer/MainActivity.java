package com.example.intervaltimer;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String TAG = "MainActivity";

    List<IntervalTest> intervalTestList; // Contains all the lists from the DB

    ListView listView; // The listView currently used by the main activity.

    UniqueID uid;
    /*
        All the following variables are used for database access.
     */
    DaoMaster.DevOpenHelper helper;
    SQLiteDatabase db;
    DaoMaster daoMaster;
    DaoSession daoSession;

    private String newName; // Holds the name of the new test being created.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new DaoMaster.DevOpenHelper(this, "intervals-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();

        List<UniqueID> ids = daoSession.getUniqueIDDao().loadAll();

        if (!ids.isEmpty()) {
            Log.d("UID:", "Unique ID Availiable");
            Log.d("UID:", String.valueOf(ids.get(0).getUnique()));
            this.uid = ids.get(0);
        } else {
            Log.d("UID:", "Unique ID not Availiable");
            this.uid = new UniqueID();
            this.uid.setUnique(new Long(0));
            daoSession.getUniqueIDDao().insert(this.uid);
        }

        intervalTestList = daoSession.getIntervalTestDao().loadAll();

        CustomListAdapter whatever = new CustomListAdapter(this, intervalTestList);

        listView = findViewById(R.id.listViewID);
        listView.setAdapter(whatever);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                long unique = intervalTestList.get(position).getId();
                String message = intervalTestList.get(position).getName();
                intent.putExtra("uid", unique);
                intent.putExtra("title", message);

                startActivity(intent);
            }
        });

        findAllWearableDevices();
        onAppearStartWatch();
    }

    /*
        Function Name: reloadList

        The purpose of this function is to reload the list once a new test is added.
     */
    public void relaodList() {
        intervalTestList = daoSession.getIntervalTestDao().loadAll();

        listView = findViewById(R.id.listViewID);
        CustomListAdapter adapter = (CustomListAdapter) listView.getAdapter();

        Collections.sort(intervalTestList);
        adapter.updateList(intervalTestList);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
        Function Name: onOptionsItemSelected

        The purpose of this function is to enable a menu that allows for new tests to be created.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.new_test) {

            LayoutInflater li = LayoutInflater.from(MainActivity.this);
            View promptsView = li.inflate(R.layout.dialog_custom, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this);

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final EditText userInput = promptsView
                    .findViewById(R.id.editTextDialogUserInput);
            userInput.setSingleLine(true);

            final TextInputEditText editText = findViewById(R.id.textViewInputLayout);

            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK", null);
            alertDialogBuilder.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            final AlertDialog alertDialog = alertDialogBuilder.create();

            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialogInterface) {
                    Button sButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);

                    sButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (userInput.getText().toString().length() != 0) {

                                boolean exists = false;

                                for (int i = 0; i < intervalTestList.size(); i++) {
                                    if (userInput.getText().toString().equals(intervalTestList.get(i).getName())) {
                                        exists = true;
                                    }
                                }

                                if (exists == false) {
                                    IntervalTest test = new IntervalTest();
                                    test.setName(userInput.getText().toString());
                                    test.setDate(new Date());

                                    daoSession.getIntervalTestDao().insert(test);

                                    relaodList();

                                    uid.setUnique(uid.getUnique() + 1);
                                    test.setUid(uid.getUnique());

                                    daoSession.update(uid);

                                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                                    intent.putExtra("title", userInput.getText().toString());
                                    intent.putExtra("uid", uid.getUnique());

                                    startActivity(intent);
                                    dialogInterface.dismiss();
                                } else {
                                    userInput.setError("Test name already exists.");
                                }
                            } else {
                                userInput.setError("Test name is blank.");
                            }
                        }
                    });
                }
            });
            // show it
            alertDialog.show();

            return true;
        }
    return super.onOptionsItemSelected(item);
}

    @Override
    public void onResume() {
        super.onResume();

        relaodList();
    }

    @Override
    public void finish() {

    }

    private void findAllWearableDevices() {
        Task<List<Node>> NodeListTask = Wearable.getNodeClient(this).getConnectedNodes();

        NodeListTask.addOnCompleteListener(new OnCompleteListener<List<Node>>() {
            @Override
            public void onComplete(Task<List<Node>> task) {

                if (task.isSuccessful()) {
                    Log.d("Nodes", "Node request succeeded.");

                } else {
                    Log.d("Nodes", "Node request failed to return any results.");
                }
            }
        });
    }

    public void onAppearStartWatch() {

        // Trigger an AsyncTask that will query for a list of connected nodes and send a
        // "start-activity" message to each connected node.
        new StartWearableActivityTask().execute();
        Log.d("Started", "Activity");
    }

    @WorkerThread
    private void sendStartActivityMessage(String node) {

        Task<Integer> sendMessageTask =
                Wearable.getMessageClient(this).sendMessage(node, START_ACTIVITY_PATH, new byte[0]);

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            Integer result = Tasks.await(sendMessageTask);
            Log.d(TAG, "Message sent: " + result);

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }
    }

    @WorkerThread
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();

        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            List<Node> nodes = Tasks.await(nodeListTask);

            for (Node node : nodes) {
                results.add(node.getId());
            }

        } catch (ExecutionException exception) {

        } catch (InterruptedException exception) {

        }

        return results;
    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                Log.d("Watch", "Found");
                sendStartActivityMessage(node);
            }
            return null;
        }
    }

}