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

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class UploadService extends Service {

    private static final String TAG = "UploadService";

    Realm realm;
    RealmConfiguration realmConfig;
    RealmResults<GalleryObject> unSyncedList;
    Client mKinveyClient;

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
        mKinveyClient = new Client.Builder(Constants.appId, Constants.appSecret, this.getApplicationContext()).build();

        mKinveyClient.user().login(new KinveyUserCallback() {
            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "Login Failure", error);
                stopSelf();
            }
            @Override
            public void onSuccess(User result) {
                Log.i(TAG,"Logged in a new implicit user with id: " + result.getId());
                upload();
            }
        });

        // Let it continue running until it is stopped.
        return START_STICKY;
    }

    private void upload()
    {
        unSyncedList = realm.where(GalleryObject.class).equalTo("synced",false).findAll();
        for(int i=0 ; i<1 ; i++)
        {
            FileMetaData myFileMetaData = new FileMetaData("myFileID");  //create the FileMetaData object
            myFileMetaData.setPublic(true);  //set the file to be pubicly accesible
            java.io.File file = new java.io.File(unSyncedList.get(i).getPath());
            myFileMetaData.setFileName(file.getName());
            mKinveyClient.file().upload(myFileMetaData, file, new UploaderProgressListener() {

                @Override
                public void onSuccess(FileMetaData fileMetaData) {
                    Log.e(TAG,fileMetaData.getFileName());
                    stopSelf();
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
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"Stopped");
        super.onDestroy();
    }
}
