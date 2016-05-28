package com.sucho.camrena.others;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.sucho.camrena.R;

/**
 * Created by ASUS on 28-May-16.
 */
public class GalleryObjectHolder extends RecyclerView.ViewHolder
{
    public ImageView imageView;
    String imagePath;
    boolean isimage;
    boolean synced;
    boolean local;

    public GalleryObjectHolder(View itemView) {
        super(itemView);
        imageView = (ImageView)itemView.findViewById(R.id.image);
    }
}
