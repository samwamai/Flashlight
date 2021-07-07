        package com.smartnovate.flashlight.Activities;

        import android.app.AlertDialog;
        import android.content.Context;
        import android.content.SharedPreferences;
        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.Window;
        import android.widget.Button;
        import android.widget.SeekBar;
        import android.widget.TextView;

        import androidx.appcompat.app.AppCompatActivity;
        import androidx.appcompat.widget.SwitchCompat;
        import androidx.appcompat.widget.Toolbar;
        import androidx.constraintlayout.widget.ConstraintLayout;

        import com.smartnovate.flashlight.Methods;
        import com.smartnovate.flashlight.R;

        public class SettingsActivity extends AppCompatActivity{
            private final String TAG="FLASHLIGHT SETTINGS";
            private SwitchCompat showStatusBar,switchSound,turnOnStartUp,turnOffAtExit,automaticSwitch,powerControlSwitch;
            private TextView timeDialog,powerControl,previoustime,previouspower;
            ConstraintLayout automaticSwitchLayout;
            ConstraintLayout powerControlLayout;
            private SharedPreferences preferenceSettings;
            private boolean enableToast=false;
            final int minTime = 3;
            final int maxTime = 100;
            int currentTime = 3;
            final int minPower = 5;
            final int maxPower = 100;
            int currentPower = 5;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                // request action bar since parent theme
                getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
                setContentView(R.layout.activity_settings);
                // set the toolbar
                Toolbar toolbar = findViewById(R.id.toolbar);
                toolbar.setTitle("");
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null){
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                }
                preferenceSettings = getSharedPreferences("settings", Context.MODE_PRIVATE);

                // initialize views
                showStatusBar =findViewById(R.id.switchStatusBar);
                switchSound = findViewById(R.id.switchSwitchSound);
                turnOnStartUp = findViewById(R.id.switchSwitchTurnOnStartUP);
                turnOffAtExit = findViewById(R.id.switchSwitchTurnOfExit);
                automaticSwitch = findViewById(R.id.switchAutomaticSwitch);
                powerControlSwitch = findViewById(R.id.switchPowerControl);
                previoustime = findViewById(R.id.textTimeSetb);
                previouspower = findViewById(R.id.textPowersetb);
                TextView removeAds = findViewById(R.id.textViewRemoveAds);
                TextView share = findViewById(R.id.tesxtShare);
                TextView moreapps = findViewById(R.id.tesxtMoreApps);
                TextView sendFeedback = findViewById(R.id.textViewSendFeedback);
                //TextView flashLightVersion = findViewById(R.id.textViewFlashlightSub);
                TextView facebook = findViewById(R.id.textViewFacebook);
                TextView instagram = findViewById(R.id.textViewInstagram);
                TextView twitter = findViewById(R.id.textViewTwitter);
                automaticSwitchLayout = findViewById(R.id.constrainLayoutAutomaticSwitch);
                powerControlLayout = findViewById(R.id.constrainPowerControlLayout);

                // set switch from preference


                // set onclick/onchange on views
                showStatusBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked){
                        settingsToast("Notification toolbar enabled");
                    }else {
                        settingsToast("Notification toolbar disabled");
                    }
                    settingsPreferencePut("showStatusBar",isChecked);
                });

                switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked){
                        settingsToast("Switch sound enabled");
                    }else {
                        settingsToast("Switch sound disabled");
                    }
                    settingsPreferencePut("switchSound",isChecked);
                });

                turnOnStartUp.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked){
                        settingsToast("Turn on at startup enabled");
                    }else {
                        settingsToast("Turn on at startup disabled");
                    }
                    settingsPreferencePut("turnOnStartUp",isChecked);
                });

                turnOffAtExit.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked){
                        settingsToast("Turn off at exit enabled");
                    }else {
                        settingsToast("Turn off at exit disabled");
                    }
                    settingsPreferencePut("turnOffAtExit",isChecked);
                });

                automaticSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked){
                        previoustime.setText(preferenceSettings.getString("automaticSwitchTime", "")+"min");
                        settingsToast("Automatic switch enabled");
                        if (enableToast){
                            showCustomDialogTimeSet();
                        }
                    }else {
                        previoustime.setText("");
                        settingsToast("Automatic switch disabled");
                    }
                    settingsPreferencePut("automaticSwitch",isChecked);
                });

                powerControlSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked){
                        previouspower.setText(preferenceSettings.getString("powerControlSwitchLevel", "")+"%");
                        settingsToast("Power control enabled");
                        if (enableToast){
                            showCustomDialogPowerControl();
                        }
                    }else {
                        previouspower.setText("");
                        settingsToast("Power control disabled");
                    }
                    settingsPreferencePut("powerControlSwitch",isChecked);
                });

                automaticSwitchLayout.setOnClickListener(v -> showCustomDialogTimeSet());

                powerControlLayout.setOnClickListener(v -> showCustomDialogPowerControl());

                // TO DO
                removeAds.setOnClickListener(v -> {
                    Methods.removeAds(this);
                });

                //lambda expression
                share.setOnClickListener(v -> {
                   Methods.shareApp(this);
                });

                moreapps.setOnClickListener(v -> {
                    Methods.moreApps(this);
                });

                sendFeedback.setOnClickListener(v -> {
                  Methods.sendFeedback(this);
                });

                facebook.setOnClickListener(v -> {
                   Methods.facebookLink(this);
                });

                instagram.setOnClickListener(v -> {
                   Methods.instagramLink(this);
                });

                twitter.setOnClickListener(v -> {
                   Methods.twitterLink(this);
                });


            }


            @Override
            public boolean onSupportNavigateUp() {
                finish();
                return true;
            }

            @Override
            protected void onResume() {
                super.onResume();
                // enable toast only after the switch are set accordingly
                setSwitchAccordingly();

                enableToast=true;
            }

            private void setSwitchAccordingly() {
                // set the switches according to settings before
                if (preferenceSettings != null) {
                    showStatusBar.setChecked(Boolean.parseBoolean(preferenceSettings.getString("showStatusBar", "true" )));
                    switchSound.setChecked(Boolean.parseBoolean(preferenceSettings.getString("switchSound", "true")));
                    turnOnStartUp.setChecked(Boolean.parseBoolean(preferenceSettings.getString("turnOnStartUp", "false")));
                    turnOffAtExit.setChecked(Boolean.parseBoolean(preferenceSettings.getString("turnOffAtExit", "false")));
                    automaticSwitch.setChecked(Boolean.parseBoolean(preferenceSettings.getString("automaticSwitch", "false")));
                    powerControlSwitch.setChecked(Boolean.parseBoolean(preferenceSettings.getString("powerControlSwitch", "false")));
                }
            }

            private void showCustomDialogTimeSet() {
                if (enableToast) {
                    currentTime= Integer.parseInt(preferenceSettings.getString("automaticSwitchTime", "3"));
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_time_set, null);
                    Button cancel = dialogView.findViewById(R.id.button_cancel_settings);
                    Button ok = dialogView.findViewById(R.id.buttonOkSettings);
                    timeDialog = dialogView.findViewById(R.id.textViewTime);
                    SeekBar seekBarSettings = dialogView.findViewById(R.id.seekBarSetting);
                    seekBarSettings.setProgress(maxTime - minTime);
                    seekBarSettings.setProgress(currentTime - minTime);
                    timeDialog.setText(" " + currentTime+"min");
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setView(dialogView);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    seekBarSettings.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            currentTime = progress + minTime;
                            timeDialog.setText(" " + currentTime + "min");
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

                    ok.setOnClickListener(v -> {
                        settingsPreferencePut("automaticSwitch",true);
                        settings_preference_time("automaticSwitchTime", currentTime);
                        previoustime.setText(preferenceSettings.getString("automaticSwitchTime", "")+"min");
                        alertDialog.dismiss();
                        settingsToast("Flashlight timer set to "+ currentTime +" min");
                        if (!automaticSwitch.isChecked())
                            enableToast=false;
                        automaticSwitch.setChecked(true);
                        enableToast=true;
                    });

                    cancel.setOnClickListener(v -> {
                        //automaticSwitch.setChecked(false);
                        alertDialog.dismiss();
                    });
                }
            }

            private void showCustomDialogPowerControl() {
                if (enableToast) {
                    currentPower= Integer.parseInt(preferenceSettings.getString("powerControlSwitchLevel", "5"));
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_power_control, null);
                    Button cancel = dialogView.findViewById(R.id.button_cancel_settings);
                    Button ok = dialogView.findViewById(R.id.buttonOkSettings);
                    powerControl = dialogView.findViewById(R.id.textViewPowerControl);
                    SeekBar seekBarSettings = dialogView.findViewById(R.id.seekBarSetting);
                    seekBarSettings.setProgress(maxPower - minPower);
                    seekBarSettings.setProgress(currentPower - minPower);
                    powerControl.setText(" " + currentPower+"%");
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setView(dialogView);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    seekBarSettings.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            currentPower = progress + minPower;
                            powerControl.setText(" " + currentPower + "%");
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

                    ok.setOnClickListener(v -> {
                        settingsPreferencePut("powerControlSwitch",true);
                        settings_preference_time("powerControlSwitchLevel", currentPower);
                        previouspower.setText(preferenceSettings.getString("powerControlSwitchLevel", "")+"%");
                        alertDialog.dismiss();
                        settingsToast("Flashlight power set to "+ currentPower +" %");
                        if (!powerControlSwitch.isChecked())
                            enableToast=false;
                        powerControlSwitch.setChecked(true);
                        enableToast=true;
                    });

                    cancel.setOnClickListener(v -> {
                        //powerControlSwitch.setChecked(false);
                        alertDialog.dismiss();
                    });
                }
            }

            private void settingsToast(String text){
                if (enableToast){
                    Methods.toast4All(SettingsActivity.this,text);
                }
            }

            private void settingsPreferencePut(String setting, boolean on_off){
                preferenceSettings.edit().putString(setting, String.valueOf(on_off)).apply();
            }
            private void settings_preference_time(String setting,int on_off){
                preferenceSettings.edit().putString(setting, String.valueOf(on_off)).apply();
            }


        }