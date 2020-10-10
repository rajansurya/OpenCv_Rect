package com.sqs.carddetect;

import android.graphics.Bitmap;

public class Card_Data {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    private Bitmap image;
}
