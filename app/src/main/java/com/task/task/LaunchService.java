package com.task.task;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;

import java.util.Timer;
import java.util.TimerTask;

public class LaunchService extends Service implements SoundPool.OnLoadCompleteListener {
    private int cycleDelay, turnedOnDelay;
    private Timer timer;
    private Boolean vibroStatus, flashStatus, soundStatus;
    private SharedPreferences data;
    private TimerTask cycleDelayTask, turnedOnDelayTask;
    private PendingIntent pi;
    final Handler handler = new Handler();
    private Camera camera;
    Camera.Parameters p;
    SoundPool sp;
    int soundIdShot;
    int streamIDShot;
    public void onCreate() {
        super.onCreate();
        camera = Camera.open();
        p = camera.getParameters();
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        sp = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
        sp.setOnLoadCompleteListener(this);

        soundIdShot = sp.load(this, R.raw.shot, 1);
        loadData();
    }

    private void loadData() {
        data = getSharedPreferences("data", 0);
        vibroStatus = data.getBoolean("vibroStatus", false);
        flashStatus = data.getBoolean("flashStatus", false);
        soundStatus = data.getBoolean("soundStatus", false);
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        pi = intent.getParcelableExtra(MainActivity.PARAM_PINTENT);
        int BPM = intent.getIntExtra("BPM", 1);
        cycleDelay = 60000/BPM;
        turnedOnDelay = cycleDelay/4;

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        stopTimerTask();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }


    public void startTimer() {
        timer = new Timer();
        startAction();
        initializeCycleDelay();
        initializeTurnedOneDelay();
        timer.schedule(cycleDelayTask, cycleDelay);
        timer.schedule(turnedOnDelayTask, turnedOnDelay);

    }

    public void stopTimerTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        stopAction();
    }

    public void initializeCycleDelay() {

        cycleDelayTask = new TimerTask() {
            public void run() {

                handler.post(new Runnable() {
                    public void run() {
                        startTimer();
                    }
                });
            }
        };
    }
    public void initializeTurnedOneDelay() {
        turnedOnDelayTask = new TimerTask() {
            public void run() {

                handler.post(new Runnable() {
                    public void run() {
                        stopAction();
                    }
                });
            }
        };
    }

    public void startAction(){
        Intent intent = new Intent().putExtra(MainActivity.PARAM_RESULT, MainActivity.TASK_RESULT_RED);
        try {
            pi.send(LaunchService.this, 1, intent);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
        if (soundStatus){
        streamIDShot = sp.play(soundIdShot, 1, 1, 0, turnedOnDelay, 1);
        }
        if (flashStatus && camera != null) {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
            camera.startPreview();
        }
        if (vibroStatus) {
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(turnedOnDelay);
        }

    }

    public void stopAction(){
        Intent intent = new Intent().putExtra(MainActivity.PARAM_RESULT, MainActivity.TASK_RESULT_GREEN);
        try {
            pi.send(LaunchService.this, 1, intent);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

        if (soundStatus) {
            sp.stop(streamIDShot);
        }
        if (flashStatus && camera != null){
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);
            camera.stopPreview();
        }

    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        startTimer();
    }
}