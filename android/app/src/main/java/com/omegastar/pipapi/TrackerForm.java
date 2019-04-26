package com.omegastar.pipapi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TrackerForm extends AppCompatActivity {

    private String ip;
    private BroadcastReceiver receiver;
    private ProgressBar pbCom;
    public boolean dd; //debug detail
    public boolean d;  //debug
    JSONObject latestKodiJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker);
        findViewById(R.id.etVsw).setEnabled(false);

        dd=false;
        d=true;

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        ip = b.getString("IP");
        try {
            latestKodiJson= new JSONObject(b.getString("auto-tracker-what"));
            showTrackerInfo(latestKodiJson);
        } catch (JSONException e) {
            if (d) Log.d("PIPAPI", "Error receiving auto-tracker-what as json");
            e.printStackTrace();
        }

        // Prepare for the answer
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (d) Log.d("PIPAPI", "rx rx ------------TRACKER--------------rx rx ");
                String action = intent.getAction();
                Bundle b = intent.getExtras();
                String result = "KO";
                if (action.equals("REFRESH_KODI")){ //----------------------------------------------
                    try {
                        result = b.getString("RESULT");
                        if (result.equals("OK")) {
                            if (dd) Log.d("PIPAPI", "kodi json" + b.getString("JSON"));
                            latestKodiJson=new JSONObject(b.getString("JSON"));
                            showTrackerInfo(latestKodiJson);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (d) Log.d("PIPAPI", "Error receiving data in tracker form activity");
                        }
                }
            }
        };


/*
        // Prepare for the answer
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(carEmBackgroundTask.CAREM_MESSAGE);
                TextView textView = findViewById(R.id.text2display);
                textView.setText("Car at "+carip+" is "+s);
                pbCom.setVisibility(View.INVISIBLE);

            }
        };

        // Send the query
        Intent intent2 = new Intent(this, carEmBackgroundTask.class);
        intent2.putExtra("rest","status");
        intent2.putExtra("CARIP",carip);
        pbCom = findViewById(R.id.pbStatus);
        pbCom.setVisibility(View.VISIBLE);
        startService(intent2);*/
    }

    private void showTrackerInfo(JSONObject json) {
        SharedHelper.fillEditText((EditText)(findViewById(R.id.etTracker)),json);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter("REFRESH_KODI"));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    public void addTrackerContents(View view) {
        try {
            Intent intent2 = new Intent(TrackerForm.this, BackgroundTask.class);
            intent2.setAction("TRACKER");
            intent2.putExtra("IP", ip);

            /*JSONObject contents=latestKodiJson.getJSONObject("auto-tracker-what");*/



            /* FROM SCRATCH*/
            JSONObject contents = new JSONObject();
            JSONArray  contents_data = new JSONArray();
            JSONObject keystrings = new JSONObject();
            JSONArray  keystrings_data = new JSONArray();

            String[] splited = ((TextView)(findViewById(R.id.tvKeyStrings))).getText().toString().split("\\s+");

            for (String s: splited) {
                keystrings_data.put(s);
            }
            keystrings.putOpt ("keystrings",keystrings_data);
            contents_data.put(keystrings);
            contents.putOpt("content",contents_data);


            if (d) Log.d("PIPAPI", "POSTING INFO:" + contents.toString());
            intent2.putExtra("JSONDATA", contents.toString());
            startService(intent2);
        } catch (JSONException e) {
            e.printStackTrace();
            if (d) Log.d("PIPAPI", "Error processing tracker data in this form");
        }
    }

    public void wipeTrackerContents(View view) {
        Intent intent2 = new Intent(TrackerForm.this, BackgroundTask.class);
        intent2.setAction("TRACKER");
        intent2.putExtra("IP", ip);
        intent2.putExtra("JSONDATA", "{ \"content\":[] }");

        startService(intent2);
    }
}
