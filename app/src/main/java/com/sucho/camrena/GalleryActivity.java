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

import com.sucho.camrena.adapters.GalleryAdapter;
import com.sucho.camrena.realm.GalleryObject;

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
    RealmResults<GalleryObject> imageList;

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

        imageList = realm.where(GalleryObject.class).findAll();

        galleryRecyclerView = (RecyclerView)findViewById(R.id.gallery_recyclerview);
        //galleryRecyclerView.setHasFixedSize(true);

        gridLayoutManager = new StaggeredGridLayoutManager(4, 1);
        galleryRecyclerView.setLayoutManager(gridLayoutManager);

        galleryAdapter = new GalleryAdapter(this,imageList);
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
