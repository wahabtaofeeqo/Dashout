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
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

    private Context context;
    private List<Item> items;
    private OnFragmentChangeListener listener;

    public ItemsAdapter(Context context, List<Item> items, OnFragmentChangeListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Item advert = items.get(position);

        holder.desc.setText(context.getResources().getString(R.string.post, advert.getPost(), advert.getAddress()));
        Glide.with(context).load(Utils.IMAGE_URL + advert.getLogo()).into(holder.logo);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onFragmentChange(DetailsFragment.getInstance(advert));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public CardView cardView;
        private AppCompatImageView logo;
        private TextView desc;

        public ViewHolder(View view) {
            super(view);

            cardView = (CardView) view;
            logo = (AppCompatImageView) view.findViewById(R.id.logo);
            desc = (TextView) view.findViewById(R.id.about);
        }
    }
}

