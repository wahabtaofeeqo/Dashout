package com.taocoder.dashout;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    private RecyclerView recyclerView;
    private AppCompatActivity activity;

    private List<Item> items;
    private ItemsAdapter adapter;

    private ProgressBar progressBar;

    private OnFragmentChangeListener listener;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.loading);

        items = new ArrayList<>();
        adapter = new ItemsAdapter(getContext(), items, listener);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        loadItems("Abeokuta");
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        listener = (OnFragmentChangeListener) getActivity();
    }

    private void loadItems(final String address) {

        if (address != null) {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utils.BASE_URL + "items", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    Log.i("RES", response);

                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.getBoolean("error")) {
                            Utils.showMessage(getContext(), jsonObject.getString("errorMessage"));
                            progressBar.setVisibility(View.GONE);
                        }
                        else {

                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                            items.clear();

                            JSONArray data = jsonObject.getJSONArray("data");

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject row = data.getJSONObject(i);

                                Item advert = new Item();
                                advert.setOwner(row.getString("firstname"));
                                advert.setName(row.getString("title"));
                                advert.setPost(row.getString("title"));
                                advert.setAddress(row.getString("location"));
                                advert.setDesc(row.getString("description"));
                                advert.setLogo(row.getString("logo"));
                                advert.setPhone(row.getString("phone"));

                                items.add(advert);
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("location", address);
                    return map;
                }
            };

            Controller.getInstance().addRequestQueue(stringRequest);
        }
        else {
            Utils.showMessage(getContext(), "Could not find your location");
        }
    }
}
