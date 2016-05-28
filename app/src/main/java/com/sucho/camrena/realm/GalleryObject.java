package com.sucho.camrena.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ASUS on 28-May-16.
 */
public class GalleryObject extends RealmObject
{
    @PrimaryKey
    int id;

    String path;
    boolean isimage;
    boolean synced;
    boolean local;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isImage() {
        return isimage;
    }

    public void setImage(boolean isimage) {
        this.isimage = isimage;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

}
