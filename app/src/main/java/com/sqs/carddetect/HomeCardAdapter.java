package com.sqs.carddetect;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeCardAdapter extends RecyclerView.Adapter<HomeCardAdapter.MyViewHolder> {
    List<Card_Data> image_card=new ArrayList<>();

    void UpdateList(List<Card_Data> image_card){
        this.image_card.clear();
        this.image_card.addAll(image_card);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return image_card.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Card_Data card_data=image_card.get(position);
        holder.title.setText(card_data.getName());
        holder.image.setImageBitmap(card_data.getImage());
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        ImageView image;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.file_name);
            image = (ImageView) view.findViewById(R.id.card_view);
        }
    }


}
