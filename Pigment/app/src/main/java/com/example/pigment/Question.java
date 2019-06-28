package com.example.pigment;

import android.net.Uri;

public class Question {
    public Uri imageUri;
    public String normal;
    public String protanopia;
    public String deuteranopia;

    Question(Uri imageUri, String n, String p, String d)
    {
        this.imageUri = imageUri;
        normal = n;
        protanopia = p;
        deuteranopia = d;
    }
}
