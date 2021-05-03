/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Modified by Gaurav on Feb 23, 2018

package com.example.park_app_admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.cameraview.CameraView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceRec;
import com.tzutalin.dlib.VisionDetRet;

import junit.framework.Assert;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

// This demo app uses dlib face recognition based on resnet
public class FaceMain extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback{

    String key;

    private static final String TAG = "FaceMain";
    private static final int INPUT_SIZE = 500;

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_OFF,
            CameraView.FLASH_AUTO,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_on,
    };

    private static final int[] FLASH_TITLES = {
            R.string.flash_off,
            R.string.flash_auto,
            R.string.flash_on,
    };

    private int mCurrentFlash;

    private CameraView mCameraView;

    private Handler mBackgroundHandler;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.take_picture) {
                if (mCameraView != null) {
                    mCameraView.takePicture();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();

        String nim = getIntent().getStringExtra("nim");
        key = getIntent().getStringExtra("key");
        File image_folder = new File(Constants.getDLibDirectoryPath() + "/", "images" );
        if (!image_folder.exists()) {
            image_folder.mkdirs();
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference islandRef = storageRef.child("images/"+ nim +".jpg");

        File localFile = null;
        localFile = new File(Constants.getDLibImageDirectoryPath() + "/" + nim + ".jpg");//("/storage/emulated/0/dlib_rec_example/images/", nim + ".jpg");

        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                Toast.makeText(FaceMain.this, "Download Berhasil!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(FaceMain.this, "Download Gagal!", Toast.LENGTH_SHORT).show();
            }
        });

        mCameraView = findViewById(R.id.camera);
        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }
        Button fab = findViewById(R.id.take_picture);
        if (fab != null) {
            fab.setOnClickListener(mOnClickListener);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

    }

    private FaceRec mFaceRec;

    private void changeProgressDialogMessage(final ProgressDialog pd, final String msg) {
        Runnable changeMessage = new Runnable() {
            @Override
            public void run() {
                pd.setMessage(msg);
            }
        };
        runOnUiThread(changeMessage);
    }

    private class initRecAsync extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(FaceMain.this);

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "initRecAsync onPreExecute called");
            dialog.setMessage("Loading...");
            dialog.setCancelable(false);
            dialog.show();
            super.onPreExecute();
        }

        protected Void doInBackground(Void... args) {
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
                    FileUtils.copyFileFromRawToOthers(FaceMain.this, R.raw.shape_predictor_5_face_landmarks, Constants.getFaceShapeModelPath());
                }
                if (!new File(Constants.getFaceDescriptorModelPath()).exists()) {
                    FileUtils.copyFileFromRawToOthers(FaceMain.this, R.raw.dlib_face_recognition_resnet_model_v1, Constants.getFaceDescriptorModelPath());
                }
            } else {
                //Log.d(TAG, "error in setting dlib_rec_example directory");
            }
            mFaceRec = new FaceRec(Constants.getDLibDirectoryPath());
            changeProgressDialogMessage(dialog, "Adding people...");
            mFaceRec.train();
            return null;
        }

        protected void onPostExecute(Void result) {
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
        }
    }

    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume called");
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraView.start();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            new initRecAsync().execute();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause called");
        mCameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        super.onDestroy();
        if (mFaceRec != null) {
            mFaceRec.release();
        }
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

/*    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something
            }
            return;
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_flash:
                if (mCameraView != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                    item.setTitle(FLASH_TITLES[mCurrentFlash]);
                    item.setIcon(FLASH_ICONS[mCurrentFlash]);
                    mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
                }
                return true;
            case R.id.switch_camera:
                if (mCameraView != null) {
                    int facing = mCameraView.getFacing();
                    mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                            CameraView.FACING_BACK : CameraView.FACING_FRONT);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

/*    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }*/

    /*private String getResultMessage(ArrayList<String> names) {
        String msg = "";
        if (names.isEmpty()) {
            msg = "Wajah Tidak Dikenali!";

        } else {
            for(int i=0; i<names.size(); i++) {
                msg += names.get(i).split(Pattern.quote("."))[0];
                if (i!=names.size()-1) msg+=", ";
            }

            final String nim = msg;
            Date c = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
            final String wkt_klr = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());
            final String tgl = dt.format(c);
            final String chKey = tgl + "" + key;

            final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Data_Pengguna");
            myRef.child(nim).child("Riwayat_Parkir").child(chKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Object> postValues = new HashMap<String, Object>();
                    postValues.put("waktu_keluar", wkt_klr);
                    myRef.child(nim).child("Riwayat_Parkir").child(chKey).updateChildren(postValues);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            myRef.child(nim).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Object> postValues = new HashMap<String, Object>();
                    postValues.put("plat", "-");
                    myRef.child(nim).updateChildren(postValues);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            final DatabaseReference Ref = FirebaseDatabase.getInstance().getReference("Parkir");
            Ref.child(tgl).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Object> postValues = new HashMap<String, Object>();
                    postValues.put("waktu_keluar", wkt_klr);
                    Ref.child(tgl).child(key).updateChildren(postValues);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });


            File file = new File(Constants.getDLibImageDirectoryPath());//"/storage/emulated/0/dlib_rec_example/images");///"+msg+".jpg");

            if (file.isDirectory()) {
                String[] children = file.list();
                for (String child : children) {
                    new File(file, child).delete();
                }
                file.delete();
            }
        }
        return msg;
    }*/

    private class recognizeAsync extends AsyncTask<Bitmap, Void, ArrayList<String>> {
        ProgressDialog dialog = new ProgressDialog(FaceMain.this);
        private int mScreenRotation = 0;
        private boolean mIsComputing = false;
        private Bitmap mCroppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Recognizing...");
            dialog.setCancelable(false);
            dialog.show();
            super.onPreExecute();
        }

        protected ArrayList<String> doInBackground(Bitmap... bp) {

            drawResizedBitmap(bp[0], mCroppedBitmap);
            Log.d(TAG, "byte to bitmap");

            long startTime = System.currentTimeMillis();
            List<VisionDetRet> results;
            results = mFaceRec.recognize(mCroppedBitmap);
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "Time cost: " + (endTime - startTime) / 1000f + " sec");

            ArrayList<String> names = new ArrayList<>();
            for(VisionDetRet n:results) {
                names.add(n.getLabel());
            }
            return names;
        }

        protected void onPostExecute(ArrayList<String> names) {
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(FaceMain.this);
                //builder1.setMessage(getResultMessage(names));
                builder1.setCancelable(false);

                String msg = "";
                if (names.isEmpty()) {
                    builder1.setMessage("Wajah Tidak Dikenali!");

                    builder1.setPositiveButton("Pindai Ulang", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            File file = new File(Constants.getDLibImageDirectoryPath());

                            if (file.isDirectory()) {
                                String[] children = file.list();
                                for (String child : children) {
                                    new File(file, child).delete();
                                }
                                file.delete();
                            }

                            Intent intent = new Intent(FaceMain.this, ScanQrCode.class);
                            startActivity(intent);
                        }
                    });
                } else {
                    for(int i=0; i<names.size(); i++) {
                        msg += names.get(i).split(Pattern.quote("."))[0];
                        if (i!=names.size()-1) msg+=", ";
                    }

                    final String nim = msg;
                    Date c = Calendar.getInstance().getTime();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
                    final String wkt_klr = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());
                    final String tgl = dt.format(c);
                    final String chKey = tgl + "" + key;

                    final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Data_Pengguna");
                    myRef.child(nim).child("Riwayat_Parkir").child(chKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Object> postValues = new HashMap<String, Object>();
                            postValues.put("waktu_keluar", wkt_klr);
                            myRef.child(nim).child("Riwayat_Parkir").child(chKey).updateChildren(postValues);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    myRef.child(nim).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Object> postValues = new HashMap<String, Object>();
                            postValues.put("plat", "-");
                            myRef.child(nim).updateChildren(postValues);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    final DatabaseReference Ref = FirebaseDatabase.getInstance().getReference("Parkir");
                    Ref.child(tgl).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Object> postValues = new HashMap<String, Object>();
                            postValues.put("waktu_keluar", wkt_klr);
                            Ref.child(tgl).child(key).updateChildren(postValues);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    builder1.setMessage("Identitas Pengendara Sesuai Data Masuk, \nDipersilahkan Keluar");
                    builder1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            File file = new File(Constants.getDLibImageDirectoryPath());

                            if (file.isDirectory()) {
                                String[] children = file.list();
                                for (String child : children) {
                                    new File(file, child).delete();
                                }
                                file.delete();
                            }

                            Intent intent = new Intent(FaceMain.this, ScanQrCode.class);
                            startActivity(intent);
                        }
                    });

                }

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        }

        private void drawResizedBitmap(final Bitmap src, final Bitmap dst) {
            Display getOrient = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int orientation = Configuration.ORIENTATION_UNDEFINED;
            Point point = new Point();
            getOrient.getSize(point);
            int screen_width = point.x;
            int screen_height = point.y;
            Log.d(TAG, String.format("screen size (%d,%d)", screen_width, screen_height));
            if (screen_width < screen_height) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
                mScreenRotation = 0;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
                mScreenRotation = 0;
            }

            Assert.assertEquals(dst.getWidth(), dst.getHeight());
            final float minDim = Math.min(src.getWidth(), src.getHeight());

            final Matrix matrix = new Matrix();

            // We only want the center square out of the original rectangle.
            final float translateX = -Math.max(0, (src.getWidth() - minDim) / 2);
            final float translateY = -Math.max(0, (src.getHeight() - minDim) / 2);
            matrix.preTranslate(translateX, translateY);

            final float scaleFactor = dst.getHeight() / minDim;
            matrix.postScale(scaleFactor, scaleFactor);

            // Rotate around the center if necessary.
            if (mScreenRotation != 0) {
                matrix.postTranslate(-dst.getWidth() / 2.0f, -dst.getHeight() / 2.0f);
                matrix.postRotate(mScreenRotation);
                matrix.postTranslate(dst.getWidth() / 2.0f, dst.getHeight() / 2.0f);
            }

            final Canvas canvas = new Canvas(dst);
            canvas.drawBitmap(src, matrix, null);
        }
    }


    private CameraView.Callback mCallback
            = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);
            Toast.makeText(cameraView.getContext(), R.string.picture_taken, Toast.LENGTH_SHORT)
                    .show();
            Bitmap bp = BitmapFactory.decodeByteArray(data, 0, data.length);
            new recognizeAsync().execute(bp);
        }

    };

    public void onBackPressed() {
        File file = new File(Constants.getDLibImageDirectoryPath());

        if (file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                new File(file, child).delete();
            }
            file.delete();
        }

        Intent intent = new Intent(FaceMain.this, ScanQrCode.class);
        startActivity(intent);
    }
}
