package com.sucho.camrena;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sucho.camrena.customview.CameraPreview;
import com.sucho.camrena.realm.GalleryObject;
import com.sucho.camrena.service.UploadService;
import com.sucho.camrena.service.VideoUploadService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

@SuppressWarnings("deprecation")
public class PhotoActivity extends AppCompatActivity implements SurfaceHolder.Callback,SensorEventListener {

    /**
     * Since Image Capture and Video Capture is in same activity, I used a Custom SurfaceView(customview.CameraPreview)
     * for the Default/Image Preview which is attached to a FrameLayout and Surfaceview for video capture. This gives
     * me to handle and listen to events for two different surfaces at the same time. Either the FrameLayout or the Surafceview
     * is View.GONE based on the action at that time
     */
    private static final String TAG = "PhotoActivity";
    private Camera camera;
    private CameraPreview cameraPreview;
    FrameLayout cameraPreviewFrame;
    FloatingActionButton photoCapture,videoCapture,swapCamera,stopRecord,gallery;
    MediaPlayer cameraClick;

    int camIdx=99;
    File imageFile;

    MediaRecorder recorder;
    Camera recordCam;
    SurfaceView videoView;
    TextView recordingText;
    SurfaceHolder videoHolder;
    boolean recording = false,recorderPrep=false,recorderPreped=false,surfaceCreated=false;
    int surfaceWidth,surfaceHeight;
    String videoPath,videoName;

    Realm realm;
    RealmConfiguration realmConfig;

    private SensorManager mSensorManager;
    Sensor accelerometer;
    float orientationValue;

    SharedPreferences syncPreference;
    SharedPreferences.Editor editor;


    Camera.PictureCallback captureCallback = new Camera.PictureCallback()   //Picture callback object
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            int angleToRotate = getRotationAngle(camIdx,0); //Get the angle in which the Image captured has to be rotated

            Bitmap orignalImage = BitmapFactory.decodeByteArray(data, 0, data.length);  //Forming Biytmap from the byte array received
            Bitmap bitmapImage = rotate(orignalImage, angleToRotate,camIdx);    //Rotating the image
            imageFile = getImageFile(); //Creating a File in which the image has to be stored
            if (imageFile == null) {
                return;
            }
            new imageSave().execute(bitmapImage);   //Saving the Image to file via AsyncTask
            camera.startPreview();
            photoCapture.setVisibility(View.VISIBLE);
            gallery.setVisibility(View.VISIBLE);
            videoCapture.setVisibility(View.VISIBLE);
            swapCamera.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);   //Hide Status Bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //Keep screen On

        realmConfig = new RealmConfiguration.Builder(this).build();
        realm = Realm.getInstance(realmConfig);

        camIdx = getIntent().getIntExtra("Camera",99);//get Cam id if returning from GalleryActivity. This makes sure that you return to the same camera from gallery

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //Accelerometer is used to get device rotation as Auto-Rotation may be turned off

        if(checkSyncStatus()==2)
            showSyncDialog();

        startUploadService();

        initCamera();   //Initializing Camera
        recorder = new MediaRecorder();
        cameraClick = MediaPlayer.create(getApplication(), R.raw.camera_click);
        cameraPreviewFrame = (FrameLayout) findViewById(R.id.camera_preview);
        cameraPreviewFrame.addView(cameraPreview);
        photoCapture = (FloatingActionButton) findViewById(R.id.photo_capture);
        photoCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraClick.start();
                camera.takePicture(null, null, captureCallback);    //Calling The Camera Capture Callback
                photoCapture.setVisibility(View.INVISIBLE);
                gallery.setVisibility(View.INVISIBLE);
                videoCapture.setVisibility(View.INVISIBLE);
                swapCamera.setVisibility(View.INVISIBLE);
            }
        });

        videoCapture = (FloatingActionButton) findViewById(R.id.video_capture);
        videoCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!recording)
                    initRecorder(); //Initializing MediaRecorder for Video Record
            }
        });

        swapCamera = (FloatingActionButton) findViewById(R.id.swap_camera);
        swapCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //This swaps the camera from Front to Back or vice versa
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

        stopRecord = (FloatingActionButton) findViewById(R.id.stop_recording);
        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recording)//Stop recording is recording is going on
                {
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                    recorder = new MediaRecorder();
                    //Going back to Image Capture Surface
                    recording = recorderPrep = recorderPreped = surfaceCreated = false;
                    videoView.setVisibility(View.GONE);
                    stopRecord.setVisibility(View.GONE);
                    cameraPreviewFrame.setVisibility(View.VISIBLE);
                    initCamera();
                    cameraPreviewFrame.addView(cameraPreview);
                    photoCapture.setVisibility(View.VISIBLE);
                    videoCapture.setVisibility(View.VISIBLE);
                    swapCamera.setVisibility(View.VISIBLE);
                    gallery.setVisibility(View.VISIBLE);

                    //Storing the video details to database
                    GalleryObject galleryObject = new GalleryObject();
                    realm.beginTransaction();
                    galleryObject.setId(videoName);
                    galleryObject.setPath(videoPath);
                    galleryObject.setImage(false);
                    galleryObject.setLocal(true);
                    galleryObject.setSynced(false);
                    realm.copyToRealmOrUpdate(galleryObject);
                    realm.commitTransaction();

                }
            }
        });

        gallery = (FloatingActionButton)findViewById(R.id.gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhotoActivity.this,GalleryActivity.class);   //Starting GalleryActivity
                intent.putExtra("Camera",camIdx);
                startActivity(intent);
                finish();
            }
        });

        videoView = (SurfaceView)findViewById(R.id.video_preview);
        recordingText = (TextView)findViewById(R.id.recording_text);

    }

    private int checkSyncStatus()
    {
        /**
         * This method checks the user's option on Auto Sync
         */
        syncPreference = this.getSharedPreferences("EventData", 0);
        return syncPreference.getInt("Sync Status",2); //0-Don't Sync; 1-Sync: 2-First Time
    }

    private void showSyncDialog()
    {
        /**
         * This Method shows the Auto Sync option Dialog on app's first run
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(PhotoActivity.this);
        builder.setTitle("Auto Sync");
        builder.setMessage("Do you automatically want your photos and videos to be synced to cloud?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                editor = syncPreference.edit();
                editor.putInt("Sync Status",1);
                editor.apply();
            }
        });
        builder.setNegativeButton("NO",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                editor = syncPreference.edit();
                editor.putInt("Sync Status",0);
                editor.apply();
            }
        });
        builder.setCancelable(false);   //The dialog box cannot be cancelled.
        builder.show();
    }

    public void setFrameLayout(int width,int height)
    {
        /**
         * This method is called from customview.CameraPreview to set the dimensions
         * of the enclosing FrameLayout to the dimensions of the Image to be captured.
         * This prevents Preview Stretch
         */
        cameraPreviewFrame.setLayoutParams(new RelativeLayout.LayoutParams(width,height));
    }

    private void initCamera()
    {
        if(camIdx==99)  //No previous Camera choice, Front camera given preference
        {
            camera = getFrontCameraInstance();
            if (camera == null) {
                swapCamera.setVisibility(View.GONE);
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
        else if(camIdx == Camera.CameraInfo.CAMERA_FACING_FRONT) {  //Front Camera Preference
            camera = Camera.open(camIdx);
            cameraPreview = new CameraPreview(this, camera);
        }
        else if(camIdx == Camera.CameraInfo.CAMERA_FACING_BACK) {   //Back Camera Preference
            camera = Camera.open(camIdx);
            cameraPreview = new CameraPreview(this, camera);
        }
    }

    private Camera getFrontCameraInstance() {
        /**
         * This method returns the front camera instance. If no front camera, will return null
         */
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
        /**
         * This method returns the back camera instance. If no back camera, will return null
         */
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
        /**
         * This method creates an empty file which is unique as it uses timestamp in the file name
         */
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

    public static Bitmap rotate(Bitmap bitmap, int degree, int cam)
    {
        /**
         * This method rotates the received Bitmap to the specified angle and returns it
         */
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        if(cam == Camera.CameraInfo.CAMERA_FACING_FRONT)
            mtx.preScale(1.0f,-1.0f);
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public int getRotationAngle(int cameraId,int type)
    {
        /**
         * This method returns the angle in which the Image/Video has to be rotated
         */
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int degrees = 0;

        //Log.e(TAG,"Orientation "+ orientationValue);

        if(type==0) //Rotation for images
        {
            if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                if(orientationValue > 7)//Left Orientation
                    degrees = 270;
                else if(orientationValue < -7)//Right orientation
                    degrees =90;
                else
                    degrees=180;
            }
            else {
                if (orientationValue > 7)//Left Orientation
                    degrees = 90;
                else if (orientationValue < -7)//Right orientation
                    degrees = 270;
                else
                    degrees = 0;
            }
        }
        else if(type==1)//Rotation for Video
        {
            if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                if(orientationValue > 7)//Left Orientation
                    degrees = 90;
                else if(orientationValue < -7)
                    degrees =270;
                else
                    degrees=180;
            }
            else {
                if (orientationValue > 7)//Left Orientation
                    degrees = 90;
                else if (orientationValue < -7)
                    degrees = 270;
                else
                    degrees = 0;
            }
        }
        else
            degrees=0;

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // Remove mirroring if front camera
        }
        else
            result = (info.orientation - degrees + 360) % 360;
        return result;
    }

    class imageSave extends AsyncTask<Bitmap, Void, ArrayList<String>>
    {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected ArrayList<String> doInBackground(Bitmap... params)
        {
            /**
             * Storing imageFile details in the Arraylist, as imageFile is global, so while the image
             * is being stored, if user clicks another image, the imageFile will be replaced. So arrayList
             * is created for each thread
             */
            ArrayList<String> imageDetails = new ArrayList<String>();
            imageDetails.add(imageFile.getAbsolutePath());
            imageDetails.add(imageFile.getName());
            Bitmap bitmapImage = params[0];
            try {
                FileOutputStream outStream = new FileOutputStream(imageFile);
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
                MediaStore.Images.Media.insertImage(getContentResolver(), imageDetails.get(0), imageDetails.get(1), imageDetails.get(1));

            }catch (IOException e)
            {
                e.printStackTrace();
            }

            return imageDetails;
        }
        @Override
        protected void onPostExecute(ArrayList<String> imageDetails) {

            //Storing the Image details in the local database after image has been stored
            GalleryObject galleryObject = new GalleryObject();
            realm.beginTransaction();
            galleryObject.setId(imageDetails.get(1));
            galleryObject.setPath(imageDetails.get(0));
            galleryObject.setImage(true);
            galleryObject.setLocal(true);
            galleryObject.setSynced(false);
            realm.copyToRealmOrUpdate(galleryObject);
            realm.commitTransaction();
        }
    }

    private void initRecorder(){
        /**
         * This method initializes the recorder
         */
        cameraPreviewFrame.setVisibility(View.GONE);
        photoCapture.setVisibility(View.GONE);
        videoCapture.setVisibility(View.GONE);
        swapCamera.setVisibility(View.GONE);
        gallery.setVisibility(View.GONE);
        cameraPreviewFrame.removeAllViews();
        videoView.setVisibility(View.VISIBLE);
        stopRecord.setVisibility(View.VISIBLE);
        videoHolder = videoView.getHolder();
        videoHolder.addCallback(this);

        recordCam = Camera.open(camIdx);
        Camera.Parameters parameters = recordCam.getParameters();
        Camera.Size videoSize = getVideoSize(parameters);   //Getting the video resolution
        parameters.setPreviewSize(videoSize.width,videoSize.height);    //Setting the preview dimensions
        setSurfaceLayout(videoSize.width,videoSize.height);
        recordCam.setDisplayOrientation(getRotationAngle(camIdx,2));    //Setting Preview orientation
        recordCam.setParameters(parameters);
        recordCam.unlock();

        //recordCam.stopPreview();
        recorder.setCamera(recordCam); //Setting camera to be used by the Media Recorder
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        File imageStorageDir = new File(Environment.getExternalStorageDirectory()+File.separator+"Camrena"+File.separator + "Videos");
        if (!imageStorageDir.exists()) {
            if (!imageStorageDir.mkdirs()) {
                Log.e(TAG, "Couldn't create Directory");
            }
        }
        CamcorderProfile cpHigh = CamcorderProfile.get(camIdx,CamcorderProfile.QUALITY_480P);
        /*recorder.setProfile(cpHigh);*/

        Log.e(TAG,""+videoSize.width+" "+videoSize.height);

        //Since i am explicitly setting a video resolution, I cannot set a profile, and thus have to individually set each property.
        recorder.setOutputFormat(cpHigh.fileFormat);
        recorder.setAudioEncoder(cpHigh.audioCodec);
        recorder.setAudioEncodingBitRate(cpHigh.audioBitRate);
        recorder.setAudioChannels(cpHigh.audioChannels);
        recorder.setAudioSamplingRate(cpHigh.audioSampleRate);
        recorder.setVideoFrameRate(cpHigh.videoFrameRate);
        recorder.setVideoEncodingBitRate(cpHigh.videoBitRate);
        recorder.setVideoEncoder(cpHigh.videoCodec);
        recorder.setVideoSize(videoSize.width,videoSize.height);
        //Log.e(TAG,"Profile Set");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Log.e(TAG,imageStorageDir.getPath() + File.separator + "VIDEO_" + timeStamp + ".mp4");
        videoPath = imageStorageDir.getPath() + File.separator + "VIDEO_" + timeStamp + ".mp4";
        videoName = "VIDEO_" + timeStamp + ".mp4";
        recorder.setOutputFile(videoPath);
        recorder.setOrientationHint(getRotationAngle(camIdx,1));    //Sets orientation for the video to be captured

        recorderPrep = true;

        if(!recorderPreped && surfaceCreated)
            prepareRecorder();


    }

    private void setSurfaceLayout(int imageWidth,int imageHeight)
    {
        /**
         * This method sets the SurfaceView layout based on the selected video size
         */
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        //Log.e(TAG,"Actual- "+metrics.widthPixels+" "+metrics.heightPixels);
        int layoutheight = (metrics.widthPixels*imageWidth)/imageHeight;
        //Log.e(TAG,"Screen- "+metrics.widthPixels+" "+layoutheight);

        videoView.setLayoutParams(new RelativeLayout.LayoutParams(metrics.widthPixels,layoutheight));
    }

    private void prepareRecorder() {
        //Log.e(TAG,"Recorder Prepared");
        try {
            recorder.prepare();
            recorderPreped=true;
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
        recording = true;
        blinkText();
        recorder.start();
    }

    private void blinkText()
    {
        /**
         * This method blicks the "REC" sign while recording video
         */
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int timeToBlink = 1000;    //in milissegunds
                try{Thread.sleep(timeToBlink);}catch (Exception e) {}
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(recordingText.getVisibility() == View.VISIBLE)
                            recordingText.setVisibility(View.INVISIBLE);
                        else
                            recordingText.setVisibility(View.VISIBLE);
                        if(recording)
                            blinkText();
                        else
                            recordingText.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Log.e(TAG,"Video Surface Created");
        surfaceCreated=true;
        recorder.setPreviewDisplay(videoHolder.getSurface());
        if(recorderPrep)
            prepareRecorder();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        //Log.e(TAG,"Video Surface Changed");
        surfaceWidth = width;
        surfaceHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Log.e(TAG,"Video Surface Destroyed");
        if (recording) {
            recorder.stop();
            recorder.release();
            recording = false;
        }
        recordCam.stopPreview();
        recordCam.release();
    }

    private Camera.Size getVideoSize(Camera.Parameters parameters)
    {
        /**
         * This method returns the appropriate video size among the supported camera sizes
         */
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        bestSize = sizeList.get(0);

        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }

        return bestSize;
    }

    private void startUploadService()
    {
        /**
         * This method starts the Video and Image upload services if
         * a. No service is running
         * b. There are Images or videos to be uploaded
         * c. There is internet connectivity
         */
        if(checkSyncStatus()==1 && isOnline())
        {
            if (realm.where(GalleryObject.class).equalTo("synced", false).equalTo("isimage", true).equalTo("local", true).findAll().size() > 0 && !isMyServiceRunning(UploadService.class))
                startService(new Intent(getBaseContext(), UploadService.class));
            if (realm.where(GalleryObject.class).equalTo("synced", false).equalTo("isimage", false).equalTo("local", true).findAll().size() > 0 && !isMyServiceRunning(VideoUploadService.class))
                startService(new Intent(getBaseContext(), VideoUploadService.class));
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        /**
         * This method checks if Upload services are already running;
         */
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isOnline()
    {
        /**
         * This method checks if there is internet connectivity
         */
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    public void onSensorChanged(SensorEvent event)
    {
        /**
         * This method sets the X Axis rotation value required for the device orientation. It is called
         * whenever the device angle changes
         */
        orientationValue =  event.values[0];
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        //Log.e(TAG,"Listened");
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(recording)   //Stops recording if minimized when recording
        {
            recorder.stop();
            recorder.release();
            recorder = null;
            File file = new File(videoPath);
            file.delete();
        }
        finish();
    }
}
