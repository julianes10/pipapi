package com.omegastar.pipapi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver receiver;
    private TextView tvTemp;
    private TextView tvHum;
    public boolean dd; //debug detail
    public boolean d;  //debug
    Handler mHandler;
    int nCounter=0;
    private final int interval = 30000;

    boolean timerActive=false;
    public boolean queryOngoing=false;
    private String ip;
    SharedPreferences preferences;
    JSONObject latestKodiJson;



    private void setIp (String ip) {
        EditText editText = (EditText) findViewById(R.id.keyString1);
        editText.setText(ip);
        this.ip = ip;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latestKodiJson=new JSONObject();
        dd=false;
        d=true;



        findViewById(R.id.comResultDht).setBackgroundColor(getResources().getColor(R.color.comNoaction));
        findViewById(R.id.pbComKODI).setVisibility(View.INVISIBLE);
        findViewById(R.id.comResultKodi).setBackgroundColor(getResources().getColor(R.color.comNoaction));
        findViewById(R.id.pbComKODI).setVisibility(View.INVISIBLE);

        findViewById(R.id.etTrackerSettings).setEnabled(false);
        tvTemp=findViewById(R.id.tvTemp);
        tvHum=findViewById(R.id.tvHum);


        if (d) Log.d("PIPAPI", "*************************CREATE MAIN*********************************");



        // Prepare for the answer
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (d) Log.d("PIPAPI", "rx rx ------------rx rx--------------rx rx");
                String action = intent.getAction();
                Bundle b = intent.getExtras();
                String result = "KO";
                if (action.equals("REFRESH_DHT")) { //----------------------------------------------
                    try {
                        result = b.getString("RESULT");

                        String aux;
                        aux = b.getString("TEMP");
                        tvTemp.setText(aux + "ºC");

                        aux = b.getString("HUM");
                        tvHum.setText(aux + "%");

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (d) Log.d("PIPAPI", "Error receiving data in main activity");
                    }


                    updatePb(result,R.id.comResultDht,R.id.pbComDHT);

                }else if (action.equals("REFRESH_KODI")){ //----------------------------------------------
                    try {
                        result = b.getString("RESULT");
                        if (result.equals("OK")) {
                            if (dd) Log.d("PIPAPI", "kodi json" + b.getString("JSON"));
                            JSONObject json = new JSONObject(b.getString("JSON"));
                            boolean playOn = json.getBoolean("play");
                            if (playOn) {
                                String title = json.getString("title");
                                ((TextView)(findViewById(R.id.tvPlayOn))).setText("ON");
                                ((TextView)(findViewById(R.id.tvPlayOn))).setTextColor(getResources().getColor(R.color.green));
                                ((TextView)(findViewById(R.id.tvPlayerTitle))).setText(title);
                                findViewById(R.id.tvPlayerTitle).setSelected(true);

                            } else {
                                ((TextView)(findViewById(R.id.tvPlayOn))).setText("OFF");
                                ((TextView)(findViewById(R.id.tvPlayOn))).setTextColor(getResources().getColor(R.color.red));
                                ((TextView)(findViewById(R.id.tvPlayerTitle))).setText("NONE");
                            }

                            if (json.getBoolean("hdmi")){
                                ((TextView)(findViewById(R.id.tvHdmi))).setText("YES");
                            }
                            else {
                                ((TextView)(findViewById(R.id.tvHdmi))).setText("NO");
                            }
                            SharedHelper.fillEditText((EditText)(findViewById(R.id.etTrackerSettings)),json);
                            latestKodiJson=json;

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (d) Log.d("PIPAPI", "Error receiving data in main activity");
                    }

                    if (!result.equals("OK")){
                        ((TextView)(findViewById(R.id.tvPlayOn))).setText("--");
                        ((TextView)(findViewById(R.id.tvPlayerTitle))).setText("--");
                        ((TextView)(findViewById(R.id.tvHdmi))).setText("--");
                        ((TextView)(findViewById(R.id.etTrackerSettings))).setText("--");
                    }

                    updatePb(result,R.id.comResultKodi,R.id.pbComKODI);
                    queryOngoing=false;
                }
            }
        };




        //public static final String PREF_FILE_NAME = "PrefFile";
        //SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        String aux="192.168.1.45";
        try {

            preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            aux=preferences.getString("ip",null);

            }
        catch (Exception e) {
            e.printStackTrace();
            if (d) Log.d("PIPAPI", "Error getting settings ip");
        }
        setIp(aux);
        refreshData();

    }


    private void updatePb(String result,int rBox,int rPb) {
        if (result.equals("START")) {
            findViewById(rBox).setBackgroundColor(getResources().getColor(R.color.comOnprogress));
            findViewById(rPb).setVisibility(View.VISIBLE);
        }
        else
        {
            findViewById(rPb).setVisibility(View.INVISIBLE);
            if (result.equals("OK")) {
                findViewById(rBox).setBackgroundColor(getResources().getColor(R.color.comOK));
            }
            else {
                findViewById(rBox).setBackgroundColor(getResources().getColor(R.color.comKO));
            }
        }
    }



    public void apply(View view) {

        EditText editText = (EditText) findViewById(R.id.keyString1);
        setIp(editText.getText().toString());
        refreshData();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ip", editText.getText().toString()); // value to store
        editor.commit();
    }


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter("REFRESH_DHT"));
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                        new IntentFilter("REFRESH_KODI"));
        timerActive=true;
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        timerActive=false;
        super.onStop();

    }

    private Handler handler = new Handler();
    private Runnable MyRunnable = new Runnable(){
        public void run() {

            if (d) Log.d("PIPAPI", "Timer expired ---- QUERY NEW DATA");
            // Send the query
            if (timerActive) {
                if (!queryOngoing) {
                    refreshData();
                }
                else {
                    if (d) Log.d("PIPAPI", "Not sending a query to refresh, it is ongoing other one running");
                }
            }
        }
    };

    private void refreshData() {

        Intent intent2 = new Intent(MainActivity.this, BackgroundTask.class);
        intent2.setAction("REFRESH");
        intent2.putExtra("IP", ip);
        startService(intent2);


        handler.postAtTime(MyRunnable, System.currentTimeMillis() + interval);
        handler.postDelayed(MyRunnable, interval);

        updatePb("START",R.id.comResultDht,R.id.pbComDHT);
        updatePb("START",R.id.comResultKodi,R.id.pbComKODI);
        queryOngoing=true;

    }

    public void reboot(View view) {
        if (d) Log.d("PIPAPI", "Asking for reboot....");

        Intent intent2 = new Intent(MainActivity.this, BackgroundTask.class);
        intent2.setAction("REBOOT");
        intent2.putExtra("IP", ip);
        startService(intent2);
    }

    public void showTracker(View view) {
        Intent intent = new Intent(this, TrackerForm.class);
        intent.putExtra("IP", ip);
        intent.putExtra("auto-tracker-what", latestKodiJson.toString());
        onStop();
        startActivity(intent);
    }


}


