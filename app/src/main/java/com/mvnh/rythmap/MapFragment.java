package com.mvnh.rythmap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import androidx.collection.LongSparseArray;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mvnh.rythmap.databinding.FragmentMapBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MapFragment extends Fragment {

    private FragmentMapBinding binding;
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getUserLocation().addOnCompleteListener(task -> {
                        Location location = task.getResult();
                        if (task.isSuccessful() && task.getResult() != null) {
                            mapView.getMapAsync(mapboxMap -> {
                                mapboxMap.setCameraPosition(new CameraPosition.Builder()
                                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                        .zoom(15)
                                        .build());
                            });
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "ligma balls", Toast.LENGTH_SHORT).show();
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
        binding = FragmentMapBinding.inflate(inflater, container, false);

        TokenManager tokenManager = new TokenManager(requireContext());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        String accessToken = SecretData.JAWG_ACCESS_TOKEN;
        String styleId = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES ? "jawg-matrix" : "jawg-terrain";
        String styleUrl = "https://api.jawg.io/styles/" + styleId + ".json?access-token=" + accessToken;

        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(styleUrl));

        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();
        Request request = new Request.Builder().url("wss://" + SecretData.SERVER_URL + "/ws").build();
        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                super.onOpen(webSocket, response);

                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                } else {
                    getUserLocation().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();

                            String json = "{\"access_token\":\"" + tokenManager.getToken() +
                                    "\",\"geolocation\":{\"latitude\":" + location.getLatitude() +
                                    ",\"longitude\":" + location.getLongitude() + "}}";
                            Log.d("Rythmap", json);

                            webSocket.send(json);
                        }
                    });
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                super.onMessage(webSocket, text);
                Log.d("Rythmap", "ws response " + text);

                getActivity().runOnUiThread(() -> {
                    mapView.getMapAsync(mapboxMap -> {
                        mapboxMap.setStyle(styleUrl, style -> {
                            SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, style);
                            symbolManager.setIconAllowOverlap(true);
                            symbolManager.setIconIgnorePlacement(true);

                            try {
                                JSONObject jsonObject = new JSONObject(text);
                                Iterator<String> keys = jsonObject.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    JSONObject user = jsonObject.getJSONObject(key);

                                    String username = user.getString("username");
                                    // Drawable pfp = will be here

                                    JSONObject geolocation = user.getJSONObject("geolocation");
                                    double latitude = geolocation.getDouble("latitude");
                                    double longitude = geolocation.getDouble("longitude");

                                    Bitmap bitmap = createMarkerBitmap(ResourcesCompat.getDrawable(getResources(), R.drawable.fuckthisworldcat, null), username);

                                    style.addImage("marker-" + username, bitmap);

                                    LongSparseArray<Symbol> symbols = symbolManager.getAnnotations();
                                    List<Symbol> symbolsList = new ArrayList<>();
                                    for (int i = 0; i < symbols.size(); i++) {
                                        symbolsList.add(symbols.valueAt(i));
                                    }
                                    Symbol symbol = null;
                                    for (Symbol s : symbolsList) {
                                        if (s.getIconImage().equals("marker-" + username)) {
                                            symbol = s;
                                            break;
                                        }
                                    }
                                    if (symbol == null) {
                                        symbol = symbolManager.create(new SymbolOptions()
                                                .withLatLng(new LatLng(latitude, longitude))
                                                .withIconImage("marker-" + username).withIconSize(0.7f)
                                                .withIconAnchor("bottom"));
                                    } else {
                                        symbol.setLatLng(new LatLng(latitude, longitude));
                                    }
                                    symbolManager.update(symbol);
                                };
                            } catch (JSONException e) {
                                Log.e("Rythmap", e.toString());
                            }
                        });
                    });
                });
            }
        });

        return binding.getRoot();
    }

    private Task<Location> getUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return null;
        } else {
            return fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationTokenSource().getToken());
        }
    }

    private Bitmap createMarkerBitmap(Drawable picture, String username) {
        View markerLayout = getLayoutInflater().inflate(R.layout.default_marker, null);

        ShapeableImageView imageView = markerLayout.findViewById(R.id.markerUserPfp);
        TextView textView = markerLayout.findViewById(R.id.markerUserNickname);

        // imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.fuckthisworldcat, null));
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