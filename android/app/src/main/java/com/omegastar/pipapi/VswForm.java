package com.omegastar.pipapi;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class VswForm extends AppCompatActivity {

    private String ip;
    private BroadcastReceiver receiver;
    private ProgressBar pbCom;
    public boolean dd; //debug detail
    public boolean d;  //debug
    JSONObject v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vsw);
        findViewById(R.id.etVsw).setEnabled(false);

        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;


        TextView tv=(TextView)(findViewById(R.id.tvVsw));
        tv.setText(BuildConfig.APPLICATION_ID + " " + versionName + " " + Integer.toString(versionCode));

        dd=false;
        d=true;

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        try {
            v=new JSONObject(b.getString("json"));
            if (d) Log.d("PIPAPI", b.getString("json"));

            showVswInfo(v);
        } catch (JSONException e) {
            if (d) Log.d("PIPAPI", "Error receiving auto-tracker-what as json");
            e.printStackTrace();
        }
    }

    private void showVswInfo(JSONObject json) {
        EditText et=(EditText)(findViewById(R.id.etVsw));

        String s = null;
        et.setText(json.toString());
        try {
            //s = json.getJSONObject("vsw").getJSONObject("sd").getJSONObject("result").getJSONObject("addon").getString("version");
            et.append("Sport Devil:\t");
            //et.append(s);
            et.append("\n");
        } catch (Exception e){//JSONException e) {
            Log.d("PIPAPI", "Error filling edit text with json object when decoding vsw");
            e.printStackTrace();
        }
    }

}
