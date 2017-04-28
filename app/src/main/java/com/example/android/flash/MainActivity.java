package com.example.android.flash;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;


import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.BoolRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import android.content.DialogInterface;
import android.content.pm.PackageManager;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

//import com.pddstudio.easyflashlight.EasyFlashlight;

import java.security.Policy;
import java.security.PrivateKey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import static com.example.android.flash.R.id.fab;

public class MainActivity extends AppCompatActivity {

    private CameraManager mCameraManager;
    private String mCameraId;
    private ImageButton mTorchOnOffButton;
    private Boolean isTorchOn;
    private Boolean isFlickering;
    private SeekBar freqSeekBar;
    private int frequency;
    private ImageButton mCameraButton;
    private RelativeLayout mainLayout;
    private Thread a = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        freqSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mTorchOnOffButton = (ImageButton) findViewById(R.id.torch_button);
        mCameraButton = (ImageButton) findViewById(R.id.camera_button);
        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        isTorchOn = false;
        isFlickering = false;



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                finish();
                System.exit(0);
            }
        });

        Boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {

            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error !!");
            alert.setMessage("Your device doesn't support flash light!");
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                    System.exit(0);
                }
            });
            alert.show();
            return;
        }

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mTorchOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isTorchOn) {
                        turnOffFlashLight();
                        mCameraButton.setVisibility(View.INVISIBLE);
                        freqSeekBar.setVisibility(View.INVISIBLE);
                        mTorchOnOffButton.setImageResource(R.drawable.off);
                        isTorchOn = false;
                        isFlickering = false;
                    } else {
                        turnOnFlashLight();
                        mCameraButton.setVisibility(View.VISIBLE);
                        freqSeekBar.setVisibility(View.VISIBLE);
                        mTorchOnOffButton.setImageResource(R.drawable.on);
                        freqSeekBar.setProgress(0);
                        isTorchOn = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        freqSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                frequency = freqSeekBar.getProgress();
                isFlickering = false;
                if(frequency > 0) {
                    try {
                        flash_effect(1000 - (100 * (frequency - 1)));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    isFlickering = false;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void turnOnFlashLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void turnOffFlashLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isTorchOn) {
            turnOffFlashLight();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTorchOn) {
            turnOffFlashLight();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTorchOn) {
            turnOnFlashLight();
        }
    }

    public void flash_effect(final int frequency) throws InterruptedException {
        Log.d("FREQ", String.valueOf(frequency));

        a = new Thread() {
            public void run() {
                Boolean isOn = false;
                while (isFlickering){
                    if(isOn) {
                        turnOffFlashLight();
                    } else {
                        turnOnFlashLight();
                    }
                    isOn = !isOn;
                    try {
                        Thread.sleep(frequency);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(!isFlickering) {
                    interrupt();
                }
            }
        };

        isFlickering = true;
        a.start();
    }
}




