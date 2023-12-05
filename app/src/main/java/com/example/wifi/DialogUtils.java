package com.example.wifi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class DialogUtils {

    public static int colorTempSettings = 2700;
    public static int colorTempBrightnessSettings = 100;
    private static int progressCTSettings = 0;
    private static int progressCTBrightnessSettings = 9;
    @SuppressLint("SetTextI18n")
    public static void openDialog(MainActivity activity, String esp8266IpAddress, int hour, int minute){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Settings");

        LayoutInflater inflater = activity.getLayoutInflater();
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
        builder.setPositiveButton("OK", (dialog, which) -> {
            TimerUtils.SendTimeSettings(activity, esp8266IpAddress, "time_on", hour, minute, colorTempSettings, colorTempBrightnessSettings);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
