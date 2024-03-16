package com.mvnh.rythmap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MapFragment extends Fragment {

    private MapView mapView;
    private String accessToken;
    private TokenManager tokenManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private OkHttpClient client;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getLocation();
                } else {
                    Toast.makeText(getContext(), "access denied", Toast.LENGTH_SHORT).show();
                    mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle("https://api.jawg.io/styles/jawg-terrain.json?access-token=" + accessToken));
                }
            });

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Mapbox.getInstance(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        TokenManager tokenManager = new TokenManager(getContext());

        client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();
        Request request = new Request.Builder().url("wss://melomap.fun/ws").build();
        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                super.onOpen(webSocket, response);

                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                } else {
                    fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                       if (task.isSuccessful() && task.getResult() != null) {
                           Location location = task.getResult();

                           String json = "{\"access_token\":\"" + tokenManager.getToken() +
                                   "\",\"geolocation\":{\"latitude\":" + location.getLatitude() +
                                   ",\"longitude\":" + location.getLongitude() + "}}";
                           webSocket.send(json);
                       }
                    });
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                super.onMessage(webSocket, text);
                Log.d("Rythmap", "websocket response" + text);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                Log.d("Rythmap", "websocket failure: " + t.getMessage());
            }
        });

        accessToken = getString(R.string.jawg_access_token);
        String styleUrl = "https://api.jawg.io/styles/jawg-matrix.json?access-token=" + accessToken;

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(styleUrl));
            getLocation();
        }

        return view;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();

                    mapView.getMapAsync(mapboxMap -> {
                        mapboxMap.setStyle("https://api.jawg.io/styles/jawg-matrix.json?access-token=" + accessToken);
                        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                .zoom(15)
                                .build());
                    });
                } else {
                    Toast.makeText(getContext(), "ligma balls", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}