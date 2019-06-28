package com.example.pigment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityManagerCompat;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.util.List;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback {

    Camera camera;
    Camera.PictureCallback jpegCallback;

    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int IMAGE_PICK_CODE = 1000;

    public static CameraFragment newInstance(){
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    private boolean safeToTakePicture = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        mSurfaceView = view.findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String [] {android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            mSurfaceHolder.addCallback(this);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        ImageView mSettings = view.findViewById(R.id.settings);
        ImageView mCapture = view.findViewById(R.id.capture);
        ImageView mUploadImage = view.findViewById(R.id.uploadImage);

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSettings(v);
            }
        });

        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        mUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        jpegCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                Bitmap decodeBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                Bitmap rotateBitmap = rotate(decodeBitmap);

                String fileLocation = SaveImageToStorage(rotateBitmap);
                camera.startPreview();
                safeToTakePicture = true;
                if(fileLocation!= null){
                    Intent intent = new Intent(getActivity(), DisplayImageActivity.class);
                    startActivity(intent);
                    return;
                }
            }
        };

        return view;
    }

    public String SaveImageToStorage(Bitmap bitmap) {
        String fileName = "imageToSend";
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        }catch(Exception e){
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    private Bitmap rotate(Bitmap decodedBitmap) {
        int w  = decodedBitmap.getWidth();
        int h = decodedBitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.setRotate(90);

        return Bitmap.createBitmap(decodedBitmap, 0, 0, w, h, matrix, true);
    }

    // TODO: made my viewpager static so i can access from other classes to change frags
    // button to go to results/settings page
    public void goToSettings(View view) {
        MainActivity.viewPager.setCurrentItem(0);
    }

    //takes the picture
    public void captureImage() {
        if (safeToTakePicture) {
            camera.takePicture(null, null, jpegCallback);
            safeToTakePicture = false;
        }
    }

    //uploads image from photo gallery
    public void uploadImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            InputStream iStream = null;
            try {
                iStream = getContext().getContentResolver().openInputStream(data.getData());
                byte[] inputData =  getBytes(iStream);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(inputData,0, inputData.length);
                String fileLocation = SaveImageToStorage(decodedBitmap);
                if(fileLocation!= null) {
                    Intent intent = new Intent(getActivity(), DisplayImageActivity.class);
                    startActivity(intent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        Camera.Parameters parameters = camera.getParameters();
        camera.setDisplayOrientation(90);

        parameters.setPreviewFrameRate(30);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

//        Camera.Size bestSize = null;
//        List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
//        bestSize = sizeList.get(0);
//        for (int i = 1; i < sizeList.size(); i++) {
//            if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
//                bestSize = sizeList.get(i);
//            }
//        }
//        parameters.setPreviewSize(bestSize.width, bestSize.height);
////
//        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        camera.startPreview();
        safeToTakePicture = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        camera.startPreview();
        safeToTakePicture = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
         switch (requestCode) {
             case CAMERA_REQUEST_CODE:{
                 if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                     mSurfaceHolder.addCallback(this);
                     mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                 } else {
                     Toast.makeText(getContext(), "Please prorivde permission", Toast.LENGTH_LONG).show();
                 }
                 break;
             }
         }
    }
}
