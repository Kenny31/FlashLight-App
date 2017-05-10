package com.example.android.flash;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements OnPictureCapturedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

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

        checkPermissions();

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

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePhotos();
            }
        });

        mTorchOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isTorchOn) {
                        isTorchOn = false;
                        isFlickering = false;
                        turnOffFlashLight();
                        mCameraButton.setVisibility(View.INVISIBLE);
                        freqSeekBar.setVisibility(View.INVISIBLE);
                        mTorchOnOffButton.setImageResource(R.drawable.off);
                        a.interrupt();
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
                int seekBarPos = freqSeekBar.getProgress();
                isFlickering = false;
                if(seekBarPos > 0) {
                    try {
                        frequency = 1000 - (100 * (seekBarPos - 1));
                        flash_effect();
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

    private void capturePhotos() {
        new PictureService().startCapturing(this, this);
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

    public void flash_effect() throws InterruptedException {
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
                    Log.d("FREQ", String.valueOf(frequency));
                    Thread.sleep(frequency);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d("THREAD", "Thread finished" + frequency);
//                if(!isFlickering) {
//                    try {
//                        wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        };

        isFlickering = true;
        if(!a.isAlive()) {
            a.start();
        }
    }

    @Override
    public void onCaptureDone(final String pictureUrl, final byte[] pictureData) {
        if (pictureData != null && pictureUrl != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
                    final int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                    final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                    if (pictureUrl.contains("0_pic.jpg")) {
//                    uploadBackPhoto.setImageBitmap(scaled);
                    } else if (pictureUrl.contains("1_pic.jpg")) {
//                    uploadFrontPhoto.setImageBitmap(scaled);
                    }
                }
            });
            Log.d("CAMERA", "Picture saved to " + pictureUrl);
        }
    }

    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {
        if (picturesTaken != null && !picturesTaken.isEmpty()) {
            Log.d("CAMERA", "Done capturing all photos!");
            return;
        }
        Log.d("CAMERA", "No camera detected!");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkPermissions();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        final String[] requiredPermissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
        };
        final List<String> neededPermissions = new ArrayList<>();
        for (final String p : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    p) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(p);
            }
        }
        if (!neededPermissions.isEmpty()) {
            requestPermissions(neededPermissions.toArray(new String[]{}),
                    1);
        }
    }
}




