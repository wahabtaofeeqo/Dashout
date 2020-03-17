package com.taocoder.dashout;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements Validator.ValidationListener, OnMapReadyCallback {


    @NotEmpty
    private TextInputEditText name;

    @NotEmpty
    private TextInputEditText desc;

    @NotEmpty
    private TextInputEditText address;

    private AppCompatImageView imageView;
    private Validator validator;
    private ProgressDialog progressDialog;

    private String location;
    private String path;

    private OnFragmentChangeListener listener;

    private GoogleMap map;

    private SessionManager sessionManager;

    private boolean permissionGranted;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(getContext());
        sessionManager = new SessionManager(getContext());
        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        listener = (OnFragmentChangeListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        SupportMapFragment mapFragment  = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        name = (TextInputEditText) view.findViewById(R.id.name);
        desc = (TextInputEditText) view.findViewById(R.id.desc);
        address = (TextInputEditText) view.findViewById(R.id.address);
        imageView = (AppCompatImageView) view.findViewById(R.id.logo);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 40);
        }

        final MaterialButton upload = (MaterialButton) view.findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 30);
                }
                else {
                    chooseLogo();
                }
            }
        });

        final MaterialButton button = (MaterialButton) view.findViewById(R.id.post);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validator.validate();
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.setMinZoomPreference(15.5f);

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                location = getLocation(latLng);
                if (location != null)
                    address.setText(location);
            }
        });
    }

    private String getLocation(LatLng latLng) {
        try {

            List<Address> addresses = Controller.getInstance().getGeocoder().getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                return addresses.get(0).getAddressLine(0);
            }

            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            Utils.showMessage(getContext(), e.getMessage());
        }

        return null;
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        Utils.showMessage(getContext(), "Required Data is OR are missing");
    }

    @Override
    public void onValidationSucceeded() {

        if (path != null) {
            uploadFile(path);
        }
        else {
            Utils.showMessage(getContext(), "Select Shop logo");
        }
    }

    private void chooseLogo() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "System Logo"), 20);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 20:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    imageView.setImageURI(uri);
                    imageView.setVisibility(View.VISIBLE);

                    path = FilePath.getPath(getContext(), uri);
                }
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 30) {
            if (grantResults[0] == Activity.RESULT_OK) {
                chooseLogo();
            }
            else {
                Utils.showMessage(getContext(), "Permission is need");
            }
        }
    }

    private void uploadFile(String path) {
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        HttpURLConnection urlConnection = null;
        String lineEnd = "\r\n";
        String boundary = "*****";
        String hyphen = "--";
        DataOutputStream dataOutputStream;
        File file = new File(path);
        String[] info = path.split("/");
        String filename = info[info.length - 1];

        int read, available, size;
        byte[] buffer;
        int maxSize = 1024 * 1024;

        if (!file.isFile()) {

            Utils.showMessage(getContext(), "File Does Not Exist");
            return;
        }

        try {

            FileInputStream fileInputStream = new FileInputStream(file);
            java.net.URL url = new URL(Utils.BASE_URL + "upload");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            urlConnection.setRequestProperty("logo", path);

            dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.writeBytes(hyphen + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"logo\"; filename=\"" +
                    path + "\"" + lineEnd);

            dataOutputStream.writeBytes(lineEnd);

            available = fileInputStream.available();
            size = Math.min(available, maxSize);

            buffer = new byte[size];
            read = fileInputStream.read(buffer, 0, size);

            while (read > 0) {
                dataOutputStream.write(buffer, 0, size);
                available = fileInputStream.available();
                size = Math.min(available, maxSize);
                read = fileInputStream.read(buffer, 0, size);
            }

            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(hyphen + boundary + lineEnd);
            dataOutputStream.flush();

            int response = urlConnection.getResponseCode();
            String message = urlConnection.getResponseMessage();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            if (response == 200) {

                JSONObject jsonObject = new JSONObject(builder.toString());

                if (jsonObject.getBoolean("error")) {

                    progressDialog.dismiss();
                    Utils.showMessage(getContext(), jsonObject.getString("errorMessage"));
                }
                else {

                    JSONObject object = jsonObject.getJSONObject("data");
                    filename = object.getString("file_name");

                    post(filename);
                }
            }
            else {
                Utils.showMessage(getContext(), "Internal Server Error. Please try again.");
            }

            fileInputStream.close();
            dataOutputStream.close();
            urlConnection.disconnect();

        }
        catch (Exception e) {
            e.printStackTrace();

            progressDialog.dismiss();
        }
    }

    private void post(final String filename) {

        final String n = name.getText().toString();
        final String d  = desc.getText().toString();
        final String a  = address.getText().toString();
        final String email = sessionManager.getUsername();

        StringRequest request = new StringRequest(Request.Method.POST, Utils.BASE_URL + "post", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("RES", response);

                progressDialog.dismiss();
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getBoolean("error")) {
                        Utils.showMessage(getContext(), object.getString("errorMessage"));
                    }
                    else {

                        responseDialog(object.getString("message"));
                    }
                }
                catch (JSONException e) {
                    Utils.showMessage(getContext(), "Error: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.showMessage(getContext(), "Server Error: " + error.getMessage());
                error.printStackTrace();
                progressDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<>();
                map.put("name", n);
                map.put("desc", d);
                map.put("address", a);
                map.put("filename", filename);
                map.put("email", email);

                return map;
            }
        };

        Controller.getInstance().addRequestQueue(request);
    }

    private void responseDialog(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setMessage(message);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.onFragmentChange(new HomeFragment());
            }
        });

        dialog.show();
    }


    class Uploader extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            String path = strings[0];

            if (path != null) {
                uploadFile(path);
            }
            else {
                Utils.showMessage(getContext(), "Path Error");
            }

            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }
}
