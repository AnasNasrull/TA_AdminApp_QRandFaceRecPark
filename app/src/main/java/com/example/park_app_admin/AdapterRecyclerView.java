package com.example.park_app_admin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class AdapterRecyclerView extends RecyclerView.Adapter<AdapterRecyclerView.ViewHolder> {
    private ArrayList<getData> data;
    Context context;

    public AdapterRecyclerView(ArrayList<getData> data1, Context context1){
        data = data1;
        context = context1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView Plat, Pengendara, NIM, Masuk, Keluar;

        ViewHolder(View v) {
            super(v);
            Plat = v.findViewById(R.id.txt_plat_hst);
            Pengendara = v.findViewById(R.id.txt_pengendara_hst);
            NIM = v.findViewById(R.id.txt_nim_hst);
            Masuk = v.findViewById(R.id.txt_masuk_hst);
            Keluar = v.findViewById(R.id.txt_keluar_hst);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_view, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final String plat = ": " + data.get(position).getPlat();
        final String nim = ": " + data.get(position).getNIM();
        final String masuk = ": " + data.get(position).getWaktu_masuk();
        final String keluar = ": " + data.get(position).getWaktu_keluar();
        final String key = data.get(position).getKey();
        final String kNim = data.get(position).getNIM();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Data_Pengguna");
        myRef.child(kNim).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nm = ": " + dataSnapshot.child("Nama").getValue().toString();
                holder.Pengendara.setText(nm);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        if (data.get(position).getWaktu_keluar().equals("-")) {
            holder.Keluar.setText("Keluar?");

            holder.Keluar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Konfirmasi");
                    dialog.setMessage("Pengendara Telah Keluar?");

                    dialog.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Date c = Calendar.getInstance().getTime();
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
                            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
                            final String wkt_klr = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());
                            final String tgl = dt.format(c);
                            final String chKey = tgl + "" + key;

                            final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Data_Pengguna");
                            myRef.child(kNim).child("Riwayat_Parkir").child(chKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Map<String, Object> postValues = new HashMap<String, Object>();
                                    postValues.put("waktu_keluar", wkt_klr);
                                    myRef.child(kNim).child("Riwayat_Parkir").child(chKey).updateChildren(postValues);
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });

                            myRef.child(kNim).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Map<String, Object> postValues = new HashMap<String, Object>();
                                    postValues.put("plat", "-");
                                    myRef.child(kNim).updateChildren(postValues);
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
                        }
                    });

                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    dialog.setCancelable(false);

                    AlertDialog dlg = dialog.create();
                    dlg.show();
                }
            });
        } else {
            holder.Keluar.setText(keluar);
        }

        holder.NIM.setText(nim);
        holder.Plat.setText(plat);
        holder.Masuk.setText(masuk);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
