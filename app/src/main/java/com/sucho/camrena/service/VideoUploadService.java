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
import com.sucho.camrena.others.Constants;
import com.sucho.camrena.realm.GalleryObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class VideoUploadService extends Service {
    private static final String TAG = "VideoUploadService";

    Realm realm;
    RealmConfiguration realmConfig;
    RealmResults<GalleryObject> unSyncedList;
    Client mKinveyClient;

    ArrayList<String> uploadedId;
    int tobeUploaded,count=0;

    public VideoUploadService()
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

        uploadedId = new ArrayList<String>();

        if (mKinveyClient.user().isUserLoggedIn())
            upload();
        else {
            mKinveyClient.user().login(new KinveyUserCallback() {
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

        // Let it continue running until it is stopped.
        return START_STICKY;
    }

    private void upload()
    {
        try {
            unSyncedList = realm.where(GalleryObject.class).equalTo("synced", false).equalTo("isimage",false).findAll();
            tobeUploaded = unSyncedList.size();
            Log.e(TAG, "" + tobeUploaded);
            if (tobeUploaded == 0)
                stopSelf();
            for (int i = 0; i < tobeUploaded; i++) {
                FileInputStream fIn = new FileInputStream(new File(unSyncedList.get(i).getPath()));
                mKinveyClient.file().upload(unSyncedList.get(i).getId(), fIn, new UploaderProgressListener() {

                    @Override
                    public void onSuccess(FileMetaData fileMetaData) {
                        Log.e(TAG, "Uploaded:" + fileMetaData.getFileName());
                        uploadedId.add(fileMetaData.getId());
                        if (count == tobeUploaded - 1) {
                            updateRealm();
                            Log.e(TAG, "Count " + count);
                        } else
                            count++;
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        Log.e(TAG, "failed to upload file.", error);
                        updateRealm();
                    }

                    @Override
                    public void progressChanged(MediaHttpUploader uploader) throws IOException {
                        Log.i(TAG, "upload progress: " + uploader.getUploadState());
                        // all updates to UI widgets need to be done on the UI thread
                    }
                });
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            updateRealm();
            stopSelf();
        }
    }

    private void updateRealm()
    {
        try {

            GalleryObject galleryObject;
            for (int i = 0; i < uploadedId.size(); i++) {
                realm.beginTransaction();
                galleryObject = realm.where(GalleryObject.class).equalTo("id", uploadedId.get(i)).findFirst();
                galleryObject.setSynced(true);
                realm.commitTransaction();
            }
            stopSelf();
        } catch (Exception e)
        {
            e.printStackTrace();
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"Stopped");
        super.onDestroy();
    }
}

