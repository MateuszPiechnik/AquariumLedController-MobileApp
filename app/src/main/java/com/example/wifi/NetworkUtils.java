package com.example.wifi;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class NetworkUtils {
    public static void LedStatus(MainActivity activity, String EspIPAddress, String command) {
        String url = "http://" + EspIPAddress + "/led_control";
        RequestQueue queue = Volley.newRequestQueue(activity);

        Log.d("MojaAplikacja", "Wysyłam żądanie HTTP na adres: " + url);

        // Modyfikacja żądania, dodanie parametru "command"
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
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
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Dodanie parametru "command" do ciała żądania
                Map<String, String> params = new HashMap<>();
                params.put("command", command);
                return params;
            }
        };
        queue.add(request);
    }

    public static void SendSliderValue(MainActivity activity, String esp8266IpAddress, String command, int value) {
        String url = "http://" + esp8266IpAddress + "/"+ command + "?value=" + value; // Przekazanie wartości jako część URL

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

    public static void SendSliderValueCT(MainActivity activity, String esp8266IpAddress, String command, int value, int percentage) {
        String url = "http://" + esp8266IpAddress + "/"+ command + "?value1=" + value +"&value2=" + percentage; // Przekazanie wartości jako część URL

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
}
