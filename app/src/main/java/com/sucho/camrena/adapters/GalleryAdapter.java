package com.sucho.camrena.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
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
            Bitmap imageBitmap = decodeAndScale(params[0]);
            return imageBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            imageView.setImageBitmap(bitmap);
            super.onPostExecute(bitmap);
        }

        private Bitmap decodeAndScale(String path) {

            int reqWidth,reqHeight;
            reqWidth = getScreenWidth((Activity)context)/4;

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            reqHeight = getImageHeight(options,reqWidth);

            options.inSampleSize = getSampleSize(options, reqWidth, reqHeight);

            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path,options);

        }

        private int getSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
        {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }
    }

    public int getScreenWidth(Activity a) {

        Display display = a.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = a.getResources().getDisplayMetrics().density;
        float dpWidth = outMetrics.widthPixels / density;

        return (int) dpWidth;
    }

    private int getImageHeight(BitmapFactory.Options options,int width)
    {
        int height;
        float imageRatio = (float)options.outHeight/(float)options.outWidth;
        height =(int)(imageRatio*width);

        return height;
    }
}
