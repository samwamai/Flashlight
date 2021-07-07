package com.smartnovate.flashlight.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.smartnovate.flashlight.Services.MainService;
import com.smartnovate.flashlight.Methods;
import com.smartnovate.flashlight.R;

import static com.smartnovate.flashlight.Methods.toast4All;
import static com.smartnovate.flashlight.Methods.toggleFlashlight;

public class MainActivity extends AppCompatActivity implements Animator.AnimatorListener {

    private final String TAG="FLASHLIGHT MAIN ACTIVITY";
    private SharedPreferences preferenceSettings;
    private ImageButton mainSwitch, frontSwitch, screenSwitch, backSwitch;
    private TextView textViewProgress,textViewBattery;
    private Handler handlerBatteryCheck;
    private Handler handlerFlick;
    private Runnable runnableFlick;
    private final long batteryUpdatePeriod=30000;
    private boolean mainSwitchState;
    private boolean frontSwitchState;
    private boolean screenSwitchState;
    private boolean feature_camera_flash;
    private boolean feature_camera_front;
    private boolean enableFlick =false;
    private boolean backSwitchState = false;
    boolean stateToggle =true;

    final int min = 0;
    final int max = 100;
    int current = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // set the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // check if device has flashlight
        feature_camera_flash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        feature_camera_front = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        //get preference settings
        preferenceSettings = getSharedPreferences("settings", Context.MODE_PRIVATE);

        // initialize views
        mainSwitch = findViewById(R.id.imageButtonPower);
        backSwitch = findViewById(R.id.imageButtonBackFlash);
        frontSwitch = findViewById(R.id.imageButtonFrontFlash);
        screenSwitch = findViewById(R.id.imageButtonScreenFlash);
        textViewProgress = findViewById(R.id.textViewProgress);
        textViewBattery = findViewById(R.id.textViewBatteryStatus);
        SeekBar seekBar = findViewById(R.id.seekBarx);
        seekBar.setProgress(max - min);
        seekBar.setProgress(current - min);
        textViewProgress.setText(""+current);


        if (Boolean.parseBoolean(preferenceSettings.getString("turnOnStartUp", "false")) && feature_camera_flash){
            // if turnOnStartUp is enabled automatically turn on app start
            mainSwitchSec();
        }

        if (feature_camera_flash){
            // check if front flashlight is available
            frontChecker();
        }




        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // update view on progress bar change
                current = progress + min;
                textViewProgress.setText("" + current);
                playSound(true);

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // on stop tracking start runner on/off at a frequency
                enableFlick =true;
                if (!mainSwitchState){
                    // if the main switch state off toast
                   toast4All(MainActivity.this,"Turn on flashlight");
                }
                // remove the previous callback b4 a new one
                if (handlerFlick !=null && runnableFlick != null){
                    handlerFlick.removeCallbacks(runnableFlick);
                }

                stateToggle =true;
                long timeFlick=current*10;
                handlerFlick = new Handler();
                runnableFlick = new Runnable() {
                    public void run() {

                        if (mainSwitchState && current != 0 && enableFlick) {
                            if (stateToggle){
                                toggleFlashlight(MainActivity.this, selectButtons(), true);
                                stateToggle =false;
                            }else {
                                toggleFlashlight(MainActivity.this, selectButtons(), false);
                                 stateToggle =true;
                            }
                            handlerFlick.postDelayed(this, timeFlick);
                        }else {
                            // turn off b4 removing handler if the switch is off
                            toggleFlashlight(MainActivity.this, selectButtons(), false);
                            handlerFlick.removeCallbacks(this);
                        }
                    }
                };
                handlerFlick.postDelayed(runnableFlick, timeFlick);

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // create menu items
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.share:
                Methods.shareApp(this);
                return true;
            case R.id.more_apps:
                Methods.moreApps(this);
                return true;
            case R.id.send_feedback:
                Methods.sendFeedback(this);
                return true;
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setAnimators(View target){
        // set animation on imageButtons when clicked
        Animator animator1 = AnimatorInflater.loadAnimator(this, R.animator.button_animators);
        animator1.setTarget(target);
        animator1.addListener(this);
        animator1.setupEndValues();
        animator1.start();
    }
    @Override
    public void onAnimationStart(Animator animation) {
        // disable power click on start
        mainSwitch.setEnabled(false);
    }
    @Override
    public void onAnimationEnd(Animator animation) {
        // enable power click when animations done
        mainSwitch.setEnabled(true);
    }
    @Override
    public void onAnimationCancel(Animator animation) {
    }
    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    @Override
    public void onBackPressed() {
        /////
        //super.onBackPressed();
        // show exit dialog if back is pressed on activity
            showCustomDialog();
    }

    @Override
    protected void onResume() {
        // on resume after screen flashlight show adds
        super.onResume();
        updateBatteryCharge();
        if (screenSwitchState){
          powerOffSwitch();
        }
    }






    private void toggleFlashLight(final boolean on_off, final int id){
        if (on_off && id==2){
            // turn off front n back flashlight
            Intent torchIntent = new Intent(MainActivity.this, MainService.class);
            torchIntent.putExtra("toggle", String.valueOf(false));
            startService(torchIntent);
            // screen flashlight intent
            Intent torchIntent1 = new Intent(MainActivity.this, FlashScreenActivity.class);
            startActivity(torchIntent1);
        }else{
            // run flashlight on background
            Intent torchIntent = new Intent(MainActivity.this, MainService.class);
            torchIntent.putExtra("id", String.valueOf(id));
            torchIntent.putExtra("toggle", String.valueOf(on_off));
            startService(torchIntent);
        }

    }
    public void mainSwitch(View view) {
           // if the main button is pressed check if device has flashlight
        if (selectButtons() != 2 && !feature_camera_flash){
            showCustomDialogError(getString(R.string.error_all_flash));
            return;
        }
        // animate view
        setAnimators(mainSwitch);
        if (mainSwitchState){
            // if then main switch was on before click set it to false turn off flashlight
            powerOffSwitch();

        }else {
            // if then main switch was off before click set it to true turn on flashlight
            playSound(true);
            mainSwitchState = true;
            mainSwitch.setImageResource(R.drawable.ic_power_btn_yellow);
            toggleFlashLight(true,selectButtons());
        }
    }

    private void powerOffSwitch(){
        if (mainSwitchState) {
            playSound(false);
            mainSwitchState = false;
            mainSwitch.setImageResource(R.drawable.ic_power_btn);
            toggleFlashLight(false, selectButtons());
        }
    }

    public void mainSwitchSec() {
        //switch on flashlight onclick of the other buttons except main switch
        enableFlick =false;
        setAnimators(mainSwitch);
        playSound(true);
        mainSwitchState = true;
        mainSwitch.setImageResource(R.drawable.ic_power_btn_yellow);
        toggleFlashLight(true,selectButtons());
        //setTimer();
    }

    public void frontFlash(View view) {
        // onclick front switch icon
        if (feature_camera_flash) {
            if (feature_camera_front){
            if (frontSwitchState) {
                frontSwitch.setEnabled(false);
                backSwitch.setEnabled(true);
                screenSwitch.setEnabled(true);
                return;
            }
            setAnimators(frontSwitch);
            frontSwitchState = true;
            backSwitchState = false;
            screenSwitchState = false;
            mainSwitchSec();
            }else {
                showCustomDialogError(getString(R.string.error_front_cam));
            }
        }else {
            //device error toast
            showCustomDialogError(getString(R.string.error_all_flash));
        }
    }

    public void backFlash(View view) {
        // onclick back switch icon
        if (feature_camera_flash) {
        if (backSwitchState){
            backSwitch.setEnabled(false);
            frontSwitch.setEnabled(true);
            screenSwitch.setEnabled(true);
            return;
        }
        setAnimators(backSwitch);
        backSwitchState =true;
        frontSwitchState =false;
        screenSwitchState =false;
        mainSwitchSec();
        }else {
            //back device error toast
            showCustomDialogError(getString(R.string.error_all_flash));
        }
    }
    public void screenFlash(View view) {
        // onclick screen switch icon
        if (screenSwitchState){
            screenSwitch.setEnabled(false);
            frontSwitch.setEnabled(true);
            backSwitch.setEnabled(true);
            return;
        }
        setAnimators(screenSwitch);
        screenSwitchState =true;
        frontSwitchState =false;
        backSwitchState =false;
        mainSwitchSec();
    }
    private int selectButtons(){
        // change imageButtons background alpha according to which is selected and return the selected item based on clicked item
        backSwitch.setAlpha(0.5f);
        frontSwitch.setAlpha(0.5f);
        screenSwitch.setAlpha(0.5f);

        if (frontSwitchState)
        {
            frontSwitch.setAlpha(1f);
            return 1;
        }
        else if (backSwitchState)
        {
            backSwitch.setAlpha(1f);
            return 0;
        }
        else if (screenSwitchState)
        {
            screenSwitch.setAlpha(1f);
            return 2;
        }
        backSwitch.setAlpha(1f);
        return 0;
    }






    private void updateBatteryCharge(){
        // run always and update the battery level at after period
        textViewBattery.setText(Methods.getBatteryPercentage(MainActivity.this)+"%");
        handlerBatteryCheck = new Handler();
        final Runnable r = () -> textViewBattery.setText(Methods.getBatteryPercentage(MainActivity.this)+"%");
        handlerBatteryCheck.postDelayed(r, batteryUpdatePeriod);
    }

    private void playSound(boolean on_off) {
        // play background sounds on on/off clicks
        if (Boolean.parseBoolean(preferenceSettings.getString("switchSound", "true"))) {
            MediaPlayer mediaPlayer;
            if (on_off) {
                mediaPlayer = MediaPlayer.create(this, R.raw.switch_on);
            } else {
                mediaPlayer = MediaPlayer.create(this, R.raw.switch_off);
            }
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            mediaPlayer.start();
        }
    }
    private void showCustomDialogError(String message) {
        // show this dialog when exiting the app
        try {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_error, null);
            Button ok=dialogView.findViewById(R.id.buttonOk);
            TextView errorMessage=dialogView.findViewById(R.id.textViewErrorM);
            errorMessage.setText(message);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            ok.setOnClickListener(v -> {
                alertDialog.dismiss();
            });
        } catch (Exception e) {
            // if dialog does not work
            toast4All(this,message);
        }

    }
    private void showCustomDialog() {
        // show this dialog when exiting the app
        try {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null);
            Button cancel=dialogView.findViewById(R.id.button_cancel);
            Button ok=dialogView.findViewById(R.id.buttonOk);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            ok.setOnClickListener(v -> {
                alertDialog.dismiss();
                if (Boolean.parseBoolean(preferenceSettings.getString("turnOffAtExit", "false"))) {
                    toggleFlashlight(MainActivity.this, selectButtons(), false);
                }
                finish();
            });
            cancel.setOnClickListener(v -> alertDialog.dismiss());
        } catch (Exception e) {
            if (Boolean.parseBoolean(preferenceSettings.getString("turnOffAtExit", "false"))) {
                toggleFlashlight(MainActivity.this, selectButtons(), false);
            }
            finish();
        }

    }

    private void frontChecker() {
        // this check if if front lights are available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager camManager;
            try {
                String cameraId;
                camManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
                if (camManager != null) {
                    cameraId = camManager.getCameraIdList()[1]; // Usually front camera is at 0 position.
                    camManager.setTorchMode(cameraId, false);
                    feature_camera_front=true;
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                feature_camera_front=false;
                return;
            }
        }else {
            feature_camera_front=false;
            return;
        }
        feature_camera_front=false;
    }
}