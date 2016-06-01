package com.sucho.camrena.customview;

import android.content.Context;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.sucho.camrena.PhotoActivity;

import java.io.IOException;
import java.util.List;

/**
 * Created by ASUS on 26-May-16.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
    private static final String TAG = "Camera Preview";
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    Context context;
    int width,height;
    List<Camera.Size> sizeList;
    
    public CameraPreview(Context context, Camera camera) 
    {
        super(context);
        this.context = context;
        this.camera = camera;
        this.surfaceHolder = this.getHolder();
        this.surfaceHolder.addCallback(this);
    }

    public void switchCamera(Camera camera)
    {
        /**
         * This method switches the camera when swapcamera button is clicked
         */
        this.camera = camera;
        try {
        camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        orientationChange(width,height);
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) 
    {
        //Log.e("Camera Preview","Camera Surface Created");
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) 
    {
        //Log.e("Camera Preview","Camera Surface Destroyed");
        camera.stopPreview();
        camera.release();
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) 
    {
        //Log.e(TAG,"Camera Surface Changed");
        camera.stopPreview();
        this.width = width;
        this.height = height;
        orientationChange(width,height);
        //Log.e(TAG,"Surface- "+width+" "+height);
    }

    private void orientationChange(int width,int height)
    {
        /**
         * This Method Sets the preview and Image orienatation
         */
        Camera.Parameters parameters = camera.getParameters();

        WindowManager wm = (WindowManager)(context).getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Camera.Size imageSize = getImageSize(width, height, parameters);


        if(display.getRotation() == Surface.ROTATION_0)
            camera.setDisplayOrientation(90);

        if(display.getRotation() == Surface.ROTATION_270)
            camera.setDisplayOrientation(0);
        //Log.e(TAG,"Image- "+imageSize.width+" "+imageSize.height);
        parameters.setPictureSize(imageSize.width, imageSize.height);
        parameters.setPreviewSize(imageSize.width, imageSize.height);

        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setFrameLayout(imageSize.width,imageSize.height);
    }

    private Camera.Size getImageSize(int width, int height, Camera.Parameters parameters)
    {
        /**
         * This Method selects and returns the Image size to be captured in among the supported
         * image sizes
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


    private void setFrameLayout(int imageWidth,int imageHeight)
    {
        /**
         * This Method calls the PhotoActivity method, which sets the FrameLayout Size to the
         * size of the image to be captured
         */
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        //Log.e(TAG,"Actual- "+metrics.widthPixels+" "+metrics.heightPixels);
        int layoutheight = (metrics.widthPixels*imageWidth)/imageHeight;
        //Log.e(TAG,"Screen- "+metrics.widthPixels+" "+layoutheight);
        ((PhotoActivity)context).setFrameLayout(metrics.widthPixels,layoutheight);
    }
}
