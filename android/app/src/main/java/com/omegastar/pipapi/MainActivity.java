package com.omegastar.pipapi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver receiver;
    private TextView comResult;
    private ProgressBar pbCom;
    private TextView tvTemp;
    private TextView tvHum;
    public boolean dd; //debug detail
    public boolean d;  //debug
    Handler mHandler;
    int nCounter=0;
    private final int interval = 30000;

    private String ip;

    private void setIp () {
        EditText editText = (EditText) findViewById(R.id.etIp);
        this.ip = editText.getText().toString();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dd=false;
        d=true;

        comResult = findViewById(R.id.comResult);
        comResult.setBackgroundColor(getResources().getColor(R.color.comNoaction));
        pbCom = findViewById(R.id.pbCom);
        pbCom.setVisibility(View.INVISIBLE);
        tvTemp=findViewById(R.id.tvTemp);
        tvHum=findViewById(R.id.tvHum);




        // Prepare for the answer
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (d) Log.d("PIPAPI", "rx rx --------------------------------------------------");

                String result="KO";
                try {
                    Bundle b = intent.getExtras();

                    result = b.getString("RESULT");

                    String aux;
                    aux = b.getString("TEMP");
                    tvTemp.setText(aux+"ºC");

                    aux = b.getString("HUM");
                    tvHum.setText(aux+"%");

                }
                catch (Exception e) {
                    e.printStackTrace();
                    if (d) Log.d("PIPAPI", "Error receiving data in main activity");
                }

                if (result == "OK") comResult.setBackgroundColor(getResources().getColor(R.color.comOK));
                else                comResult.setBackgroundColor(getResources().getColor(R.color.comKO));
                pbCom.setVisibility(View.INVISIBLE);
            }
        };


        setIp();

        refreshData();

    }



    private void intent2target (String rest,String tag,String value) {
        Intent intent = new Intent(this, BackgroundTask.class);
        intent.putExtra("rest",rest);
        intent.putExtra("tag",tag);
        intent.putExtra("value",value);

        EditText editText = (EditText) findViewById(R.id.etIp);
        intent.putExtra("IP",ip);
        comResult.setBackgroundColor(getResources().getColor(R.color.comOnprogress));
        pbCom.setVisibility(View.VISIBLE);
        startService(intent);
    }



    public void apply(View view) {

        setIp();
        refreshData();
    }


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter("COM_RESULT")
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    private Handler handler = new Handler();
    private Runnable MyRunnable = new Runnable(){
        public void run() {

            if (d) Log.d("PIPAPI", "Timer expired ---- QUERY NEW DATA");
            // Send the query
            refreshData();
        }
    };

    private void refreshData() {
        Intent intent2 = new Intent(MainActivity.this, BackgroundTask.class);
        intent2.putExtra("rest", "sensors/now");
        intent2.putExtra("IP", ip);
        startService(intent2);


        handler.postAtTime(MyRunnable, System.currentTimeMillis() + interval);
        handler.postDelayed(MyRunnable, interval);
        comResult.setBackgroundColor(getResources().getColor(R.color.comOnprogress));
        pbCom.setVisibility(View.VISIBLE);
    }
}


