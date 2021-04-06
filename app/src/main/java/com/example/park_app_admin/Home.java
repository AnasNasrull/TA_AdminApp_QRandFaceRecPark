package com.example.park_app_admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceRec;

import java.io.File;

public class Home extends AppCompatActivity {
    Button Scan, History, AddAdmin, GenQRCode, Logout;

    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fStateListener;
    private FirebaseUser user;

    private static final String TAG = Home.class.getSimpleName();

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        checkDir();

        fAuth = FirebaseAuth.getInstance();

        fStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.v(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Toast.makeText(Home.this, "User Logout\n", Toast.LENGTH_SHORT).show();
                    Log.v(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(Home.this, Login.class);
                    startActivity(intent);
                }
            }
        };

        Scan = findViewById(R.id.scanqr);
        History = findViewById(R.id.hist);
        AddAdmin = findViewById(R.id.bt_addadmin);
        GenQRCode = findViewById(R.id.bt_gen_qrcode);
        Logout = findViewById(R.id.bt_logout);

        Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, ScanQrCode.class);
                startActivity(intent);
            }
        });

        History.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, History.class);
                startActivity(intent);
                //fAuth.signOut();
            }
        });

        GenQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, GenerateQRCode.class);
                startActivity(intent);
            }
        });

        AddAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference("Data_Admin");

                myRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String data = dataSnapshot.child("level_acces").getValue().toString();

                        if (data.equals("Super Admin")) {
                            Intent intent = new Intent(Home.this, AddAdmin.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(Home.this, "Anda Tidak Punya Akses\n" +
                                    "Anda  Bukan Super Admin", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    protected void checkDir() {
        // create dlib_rec_example directory in sd card and copy model files
        File folder = new File(Constants.getDLibDirectoryPath());
        boolean success = false;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            File image_folder = new File(Constants.getDLibImageDirectoryPath());
            image_folder.mkdirs();
            if (!new File(Constants.getFaceShapeModelPath()).exists()) {
                FileUtils.copyFileFromRawToOthers(Home.this, R.raw.shape_predictor_5_face_landmarks, Constants.getFaceShapeModelPath());
            }
            if (!new File(Constants.getFaceDescriptorModelPath()).exists()) {
                FileUtils.copyFileFromRawToOthers(Home.this, R.raw.dlib_face_recognition_resnet_model_v1, Constants.getFaceDescriptorModelPath());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        fAuth.addAuthStateListener(fStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fStateListener != null) {
            fAuth.removeAuthStateListener(fStateListener);
        }
    }

    private void signOut(){
        fAuth.signOut();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Keluar Aplikasi");
        builder.setMessage("Anda yakin ingin keluar aplikasi? ");
        builder.setCancelable(false);

        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAndRemoveTask();
                //finish();
                //Home.this.finish();
                finish();
                moveTaskToBack(true);
                //System.exit(0);
                //super.finish();
            }
        });

        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
