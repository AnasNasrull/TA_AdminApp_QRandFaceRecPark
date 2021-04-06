package com.example.park_app_admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;
import com.tzutalin.dlib.Constants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQrCode extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    String nim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);

    }

    public void handleResult(final Result rawResult) {
        final String plat = rawResult.getText(); //"BE 5334 PJ";//MASIH ON CODE================================================================

        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
        String tm = new SimpleDateFormat("HHmmss").format(cal.getTime());
        String tgl = dt.format(c);
        String chKey = tgl + "" + tm;

        Query query = FirebaseDatabase.getInstance().getReference("Parkir").child(tgl).orderByChild("plat").equalTo(plat);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ////Log.e("app","found: "+snapshot.getValue());
                String data="";
                for (DataSnapshot chilSnapshot:snapshot.getChildren()) {
                     //data = chilSnapshot.child("waktu_masuk").getValue().toString();
                    data = chilSnapshot.getKey();
                }

                if (snapshot.getValue() != null) {
                    GoToFaceRecog(data);
                } else {
                    //Toast.makeText(RegisterKendaraan.this, "Kosong :"+snapshot.getValue(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(ScanQrCode.this, "Kendaraan Tidak Terdata Parkir!", Toast.LENGTH_SHORT).show();

                    //finish();
                    //startActivity(getIntent());
                }

            }
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mScannerView.resumeCameraPreview(this);
    }

    private void GoToFaceRecog(final String key) {
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
        String tm = new SimpleDateFormat("HHmmss").format(cal.getTime());
        String tgl = dt.format(c);
        String chKey = tgl + "" + tm;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Parkir");

        myRef.child(tgl).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.child("NIM").getValue().toString();

                Intent intent = new Intent(getBaseContext(), FaceMain.class);
                intent.putExtra("nim", data);
                intent.putExtra("key", key);
                startActivity(intent);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ScanQrCode.this, Home.class);
        startActivity(intent);
    }
}
