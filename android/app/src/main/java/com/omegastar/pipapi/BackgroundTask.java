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
        dd=false;
        d=true;
        if (dd) Log.d("PIPAPI","Starting");
    }

    private class ResponseDht {
        boolean ok;
        String t;
        String h;
        ResponseDht(){ok=false; t = "--"; h="--";}
    }

    private class ResponseKodi {
        boolean ok;
        String jsonResponse;
        ResponseKodi(){ok=false;jsonResponse="";}
    }


    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (dd) Log.d("PIPAPI", "onHandleIntent-enter");
        Bundle b = intent.getExtras();

        String ip = b.getString("IP");
        if (d) Log.d("PIPAPI", "Querying to ------------- " + ip + " ----·---------" + intent.getAction() + "-----------");


        if (intent.getAction().equals("REFRESH")) {

            ResponseDht dht = queryDht(ip, ":5056/api/v1.0/dht/", "sensors/now");
            sendResultDhtUI(dht.ok,dht.t,dht.h);
            ResponseKodi kodi = queryKodi(ip, ":5057/api/v1.0/kodi/", "status", null);
            sendResultKodiUI(kodi.ok,kodi.jsonResponse);
            //TODO queryPir(); sendResultKodiUI(kodi.ok,kodi.jsonResponse);
        }
        else if (intent.getAction().equals("TRACKER")) {
            ResponseKodi kodi = queryKodi(ip, ":5057/api/v1.0/kodi/", "tracker", b.getString("JSONDATA"));
            kodi = queryKodi(ip, ":5057/api/v1.0/kodi/", "status", null);
            sendResultKodiUI(kodi.ok,kodi.jsonResponse);
        }
        else if (intent.getAction().equals("REBOOT")) {
            ResponseKodi kodi = queryKodi(ip, ":5057/api/v1.0/kodi/", "reboot", null);
            //sendResultKodiUI(kodi.ok,kodi.jsonResponse);
        }

        if (dd) Log.d("PIPAPI", "onHandleIntent-exit");
    }

    //":5056/api/v1.0/dht/"
    protected String sendQuery (String ip, String apibase, String rest, String jsondata) {
        String rt=null;
        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL("http://" + ip + apibase + rest);
            urlConnection = (HttpURLConnection) url.openConnection();
            // set the connection timeout to 5 seconds and the read timeout to 10 seconds
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(15000);
            if ((urlConnection != null) && (jsondata != null && !jsondata.isEmpty())) {
                // POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                //urlConnection.setRequestProperty("Content-Length", "" + Integer.toString(otherParametersUrServiceNeed.getBytes().length));
                urlConnection.setUseCaches(false);
                urlConnection.connect();
                if (dd)
                    Log.d("PIPAPI", "POST IP:" + ip + " " + apibase + rest + " " + jsondata);
                DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
                printout.writeBytes(jsondata);
                printout.flush();
                printout.close();
            } else {
                if (dd) Log.d("PIPAPI", "GET method at :" + ip + " " + rest);
            }

            if (urlConnection != null) {

                StringBuilder translateResult = new StringBuilder(200);
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    char[] buffer = new char[8192];
                    int charsRead;
                    while ((charsRead = in.read
                            (buffer)) > 0) {
                        translateResult.append(buffer, 0, charsRead);
                    }
                    if (dd) Log.d("PIPAPI", "response:" + translateResult.toString());
                } catch (Exception e) {
                    if (d) Log.d("PIPAPI", "Exception" + e.getLocalizedMessage());
                }
                //Process json
                try {
                    if (dd) Log.d("PIPAPI", "response in raw chars:" + translateResult.toString());
                    rt = translateResult.toString();
                } catch (Exception e) {
                    if (d) Log.d("PIPAPI", "Exception " + e.getLocalizedMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (d) Log.d("PIPAPI", "onHandleIntent-exit-exception");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return rt;
    }
    protected void queryPir() {
    }
    protected ResponseKodi queryKodi(String ip, String apibase, String rest, String jsondata) {

        if (dd) Log.d("PIPAPI", "queryKodi");
        ResponseKodi rt=new ResponseKodi();
        try {
            String response = this.sendQuery(ip, apibase, rest,jsondata);
            if (response != null  && !response.equals("")) {
                JSONObject object;
                if (rest.contains("status")) {
                    if (dd) Log.d("PIPAPI", "JSON status" + response);
                    object = new JSONObject(response);
                    if (object.getString("result").equals("OK")) {
                        rt.ok = true;
                        rt.jsonResponse = response;
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            if (d) Log.d("PIPAPI", "queryKodi-exception");
        }
        return rt;
    }
    protected ResponseDht queryDht(String ip, String apibase, String rest) {
        if (dd) Log.d("PIPAPI", "queryDhtr");
        ResponseDht rt=new ResponseDht();
        try {
            String response = this.sendQuery(ip, apibase, rest,null);
            if (response != null  && !response.equals("")) {
                JSONObject object;
                if (rest.contains("sensors")) {
                    if (dd) Log.d("PIPAPI", "ARRAY is expected");
                    JSONArray jsonarray = new JSONArray(response);
                    //Only get 1st one, TODO manage all if use case really
                    object = jsonarray.getJSONObject(0);
                    if (dd) Log.d("PIPAPI", "First item:" + object.toString());
                    rt.t = object.getString("temperature");
                    rt.h = object.getString("humidity");
                    rt.ok = true;
                } else {
                    if (dd) Log.d("PIPAPI", "OBJECT is expected");
                    object = new JSONObject(response);
                    rt.ok = (object.getString("result") == "OK");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            if (d) Log.d("PIPAPI", "queryDht-exception");
        }
        return rt;
    }


    public void sendResultKodiUI(boolean r,String res) {
        if (d) Log.d("PIPAPI", "sendResultKodiUI:" + r );
        Intent intent = new Intent("REFRESH_KODI");
        if(r){
            intent.putExtra("RESULT", "OK");
            if (d) Log.d("PIPAPI", "sendResultKodiUI:" + res );
            intent.putExtra("JSON", res);
        }else {
            intent.putExtra("RESULT", "KO");
        }

        sendResultUI(intent);
    }

    public void sendResultDhtUI(boolean r,String t , String h) {
        if (d) Log.d("PIPAPI", "sendResultDhtUI:" +r + " " + t + " " + h);
        Intent intent = new Intent("REFRESH_DHT");
        if(r){
            intent.putExtra("RESULT", "OK");
        }else {
            intent.putExtra("RESULT", "KO");
        }

        if(t != "")
            intent.putExtra("TEMP", t);
        if(h != "")
            intent.putExtra("HUM", h);

        sendResultUI(intent);
    }

    public void sendResultUI(Intent intent) {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
        if (d) Log.d("PIPAPI", "sendResultUI:");
        broadcaster.sendBroadcast(intent);
    }
}





