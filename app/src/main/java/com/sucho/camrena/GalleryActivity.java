package com.sucho.camrena;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.sucho.camrena.adapters.GalleryAdapter;
import com.sucho.camrena.realm.GalleryObject;


import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class GalleryActivity extends AppCompatActivity {
    private static final String TAG = "GalleryActivity";

    Toolbar mToolbar;
    Switch toolbarSwitch;

    Realm realm;
    RealmConfiguration realmConfig;
    RealmResults<GalleryObject> galleryList;

    RecyclerView galleryRecyclerView;
    private GridLayoutManager gridLayoutManager;
    GalleryAdapter galleryAdapter;

    SharedPreferences syncPreference;
    SharedPreferences.Editor editor;

    int camId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        realmConfig = new RealmConfiguration.Builder(this).build();
        realm = Realm.getInstance(realmConfig);

        camId = getIntent().getIntExtra("Camera",99);

        syncPreference = this.getSharedPreferences("EventData", 0); //This SharedPreference stores the user's option whether to AutoSync

        initToolbar();
        setSyncSwitch();

        galleryList = realm.where(GalleryObject.class).findAll();   //Get all Images and Videos

        //Log.e(TAG,""+realm.where(GalleryObject.class).equalTo("isimage",false).findAll().size());

        galleryRecyclerView = (RecyclerView)findViewById(R.id.gallery_recyclerview);
        galleryRecyclerView.setHasFixedSize(true);

        gridLayoutManager = new GridLayoutManager(this,4);
        galleryRecyclerView.setLayoutManager(gridLayoutManager);

        galleryAdapter = new GalleryAdapter(this,galleryList);
        galleryRecyclerView.setAdapter(galleryAdapter);

    }

    private void setSyncSwitch()
    {
        if(syncPreference.getInt("Sync Status",2)==1)
            toolbarSwitch.setChecked(true);
        else
            toolbarSwitch.setChecked(false);
    }



    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarSwitch = (Switch) findViewById(R.id.toolbar_switch);
        toolbarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {    //Changing Auto Sync option based on switch
                if(isChecked)
                {
                    editor = syncPreference.edit();
                    editor.putInt("Sync Status",1);
                    editor.apply();
                }
                else
                {
                    editor = syncPreference.edit();
                    editor.putInt("Sync Status",0);
                    editor.apply();
                }
            }
        });
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(GalleryActivity.this,PhotoActivity.class);   //Going back to PhotoActivity
        intent.putExtra("Camera",camId);
        startActivity(intent);
        finish();
    }

}
