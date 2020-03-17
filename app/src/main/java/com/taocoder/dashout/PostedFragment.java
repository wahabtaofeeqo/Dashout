package com.taocoder.dashout;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
public class PostedFragment extends Fragment {


    public PostedFragment() {
        // Required empty public constructor
    }


    private RecyclerView recyclerView;
    private AppCompatActivity activity;

    private List<Item> items;
    private PostedAdapter adapter;

    private ProgressBar progressBar;
    private ProgressDialog progressDialog;

    private OnFragmentChangeListener listener;

    private SessionManager sessionManager;

    private String email;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getContext());
        sessionManager = new SessionManager(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_posted, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.loading);

        items = new ArrayList<>();
        adapter = new PostedAdapter(getContext(), items, this);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        final SessionManager sessionManager = new SessionManager(getContext());
        email = sessionManager.getUsername();
        loadProperties(email);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        listener = (OnFragmentChangeListener) getActivity();
    }


    private void loadProperties(final String email) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utils.BASE_URL + "my-items", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

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
                            advert.setId(row.getInt("id"));
                            advert.setName(row.getString("title"));
                            advert.setPost(row.getString("title"));
                            advert.setAddress(row.getString("location"));
                            advert.setDesc(row.getString("description"));
                            advert.setLogo(row.getString("logo"));
                            advert.setPhone(row.getString("phone"));
                            advert.setStatus(row.getInt("status"));

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
                map.put("email", email);

                return map;
            }
        };

        Controller.getInstance().addRequestQueue(stringRequest);
    }

    private void delete(final int id) {

        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utils.BASE_URL + "delete", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("error")) {
                        Utils.showMessage(getContext(), jsonObject.getString("errorMessage"));
                        progressDialog.dismiss();
                    }
                    else {
                        Utils.showMessage(getContext(), jsonObject.getString("message"));
                        loadProperties(email);
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
                        progressDialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("id", String.valueOf(id));

                return map;
            }
        };

        Controller.getInstance().addRequestQueue(stringRequest);
    }

    void deleteAsk(final int id) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setMessage("Delete this item?");

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                delete(id);
            }
        });

        dialog.show();
    }

    void markAsk(final int id) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setMessage("Somebody has collected this item?");

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               mark(id);
            }
        });

        dialog.show();
    }

    private void mark(final int id) {

        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utils.BASE_URL + "mark", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("error")) {
                        Utils.showMessage(getContext(), jsonObject.getString("errorMessage"));
                        progressDialog.dismiss();
                    }
                    else {
                        Utils.showMessage(getContext(), jsonObject.getString("message"));
                        loadProperties(email);
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
                        progressDialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("id", String.valueOf(id));

                return map;
            }
        };

        Controller.getInstance().addRequestQueue(stringRequest);
    }
}