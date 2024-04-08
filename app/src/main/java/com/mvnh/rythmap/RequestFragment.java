package com.mvnh.rythmap;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import com.mvnh.rythmap.databinding.FragmentRequestBinding;
import com.mvnh.rythmap.responses.ServiceGenerator;
import com.mvnh.rythmap.responses.yandex.YandexApi;
import com.mvnh.rythmap.responses.yandex.YandexInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.yandex.authsdk.YandexAuthLoginOptions;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthResult;
import com.yandex.authsdk.YandexAuthSdk;

public class RequestFragment extends Fragment {

    private FragmentRequestBinding binding;
    private ActivityResultLauncher<YandexAuthLoginOptions> launcher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YandexAuthSdk sdk = YandexAuthSdk.create(new YandexAuthOptions(requireContext()));
        launcher = registerForActivityResult(sdk.getContract(), this::handleYandexResult);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRequestBinding.inflate(inflater, container, false);

        binding.requestButton.setOnClickListener(v -> yandexRequest(binding.yandexMusicTokenLabel.getText().toString()));
        binding.yandexAuthButton.setOnClickListener(v -> {
            YandexAuthLoginOptions loginOptions = new YandexAuthLoginOptions();
            launcher.launch(loginOptions);
        });

        return binding.getRoot();
    }

    private void handleYandexResult(YandexAuthResult result) {
        if (result instanceof YandexAuthResult.Success) {
            Log.d("Rythmap", String.valueOf(((YandexAuthResult.Success) result).getToken()));
            yandexRequest(String.valueOf(((YandexAuthResult.Success) result).getToken()));
        } else if (result instanceof YandexAuthResult.Failure) {
            Log.e("Rythmap", String.valueOf(((YandexAuthResult.Failure) result).getException()));
            binding.yandexMusicResultView.setText(String.valueOf(((YandexAuthResult.Failure) result).getException()));
        } else {
            binding.yandexMusicResultView.setText("something else happened and idk what");
        }
    }

    private void yandexRequest(String token) {
        binding.yandexMusicResultView.setText(R.string.wait);

        YandexApi yandexApi = ServiceGenerator.createService(YandexApi.class);
        Call<YandexInfo> call = yandexApi.getCurrentTrack(token);

        call.enqueue(new Callback<YandexInfo>() {
            @Override
            public void onResponse(Call<YandexInfo> call, Response<YandexInfo> response) {
                if (isAdded() && getActivity() != null) {
                    if (response.isSuccessful()) {
                        Log.d("Response", String.valueOf(response.code()));
                        YandexInfo yandexInfo = response.body();
                        if (yandexInfo != null) {
                            StringBuilder artistsNames = getStringBuilder(yandexInfo);

                            saveYandexResponse(artistsNames + " - " + yandexInfo.getTitle());
                            Log.d("Yandex response", getYandexResponse());
                            requireActivity().runOnUiThread(() -> binding.yandexMusicResultView.setText(getYandexResponse()));
                        }
                    } else {
                        saveYandexResponse("connection failed " + response.code());
                        Log.e("Yandex response", getYandexResponse());
                        requireActivity().runOnUiThread(() -> binding.yandexMusicResultView.setText(getYandexResponse()));
                    }
                }
            }

            @NonNull
            private StringBuilder getStringBuilder(YandexInfo yandexInfo) {
                List<YandexInfo.Artist> artists = yandexInfo.getArtists();
                StringBuilder artistsNames = new StringBuilder();
                for (int i = 0; i < artists.size(); i++) {
                    YandexInfo.Artist artist = artists.get(i);
                    if (i != 0) {
                        artistsNames.append(", ");
                    }
                    artistsNames.append(artist.getName());
                }
                return artistsNames;
            }

            @Override
            public void onFailure(Call<YandexInfo> call, Throwable t) {
                if (isAdded() && getActivity() != null) {
                    saveYandexResponse(t.getMessage());
                    Log.e("Yandex response", getYandexResponse());
                    requireActivity().runOnUiThread(() -> binding.yandexMusicResultView.setText(getYandexResponse()));
                }
            }
        });
    }

    private String getYandexResponse() {
        SharedPreferences prefs = requireContext().getSharedPreferences("YandexResponsePrefs", Context.MODE_PRIVATE);
        return prefs.getString("YandexResponseKey", null);
    }

    private void saveYandexResponse(String yandexResponse) {
        SharedPreferences prefs = requireContext().getSharedPreferences("YandexResponsePrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("YandexResponseKey", yandexResponse).apply();
    }
}