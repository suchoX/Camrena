package com.sucho.camrena;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sucho.camrena.customview.CameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PhotoActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = "PhotoActivity";
    private Camera camera;
    private CameraPreview cameraPreview;
    FrameLayout cameraPreviewFrame;
    FloatingActionButton photoCapture,videoCapture,swapCamera;

    int camIdx=0;
    File imageFile;

    MediaRecorder recorder;
    Camera recordCam;
    SurfaceView videoView;
    TextView recordingText;
    SurfaceHolder videoHolder;
    boolean recording = false;
    int surfaceWidth,surfaceHeight;

    Camera.PictureCallback captureCallback = new Camera.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            int angleToRotate = getRoatationAngle(camIdx);

            angleToRotate = angleToRotate + 180;
            Bitmap orignalImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap bitmapImage = rotate(orignalImage, angleToRotate);
            imageFile = getImageFile();
            if (imageFile == null) {
                return;
            }
            new imageSave().execute(bitmapImage);
            camera.startPreview();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initCamera();
        cameraPreviewFrame = (FrameLayout) findViewById(R.id.camera_preview);
        cameraPreviewFrame.addView(cameraPreview);

        photoCapture = (FloatingActionButton) findViewById(R.id.photo_capture);
        photoCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, captureCallback);
            }
        });

        videoCapture = (FloatingActionButton) findViewById(R.id.video_capture);
        videoCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!recording)
                    initRecorder();
                else
                {
                    recorder.stop();
                    recorder.release();
                    recording = false;
                    videoView.setVisibility(View.GONE);
                    cameraPreviewFrame.setVisibility(View.VISIBLE);
                    initCamera();
                    cameraPreviewFrame.addView(cameraPreview);
                }
            }
        });

        swapCamera = (FloatingActionButton) findViewById(R.id.swap_camera);
        swapCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(camIdx == Camera.CameraInfo.CAMERA_FACING_FRONT)
                    camIdx = Camera.CameraInfo.CAMERA_FACING_BACK;
                else
                    camIdx = Camera.CameraInfo.CAMERA_FACING_FRONT;
                camera.stopPreview();
                camera.release();
                camera = Camera.open(camIdx);
                cameraPreview.switchCamera(camera);
            }
        });

        videoView = (SurfaceView)findViewById(R.id.video_preview);
        recordingText = (TextView)findViewById(R.id.recording_text);

    }

    private void initCamera()
    {
        if(camIdx==0) {
            camera = getFrontCameraInstance();
            if (camera == null) {
                Toast.makeText(PhotoActivity.this, "No Front Camera! Switching Back Camera", Toast.LENGTH_LONG).show();
                camera = getBackCameraInstance();
                if (camera == null) {
                    Toast.makeText(PhotoActivity.this, "No Camera Found", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            else
                cameraPreview = new CameraPreview(this, camera);
        }
        else if(camIdx == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            camera = Camera.open(camIdx);
            cameraPreview = new CameraPreview(this, camera);
        }
        else if(camIdx == Camera.CameraInfo.CAMERA_FACING_BACK) {
            camera = Camera.open(camIdx);
            cameraPreview = new CameraPreview(this, camera);
        }
    }

    private Camera getFrontCameraInstance() {
        int cameraCount = 0;
        Camera camera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    this.camIdx = camIdx;
                    camera = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
        return camera;
    }

    private Camera getBackCameraInstance() {
        int cameraCount = 0;
        Camera camera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    this.camIdx = camIdx;
                    camera = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
        return camera;
    }
    
    private static File getImageFile() {
        File imageStorageDir = new File(Environment.getExternalStorageDirectory()+File.separator+"Camrena"+File.separator + "Photos");
        if (!imageStorageDir.exists()) {
            if (!imageStorageDir.mkdirs()) {
                Log.e(TAG, "Couldn't create Directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imageFile;
        imageFile = new File(imageStorageDir.getPath() + File.separator + "PHOTO_" + timeStamp + ".jpg");
        Log.e(TAG,imageFile.getAbsolutePath());
        return imageFile;
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public int getRoatationAngle(int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    class imageSave extends AsyncTask<Bitmap, Void, Void>
    {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Bitmap... params)
        {
            Bitmap bitmapImage = params[0];
            try {
                FileOutputStream outStream = new FileOutputStream(imageFile);
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
                MediaStore.Images.Media.insertImage(getContentResolver(), imageFile.getAbsolutePath(), imageFile.getName(), imageFile.getName());
            }catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {

        }
    }

    private void initRecorder(){
        cameraPreviewFrame.setVisibility(View.GONE);
        cameraPreviewFrame.removeAllViews();
        videoView.setVisibility(View.VISIBLE);
        videoHolder = videoView.getHolder();
        videoHolder.addCallback(this);


        recordCam = Camera.open(camIdx);
        setOrientation();
        recordCam.unlock();

        //recordCam.stopPreview();
        recorder = new MediaRecorder();
        recorder.setCamera(recordCam);
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        File imageStorageDir = new File(Environment.getExternalStorageDirectory()+File.separator+"Camrena"+File.separator + "Videos");
        if (!imageStorageDir.exists()) {
            if (!imageStorageDir.mkdirs()) {
                Log.e(TAG, "Couldn't create Directory");
            }
        }
        CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        recorder.setOutputFile(imageStorageDir.getPath() + File.separator + "VIDEO_" + timeStamp + ".mp4");
        //setOrientation();
        /*recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);*/
        /*recorder.setMaxDuration(50000); // 50 seconds
        recorder.setMaxFileSize(5000000); // Approximately 5 megabytes*/


    }

    private void prepareRecorder() {
        recorder.setPreviewDisplay(videoHolder.getSurface());
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
        recording = true;
        recorder.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG,"Video Surface Created");
        prepareRecorder();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        surfaceWidth = width;
        surfaceHeight = height;
        //setOrientation();
        /*try {
            recordCam.setPreviewDisplay(videoHolder);
            recordCam.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*Camera.Size size = getBestPreviewSize(width,height, recordCam.getParameters());
        recorder.setVideoSize(size.width,size.height);*/
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG,"Video Surface Destroyed");
        if (recording) {
            recorder.stop();
            recorder.release();
            recording = false;
        }
        recordCam.stopPreview();
        recordCam.release();
    }

    private void setOrientation()
    {
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        //recordCam.stopPreview();

        if(display.getRotation() == Surface.ROTATION_0)
            recordCam.setDisplayOrientation(90);

        if(display.getRotation() == Surface.ROTATION_270)
            recordCam.setDisplayOrientation(180);
        //recordCam.startPreview();
    }
    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return (result);
    }
}
