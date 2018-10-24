package com.omegastar.pipapi;


import android.util.Log;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SharedHelper{

    public static void fillEditText(EditText textbox, JSONObject json){

        String rawContents = null;
        try {
            rawContents = json.getString("auto-tracker-what");
            textbox.setText(rawContents);
            JSONObject tracker=json.getJSONObject("auto-tracker-what");
            JSONArray contents=tracker.getJSONArray("content");
            textbox.setText("");
            boolean isFirst=true;
            for(int i=0; i<contents.length(); i++){
                JSONObject keys=contents.getJSONObject(i);
                JSONArray k=keys.getJSONArray("keystrings");
                String aux="";
                for(int j=0; j<k.length(); j++){
                    aux=aux + k.getString(j)+" ";
                }
                if (!isFirst)
                    textbox.append("\n");
                textbox.append(aux);
                isFirst=false;
            }
        } catch (JSONException e) {
            Log.d("PIPAPI", "Error filling edit text with json object");
            e.printStackTrace();
        }

    }
}
