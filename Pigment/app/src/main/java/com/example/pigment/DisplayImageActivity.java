package com.example.pigment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.IntBuffer;

import colorblind.Deficiency;
import colorblind.generators.ColorDeficiencySimulator;
import colorblind.generators.ColorTranformGenerator;
import colorblind.generators.DaltonizeGenerator;

public class DisplayImageActivity extends AppCompatActivity {

    Bitmap bitmap;

    ColorTranformGenerator CTG;

    private ImageView button;
    private RelativeLayout loading;
    int[] image_pixels;
    boolean colorized = false;
    boolean toCustomize = false;
    byte[] byteArray;
    String fileLocation;
    String diagnosis;
    Deficiency deficiency;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        diagnosis = sharedPreferences.getString("diag", null);
        if (diagnosis.equals("Protanopia")) {
            deficiency = Deficiency.PROTANOPIA;
        } else if (diagnosis.equals("Deuteranopia")) {
            deficiency = Deficiency.DEUTERANOPIA;
        } else {
            deficiency = null;
        }
        if (deficiency != null) {
            CTG = DaltonizeGenerator.createDaltonizer(deficiency);
//            CTG = ColorDeficiencySimulator.createSimulator(deficiency);
        }


        button = findViewById(R.id.colorTransform);
        loading = findViewById(R.id.loadingPanel);
        loading.setVisibility(View.GONE);

        try {
            bitmap = BitmapFactory.decodeStream(getApplication().openFileInput("imageToSend"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            finish();
            return;
        }

        ImageView image = findViewById(R.id.imageCaptured);

        image.setImageBitmap(bitmap);
        image_pixels = getImageData(bitmap);
        new Colorize().execute("");

        final ImageView colorTransform = findViewById(R.id.colorTransform);
        colorTransform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toCustomize = true;
                Toast.makeText(getApplication(), "Coloring: May take a couple seconds", Toast.LENGTH_LONG).show();
                loading.setVisibility(View.VISIBLE);
                button.setEnabled(false);
                if (colorized == true) {
                    if(fileLocation!= null){
                        Intent intent = new Intent(getApplication(), CustomizeImageActivity.class);
                        loading.setVisibility(View.GONE);
                        startActivity(intent);
                        return;
                    }
                }
            }
        });
    }

    protected int[] getImageData(Bitmap img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] data = new int[w * h];
        img.getPixels(data, 0, w, 0, 0, w, h);
        return data;
    }

    public String SaveImageToStorage(Bitmap bitmap) {
        String fileName = "colorizedImage";
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        }catch(Exception e){
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    private class Colorize extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (deficiency != null) {
                CTG.transformPixels(image_pixels);
                bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(IntBuffer.wrap(image_pixels));
            }

            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
            byteArray = bStream.toByteArray();
            Bitmap decodeBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            fileLocation = SaveImageToStorage(decodeBitmap);
            return "Colorized";
        }

        @Override
        protected void onPostExecute(String result) {
            colorized = true;
            if (toCustomize == true) {
                if(fileLocation!= null){
                    Intent intent = new Intent(getApplication(), CustomizeImageActivity.class);
                    loading.setVisibility(View.GONE);
                    startActivity(intent);
                    return;
                }
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
