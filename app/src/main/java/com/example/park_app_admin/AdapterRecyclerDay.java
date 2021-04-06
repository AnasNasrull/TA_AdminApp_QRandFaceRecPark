package com.example.park_app_admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdapterRecyclerDay extends RecyclerView.Adapter<AdapterRecyclerDay.ViewHolder> {
    private ArrayList<getKey> data;

    Context context;

    public AdapterRecyclerDay(ArrayList<getKey> data1, Context context1){
        data = data1;
        context = context1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout LL;
        TextView List;

        ViewHolder(View v) {
            super(v);
            LL = v.findViewById(R.id.lnr_day_hst);
            List = v.findViewById(R.id.txt_day_hst);
        }
    }

    @Override
    public AdapterRecyclerDay.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_day, parent, false);

        AdapterRecyclerDay.ViewHolder vh = new AdapterRecyclerDay.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(AdapterRecyclerDay.ViewHolder holder, final int position) {
        final String list = data.get(position).getKey();
        String tgl = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            Date d = sdf.parse(list);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dt = new SimpleDateFormat("dd MMM yyyy");
            tgl = dt.format(d);
        } catch (ParseException ignored) {

        }

        final String finalTgl = tgl;
        holder.LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HistoryDay.class);
                intent.putExtra("key", list);
                intent.putExtra("tgl", finalTgl);
                context.startActivity(intent);
                //Toast.makeText(context, "key nya: " + finalTgl, Toast.LENGTH_SHORT).show();
            }
        });

        holder.List.setText(tgl);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
