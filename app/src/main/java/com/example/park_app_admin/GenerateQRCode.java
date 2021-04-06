package com.example.park_app_admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

public class GenerateQRCode extends AppCompatActivity {
    ImageView generateQrCode;
    Button Download;
    QRGEncoder qrgEncoder;
    Bitmap bitmap;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generateqrcode);

        generateQrCode = findViewById(R.id.generate_qr_code);
        Download = findViewById(R.id.bt_download);

        qrgEncoder = new QRGEncoder("Parkir Gedung C & D", null, QRGContents.Type.TEXT, 600);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);

        bitmap = qrgEncoder.getBitmap();

        generateQrCode.setImageBitmap(bitmap);

        Download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean save;
                String result;
                try {
                    String savePath = Environment.getExternalStorageDirectory() + "/QRCode/";
                    save = new QRGSaver().save(savePath, "Parkir Gedung C & D", bitmap, QRGContents.ImageType.IMAGE_JPEG);
                    result = save ? "Image Saved" : "Image Not Saved";
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent(GenerateQRCode.this, Home.class);
        startActivity(intent);
    }
}
