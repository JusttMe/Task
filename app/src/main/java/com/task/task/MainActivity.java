package com.task.task;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener{
    public final static String PARAM_PINTENT = "pendingIntent";
    public final static String PARAM_RESULT = "result";
    public final static int TASK_RESULT_GREEN = 1;
    public final static int TASK_RESULT_RED = 2;
    private ToggleButton vibroBtn, flashBtn, soundBtn, startBtn;
    private TextView labelBPMHint;
    private ImageView indicator;
    private EditText BPMField;
    private SeekBar BPMSeekBar;
    private SharedPreferences data;
    private Boolean vibroStatus, flashStatus, soundStatus;
    private Integer BPM;
    private Timer timer;
    private TimerTask delayTask;
    final Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        labelBPMHint = (TextView) findViewById(R.id.labelBPMHint);
        vibroBtn = (ToggleButton) findViewById(R.id.vibroBtn);
        flashBtn = (ToggleButton) findViewById(R.id.flashBtn);
        soundBtn = (ToggleButton) findViewById(R.id.soundBtn);
        startBtn = (ToggleButton) findViewById(R.id.startBtn);
        BPMSeekBar = (SeekBar) findViewById(R.id.seekBar);
        BPMField = (EditText) findViewById(R.id.BPMField);
        indicator = (ImageView) findViewById(R.id.indicator);

        Typeface ms100 = Typeface.createFromAsset(getAssets(), "fonts/museosans-100-webfont.ttf");
        Typeface ms500 = Typeface.createFromAsset(getAssets(), "fonts/museosans-500-webfont.ttf");
        labelBPMHint.setTypeface(ms100);
        vibroBtn.setTypeface(ms500);
        flashBtn.setTypeface(ms500);
        soundBtn.setTypeface(ms500);

        BPMSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                BPMField.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        BPMField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    BPMSeekBar.setProgress(Integer.parseInt(BPMField.getText().toString()));
                } catch (NumberFormatException e){
                }
            }
        });

        setData();

        vibroBtn.setChecked(vibroStatus);
        flashBtn.setChecked(flashStatus);
        soundBtn.setChecked(soundStatus);
        BPMSeekBar.setProgress(BPM);
        BPMField.setText(Integer.toString(BPM));
    }



    @Override
    protected void onStop() {
        super.onStop();

    }



    private void saveData() {
        data = getSharedPreferences("data", 0);
        SharedPreferences.Editor editor = data.edit();
        editor.putBoolean("vibroStatus", vibroStatus);
        editor.putBoolean("flashStatus", flashStatus);
        editor.putBoolean("soundStatus", soundStatus);
        editor.putInt("BPM", BPM);
        editor.commit();
    }

    public void getData() {
        vibroStatus = vibroBtn.isChecked();
        flashStatus = flashBtn.isChecked();
        soundStatus = soundBtn.isChecked();
        try {
            BPM = Integer.parseInt(BPMField.getText().toString()) ;
        } catch (NumberFormatException e){
            Toast.makeText(this, "Wrong input", Toast.LENGTH_SHORT).show();
        }
    }
    public void setData() {
        data = getSharedPreferences("data", 0);
        vibroStatus = data.getBoolean("vibroStatus", false);
        flashStatus = data.getBoolean("flashStatus", false);
        soundStatus = data.getBoolean("soundStatus", false);
        BPM = data.getInt("BPM", 100);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startBtn:
                getData();
                if (BPM == 0 || BPM>200 ){
                    Toast.makeText(this, "BPM can't be that", Toast.LENGTH_SHORT).show();
                    startBtn.setChecked(false);
                    break;
                }

                if (startBtn.isChecked()){
                    PendingIntent pi;
                    pi = createPendingResult(1, new Intent(), 0);
                    startService(new Intent(this, LaunchService.class).putExtra("BPM", BPM).
                            putExtra(PARAM_PINTENT, pi));
                    startBtn.setClickable(false);
                    startTimer();
                    saveData();
                } else {
                    stopService(new Intent(this, LaunchService.class));
                }


                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            int result = data.getIntExtra(PARAM_RESULT, 0);
            switch (result) {
                case TASK_RESULT_RED:
                    indicator.setBackground(getResources().getDrawable(R.drawable.indicator_red));
                    break;
                case TASK_RESULT_GREEN:
                    indicator.setBackground(getResources().getDrawable(R.drawable.indicator_green));
                    break;
            }
    }

    public void startTimer() {
        timer = new Timer();
        initializeDelay();
        timer.schedule(delayTask, 500);

    }



    public void initializeDelay() {

        delayTask = new TimerTask() {
            public void run() {

                handler.post(new Runnable() {
                    public void run() {
                        startBtn.setClickable(true);
                    }
                });
            }
        };
    }
}
