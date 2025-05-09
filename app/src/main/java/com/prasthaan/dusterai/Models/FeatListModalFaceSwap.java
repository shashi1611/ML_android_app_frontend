package com.prasthaan.dusterai.Models;

public class FeatListModalFaceSwap {
    int img;
    String feat_name;

    public FeatListModalFaceSwap(int img, String feat_name) {
        this.img = img;
        this.feat_name = feat_name;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public String getFeat_name() {
        return feat_name;
    }

    public void setFeat_name(String feat_name) {
        this.feat_name = feat_name;
    }
}
