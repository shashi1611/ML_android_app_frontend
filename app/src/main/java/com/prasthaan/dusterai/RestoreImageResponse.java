package com.prasthaan.dusterai;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RestoreImageResponse {
    @SerializedName("restored_faces")
    private List<String> restoredFaces;

    @SerializedName("restored_image")
    private String restoredImage;

    public List<String> getRestoredFaces() {
        return restoredFaces;
    }

    public String getRestoredImage() {
        return restoredImage;
    }
}
