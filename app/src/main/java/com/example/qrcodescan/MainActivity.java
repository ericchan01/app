package com.example.qrcodescan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;


import java.io.IOException;



public class MainActivity extends AppCompatActivity {
    SurfaceView sv;
    CameraSource cameraSource;
    Button copyBtn;
    BarcodeScanner bScanner;
    BarcodeDetector barcodeDetector;
    ClipData clipdata;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sv = (SurfaceView) findViewById(R.id.surfaceView);

        handler = new Handler();
        getSupportActionBar().hide();

        getPermissionsCamera();

        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).setAutoFocusEnabled(true).build();


        barcodeDetector.setProcessor(new Detector.Processor<com.google.android.gms.vision.barcode.Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<com.google.android.gms.vision.barcode.Barcode> detections) {
                final SparseArray<com.google.android.gms.vision.barcode.Barcode> qrcode = detections.getDetectedItems();
                if (qrcode.size() != 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("errorchecking", "1");
                            showDialog(qrcode.valueAt(0).displayValue);

                        }
                    });

                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        sv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission", "Not allow");
                    return;
                }

                try {
                    cameraSource.start(holder);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
        sv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    return;

                try {
                    cameraSource.start(sv.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean getPermissionsCamera() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
        return true;
    }

    private void showDialog(String url) {
        Log.d("errorchecking", "2");
        Dialog dialog = new Dialog(MainActivity.this);
        Log.d("errorchecking", "3");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.d("errorchecking", "4");
        dialog.setContentView(R.layout.buttonsheetlayout);
        Log.d("errorchecking", "5");
        copyBtn = (Button) dialog.findViewById(R.id.copyBtn);
        copyBtn.setText(url);
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Log.d("url", copyBtn.getText().toString().toLowerCase());
                if (!url.equalsIgnoreCase("URL:"))
                    clipdata = ClipData.newPlainText("url", url);
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("url", copyBtn.getText().toString());
                clipboard.setPrimaryClip(clip);
                handler.post(() -> Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_LONG));


                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    return;

                try {
                    cameraSource.start(sv.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);


    }


}