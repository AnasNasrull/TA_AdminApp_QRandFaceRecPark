package com.example.park_app_admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
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

public class HistoryDay extends AppCompatActivity {
    private RecyclerView rvView;
    private AdapterRecyclerView adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<getData> dataHistory;

    String key, tgl;

    TextView Tanggal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_day);

        Tanggal = findViewById(R.id.tgl_hist);

        key = getIntent().getStringExtra("key");//"20210312";
        tgl = getIntent().getStringExtra("tgl");//"6565656";

        Tanggal.setText(tgl);

        rvView = findViewById(R.id.rv_main_day);
        rvView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rvView.setLayoutManager(layoutManager);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Parkir");

        database.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataHistory = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    getData dtHist = noteDataSnapshot.getValue(getData.class);
                    dtHist.setKey(noteDataSnapshot.getKey());

                    dataHistory.add(dtHist);
                }
                adapter = new AdapterRecyclerView(dataHistory, HistoryDay.this);
                rvView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(), "Error"+databaseError, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent(HistoryDay.this, History.class);
        startActivity(intent);
    }
}
