package com.sucho.camrena;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyPingCallback;
import com.sucho.camrena.adapters.GalleryAdapter;
import com.sucho.camrena.others.Constants;
import com.sucho.camrena.realm.GalleryObject;
import com.sucho.camrena.service.UploadService;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

public class GalleryActivity extends AppCompatActivity {
    private static final String TAG = "GalleryActivity";

    Toolbar mToolbar;

    Realm realm;
    RealmConfiguration realmConfig;
    RealmResults<GalleryObject> galleryList;

    RecyclerView galleryRecyclerView;
    private StaggeredGridLayoutManager gridLayoutManager;
    GalleryAdapter galleryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        realmConfig = new RealmConfiguration.Builder(this).build();
        realm = Realm.getInstance(realmConfig);
        initToolbar();

        galleryList = realm.where(GalleryObject.class).findAll();

        //Log.e(TAG,""+realm.where(GalleryObject.class).equalTo("isimage",false).findAll().size());

        galleryRecyclerView = (RecyclerView)findViewById(R.id.gallery_recyclerview);
        galleryRecyclerView.setHasFixedSize(true);

        gridLayoutManager = new StaggeredGridLayoutManager(4,StaggeredGridLayoutManager.VERTICAL);
        galleryRecyclerView.setLayoutManager(gridLayoutManager);

        galleryAdapter = new GalleryAdapter(this,galleryList);
        galleryRecyclerView.setAdapter(galleryAdapter);

    }



    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Gallery");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(GalleryActivity.this,PhotoActivity.class));
        finish();
    }

}
