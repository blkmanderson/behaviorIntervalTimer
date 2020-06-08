package com.example.intervaltimer;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.samsung.android.sdk.accessory.SAAgentV2;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class DetailActivity extends AppCompatActivity implements StartDragListener, CustomTimer {

    private static final String TYPE_KEY = "com.example.key.type";
    private static final String TAG = "DetailActivity";
    private static final String FINISHED_KEY = "/finished";
    private static final String PAUSED_KEY = "/paused";
    private static final String READY_KEY = "/ready";
    private static final String VIBRATION_KEY = "/vibration";
    private static final String WAIT_KEY = "/wait";

    private ProviderService mSenderService = null;

    private SAAgentV2.RequestAgentCallback mAgentCallback = new SAAgentV2.RequestAgentCallback() {

        @Override
        public void onAgentAvailable(SAAgentV2 agent) {
            mSenderService = (ProviderService) agent;
        }

        @Override
        public void onError(int errorCode, String message) {
            Log.e(TAG, "Agent initialization error: " + errorCode + ". ErrorMsg: " + message);
        }
    };
    private long currentTransId;
    private List<Long> mTransactions = new ArrayList<Long>();

    long uid;

    IntervalTest test;
    List<Interval> intervals;

    int type = 1;

    RecyclerView recyclerView;
    RecycleViewAdapter mAdapter;
    ItemTouchHelper touchHelper;

    TextView remainingDTView;
    AlertDialog currentAlert;

    DaoMaster.DevOpenHelper helper;
    SQLiteDatabase db;
    DaoMaster daoMaster;
    DaoSession daoSession;

    CustomCountDownTimer currentTimer;

    DataClient dataClient;
    Boolean vibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        recyclerView = findViewById(R.id.recyclerView);

        vibrate = false;

        SAAgentV2.requestAgent(getApplicationContext(), ProviderService.class.getName(), mAgentCallback);

        helper = new DaoMaster.DevOpenHelper(this, "intervals-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();

        String testTitle = getIntent().getStringExtra("title");
        uid = getIntent().getLongExtra("uid", 0);

        this.test = daoSession.getIntervalTestDao()
                .queryBuilder()
                .where(IntervalTestDao.Properties.Name.eq(testTitle))
                .unique();

        this.intervals = this.test.getTests();

        dataClient = Wearable.getDataClient(this);

        populateRecyclerView();

        new ReadyWatchFace().execute();

        setTitle(testTitle);
        getSupportActionBar();

        final FloatingActionButton fabClose = findViewById(R.id.fab_stopEdit);
        fabClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               RecycleViewAdapter rVAdptr = (RecycleViewAdapter) recyclerView.getAdapter();
               rVAdptr.setEditable(false);
               rVAdptr.notifyDataSetChanged();

               fabClose.hide();

               FloatingActionButton fabNewInterval = findViewById(R.id.fab1);
               fabNewInterval.show();

               FloatingActionButton fabStartTimer = findViewById(R.id.fab2);
               fabStartTimer.show();
            }
        });
        fabClose.hide();

        FloatingActionButton fabNewInterval = findViewById(R.id.fab1);
        fabNewInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater li = LayoutInflater.from(DetailActivity.this);
                View promptsView = li.inflate(R.layout.dialog_new_interval, null);

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        DetailActivity.this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final RadioGroup rGroup = promptsView.findViewById(R.id.radioGroup);

                final NumberPicker hPicker = promptsView.findViewById(R.id.hourPicker);
                hPicker.setMinValue(0);
                hPicker.setMaxValue(23);

                final NumberPicker mPicker = promptsView.findViewById(R.id.minPicker);
                mPicker.setMinValue(0);
                mPicker.setMaxValue(59);

                final NumberPicker sPicker = promptsView.findViewById(R.id.secPicker);
                sPicker.setMinValue(0);
                sPicker.setMaxValue(59);


                rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {

                        switch(checkedId) {
                            case(R.id.black):
                                type = 0;
                                break;
                            case(R.id.green):
                                type = 1;
                                break;
                        }
                    }
                });

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        long hours = hPicker.getValue() * 3600000;
                                        long minutes = mPicker.getValue() * 60000;
                                        long seconds = sPicker.getValue() * 1000;

                                        if(hours != 0 || minutes != 0 || seconds != 0) {

                                            Interval interval = new Interval();
                                            interval.setType(type);
                                            interval.setNumber(uid);
                                            interval.setPlace(intervals.size() + 1);
                                            interval.setLength(hours + minutes + seconds);

                                            daoSession.getIntervalDao().insert(interval);
                                            daoSession.getIntervalDao().save(interval);

                                            interval.setTest(test);
                                            test.addInterval(interval);

                                            daoSession.getIntervalTestDao().update(test);

                                            intervals = test.getTests();

                                            type = 1;
                                        } else {
                                            type = 1;
                                            dialog.cancel();
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        recyclerView.getAdapter().notifyDataSetChanged();
                        daoSession.getIntervalTestDao().save(test);
                    }
                });
                // show it
                alertDialog.show();



            }
        });

        FloatingActionButton fabStartTimer = findViewById(R.id.fab2);
        fabStartTimer.setOnClickListener(new View.OnClickListener() {

          @Override
            public void onClick(View view) {

              if (intervals.size() > 0) {
                  LayoutInflater li = LayoutInflater.from(DetailActivity.this);
                  View promptsView = li.inflate(R.layout.dialog_timer, null);

                  final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                          DetailActivity.this);

                  final TextView timerText = promptsView.findViewById(R.id.timer);
                  remainingDTView = promptsView.findViewById(R.id.remainingText);

                  // set prompts.xml to alertdialog builder
                  alertDialogBuilder.setView(promptsView);

                  alertDialogBuilder.setCancelable(false);
                  alertDialogBuilder.setNegativeButton("Stop", null);
                  alertDialogBuilder.setPositiveButton("Start", null);
                  alertDialogBuilder.setNeutralButton("Close", null);
                  // create alert dialog
                  currentAlert = alertDialogBuilder.create();

                  currentAlert.setOnShowListener(new DialogInterface.OnShowListener() {

                      @Override
                      public void onShow(final DialogInterface dialog) {

                          final Button pButton = currentAlert.getButton(AlertDialog.BUTTON_POSITIVE);
                          final Button negButton = currentAlert.getButton(AlertDialog.BUTTON_NEGATIVE);
                          final Button neutButton = currentAlert.getButton(AlertDialog.BUTTON_NEUTRAL);
                          final ArrayList<CustomCountDownTimer> countDownTimers = new ArrayList<CustomCountDownTimer>();

                          Log.d("Intervals", intervals.toString());

                          final Interval current = intervals.get(0);

                          currentTimer = new CustomCountDownTimer(current.getLength(), 1000, timerText,
                                  0, current.getType(), getBaseContext(), DetailActivity.this);

                          if ((intervals.size() - 1) == 1) {
                              remainingDTView.setText(1 + " Interval Remaining");
                          } else if ((intervals.size() - 1) == 0) {
                              remainingDTView.setText("0 Intervals Remaining");
                          } else {
                              remainingDTView.setText((intervals.size() - 1) + " Intervals Remaining");
                          }

                          negButton.setEnabled(false);

                          pButton.setOnClickListener(new View.OnClickListener() {

                              @Override
                              public void onClick(View view) {
                                  new UpdateWatchFaceTask().execute();

                                  if(mSenderService != null) {
                                      if(currentTimer.type == 0) {
                                          mSenderService.send("Black");
                                      } else {
                                          mSenderService.send("Green");
                                      }
                                  }

                                  negButton.setEnabled(!negButton.isEnabled());
                                  pButton.setEnabled(!pButton.isEnabled());
                                  neutButton.setEnabled(!neutButton.isEnabled());
                                  currentTimer.start();
                              }
                          });

                          neutButton.setOnClickListener(new View.OnClickListener() {

                              @Override
                              public void onClick(View view) {
                                  new ReadyWatchFace().execute();

                                  if(mSenderService != null) {
                                      mSenderService.send("Ready");
                                  }

                                  dialog.dismiss();
                                  currentAlert = null;
                                  currentTimer = null;
                                  remainingDTView = null;
                              }
                          });

                          negButton.setOnClickListener(new View.OnClickListener() {

                              @Override
                              public void onClick(View view) {
                                  pButton.setEnabled(!pButton.isEnabled());
                                  negButton.setEnabled(!negButton.isEnabled());
                                  neutButton.setEnabled(!neutButton.isEnabled());

                                  new PausedWatchFaceTask().execute();

                                  if(mSenderService != null) {
                                      mSenderService.send("Pause");
                                  }

                                  currentTimer.cancel();
                                  currentTimer.pause();
                              }
                          });
                      }
                  });

                  // show it
                  currentAlert.show();
              }else {
                  Snackbar.make(view, "There are no intervals to start a timer.", Snackbar.LENGTH_SHORT).show();
              }
          }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        final MenuItem toggleservice = menu.findItem(R.id.toggleservice);
        final Switch aSwitch = (Switch) toggleservice.getActionView();
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true) {
                    Snackbar.make(recyclerView, "Watch vibration turned on.", Snackbar.LENGTH_SHORT).show();
                    vibrate = true;
                } else {
                    Snackbar.make(recyclerView, "Watch vibration turned off.", Snackbar.LENGTH_SHORT).show();
                    vibrate = false;
                }
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.edit) {

            if(intervals.size() > 0) {
                RecycleViewAdapter rAdptr = (RecycleViewAdapter) recyclerView.getAdapter();
                rAdptr.setEditable(true);
                rAdptr.notifyDataSetChanged();

                FloatingActionButton fabStartTimer = findViewById(R.id.fab2);
                fabStartTimer.hide();

                FloatingActionButton fabNewInterval = findViewById(R.id.fab1);
                fabNewInterval.hide();

                FloatingActionButton fabClose = findViewById(R.id.fab_stopEdit);
                fabClose.show();
            } else {

                Snackbar.make(this.recyclerView, "There are no intervals to edit.", Snackbar.LENGTH_SHORT).show();

            }

            return true;
        }

        if (id == R.id.delete) {

            LayoutInflater li = LayoutInflater.from(DetailActivity.this);
            View promptsView = li.inflate(R.layout.dialog_delete, null);

            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    DetailActivity.this);

            alertDialogBuilder.setView(promptsView);

            alertDialogBuilder.setCancelable(false);

            alertDialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                }
            });

            alertDialogBuilder.setNeutralButton("DELETE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            for(int i = 0; i < intervals.size(); i++) {
                                intervals.get(i).delete();
                                intervals.remove(i);
                            }

                            test.delete();
                            finish();
                        }
            });

            alertDialogBuilder.create();
            alertDialogBuilder.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateRecyclerView() {

        Collections.sort(intervals);

        mAdapter = new RecycleViewAdapter(intervals,this);

        ItemTouchHelper.Callback callback = new ItemMoveCallback(mAdapter);
        touchHelper  = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        if(mSenderService != null) {
            mSenderService.send("Wait");
            mSenderService.releaseAgent();
            mSenderService = null;
        }

        new WaitingTextTask().execute();

        super.onDestroy();
    }

    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }

    @Override
    public void endDrag() {
        for(int i = 0; i < intervals.size(); i++) {
            daoSession.getIntervalDao().update(intervals.get(i));
        }
    }

    @Override
    public void timerPaused(int index, TextView textView, long timeRemaining) {

        Interval current = intervals.get(index);

        currentTimer = new CustomCountDownTimer(timeRemaining, 1000, textView, index, current.getType(), getBaseContext(), DetailActivity.this);
    }

    @Override
    public void timerResumed() {
        currentTimer.start();
    }

    @Override
    public void tick() {

        if (vibrate == true) {
            new SetWatchVibration().execute();

            if(mSenderService != null) {
                mSenderService.send("Vibe");
            }
        }
    }

    @Override
    public void startNextTimer(int index, TextView textView) {

        if (index < intervals.size()) {
            Interval current = intervals.get(index);

            if ((intervals.size() - index) == 1) {
                remainingDTView.setText(0 + " Interval Remaining");
            } else {
                remainingDTView.setText((intervals.size() - (index + 1)) + " Intervals Remaining");
            }

            currentTimer = new CustomCountDownTimer(current.getLength(), 1000, textView,
                    index, current.getType(), getBaseContext(), DetailActivity.this);

            new UpdateWatchFaceTask().execute();

            if(mSenderService != null) {
                if(currentTimer.type == 0) {
                    mSenderService.send("Black");
                } else {
                    mSenderService.send("Green");
                }
            }

            currentTimer.start();
        } else {

            new FinishedWatchFaceTask().execute();
            if(mSenderService != null) {
                mSenderService.send("Fin");
            }

            textView.setText("FINISHED");
            currentAlert.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
            currentAlert.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(true);
        }
    }

    public void deleteInterval(int i) {
        final int position = i;

        LayoutInflater li = LayoutInflater.from(DetailActivity.this);
        final View promptsView = li.inflate(R.layout.dialog_delete_interval, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                DetailActivity.this);

        alertDialogBuilder.setView(promptsView);

        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Canel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });

        alertDialogBuilder.setNeutralButton("DELETE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d("Delete", "interval " + position);
                Interval delete = intervals.get(position);
                delete.delete();
                intervals.remove(delete);

                for(int i = position + 1; i < intervals.size(); i++) {
                    intervals.get(i).setPlace(intervals.get(i).getPlace() + 1);
                }

                recyclerView.getAdapter().notifyDataSetChanged();
                final FloatingActionButton fabClose = findViewById(R.id.fab_stopEdit);
                fabClose.performClick();
            }
        });

        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    private class UpdateWatchFaceTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                Log.d("Watch", "Found");
                updateWatchFace(node);
            }
            return null;
        }
    }
    private void updateWatchFace(String node) {

        BigInteger sendType = BigInteger.valueOf(currentTimer.type);

        Task<Integer> sendTask = Wearable.getMessageClient(this).sendMessage(node, TYPE_KEY, sendType.toByteArray());

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            Integer result = Tasks.await(sendTask);
            Log.d(TAG, "Message sent: " + result);

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }

    }

    private class SetWatchVibration extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                Log.d("Watch", "Found");
                setWatchVibration(node);
            }
            return null;
        }
    }
    private void setWatchVibration(String node) {
        Task<Integer> sendTask = Wearable.getMessageClient(this).sendMessage(node, VIBRATION_KEY, new byte[0]);

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            Integer result = Tasks.await(sendTask);
            Log.d(TAG, "Message sent: " + result);

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }
    }

    private class PausedWatchFaceTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                Log.d("Watch", "Found");
                pausedWatchFace(node);
            }
            return null;
        }
    }
    private void pausedWatchFace(String node) {
        Task<Integer> sendTask = Wearable.getMessageClient(this).sendMessage(node, PAUSED_KEY, new byte[0]);

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            Integer result = Tasks.await(sendTask);
            Log.d(TAG, "Message sent: " + result);

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }
    }

    private class ReadyWatchFace extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                Log.d("Watch", "Found");
                readyWatchFace(node);
            }
            return null;
        }
    }

    private class WaitingTextTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                Log.d("Watch", "Found");
                waiting(node);
            }
            return null;
        }
    }
    private void waiting(String node) {
        Task<Integer> sendTask = Wearable.getMessageClient(this).sendMessage(node, WAIT_KEY, new byte[0]);

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            Integer result = Tasks.await(sendTask);
            Log.d(TAG, "Message sent: " + result);

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }
    }

    private class FinishedWatchFaceTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                Log.d("Watch", "Found");
                finishedWatchFace(node);
            }
            return null;
        }
    }
    private void finishedWatchFace(String node) {
        Task<Integer> sendTask = Wearable.getMessageClient(this).sendMessage(node, FINISHED_KEY, new byte[0]);

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            Integer result = Tasks.await(sendTask);
            Log.d(TAG, "Message sent: " + result);

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }
    }

    private void readyWatchFace(String node) {
        Task<Integer> sendTask = Wearable.getMessageClient(this).sendMessage(node, READY_KEY, new byte[0]);

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            Integer result = Tasks.await(sendTask);
            Log.d(TAG, "Message sent: " + result);

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }
    }

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
            Log.e(TAG, "Task failed: " + exception);
        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }

        return results;
    }

}