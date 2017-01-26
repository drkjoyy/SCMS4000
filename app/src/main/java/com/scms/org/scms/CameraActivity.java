package com.scms.org.scms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

public class CameraActivity extends Activity {
    private static final String TAG = "CameraActivity";
    private Camera mCamera = null;
    private CameraView mCameraView = null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static Bitmap imgResult = null;
    static final String MEDIA_FILE = "mediafile";
    ImageView redReference;
    ImageView greenReference;
    ImageView blueReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            int cameraMode = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_CAMERA, "0"));
            mCamera = Camera.open(cameraMode);
        } catch (Exception e){
            Log.d(TAG, "Failed to get camera: " + e.getMessage());
            setResult(RESULT_CANCELED);
            finish();
        }

        if(mCamera != null) {
            mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }

        //btn to close the application
        ImageButton imgClose = (ImageButton)findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        //ImageView img = (ImageView) findViewById(R.id.imgResult);
        //img.setImageResource(R.drawable.demo_level_planet);
        //img.setImageBitmap(R.drawable.demo_level_planet);
        //img.getLayoutParams().height = 200;
        //img.getLayoutParams().width = 200;
        //btn to capture photo
        Button imgCapture = (Button)findViewById(R.id.capture);
        imgCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runThread();
            }
        });

        redReference = (ImageView)findViewById(R.id.redReference);
        redReference.setAlpha(0);
        greenReference = (ImageView)findViewById(R.id.greenReference);
        greenReference.setAlpha(0);
        blueReference = (ImageView)findViewById(R.id.blueReference);
        blueReference.setAlpha(0);

    }

    private final Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            galleryAddPic();
            File pictureFile = getOutputMediaFile(1);
            if (pictureFile == null) {
                setResult(RESULT_CANCELED);
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Intent returnedData = new Intent();
                returnedData.putExtra(MEDIA_FILE, pictureFile);
                setResult(RESULT_OK, returnedData);
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        }
    };

    private void runThread() {
        Thread s = new Thread(){
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                redReference.setAlpha(255);
                                mCamera.takePicture(null, null, mPicture);
                                mCamera.startPreview();
                            }
                        });
                        wait(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                redReference.setAlpha(0);
                                greenReference.setAlpha(255);
                                mCamera.takePicture(null, null, mPicture);
                                mCamera.startPreview();
                            }
                        });
                        wait(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                greenReference.setAlpha(0);
                                blueReference.setAlpha(255);
                                mCamera.takePicture(null, null, mPicture);
                                mCamera.startPreview();
                            }
                        });
                        wait(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                blueReference.setAlpha(0);
                            }
                        });
                        finish();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
        };
        s.start();
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = getOutputMediaFile(1);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == 2) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}