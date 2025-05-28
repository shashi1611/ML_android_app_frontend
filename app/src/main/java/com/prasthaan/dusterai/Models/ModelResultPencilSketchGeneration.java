package com.prasthaan.dusterai.Models;

public class ModelResultPencilSketchGeneration {
    String resultImg;
    String resultText;

    public ModelResultPencilSketchGeneration(String resultImg, String resultText) {
        this.resultImg = resultImg;
        this.resultText = resultText;
    }

    public String getResultImg() {
        return resultImg;
    }

    public void setResultImg(String resultImg) {
        this.resultImg = resultImg;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }
}
