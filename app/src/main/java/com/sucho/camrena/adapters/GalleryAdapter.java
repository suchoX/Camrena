package com.sucho.camrena.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sucho.camrena.R;
import com.sucho.camrena.others.GalleryObjectHolder;
import com.sucho.camrena.realm.GalleryObject;

import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by ASUS on 28-May-16.
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryObjectHolder>
{
    private RealmResults<GalleryObject> imageList;
    private Context context;

    public GalleryAdapter(Context context, RealmResults<GalleryObject> imageList)
    {
        this.context = context;
        this.imageList = imageList;
    }
    @Override
    public GalleryObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_single_image, null);
        GalleryObjectHolder rcv = new GalleryObjectHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(GalleryObjectHolder holder, int position)
    {
        new showImage(holder.imageView).execute(imageList.get(position).getPath());
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return this.imageList.size();
    }

    class showImage extends AsyncTask<String, Void, Bitmap>
    {
        ImageView imageView;
        showImage(ImageView imageView)
        {
            this.imageView = imageView;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(params[0]);
            return imageBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            imageView.setImageBitmap(bitmap);
            super.onPostExecute(bitmap);
        }
    }
}
