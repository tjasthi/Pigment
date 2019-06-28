package com.example.pigment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class CustomizeImageActivity extends AppCompatActivity {

    Bitmap bitmapMaster;
    Bitmap copy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_image);
        ActivityCompat.requestPermissions(CustomizeImageActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        final ImageView image = (ImageView) findViewById(R.id.imageView);
        Bitmap bmp;
        try {
            bmp = BitmapFactory.decodeStream(getApplication().openFileInput("colorizedImage"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            finish();
            return;
        }

        bitmapMaster = bmp;
        copy = bmp;
        image.setImageBitmap(bitmapMaster);


        final SeekBar tempBar = (SeekBar) findViewById(R.id.seekBarTemp);

        tempBar.setMax(510);
        tempBar.setProgress(255);

        tempBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                image.setColorFilter(setBrightness(progress));
                image.setColorFilter(setTemp(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });




        final SeekBar satBar = (SeekBar) findViewById(R.id.seekBarSat);

        satBar.setMax(512);
        satBar.setProgress(256);

        satBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (bitmapMaster != null) {

                    int progressSat = satBar.getProgress();

                    float sat = (float) progressSat / 256;
                    image.setImageBitmap(updateSat(bitmapMaster, sat));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });




        final SeekBar contrastBar = (SeekBar) findViewById(R.id.seekBarContrast);

        contrastBar.setMax(80);
        contrastBar.setProgress(40);

        contrastBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (bitmapMaster != null) {

                    int progressSat = contrastBar.getProgress();

                    float value = (float) progressSat + 1;
                    image.setImageBitmap(updateContrast(bitmapMaster, value));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });




        final Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
                Bitmap currentState = drawable.getBitmap();

                SaveImage s = new SaveImage();
                s.SaveImage(v.getContext(), currentState);
            }
        });



        final Button homeButton = (Button) findViewById(R.id.home);
        homeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(CustomizeImageActivity.this, MainActivity.class);
                intent.putExtra("message", "message");
                startActivity(intent);
            }
        });


        final Button resetButton = (Button) findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bitmapMaster = copy;
                image.setImageBitmap(bitmapMaster);
                tempBar.setProgress(255);
                satBar.setProgress(256);
                contrastBar.setProgress(40);

                Toast.makeText(v.getContext(), "Reset sliders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap loadImageFromStorage(String path) {

        try {
            File f=new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap updateSat(Bitmap src, float value) {

        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap bitmapResult = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvasResult = new Canvas(bitmapResult);

        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(value);
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvasResult.drawBitmap(src, new Rect(0,0, width, height), new Rect(0,0, width, height), paint);
        return bitmapResult;
    }

    private Bitmap updateContrast(Bitmap src, float value) {

        value = value / 40;
        int width = src.getWidth();
        int height = src.getHeight();

        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        value, 0, 0, 0, 0,
                        0, value, 0, 0, 0,
                        0, 0, value, 0, 0,
                        0, 0, 0, 1, 0
                });

        Bitmap bitmapResult = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvasResult = new Canvas(bitmapResult);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvasResult.drawBitmap(src, new Rect(0,0, width, height), new Rect(0,0, width, height), paint);
        return bitmapResult;
    }

    public static PorterDuffColorFilter setBrightness(int progress) {
        if (progress < 100) {
            int value = (int) 255 - (progress - 100) * 255 / 100;
            return new PorterDuffColorFilter(Color.argb(value, 255, 255, 255), PorterDuff.Mode.SRC_OVER);
        } else if (progress > 100) {
            int value = (int) 255 - (100 - progress) * 255 / 100;
            return new PorterDuffColorFilter(Color.argb(value, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
        } else {
            return new PorterDuffColorFilter(Color.argb(0, 255, 255, 255), PorterDuff.Mode.SRC_OVER);
        }
    }

    public static ColorMatrixColorFilter setTemp(int progress) {
        ColorMatrix colorMatrix = new ColorMatrix();

        if (progress < 255) {
            colorMatrix.set(new float[]{
                    1, 0, 0, 0, 0,
                    0, 1, 0, 0, 0,
                    0, 0, progress / 255.0f, 0, 0,
                    0, 0, 0, 1, 0});
        } else if (progress > 255) {
            colorMatrix.set(new float[]{
                    (510 - progress) / 255.0f, 0, 0, 0, 0,
                    0, 1, 0, 0, 0,
                    0, 0, 1, 0, 0,
                    0, 0, 0, 1, 0});
        } else {
            colorMatrix.set(new float[]{
                    1, 0, 0, 0, 0,
                    0, 1, 0, 0, 0,
                    0, 0, 1, 0, 0,
                    0, 0, 0, 1, 0});
        }
        return new ColorMatrixColorFilter(colorMatrix);
    }
}
