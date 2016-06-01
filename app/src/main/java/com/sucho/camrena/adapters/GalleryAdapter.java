package com.sucho.camrena.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.FileMetaData;
import com.squareup.picasso.Picasso;
import com.sucho.camrena.R;
import com.sucho.camrena.others.Constants;
import com.sucho.camrena.others.GalleryObjectHolder;
import com.sucho.camrena.realm.GalleryObject;

import java.io.File;

import io.realm.RealmResults;

/**
 * Created by ASUS on 28-May-16.
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryObjectHolder>
{
    /**
     * This gallery shows a resized image in the Gridview. There is no use loading a 2000x2000 10MB image
     * in memory if the image in the gris is 100x100. Without resizing the app will encounter
     * OUT OF MEMORY Error.
     *
     * Also the image Loading in Imageview is done by an AsyncTask to prevent UI Stuttering
     */
    private static final String TAG = "GalleryAdapter";
    private RealmResults<GalleryObject> galleryList;
    private Context context;

    Client mKinveyClient;

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
        holder.id = galleryList.get(position).getId();
        holder.path = galleryList.get(position).getPath();
        holder.isimage = galleryList.get(position).isImage();
        holder.local = galleryList.get(position).isLocal();
        holder.synced = galleryList.get(position).isSynced();
        if(galleryList.get(position).isSynced())
            holder.syncedgalleryImage.setVisibility(View.VISIBLE);
        if(!galleryList.get(position).isImage())
            holder.playOverlayImage.setVisibility(View.VISIBLE);
        if(galleryList.get(position).isImage())
            new showImage(holder.imageView,galleryList.get(position).isImage(),galleryList.get(position).getPath(),galleryList.get(position).getId()).execute();
        else
            new showImage(holder.imageView,galleryList.get(position).isImage(),galleryList.get(position).getPath()).execute();
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
        String path;
        String imgId;
        showImage(ImageView imageView,boolean isImage,String path,String imgId)
        {
            this.imageView = imageView;
            this.isImage = isImage;
            this.path = path;
            this.imgId = imgId;
        }
        showImage(ImageView imageView,boolean isImage,String path)
        {
            this.imageView = imageView;
            this.isImage = isImage;
            this.path = path;
        }
        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap imageBitmap;
            if(isImage) {
                File file = new File(path);
                if(file.exists())
                    imageBitmap = decodeAndScale(path);
                else {
                    imageBitmap = decodeAndScale(R.drawable.image_default);
                    loginCheck(imgId,imageView);
                }

            }
            else {
                return ThumbnailUtils.createVideoThumbnail(path,   MediaStore.Images.Thumbnails.MINI_KIND);
            }
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
        private Bitmap decodeAndScale(int imgResId)
        {
            /**
             * This method returns a resized image
             */
            int reqWidth,reqHeight;
            reqWidth = getScreenWidth((Activity)context)/4;

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(context.getResources(),imgResId,options);
            reqHeight = getImageHeight(options,reqWidth);

            options.inSampleSize = getSampleSize(options, reqWidth, reqHeight);

            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(context.getResources(),imgResId,options);

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

    private void loginCheck(final String imgId, final ImageView imageView)
    {
        mKinveyClient = new Client.Builder(Constants.appId, Constants.appSecret, context.getApplicationContext()).build();
        if (mKinveyClient.user().isUserLoggedIn())
            downloadDeletedImage(imgId,imageView);
        else {
            mKinveyClient.user().login(new KinveyUserCallback() {
                @Override
                public void onFailure(Throwable error) {
                }

                @Override
                public void onSuccess(User result) {
                    downloadDeletedImage(imgId,imageView);
                }
            });
        }
    }

    private void downloadDeletedImage(String imgId, final ImageView imageView)
    {
       mKinveyClient.file().downloadMetaData(imgId, new KinveyClientCallback<FileMetaData>() {
           @Override
           public void onSuccess(FileMetaData fileMetaData) {
               Picasso.with(context).load(fileMetaData.getDownloadURL()).fit().centerCrop().placeholder(R.drawable.image_default).into(imageView);
           }

           @Override
           public void onFailure(Throwable throwable) {
               Picasso.with(context).load(R.drawable.image_error).fit().centerCrop().placeholder(R.drawable.image_default).into(imageView);
           }
       });
    }
}
