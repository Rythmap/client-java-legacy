package com.mvnh.rythmap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.imageview.ShapeableImageView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MapFragment extends Fragment {

    private MapView mapView;
    private String accessToken;
    private FusedLocationProviderClient fusedLocationProviderClient;

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

        accessToken = SecretData.JAWG_ACCESS_TOKEN;
        String styleUrl = "https://api.jawg.io/styles/jawg-matrix.json?access-token=" + accessToken;

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            getLocation();
        }

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();
        Request request = new Request.Builder().url("wss://" + SecretData.SERVER_URL + "/ws").build();
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
                Log.d("Rythmap", "websocket response " + text);

                try {
                    JSONObject jsonObject = new JSONObject(text);
                    Iterator<String> keys = jsonObject.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject user = new JSONObject(jsonObject.getJSONObject(key).toString());

                        Log.d("Rythmap", key);

                        getActivity().runOnUiThread(() -> {
                            mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(styleUrl, style -> {
                                try {
                                    String username = user.getString("username");
                                    Log.d("Rythmap", username);
                                    // Drawable picture = ResourcesCompat.getDrawable(getResources(), R.drawable.fuckthisworldcat, null);
                                    JSONObject geolocation = user.getJSONObject("geolocation");

                                    double latitude = geolocation.getDouble("latitude");
                                    double longitude = geolocation.getDouble("longitude");

                                    Bitmap bitmap = createMarkerBitmap(null, username);

                                    style.addImage("marker-" + username, bitmap);

                                    SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, style);
                                    symbolManager.setIconAllowOverlap(true);
                                    symbolManager.setIconIgnorePlacement(true);

                                    Symbol symbol = symbolManager.create(new SymbolOptions()
                                            .withLatLng(new LatLng(latitude, longitude))
                                            .withIconImage("marker-" + username).withIconSize(0.625f)
                                            .withIconAnchor("bottom"));

                                    symbolManager.update(symbol);
                                } catch (JSONException e) {
                                    Log.e("Rythmap", e.toString());
                                }
                            }));
                        });
                    }
                } catch (JSONException e) {
                    Log.e("Rythmap", e.toString());
                }
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                Log.d("Rythmap", "websocket failure: " + t.getMessage());
            }
        });

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

    public Bitmap createMarkerBitmap(Drawable picture, String username) {
        View markerLayout = getLayoutInflater().inflate(R.layout.default_marker, null);

        ShapeableImageView imageView = markerLayout.findViewById(R.id.markerUserPfp);
        TextView textView = markerLayout.findViewById(R.id.markerUserNickname);

        // picasso will be here
        imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.fuckthisworldcat, null));
        textView.setText(username);

        markerLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());
        markerLayout.draw(canvas);

        return bitmap;
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