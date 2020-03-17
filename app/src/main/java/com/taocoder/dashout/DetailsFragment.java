package com.taocoder.dashout;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {

    private static Item advert;
    public static DetailsFragment getInstance(Item a) {
        advert = a;
        return new DetailsFragment();
    }

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        if (advert != null) {

            AppCompatImageView logo = (AppCompatImageView) view.findViewById(R.id.logo);
            TextView name = (TextView) view.findViewById(R.id.name);
            TextView desc = (TextView) view.findViewById(R.id.desc);
            TextView addr = (TextView) view.findViewById(R.id.address);
            TextView ownr = (TextView) view.findViewById(R.id.owner);

            ownr.setText(advert.getOwner());
            name.setText(advert.getName());
            addr.setText(advert.getAddress());
            desc.setText(advert.getDesc());

            Glide.with(getContext()).load(Utils.IMAGE_URL + advert.getLogo()).into(logo);
            final MaterialButton call = (MaterialButton) view.findViewById(R.id.call);
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    makeCall(advert.getPhone());
                }
            });
        }
        else {
            Utils.showMessage(getContext(), "Can find Owner");
        }

        return view;
    }

    private void makeCall(final String number) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Place a call to this Item Owner?");
        builder.setPositiveButton("Call", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + number));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
    }

}