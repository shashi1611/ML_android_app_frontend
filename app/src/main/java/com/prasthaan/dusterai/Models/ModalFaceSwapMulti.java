package com.prasthaan.dusterai.Models;

import android.net.Uri;

public class ModalFaceSwapMulti {

    private int defaultImageResId;
    private Uri image;
    private Uri selectedImageUri;

    public ModalFaceSwapMulti(int defaultImageResId) {
        this.defaultImageResId = defaultImageResId;
    }

    public ModalFaceSwapMulti(Uri image, int defaultImageResId) {
        this.image = image;
        this.defaultImageResId = defaultImageResId;
    }

    public int getDefaultImageResId() {
        return defaultImageResId;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }

    public Uri getSelectedImageUri() {
        return selectedImageUri;
    }

    public void setSelectedImageUri(Uri selectedImageUri) {
        this.selectedImageUri = selectedImageUri;
    }
}
