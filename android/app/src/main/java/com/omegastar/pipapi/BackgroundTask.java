package com.omegastar.pipapi;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class BackgroundTask extends IntentService {

    public boolean dd; //debug detail
    public boolean d;  //debug

    public BackgroundTask() {
        super("HelloIntentService");
        dd=true;
        d=true;
        if (dd) Log.d("PIPAPI","Starting");
    }


    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (dd) Log.d("PIPAPI", "onHandleIntent-enter");
        URL url;
        HttpURLConnection urlConnection = null;
        String result="KO";
        String t="--";
        String h="--";
        try {
            Bundle b = intent.getExtras();

            String rest="";
            String tag = "";
            String value = "";

            String ip = b.getString("IP");
            rest = b.getString("rest");
            try {
                tag = b.getString("tag");
                value = b.getString("value");
            }  catch (Exception e) {
                e.printStackTrace();
                if (d) Log.d("PIPAPI", "no tar or value");
            }

            url = new URL("http://" + ip + ":5056/api/v1.0/dht/" + rest);
            urlConnection = (HttpURLConnection) url.openConnection();

            if (tag != null && !tag.isEmpty()) {
               // POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                //urlConnection.setRequestProperty("Content-Length", "" + Integer.toString(otherParametersUrServiceNeed.getBytes().length));
                urlConnection.setUseCaches (false);

                urlConnection.connect();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put(tag, value);
                if (d) Log.d("PIPAPI", "POST IP:"+ip+" " + rest + " " + tag + " " + value );

                DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
                printout.writeBytes(jsonParam.toString());
                printout.flush ();
                printout.close ();
            }
            else {
                if (d) Log.d("PIPAPI", "GET IP:"+ip+" " + rest);
            }

            StringBuilder translateResult = new StringBuilder(200);
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                char[] buffer = new char[8192];
                int charsRead;
                while ((charsRead = in.read
                        (buffer)) > 0) {
                    translateResult.append(buffer, 0, charsRead);
                }
                if (dd) Log.d("PIPAPI","response:"+ translateResult.toString());
            } catch (Exception e) {
                if (d) Log.d("PIPAPI", "Exception" + e.getLocalizedMessage());
            }
            //Process json
            try {
                if (dd) Log.d("PIPAPI","response in raw chars:"+ translateResult.toString());
                JSONObject object;
                if (rest.contains("sensors")){
                    if (dd) Log.d("PIPAPI","ARRAY is expected");
                    JSONArray jsonarray = new JSONArray(translateResult.toString());
                    //Only get 1st one, TODO manage all if use case really
                    object = jsonarray.getJSONObject(0);
                    if (dd) Log.d("PIPAPI","First item:"+ object.toString());
                    t = object.getString("temperature");
                    h = object.getString("humidity");
                    result = "OK";
                }
                else {
                    if (dd) Log.d("PIPAPI","OBJECT is expected");
                    object = new JSONObject(translateResult.toString());
                    result = object.getString("result");
                }
                if (dd) Log.d("PIPAPI","response json:"+ object.toString());

            } catch (Exception e) {
                if (d) Log.d("PIPAPI","Exception "+ e.getLocalizedMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (d) Log.d("PIPAPI", "onHandleIntent-exit-exception");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        sendResultUI(result,t,h);
        if (dd) Log.d("PIPAPI", "onHandleIntent-exit");
    }


    public void sendResultUI(String r,String t , String h) {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
        if (d) Log.d("PIPAPI", "sendResultUI:" +r + " " + t + " " + h);
        Intent intent = new Intent("COM_RESULT");
        if(r != "")
            intent.putExtra("RESULT", r);
        if(t != "")
            intent.putExtra("TEMP", t);
        if(h != "")
            intent.putExtra("HUM", t);

        broadcaster.sendBroadcast(intent);
    }
}



