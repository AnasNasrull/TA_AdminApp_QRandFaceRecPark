package com.example.park_app_admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class History extends AppCompatActivity {
    private RecyclerView rvView;
    private AdapterRecyclerDay adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<getKey> dataHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvView = findViewById(R.id.rv_main);
        rvView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rvView.setLayoutManager(layoutManager);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Parkir");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataHistory = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    getKey dtHist = noteDataSnapshot.getValue(getKey.class);
                    dtHist.setKey(noteDataSnapshot.getKey());

                    dataHistory.add(dtHist);
                }
                Collections.reverse(dataHistory);
                adapter = new AdapterRecyclerDay(dataHistory, History.this);
                rvView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(), "Error"+databaseError, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent(History.this, Home.class);
        startActivity(intent);
    }
}
