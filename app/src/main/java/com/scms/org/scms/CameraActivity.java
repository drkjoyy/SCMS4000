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
                mCamera.takePicture(null, null, mPicture);
                System.out.println("TOOK THE IMAGE");
                finish();
            }
        });
    }

    private final Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
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

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.mkdirs()) { // mkdirs() checks for exists() first.
            Log.d(TAG, "failed to create directory");
            return null;
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir, "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }
}
