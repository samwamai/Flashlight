package com.smartnovate.flashlight;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

import static android.content.ContentValues.TAG;

public class Methods {

    // methods recycle

    public static   void toggleFlashlight(Context context, int id, boolean toggle) {
        SharedPreferences preferenceSettings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        preferenceSettings.edit().putString("id", String.valueOf(id)).apply();
        Camera.Parameters parameters;
       CameraManager camManager;
        Camera mCamera;

        if (toggle){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    camManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                    String cameraId;
                    if (camManager != null) {
                        cameraId = camManager.getCameraIdList()[id];
                        camManager.setTorchMode(cameraId, true);
                        //preferenceSettings.edit().putString("flashState", String.valueOf(true)).apply();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    //preferenceSettings.edit().putString("flashState", String.valueOf(false)).apply();
                }
            } else {
                mCamera = Camera.open();
                parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                mCamera.startPreview();
                //preferenceSettings.edit().putString("flashState", String.valueOf(true)).apply();
            }
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    String cameraId;
                    camManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                    if (camManager != null) {
                        cameraId = camManager.getCameraIdList()[id]; // Usually front camera is at 0 position.
                        camManager.setTorchMode(cameraId, false);
                        //preferenceSettings.edit().putString("flashState", String.valueOf(false)).apply();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mCamera = Camera.open();
                parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                mCamera.stopPreview();
                //preferenceSettings.edit().putString("flashState", String.valueOf(false)).apply();
            }
        }
    }

    public static String getBatteryPercentage(Context context)
    {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            int level = Objects.requireNonNull(batteryStatus).getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level / (float)scale;
            float p = batteryPct * 100;
            return Math.round(p) +"" ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "*";
    }

    public static void shareApp(Context context){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_link) + BuildConfig.APPLICATION_ID);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }
    public static void sendFeedback(Context context){
        // send via email
        Intent mIntent = new Intent(Intent.ACTION_SENDTO);
        mIntent.setData(Uri.parse("mailto:"));
        mIntent.putExtra(Intent.EXTRA_EMAIL  , new String[] {context.getString(R.string.my_email)});
        mIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.my_email_subject);
        context.startActivity(Intent.createChooser(mIntent, context.getString(R.string.send_email)));
    }
    public static void removeAds(Context context){
        Intent mIntent = new Intent(Intent.ACTION_SENDTO);
        mIntent.setData(Uri.parse("mailto:"));
        mIntent.putExtra(Intent.EXTRA_EMAIL  , new String[] {context.getString(R.string.my_email)});
        mIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.my_email_subject_remove_ads);
        context.startActivity(Intent.createChooser(mIntent, context.getString(R.string.send_email)));
    }
    public static void facebookLink(Context context){
        try{ //first try to open in facebook app
            Intent appIntent = context.getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
            if(appIntent != null){
                appIntent.setAction(Intent.ACTION_VIEW);
                appIntent.setData(Uri.parse("fb://profile/"+R.string.facebook_profile_id));
                context.startActivity(appIntent);
            }
        }catch(Exception e){ //or else open in browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com/profile.php?id="+R.string.facebook_profile_id));
            context.startActivity(browserIntent);
        }
    }
    public static void instagramLink(Context context){
        try{ //first try to open in instagram app
            Intent appIntent = context.getPackageManager().getLaunchIntentForPackage("com.instagram.android");
            if(appIntent != null){
                appIntent.setAction(Intent.ACTION_VIEW);
                appIntent.setData(Uri.parse("https://instagram.com/_u/"+R.string.instagram_profile));
                context.startActivity(appIntent);
            }
        }catch(Exception e){ //or else open in browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/"+R.string.instagram_profile));
            context.startActivity(browserIntent);
        }
    }
    public static void twitterLink(Context context){
        try{ //first try to open in twitter app
            Intent appIntent = context.getPackageManager().getLaunchIntentForPackage("com.twitter.android");
            if(appIntent != null){
                appIntent.setAction(Intent.ACTION_VIEW);
                appIntent.setData(Uri.parse("twitter://user?user_id="+R.string.twitter_web_user));
                context.startActivity(appIntent);
            }
        }catch(Exception e){ //or else open in browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,  Uri.parse("https://twitter.com/"+R.string.twitter_web_user));
            context.startActivity(browserIntent);
        }

    }
    public static void moreApps(Context context){
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,  Uri.parse(String.valueOf(R.string.more_apps_url)));
            context.startActivity(browserIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void toast4All(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }


}
