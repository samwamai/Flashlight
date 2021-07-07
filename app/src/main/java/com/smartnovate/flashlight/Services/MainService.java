        package com.smartnovate.flashlight.Services;

        import android.app.NotificationChannel;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.os.Build;
        import android.os.Handler;
        import android.os.IBinder;
        import android.os.Looper;
        import android.util.Log;

        import androidx.annotation.Nullable;
        import androidx.core.app.NotificationCompat;

        import com.smartnovate.flashlight.Activities.MainActivity;
        import com.smartnovate.flashlight.Activities.SettingsActivity;
        import com.smartnovate.flashlight.Methods;
        import com.smartnovate.flashlight.R;

        import static android.content.ContentValues.TAG;
        import static com.smartnovate.flashlight.Methods.toast4All;
        import static com.smartnovate.flashlight.Methods.toggleFlashlight;

        public class MainService extends Service {
            private SharedPreferences preferenceSettings;
            private Handler shandlerBatteryCheck;
            private Handler handlerTimer;
            private Handler handlermain;
            private Runnable rtimer;
            private Runnable rmain;
            private Runnable srBatteryCheck;
            private final long batteryUpdatePeriod=30000;
            private boolean toggle =false;
            private int id=0;

            @Override
            public void onCreate() {


                preferenceSettings = getSharedPreferences("settings", Context.MODE_PRIVATE);

                if (Boolean.parseBoolean(preferenceSettings.getString("showStatusBar", "true"))) {
                    showPushNotification();
                }

            }


            private void runOnStartup(){
                handlermain = new Handler();
                rmain = new Runnable() {
                    public void run() {
                        if (toggle) {
                            only_lights_up_one();
                            handlermain.postDelayed(this, 5000);
                        }
                    }
                };
                handlermain.postDelayed(rmain, 5000);
            }

            @Nullable
            @Override
            public IBinder onBind(Intent intent) {
                return null;
            }
            @Override
            public void onDestroy() {
                off();
                if (!Boolean.parseBoolean(preferenceSettings.getString("showStatusBar", "true"))) {
                    cancelNotification();
                }
            }

            @Override
            public int onStartCommand(Intent intent, int flags, int startId) {

                try {
                    if ((intent.getStringExtra("id")) != null){
                        id = Integer.parseInt((intent.getStringExtra("id")));
                    }
                    if ((intent.getStringExtra("toggle")) != null){
                        toggle = Boolean.parseBoolean(intent.getStringExtra("toggle"));
                    }
                    if(toggle){
                        removeRunnerbleCallbacks();
                        only_lights_up_one();
                        runOnStartup();
                        setTimer();
                        BatteryCheck();

                    }else{
                        off();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return Service.START_STICKY;
            }



            private void setTimer() {
                // start timer to switch off automatically each its turned on
                if (Boolean.parseBoolean(preferenceSettings.getString("automaticSwitch", "false"))) {
                    long time_= Long.parseLong(preferenceSettings.getString("automaticSwitchTime", "10"))*1000*60;
                    handlerTimer = new Handler();
                    rtimer = new Runnable() {
                        public void run() {
                            if (toggle) {
                                off();
                                toast_background("Flashlight turned off time schedule");
                                handlerTimer.removeCallbacks(this);
                                stopSelf();
                            }
                            handlerTimer.postDelayed(this, time_);
                        }
                    };
                    handlerTimer.postDelayed(rtimer, time_);
                }
            }
            private void BatteryCheck() {
                // run always and update the battery level
                    shandlerBatteryCheck = new Handler();
                    srBatteryCheck = new Runnable() {
                        public void run() {
                            if (Boolean.parseBoolean(preferenceSettings.getString("powerControlSwitch", "false")) && toggle) {
                                int batteryLevel = Integer.parseInt(Methods.getBatteryPercentage(MainService.this));
                                int setLevel = Integer.parseInt(preferenceSettings.getString("powerControlSwitchLevel", "5"));
                                if (batteryLevel <= setLevel) {
                                    if (toggle) {
                                        //if the battery level is below set switch off
                                        off();
                                        toast_background("Flashlight turned off power schedule");
                                        settingsPreferencePut("powerControlSwitch", false);
                                        stopSelf();
                                    }
                                }
                            }
                            shandlerBatteryCheck.postDelayed(this, batteryUpdatePeriod);
                        }
                    };
                    shandlerBatteryCheck.postDelayed(srBatteryCheck, batteryUpdatePeriod);
            }


            private void showPushNotification() {
                // its obvious
                int requestCode = 0;//(int) (Math.random() * 3000);
                int id = 0; //(int) (Math.random() * 300000);
                String channelId = "fcm_default_channel_id";
                //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                //Bitmap iconLarge = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                Intent intentOn = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Intent intentOff = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Intent intentSettings = new Intent(this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntentOn = PendingIntent.getActivity(this, requestCode, intentOn,PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent pendingIntentOff = PendingIntent.getActivity(this, requestCode, intentOff,PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent pendingIntentSettings = PendingIntent.getActivity(this, requestCode, intentSettings,PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action action = new NotificationCompat.Action.Builder(0, "Turn On", pendingIntentOn).build();
                NotificationCompat.Action action1 = new NotificationCompat.Action.Builder(0, "Turn Off", pendingIntentOff).build();
                NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(0, "Settings", pendingIntentSettings).build();
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_flashlight)
                        //.setLargeIcon(iconLarge)
                        //.setContentTitle("title")
                        //.setContentText("body")
                        //.setContentIntent(smsMessageIntent)
                        .setAutoCancel(true)
                        //.setSound(defaultSoundUri)
                        .addAction(action)
                        .addAction(action1)
                        .addAction(action2);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // Since android Oreo notification channel is needed.
                if (notificationManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
                        notificationManager.createNotificationChannel(channel);
                    }
                    notificationManager.notify(id, notificationBuilder.build());
                } else {
                    Log.e(TAG, "notificationManager null");
                }
            }
            public void cancelNotification() {
                // remove notification if show notification is on
                NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(0);
                }
            }
            private void settingsPreferencePut(String setting, boolean on_off){
                preferenceSettings.edit().putString(setting, String.valueOf(on_off)).apply();
            }
            private void off(){
                toggleFlashlight(MainService.this,0,false);
                toggleFlashlight(MainService.this,1,false);
                removeRunnerbleCallbacks();
            }
            private void removeRunnerbleCallbacks(){
                if (handlerTimer != null && rtimer != null){
                    handlerTimer.removeCallbacks(rtimer);
                }
                if (shandlerBatteryCheck != null && srBatteryCheck != null){
                    shandlerBatteryCheck.removeCallbacks(srBatteryCheck);
                }
                if (handlermain  != null && rmain != null){
                    handlermain.removeCallbacks(rmain);
                }
            }

            private void only_lights_up_one(){
                // there can only be one flashlight on at any time
                if (id==0){
                    toggleFlashlight(this,1,false);
                    toggleFlashlight(this,0,true);
                }else if (id==1){
                    toggleFlashlight(this,0,false);
                    toggleFlashlight(this,1,true);
                }
            }
            private void toast_background(String message){
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> toast4All(MainService.this, message));
            }


        }
