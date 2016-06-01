package com.sucho.camrena.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.model.KinveyMetaData;
import com.sucho.camrena.others.Constants;
import com.sucho.camrena.realm.GalleryObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class UploadService extends Service {
    /**
     * This service Uploads Images one at a time. To keep number of simultaneous uploads
     * to one, which gives better performance (Too many simultaneous upload Hangs device),
     * The service is started again if there are more more Images to be uploaded, else stopped.
     */

    private static final String TAG = "UploadService";

    Realm realm;
    RealmConfiguration realmConfig;
    GalleryObject toUploadObject;
    Client mKinveyClient;

    int tobeUploaded;

    public UploadService()
    {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        realmConfig = new RealmConfiguration.Builder(this).build();
        realm = Realm.getInstance(realmConfig);

        tobeUploaded = realm.where(GalleryObject.class).equalTo("synced", false).equalTo("isimage",true).equalTo("local",true).findAll().size();
        Log.e(TAG, "" + tobeUploaded);
        if (tobeUploaded == 0)
            stopSelf();

        mKinveyClient = new Client.Builder(Constants.appId, Constants.appSecret, this.getApplicationContext()).build();

        if (mKinveyClient.user().isUserLoggedIn())  //Checks if Device is logged in Kinvey
            upload();
        else {
            mKinveyClient.user().login(new KinveyUserCallback() //Login User if user not Logged In
            {
                @Override
                public void onFailure(Throwable error) {
                    Log.e(TAG, "Login Failure", error);
                    stopSelf();
                }

                @Override
                public void onSuccess(User result) {
                    Log.i(TAG, "Logged in a new implicit user with id: " + result.getId());
                    upload();
                }
            });
        }

        return START_NOT_STICKY;
    }

    private void upload()
    {
        try
        {
            //Get the data for the next image to be uploaded
            toUploadObject = realm.where(GalleryObject.class).equalTo("synced", false).equalTo("isimage",true).equalTo("local",true).findFirst();
            FileMetaData myFileMetaData = new FileMetaData(toUploadObject.getId());  //create the FileMetaData object
            myFileMetaData.setPublic(true);  //set the file to be pubicly accesible
            myFileMetaData.setFileName(toUploadObject.getId());
            java.io.File file = new java.io.File(toUploadObject.getPath());
            if(!file.exists())
            {
                Log.e(TAG,toUploadObject.getId()+" deleted(Not Synced)");   //File is deleted, and thus cannot be synced
                updateRealmAvoid();
            }
            //Uploading the Image as file
            mKinveyClient.file().upload(myFileMetaData, file, new UploaderProgressListener() {

                @Override
                public void onSuccess(FileMetaData fileMetaData) {
                    Log.e(TAG, "Uploaded:" + fileMetaData.getFileName());
                    updateRealmUpload();
                }

                @Override
                public void onFailure(Throwable error) {
                    Log.e(TAG, "failed to upload file.", error);
                    stopSelf();
                }

                @Override
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    Log.i(TAG, "upload progress: " + uploader.getUploadState());
                    // all updates to UI widgets need to be done on the UI thread
                }
            });
        } catch (Exception e)
        {
            e.printStackTrace();
            stopSelf();
        }
    }

    private void updateRealmUpload()
    {
        /**
         * This marks the Image uploaded data as synced
         */
        realm.beginTransaction();
        toUploadObject.setSynced(true);
        realm.commitTransaction();
        next();
    }

    private void updateRealmAvoid()
    {
        /**
         * This marks the File which has been deleted without syncing as not Local,
         * So that next time, attempts are not made to upload it.
         */
        realm.beginTransaction();
        toUploadObject.setLocal(false);
        realm.commitTransaction();
        next();
    }

    private void next()
    {
        /**
         * If more images has to be uploaded, resstart service, else stop
         */
        if (tobeUploaded == 1)
            stopSelf();
        else
        {
            stopSelf();
            startService(new Intent(getBaseContext(), UploadService.class));
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"Stopped");
        super.onDestroy();
    }
}
