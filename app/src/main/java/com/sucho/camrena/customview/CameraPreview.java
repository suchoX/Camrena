package com.sucho.camrena.customview;

import android.content.Context;
import android.hardware.Camera;
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
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    Context context;
    int width,height;
    
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
        Log.e("Camera Preview","Camera Surface Created");
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
        Log.e("Camera Preview","Camera Surface Destroyed");
        camera.stopPreview();
        camera.release();
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) 
    {
        Log.e("Camera Preview","Camera Surface Changed");
        camera.stopPreview();
        this.width = width;
        this.height = height;
        orientationChange(width,height);
    }

    private void orientationChange(int width,int height)
    {
        Camera.Parameters parameters = camera.getParameters();
        WindowManager wm = (WindowManager)(context).getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Camera.Size imageSize = getImageSize(width, height, parameters);

        if(display.getRotation() == Surface.ROTATION_0)
        {
            parameters.setPictureSize(imageSize.width, imageSize.height);
            camera.setDisplayOrientation(90);
        }

        if(display.getRotation() == Surface.ROTATION_90)
        {
            parameters.setPictureSize(imageSize.width, imageSize.height);
        }

        if(display.getRotation() == Surface.ROTATION_180)
        {
            parameters.setPictureSize(imageSize.width, imageSize.height);
        }

        if(display.getRotation() == Surface.ROTATION_270)
        {
            parameters.setPictureSize(imageSize.width, imageSize.height);
            camera.setDisplayOrientation(180);
        }

        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Camera.Size getImageSize(int width, int height, Camera.Parameters parameters){
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        bestSize = sizeList.get(0);

        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }

        return bestSize;
    }
}
