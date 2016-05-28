package com.sucho.camrena.others;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.sucho.camrena.R;

/**
 * Created by ASUS on 28-May-16.
 */
public class GalleryObjectHolder extends RecyclerView.ViewHolder  implements View.OnClickListener
{
    public ImageView imageView;
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

    ImageView syncedImage;

    public GalleryObjectHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        imageView = (ImageView)itemView.findViewById(R.id.image);
    }

    @Override
    public void onClick(View view)
    {
        tempBuilder=new AlertDialog.Builder(view.getContext());
        popupDialog=tempBuilder.create();
        factory = (LayoutInflater) view.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        if(isimage) {
            imageViewDialog = factory.inflate(R.layout.dialog_image, null);

            dialogImage = (ImageView) imageViewDialog.findViewById(R.id.dialog_image);
            Bitmap bmp = BitmapFactory.decodeFile(path);
            dialogImage.setImageBitmap(bmp);
            popupDialog.setView(imageViewDialog);
            syncedImage = (ImageView)imageViewDialog.findViewById(R.id.synced);
            if(synced) {
                syncedImage.setVisibility(View.VISIBLE);
                Log.e("HOLDER","Synced");
            }
            else
                syncedImage.setVisibility(View.GONE);

        }
        else
        {
            videoViewDialog = factory.inflate(R.layout.dialog_video,null);
            dialogVideo = (VideoView)videoViewDialog.findViewById(R.id.dialog_video);
            popupDialog.setView(videoViewDialog);
            dialogVideo.setVideoPath(path);

            MediaController controller = new MediaController(view.getContext());
            controller.setAnchorView(this.dialogVideo);
            controller.setMediaPlayer(this.dialogVideo);
            dialogVideo.setMediaController(controller);
            dialogVideo.start();
        }
        popupDialog.setCancelable(true);
        popupDialog.show();

        //Toast.makeText(view.getContext(), "Clicked Position = " + teamName.getText().toString(), Toast.LENGTH_SHORT).show();
    }
}
