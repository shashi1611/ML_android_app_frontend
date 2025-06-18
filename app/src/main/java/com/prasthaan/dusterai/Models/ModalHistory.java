package com.prasthaan.dusterai.Models;

public class ModalHistory {
    private String imagePath;
    private String fileName;
    private String dateTime;

    public ModalHistory(String imagePath, String fileName, String dateTime) {
        this.imagePath = imagePath;
        this.fileName = fileName;
        this.dateTime = dateTime;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
