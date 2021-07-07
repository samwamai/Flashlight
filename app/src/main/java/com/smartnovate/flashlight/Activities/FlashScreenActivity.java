        package com.smartnovate.flashlight.Activities;

        import androidx.appcompat.app.AppCompatActivity;

        import android.content.Context;
        import android.content.SharedPreferences;
        import android.os.Bundle;
        import android.os.Handler;
        import android.view.View;
        import com.smartnovate.flashlight.Methods;
        import com.smartnovate.flashlight.R;

        public class FlashScreenActivity extends AppCompatActivity {

            // Whether or not the system UI should be auto-hidden after
            private final String TAG="FLASH SCREEN ACTIVITY";
            private boolean visible = true;
            private View mContentView;
            private SharedPreferences preferenceSettings;
            private Handler handlerBatteryCheck;
            Handler handlerTimer;
            Runnable r=null;
            Runnable r2=null;
            private final long batteryUpdatePeriod=30000;

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            final int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_flash_screen);

                getWindow().getDecorView().setSystemUiVisibility(flags);
                preferenceSettings = getSharedPreferences("settings", Context.MODE_PRIVATE);
                mContentView = findViewById(R.id.flashScreenContent);
                mContentView.setOnClickListener(view1 -> toggle()
                );
                checkBatteryCharge();


            }

            private void toggle() {
// TO DO
            }

            public void powerOffSwitch(View view) {
                finish();
            }

            private void removeCallbacks(){
                if (handlerBatteryCheck != null) {
                    handlerBatteryCheck.removeCallbacks(r);
                }
                if (handlerTimer != null){
                    handlerTimer.removeCallbacks(r2);
                }
            }
            @Override
            protected void onDestroy() {
                super.onDestroy();
                removeCallbacks();
            }

            @Override
            protected void onPause() {
                super.onPause();
                if (handlerTimer != null){
                    handlerTimer.removeCallbacks(r2);
                }
            }

            @Override
            protected void onResume() {
                super.onResume();
                getWindow().getDecorView().setSystemUiVisibility(flags);
                setTimer();
            }

            @Override
            public void onBackPressed() {
                //super.onBackPressed();
            }



            private void checkBatteryCharge(){
                // run always and update the battery level
                handlerBatteryCheck = new Handler();
                r = new Runnable() {
                    public void run() {
                        if (Boolean.parseBoolean(preferenceSettings.getString("powerControlSwitch", "false"))){
                            int batteryLevel = Integer.parseInt(Methods.getBatteryPercentage(FlashScreenActivity.this));
                            int setLevel = Integer.parseInt(preferenceSettings.getString("powerControlSwitchLevel", "0"));
                            if (batteryLevel <= setLevel){

                                    //if the battery level is below set switch off
                                    Methods.toast4All(FlashScreenActivity.this,"Flashlight turned off battery low timer");
                                    settingsPreferencePut("powerControlSwitch",false);
                                    finish();
                            }
                        }
                        handlerBatteryCheck.postDelayed(this, batteryUpdatePeriod);
                    }
                };
                handlerBatteryCheck.postDelayed(r, batteryUpdatePeriod);
            }

            private void setTimer() {
                // start timer to switch off automatically each its turned on
                if (Boolean.parseBoolean(preferenceSettings.getString("automaticSwitch", "false"))) {
                    long time_= Long.parseLong(preferenceSettings.getString("automaticSwitchTime", "10"))*1000*60;
                    handlerTimer = new Handler();
                    r2 = new Runnable() {
                        public void run() {
                            Methods.toast4All(FlashScreenActivity.this,"Flashlight turned off time schedule");
                            handlerTimer.removeCallbacks(this);
                            finish();
                            handlerTimer.postDelayed(this, time_);
                        }
                    };
                    handlerTimer.postDelayed(r2, time_);
                }
            }


            private void settingsPreferencePut(String setting, boolean on_off){
                preferenceSettings.edit().putString(setting, String.valueOf(on_off)).apply();
            }
        }