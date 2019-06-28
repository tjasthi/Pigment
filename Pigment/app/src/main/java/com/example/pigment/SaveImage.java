package com.example.pigment;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SaveImage {
    private Context con;

    public void SaveImage(Context context, Bitmap saveMe) {
        con = context;
        String path = Environment.getExternalStorageDirectory() + "/Pigment";
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String dateAndTime = getCurrentDateAndTime();
        File file = new File(directory, "image" + dateAndTime + ".jpg");
        try {
            FileOutputStream output = new FileOutputStream(file);
            saveMe.compress(Bitmap.CompressFormat.JPEG,100, output);
            output.flush();
            output.close();
            makeAvailable(file);
            saved();
        } catch (FileNotFoundException e) {
            notSaved();
        } catch (IOException e) {
            notSaved();
        }
    }

    private String getCurrentDateAndTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat d = new SimpleDateFormat("yyy-MM-dd-HH-mm-ss");
        String formattedDate = d.format(cal.getTime());
        return formattedDate;
    }

    private void makeAvailable(File file) {
        MediaScannerConnection.scanFile(con, new String[]{file.toString()},
                null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.e("ExternalStorage", "Scanned" + path + ":");
                        Log.e("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    private void saved() {
        Toast.makeText(con, "Picture saved", Toast.LENGTH_SHORT).show();
    }

    private void notSaved() {
        Toast.makeText(con, "Saving failed!", Toast.LENGTH_SHORT).show();
    }
}
