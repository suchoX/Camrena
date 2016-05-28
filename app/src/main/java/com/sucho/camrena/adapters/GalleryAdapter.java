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
    private RealmResults<GalleryObject> galleryList;
    private Context context;

    public GalleryAdapter(Context context, RealmResults<GalleryObject> galleryList)
    {
        this.context = context;
        this.galleryList = galleryList;
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
        if(galleryList.get(position).isImage())
            new showImage(holder.imageView,galleryList.get(position).isImage(),galleryList.get(position).getPath()).execute();
        else
            new showImage(holder.imageView,galleryList.get(position).isImage(),R.drawable.video_default).execute();
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
        return this.galleryList.size();
    }

    class showImage extends AsyncTask<Void, Void, Bitmap>
    {
        ImageView imageView;
        boolean isImage;
        int imgId;
        String path;
        showImage(ImageView imageView,boolean isImage,String path)
        {
            this.imageView = imageView;
            this.isImage = isImage;
            this.path = path;
        }
        showImage(ImageView imageView,boolean isImage,int imgId)
        {
            this.imageView = imageView;
            this.isImage = isImage;
            this.imgId = imgId;
        }
        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap imageBitmap;
            if(isImage)
                imageBitmap = decodeAndScale(path);
            else
                imageBitmap = decodeAndScale(imgId);
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
        private Bitmap decodeAndScale(int imgId) {

            int reqWidth,reqHeight;
            reqWidth = getScreenWidth((Activity)context)/4;

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(context.getResources(),imgId,options);
            reqHeight = getImageHeight(options,reqWidth);
            Log.e("GalleryAdapter",""+options.outWidth+" "+options.outHeight);
            Log.e("GalleryAdapter",""+reqWidth+" "+reqHeight);

            options.inSampleSize = getSampleSize(options, reqWidth, reqHeight);

            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(context.getResources(),imgId,options);

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
        float dpHeight = outMetrics.heightPixels / density;

        return (int) dpHeight;
    }

    private int getImageHeight(BitmapFactory.Options options,int width)
    {
        int height;
        float imageRatio = (float)options.outHeight/(float)options.outWidth;
        height =(int)(imageRatio*width);

        return height;
    }
}
