package com.prasthaan.dusterai;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PencilSketchGenerationResponse {
    @SerializedName("output_images")
    private List<String> pencilSketchResult;

    @SerializedName("output_video_url")
    private String output_video_url;

    public List<String> getResultsImage() {
        return pencilSketchResult;
    }

    public String getResultsVideo() {
        return output_video_url;
    }
}
