package com.taocoder.dashout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class PostedAdapter extends RecyclerView.Adapter<PostedAdapter.ViewHolder> {

    private Context context;
    private List<Item> properties;
    private PostedFragment fragment;

    public PostedAdapter(Context context, List<Item> properties, PostedFragment fragment) {
        this.context = context;
        this.properties = properties;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public PostedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.posted_layout, parent, false);
        return new PostedAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostedAdapter.ViewHolder holder, int position) {
        final Item advert = properties.get(position);
        holder.desc.setText(advert.getAddress());
        Glide.with(context).load(Utils.IMAGE_URL + advert.getLogo()).into(holder.logo);

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.deleteAsk(advert.getId());
            }
        });

        if (advert.getStatus() != 0) {
            holder.mark.setVisibility(View.GONE);
        }

        holder.mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.markAsk(advert.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public CardView cardView;
        private AppCompatImageView logo;
        private TextView desc;
        private MaterialButton delete;
        private MaterialButton mark;

        public ViewHolder(View view) {
            super(view);

            cardView = (CardView) view;
            logo = (AppCompatImageView) view.findViewById(R.id.logo);
            desc = (TextView) view.findViewById(R.id.about);
            delete = (MaterialButton) view.findViewById(R.id.delete);
            mark = (MaterialButton) view.findViewById(R.id.mark);
        }
    }
}

