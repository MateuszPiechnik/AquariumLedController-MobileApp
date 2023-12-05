package com.example.wifi;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class TimerUtils {
    public static void SendTime(MainActivity activity, String esp8266IpAddress, String command, int hour, int minute){
        int time = hour * 100 + minute;
        String url = "http://" + esp8266IpAddress + "/"+ command + "?value=" + time;

        RequestQueue queue = Volley.newRequestQueue(activity);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MojaAplikacja", "Odpowiedź od serwera: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Obsłuż błąd, jeśli wystąpi
                Log.e("MojaAplikacja", "Błąd żądania HTTP: " + error.getMessage());
            }
        });

        queue.add(request);
    }

    public static void SendTimeSettings(MainActivity activity, String esp8266IpAddress, String command, int hour, int minute, int CT, int brightness){
        int time = hour * 100 + minute;
        String url = "http://" + esp8266IpAddress + "/"+ command + "?value1=" + time +"&value2=" + CT + "&value3=" + brightness;

        RequestQueue queue = Volley.newRequestQueue(activity);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MojaAplikacja", "Odpowiedź od serwera: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Obsłuż błąd, jeśli wystąpi
                Log.e("MojaAplikacja", "Błąd żądania HTTP: " + error.getMessage());
            }
        });

        queue.add(request);
    }

    @SuppressLint("SetTextI18n")
    public static void ResetTimerFunctionality(Button timeButton, Button resetButton){
        timeButton.setTextSize(15);
        timeButton.setText("Select Time");
        resetButton.setVisibility(View.GONE);
    }
}
