package com.example.park_app_admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import cyd.awesome.material.AwesomeText;
import cyd.awesome.material.FontCharacterMaps;

public class AddAdmin extends AppCompatActivity {
    private FirebaseAuth fAuth;

    EditText Email, Password;
    Button AddAdmin;
    Spinner spinner;

    AwesomeText showPass;
    boolean stat_pw = true;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_admin);

        fAuth = FirebaseAuth.getInstance();

        Email = findViewById(R.id.et_email_admin);
        Password = findViewById(R.id.et_password_admin);
        AddAdmin = findViewById(R.id.bt_tmb_admin);
        spinner = findViewById(R.id.spin_lvl_user);
        showPass = findViewById(R.id.show_pw_addmin);

        showPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stat_pw) {
                    Password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    stat_pw = false;
                    showPass.setMaterialDesignIcon(FontCharacterMaps.MaterialDesign.MD_VISIBILITY);
                    Password.setSelection(Password.length());
                } else {
                    Password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    stat_pw = true;
                    showPass.setMaterialDesignIcon(FontCharacterMaps.MaterialDesign.MD_VISIBILITY_OFF);
                    Password.setSelection(Password.length());
                }
            }
        });

        AddAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(Email.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "Enter Email!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(Password.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "Enter Password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Daftar(Email.getText().toString().trim(), Password.getText().toString().trim());
            }
        });
    }

    private void Daftar(final String email, final String password) {
        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Toast.makeText(AddAdmin.this, "Proses Pendaftaran Gagal",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            FirebaseUser user = fAuth.getCurrentUser();
                            HashMap<String, String> pengguna = new HashMap();

                            pengguna.put("email", email);
                            pengguna.put("password", password);
                            pengguna.put("level_acces", spinner.getSelectedItem().toString());

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("Data_Admin");

                            myRef.child(user.getUid()).setValue(pengguna).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //
                                    Toast.makeText(AddAdmin.this, "Proses Pendaftaran Berhasil", Toast.LENGTH_SHORT).show();
                                }
                            });

                            fAuth.signOut();

                            Intent intent = new Intent(AddAdmin.this, Login.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    public void onBackPressed() {
        Intent intent = new Intent(AddAdmin.this, Home.class);
        startActivity(intent);
    }
}
