package com.example.wifi;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private final String esp8266IpAddress = "192.168.1.46"; // Adress IP ESP8266
    //private final String esp8266IpAddress = "192.168.1.69";
    boolean isColdLedOn, isWarmLedOn, isColorTempOn, isDaySimOn;
    int minute, hour, minuteOff, hourOff;
    private int sunriseHour, sunriseMinute, sunsetHour, sunsetMinute;
    int colorTemp = 2700;
    int colorTempPerc = 100;
    int colorTempSettings = 2700;
    int colorTempBrightnessSettings = 100;
    Button ledOn, ledOff; //buttons to both leds
    Button timeButton, resetButton, timeButtonOff, resetButtonOff;   //timer buttons
    Button colorTemperatureButton;
    ImageButton coldWhiteLed, warmWhiteLed;
    boolean isTimeSelected = false;
    boolean isTimeSelectedOff = false;
    Handler handler, handlerOff;
    TextView CTtext, CTPerctext, text, textCold, textWarm;
    SeekBar CTsBar, CTPercsBar, sBar, sBarCold, sBarWarm;
    Button daySimButton;
    Button openDialogButton;
    int progressCTSettings = 0;
    int progressCTBrightnessSettings = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ledOn = findViewById(R.id.LedOn);
        ledOff = findViewById(R.id.LedOff);

        coldWhiteLed = findViewById(R.id.coldWhiteOff);
        warmWhiteLed = findViewById(R.id.warmWhiteOff);
        isColdLedOn = false;
        isWarmLedOn = false;
        isColorTempOn = false;
        isDaySimOn = false;

        sBar = findViewById(R.id.seekBar1);
        text = findViewById(R.id.seekBarText);
        text.setText(sBar.getProgress() + " %");

        sBarCold = findViewById(R.id.seekBarCold);
        textCold = findViewById(R.id.seekBarTextCold);
        textCold.setText(sBarCold.getProgress() + " %");

        sBarWarm = findViewById(R.id.seekBarWarm);
        textWarm = findViewById(R.id.seekBarTextWarm);
        textWarm.setText(sBarWarm.getProgress() + " %");

        timeButton = findViewById(R.id.timeButton);
        timeButtonOff = findViewById(R.id.timeButtonOff);
        resetButton = findViewById(R.id.Reset);
        resetButtonOff = findViewById(R.id.ResetOff);

        colorTemperatureButton = findViewById(R.id.colorTemperatureButton);
        CTsBar = findViewById(R.id.CTSeekBar);
        CTtext = findViewById(R.id.CTtext);
        CTtext.setText("Color Temperature: " + colorTemp +"K");

        CTPercsBar = findViewById(R.id.CTpercentageSeekBar);
        CTPerctext = findViewById(R.id.CTpercentagetext);
        CTPerctext.setText("Brightness: "+ colorTempPerc + " %");

        daySimButton = findViewById(R.id.daySimButton);

        openDialogButton = findViewById(R.id.buttonOpenDialog);

        openDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        daySimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isDaySimOn)
                {
                    isDaySimOn = true;
                    daySimButton.setBackgroundColor(Color.YELLOW);
                    daySimButton.setTextColor(Color.BLACK);
                    getSunriseSunsetTimes();
                    Log.d("Tag", "Hour: " + sunsetHour + sunsetMinute);
                }
                else
                {
                    resetDaySimulationFunctionality();
                }
            }
        });

        CTPercsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                colorTempPerc = 10 + progress*10;
                CTPerctext.setText("Brightness: "+ colorTempPerc + " %");
                sendSliderValueCT("color_temperature", colorTemp,colorTempPerc);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                CTPerctext.setText("Brightness: "+ colorTempPerc + " %");
                sendSliderValueCT("color_temperature", colorTemp,colorTempPerc);
                adjustSeekBarsForColorTemperature(colorTemp, colorTempPerc);
            }
        });

        CTsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress==0){
                    colorTemp = 2700;
                }
                else
                {
                    colorTemp = 2500 + 500*progress;
                }
                CTtext.setText("Color Temperature: " + colorTemp + "K");
                sendSliderValueCT("color_temperature", colorTemp,colorTempPerc);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                CTtext.setText("Color Temperature: " + colorTemp + "K");
                sendSliderValueCT("color_temperature", colorTemp,colorTempPerc);
                adjustSeekBarsForColorTemperature(colorTemp, colorTempPerc);
            }
        });

        colorTemperatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isColorTempOn){
                    isColorTempOn = true;
                    colorTemperatureButton.setBackgroundColor(Color.YELLOW);
                    colorTemperatureButton.setTextColor(Color.BLACK);
                    CTsBar.setVisibility(View.VISIBLE);
                    CTtext.setVisibility(View.VISIBLE);
                    CTPercsBar.setVisibility(View.VISIBLE);
                    CTPerctext.setVisibility(View.VISIBLE);
                    warmWhiteLed.setImageResource(R.drawable.warmwhiteon);
                    coldWhiteLed.setImageResource(R.drawable.coldwhiteon);
                    adjustSeekBarsForColorTemperature(2700, colorTempPerc);
                    sendSliderValue("color_temperature", 2700);
                }
                else
                {
                    resetColorTemperatureFunctionality();
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTimeSelected = false;
                resetTimerFunctionality();
            }
        });

        resetButtonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTimeSelectedOff = false;
                resetTimerOffFunctionality();
            }
        });

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
                if (isDaySimOn){
                    resetDaySimulationFunctionality();
                }
            }
        });
        timeButtonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialogOff();
                if (isDaySimOn){
                    resetDaySimulationFunctionality();
                }
            }
        });

        sBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int value;
            int sendValue;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sendValue = progress;
                value = (int) ((progress / 255.0) * 100);
                text.setText(value + " %");
                sendSliderValue("set_value", sendValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                resetColorTemperatureFunctionality();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (sendValue==0){
                    coldWhiteLed.setImageResource(R.drawable.coldwhiteoff);
                    warmWhiteLed.setImageResource(R.drawable.warmwhiteoff);
                }
                else {
                    coldWhiteLed.setImageResource(R.drawable.coldwhiteon);
                    warmWhiteLed.setImageResource(R.drawable.warmwhiteon);
                }
                text.setText(value + " %");
                textWarm.setText(value + " %");
                textCold.setText(value + " %");
                sBarCold.setProgress(sendValue);
                sBarWarm.setProgress(sendValue);
                sendSliderValue("set_value", sendValue);
            }
        });

        sBarCold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int value;
            int sendValue;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sendValue = progress;
                value = (int) ((progress / 255.0) * 100);
                textCold.setText(value + " %");
                //sendSliderValue("set_value_cold", sendValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                resetColorTemperatureFunctionality();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (sendValue==0){
                    coldWhiteLed.setImageResource(R.drawable.coldwhiteoff);
                }
                else {
                    coldWhiteLed.setImageResource(R.drawable.coldwhiteon);
                }
                textCold.setText(value + " %");
                text.setText(0 + " %");
                sBar.setProgress(0);
                sendSliderValue("set_value_cold", sendValue);
                isColdLedOn = true;
            }
        });

        sBarWarm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int value;
            int sendValue;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sendValue = progress;
                value = (int) ((progress / 255.0) * 100);
                textWarm.setText(value + " %");
                //sendSliderValue("set_value_warm", sendValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                resetColorTemperatureFunctionality();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (sendValue==0){
                    warmWhiteLed.setImageResource(R.drawable.warmwhiteoff);
                }
                else {
                    warmWhiteLed.setImageResource(R.drawable.warmwhiteon);
                }
                textWarm.setText(value + " %");
                sendSliderValue("set_value_warm", sendValue);
                text.setText(0 + " %");
                sBar.setProgress(0);
                isWarmLedOn = true;
            }
        });

        coldWhiteLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetColorTemperatureFunctionality();
                if (!isColdLedOn) {
                    coldWhiteLed.setImageResource(R.drawable.coldwhiteon);
                    isColdLedOn = true;
                    LedStatus("cold_led_on");
                    sBarCold.setProgress(255);
                    textCold.setText(100+" %");
                } else {
                    coldWhiteLed.setImageResource((R.drawable.coldwhiteoff));
                    isColdLedOn = false;
                    LedStatus("cold_led_off");
                    sBarCold.setProgress(0);
                    textCold.setText(0+" %");
                }
            }
        });

        warmWhiteLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetColorTemperatureFunctionality();
                if (!isWarmLedOn) {
                    warmWhiteLed.setImageResource(R.drawable.warmwhiteon);
                    isWarmLedOn = true;
                    LedStatus("warm_led_on");
                    sBarWarm.setProgress(255);
                    textWarm.setText(100+" %");
                } else {
                    warmWhiteLed.setImageResource((R.drawable.warmwhiteoff));
                    isWarmLedOn = false;
                    LedStatus("warm_led_off");
                    sBarWarm.setProgress(0);
                    textWarm.setText(0+" %");
                }
            }
        });

        ledOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isWarmLedOn = true;
                isColdLedOn = true;
                changeLedStatusOn(true);
                resetColorTemperatureFunctionality();
            }
        });

        ledOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isWarmLedOn = false;
                isColdLedOn = false;
                changeLedStatusOn(false);
                resetColorTemperatureFunctionality();
            }
        });

        handler = new Handler();
        handler.postDelayed(checkTime, 1000);
    }

    private void LedStatus(String command){
        String url = "http://" + esp8266IpAddress + "/led_control";
        RequestQueue queue = Volley.newRequestQueue(this);

        Log.d("MojaAplikacja", "Wysyłam żądanie HTTP na adres: " + url);
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
        });
        queue.add(request);
    }

    private void sendSliderValue(String command, int value) {
        String url = "http://" + esp8266IpAddress + "/"+ command + "?value=" + value; // Przekazanie wartości jako część URL

        RequestQueue queue = Volley.newRequestQueue(this);

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
    private void sendSliderValueCT(String command, int value, int percentage) {
        String url = "http://" + esp8266IpAddress + "/"+ command + "?value1=" + value +"&value2=" + percentage; // Przekazanie wartości jako część URL

        RequestQueue queue = Volley.newRequestQueue(this);

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

    private void sendTime(String command, int hour, int minute){
        int time = hour * 100 + minute;
        String url = "http://" + esp8266IpAddress + "/"+ command + "?value=" + time;

        RequestQueue queue = Volley.newRequestQueue(this);

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

    private void sendTimeSettings(String command, int hour, int minute, int CT, int brightness){
        int time = hour * 100 + minute;
        String url = "http://" + esp8266IpAddress + "/"+ command + "?value1=" + time +"&value2=" + CT + "&value3=" + brightness;

        RequestQueue queue = Volley.newRequestQueue(this);

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

    private void showTimePickerDialog(){
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                minute = selectedMinute;
                hour = selectedHour;
                isTimeSelected = true;
                timeButton.setTextSize(22);
                timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                resetButton.setVisibility(View.VISIBLE);
                openDialogButton.setVisibility(View.VISIBLE);
            }
        }, hour, minute, true);
        timePickerDialog.setTitle("Select time");
        timePickerDialog.show();
    }

    private void showTimePickerDialogOff(){
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                minuteOff = selectedMinute;
                hourOff = selectedHour;
                isTimeSelectedOff = true;
                timeButtonOff.setTextSize(22);
                timeButtonOff.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOff, minuteOff));
                resetButtonOff.setVisibility(View.VISIBLE);
                sendTime("time_off", hourOff, minuteOff);
            }
        }, hourOff, minuteOff, true);
        timePickerDialog.setTitle("Select time");
        timePickerDialog.show();
    }

    private Runnable checkTime = new Runnable() {
        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            if(isTimeSelected && currentMinute == minute && currentHour == hour){
                isTimeSelected = false;
                adjustSeekBarsForColorTemperature(colorTempSettings, colorTempBrightnessSettings);
                warmWhiteLed.setImageResource(R.drawable.warmwhiteon);
                coldWhiteLed.setImageResource(R.drawable.coldwhiteon);
                resetTimerFunctionality();
            }
            if(isTimeSelectedOff && currentMinute == minuteOff && currentHour == hourOff){
                isTimeSelectedOff = false;
                resetTimerOffFunctionality();
                changeLedStatusOn(false);
            }
            handler.postDelayed(this,1000);
        }
    };
    private void resetColorTemperatureFunctionality(){
        isColorTempOn = false;
        colorTemp = 2700;
        colorTempPerc = 100;
        CTsBar.setProgress(0);
        CTsBar.setVisibility(View.GONE);
        CTtext.setText("Color Temperature: " + colorTemp + "K");
        CTtext.setVisibility(View.GONE);
        CTPercsBar.setProgress(9);
        CTPercsBar.setVisibility(View.GONE);
        CTPerctext.setVisibility(View.GONE);
        colorTemperatureButton.setBackgroundColor(getColor(R.color.lb));
        colorTemperatureButton.setTextColor(Color.WHITE);
    }

    private void resetDaySimulationFunctionality(){
        isDaySimOn = false;
        daySimButton.setBackgroundColor(getColor(R.color.lb));
        daySimButton.setTextColor(Color.WHITE);
        LedStatus("day_sim_off");
    }
    private void resetTimerFunctionality(){
        timeButton.setTextSize(15);
        timeButton.setText("Select Time");
        resetButton.setVisibility(View.GONE);
    }

    private void resetTimerOffFunctionality(){
        timeButtonOff.setTextSize(15);
        timeButtonOff.setText("Select Time");
        resetButtonOff.setVisibility(View.GONE);
    }

    private void changeLedStatusOn(boolean status){
        if(status){
            LedStatus("led_on");
            LedStatus("warm_led_on");
            LedStatus("cold_led_on");
            coldWhiteLed.setImageResource(R.drawable.coldwhiteon);
            warmWhiteLed.setImageResource(R.drawable.warmwhiteon);
            sBar.setProgress(255);
            sBarWarm.setProgress(255);
            sBarCold.setProgress(255);
            textCold.setText(100 + " %");
            textWarm.setText(100 + " %");
            text.setText(100 + " %");
        }
        else{
            LedStatus("led_off");
            LedStatus("warm_led_off");
            LedStatus("cold_led_off");
            coldWhiteLed.setImageResource(R.drawable.coldwhiteoff);
            warmWhiteLed.setImageResource(R.drawable.warmwhiteoff);
            sBar.setProgress(0);
            sBarWarm.setProgress(0);
            sBarCold.setProgress(0);
            text.setText(0 + " %");
            textCold.setText(0 + " %");
            textWarm.setText(0 + " %");
        }
    }

    private void adjustSeekBarsForColorTemperature(int value, int percentage)
    {
        switch (value){
            case(2700):
            {
                sBarWarm.setProgress(255 * percentage/100);
                sBarCold.setProgress(0);
                textWarm.setText(100 * percentage/100 + " %");
                textCold.setText(0 + " %");
                break;
            }
            case(3000):
            {
                sBarWarm.setProgress(255 * percentage/100);
                sBarCold.setProgress(50 * percentage/100);
                textWarm.setText(100 * percentage/100 + " %");
                textCold.setText(20 * percentage/100 + " %");
                break;
            }
            case(3500):
            {
                sBarWarm.setProgress(255 * percentage/100);
                sBarCold.setProgress(140 * percentage/100);
                textWarm.setText(100 * percentage/100 + " %");
                textCold.setText(55 * percentage/100 + " %");
                break;
            }
            case(4000):
            {
                sBarWarm.setProgress(255 * percentage/100);
                sBarCold.setProgress(255 * percentage/100);
                textWarm.setText(100 * percentage/100 + " %");
                textCold.setText(100 * percentage/100 + " %");
                break;
            }
            case(4500):
            {
                sBarWarm.setProgress(140 * percentage/100);
                sBarCold.setProgress(255 * percentage/100);
                textWarm.setText(55 * percentage/100 + " %");
                textCold.setText(100 * percentage/100 + " %");
                break;
            }
            case(5000):
            {
                sBarWarm.setProgress(89 * percentage/100);
                sBarCold.setProgress(255 * percentage/100);
                textWarm.setText(35 * percentage/100 + " %");
                textCold.setText(100 * percentage/100 + " %");
                break;
            }
            case(5500):
            {
                sBarWarm.setProgress(48 * percentage/100);
                sBarCold.setProgress(255 * percentage/100);
                textWarm.setText(19 * percentage/100 + " %");
                textCold.setText(100 * percentage/100 + " %");
                break;
            }
            case(6000):
            {
                sBarWarm.setProgress(20 * percentage/100);
                sBarCold.setProgress(255 * percentage/100);
                textWarm.setText(8 * percentage/100 + " %");
                textCold.setText(100 * percentage/100 + " %");
                break;
            }
            case(6500):
            {
                sBarWarm.setProgress(0);
                sBarCold.setProgress(255 * percentage/100);
                textWarm.setText(0 + " %");
                textCold.setText(100 * percentage/100 + " %");
                break;
            }
        }
    }
    private void getSunriseSunsetTimes() {
        // Współrzędne geograficzne Krakowa
        double latitude = 50.061947;
        double longitude = 19.936855;

        TimeZone europeWarsaw = TimeZone.getTimeZone("Europe/Warsaw");
        long CurrentTime = System.currentTimeMillis();
        boolean isSummerTime = europeWarsaw.inDaylightTime(new java.util.Date(CurrentTime));

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        // Data (możesz dostosować do swoich potrzeb)
        String date = dateFormat.format(calendar.getTime());

        String url = "https://api.sunrise-sunset.org/json?lat=" + latitude + "&lng=" + longitude + "&date=" + date;

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    String sunrise = json.getJSONObject("results").getString("sunrise");
                    String sunset = json.getJSONObject("results").getString("sunset");

                    // Konwersja na polski format godzinowy (24-godzinny)
                    SimpleDateFormat inputFormat = new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("HHmm", Locale.ENGLISH);

                    Calendar sunriseDate = Calendar.getInstance();
                    Calendar sunsetDate = Calendar.getInstance();

                    sunriseDate.setTime(inputFormat.parse(sunrise));
                    sunsetDate.setTime(inputFormat.parse(sunset));

                    if(!isSummerTime){
                        sunriseDate.add(Calendar.HOUR,1);
                        sunsetDate.add(Calendar.HOUR, 1);
                    }

                    String formattedSunrise = outputFormat.format(sunriseDate.getTime());
                    String formattedSunset = outputFormat.format(sunsetDate.getTime());

                    // Wyświetl czasy wschodu i zachodu słońca
                    Log.d("SunriseSunset", date + " Czas wschodu: " + formattedSunrise);
                    Log.d("SunriseSunset", date + " Czas zachodu: " + formattedSunset);

                    sunriseHour = sunriseDate.get(Calendar.HOUR_OF_DAY);
                    sunsetHour = sunsetDate.get(Calendar.HOUR_OF_DAY);
                    sunriseMinute = sunriseDate.get(Calendar.MINUTE);
                    sunsetMinute = sunsetDate.get(Calendar.MINUTE);

                    Log.d("SunriseSunset", "Hour " + sunsetHour + " " + sunsetMinute);
                    Log.d("SunriseSunset", "Hour " + sunriseHour + " " + sunriseMinute);

                    sendTime("sunrise_time", sunriseHour, sunriseMinute);
                    sendTime("sunset_time", sunsetHour, sunsetMinute);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LedStatus("day_sim_on");
                        }
                    }, 500); // Opóźnienie na 1 sekundę (1000 milisekund)

                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Obsłuż błąd, jeśli wystąpi
                Log.e("SunriseSunset", "Błąd żądania HTTP: " + error.getMessage());
            }
        });

        queue.add(request);

    }

    private void openDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_layout, null);
        builder.setView(dialogView);

        SeekBar CTSettingsSBar = dialogView.findViewById(R.id.CTSettingsSeekBar);
        TextView CTSettingsText = dialogView.findViewById(R.id.CTSettingsText);
        CTSettingsText.setText("Color Temperature: " + colorTempSettings + "K");
        CTSettingsSBar.setProgress(progressCTSettings);

        SeekBar CTSettingsBrightness = dialogView.findViewById(R.id.CTSettingsPercentageSeekBar);
        TextView CTSettingsBrightnessText = dialogView.findViewById(R.id.CTSettingsPercentageText);
        CTSettingsBrightnessText.setText("Brightness: "+ colorTempBrightnessSettings + " %");
        CTSettingsBrightness.setProgress(progressCTBrightnessSettings);

        CTSettingsSBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress==0){
                    colorTempSettings = 2700;
                }
                else
                {
                    colorTempSettings = 2500 + 500*progress;
                }
                progressCTSettings = progress;
                CTSettingsText.setText("Color Temperature: " + colorTempSettings + "K");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                CTSettingsText.setText("Color Temperature: " + colorTempSettings + "K");
            }
        });

        CTSettingsBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressCTBrightnessSettings = progress;
                colorTempBrightnessSettings = 10 + progress*10;
                CTSettingsBrightnessText.setText("Brightness: "+ colorTempBrightnessSettings + " %");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                CTSettingsBrightnessText.setText("Brightness: "+ colorTempBrightnessSettings + " %");
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendTimeSettings("time_on", hour, minute, colorTempSettings, colorTempBrightnessSettings);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
