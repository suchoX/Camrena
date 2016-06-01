package com.sucho.camrena.others;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.FileMetaData;
import com.squareup.picasso.Picasso;
import com.sucho.camrena.R;

import java.io.File;

/**
 * Created by ASUS on 28-May-16.
 */
public class GalleryObjectHolder extends RecyclerView.ViewHolder  implements View.OnClickListener
{
    public ImageView imageView;
    public ImageView syncedgalleryImage;
    public ImageView playOverlayImage;
    public String id;
    public String path;
    public boolean isimage;
    public boolean synced;
    public boolean local;

    AlertDialog popupDialog;
    AlertDialog.Builder tempBuilder;
    LayoutInflater factory;
    View imageViewDialog;
    View videoViewDialog;
    ImageView dialogImage;
    VideoView dialogVideo;
    boolean canPlay = false;

    ImageView syncedImage,storageImage;

    Client mKinveyClient;

    public GalleryObjectHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        imageView = (ImageView)itemView.findViewById(R.id.image);
        syncedgalleryImage = (ImageView)itemView.findViewById(R.id.synced_gallery);
        playOverlayImage = (ImageView)itemView.findViewById(R.id.play_overlay);
    }

    @Override
    public void onClick(final View view)
    {
        tempBuilder=new AlertDialog.Builder(view.getContext());
        popupDialog=tempBuilder.create();
        factory = (LayoutInflater) view.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        if(isimage)
        {
            imageViewDialog = factory.inflate(R.layout.dialog_image, null);

            dialogImage = (ImageView) imageViewDialog.findViewById(R.id.dialog_image);
            syncedImage = (ImageView)imageViewDialog.findViewById(R.id.synced);
            storageImage = (ImageView)imageViewDialog.findViewById(R.id.storage_location);

            File file = new File(path);
            if(file.exists())
            {
                Bitmap bmp = BitmapFactory.decodeFile(path);
                dialogImage.setImageBitmap(bmp);
                popupDialog.setView(imageViewDialog);
            }
            else {
                loginCheck(id,dialogImage,view);
                storageImage.setImageDrawable(ContextCompat.getDrawable(view.getContext(),R.drawable.cloud));
                storageImage.setVisibility(View.GONE);
                popupDialog.setView(imageViewDialog);
            }

            if(synced)
                syncedImage.setVisibility(View.VISIBLE);
            else
                syncedImage.setVisibility(View.GONE);

        }
        else
        {
            videoViewDialog = factory.inflate(R.layout.dialog_video,null);
            syncedImage = (ImageView)videoViewDialog.findViewById(R.id.synced);
            storageImage = (ImageView)videoViewDialog.findViewById(R.id.storage_location);
            if(synced)
                syncedImage.setVisibility(View.VISIBLE);
            else
                syncedImage.setVisibility(View.GONE);

            dialogVideo = (VideoView)videoViewDialog.findViewById(R.id.dialog_video);
            popupDialog.setView(videoViewDialog);

            File file = new File(path);
            if(file.exists()) {
                canPlay = true;
                dialogVideo.setVideoPath(path);

                MediaController controller = new MediaController(view.getContext());
                controller.setAnchorView(this.dialogVideo);
                controller.setMediaPlayer(this.dialogVideo);
                dialogVideo.setMediaController(controller);
                dialogVideo.start();
            }
            else
            {
                storageImage.setImageDrawable(ContextCompat.getDrawable(view.getContext(),R.drawable.cloud));
                storageImage.setVisibility(View.GONE);
                mKinveyClient = new Client.Builder(Constants.appId, Constants.appSecret, view.getContext().getApplicationContext()).build();
                if (mKinveyClient.user().isUserLoggedIn())
                    playVideoStream(id,dialogVideo);
                else {
                    mKinveyClient.user().login(new KinveyUserCallback() {
                        @Override
                        public void onFailure(Throwable error) {
                            Toast.makeText(view.getContext(),"Cannot Play Video",Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onSuccess(User result) {
                            Log.i("Holder", "Logged in a new implicit user with id: " + result.getId());
                            playVideoStream(id,dialogVideo);
                        }
                    });
                }
            }
        }
        popupDialog.setCancelable(true);
        popupDialog.show();

        //Toast.makeText(view.getContext(), "Clicked Position = " + teamName.getText().toString(), Toast.LENGTH_SHORT).show();
    }

    private void loginCheck(final String imgId, final ImageView imageView, final View view)
    {
        mKinveyClient = new Client.Builder(Constants.appId, Constants.appSecret, view.getContext().getApplicationContext()).build();
        if (mKinveyClient.user().isUserLoggedIn())
            downloadDeletedImage(imgId,imageView,view);
        else {
            mKinveyClient.user().login(new KinveyUserCallback() {
                @Override
                public void onFailure(Throwable error) {
                }

                @Override
                public void onSuccess(User result) {
                    downloadDeletedImage(imgId,imageView,view);
                }
            });
        }
    }

    private void downloadDeletedImage(String imgId, final ImageView imageView,final View view)
    {
        mKinveyClient.file().downloadMetaData(imgId, new KinveyClientCallback<FileMetaData>() {
            @Override
            public void onSuccess(FileMetaData fileMetaData) {
                storageImage.setVisibility(View.VISIBLE);
                Picasso.with(view.getContext()).load(fileMetaData.getDownloadURL()).fit().centerCrop().placeholder(R.drawable.image_default).error(R.drawable.image_error).into(imageView);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
                Picasso.with(view.getContext()).load(R.drawable.image_error).fit().centerCrop().placeholder(R.drawable.image_default).into(imageView);
            }
        });
    }

    private void playVideoStream(String id,final VideoView videoView)
    {
        mKinveyClient.file().downloadMetaData(id, new KinveyClientCallback<FileMetaData>() {
            @Override
            public void onSuccess(FileMetaData fileMetaData) {
                Log.e("Holder",fileMetaData.getDownloadURL());
                storageImage.setVisibility(View.VISIBLE);
                videoView.setVideoURI(Uri.parse(fileMetaData.getDownloadURL()));
                videoView.start();
            }

            @Override
            public void onFailure(Throwable throwable) {
                videoView.stopPlayback();
                Toast.makeText(videoView.getContext(),"Video deleted from Local Storage. Either no Internet/Video not synced",Toast.LENGTH_LONG).show();
                popupDialog.dismiss();
            }
        });
    }
}
