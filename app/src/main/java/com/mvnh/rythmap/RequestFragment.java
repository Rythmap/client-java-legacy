package com.mvnh.rythmap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mvnh.rythmap.responses.ServiceGenerator;
import com.mvnh.rythmap.responses.yandex.YandexApi;
import com.mvnh.rythmap.responses.yandex.YandexInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestFragment extends Fragment {

    private TextView yandexMusicResultView, soundCloudResultView;

    private EditText yandexMusicToken;
    private EditText soundCloudOAuth, soundCloudClientID;

    private Button request;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);

        yandexMusicResultView = view.findViewById(R.id.yandexMusicResultView);
        soundCloudResultView = view.findViewById(R.id.soundCloudResultView);

        yandexMusicToken = view.findViewById(R.id.yandexMusicTokenLabel);
        soundCloudOAuth = view.findViewById(R.id.soundCloudOauthLabel);
        soundCloudClientID = view.findViewById(R.id.soundCloudClientIdLabel);

        request = view.findViewById(R.id.requestButton);

        String yandexResponse = getYandexResponse();
        if (yandexResponse != null) {
            Log.d("Yandex response", yandexResponse);
            yandexMusicResultView.setText(yandexResponse);
        }

        request.setOnClickListener(v -> yandexRequest(yandexMusicToken.getText().toString()));

        return view;
    }

    private void yandexRequest(String token) {
        yandexMusicResultView.setText(R.string.wait);

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
                            List<YandexInfo.Artist> artists = yandexInfo.getArtists();
                            StringBuilder artistsNames = new StringBuilder();
                            for (int i = 0; i < artists.size(); i++) {
                                YandexInfo.Artist artist = artists.get(i);
                                if (i != 0) {
                                    artistsNames.append(", ");
                                }
                                artistsNames.append(artist.getName());
                            }

                            saveYandexResponse(artistsNames + " - " + yandexInfo.getTitle());
                            Log.d("Yandex response", getYandexResponse());
                            requireActivity().runOnUiThread(() -> yandexMusicResultView.setText(getYandexResponse()));
                        }
                    } else {
                        saveYandexResponse("connection failed " + response.code());
                        Log.e("Yandex response", getYandexResponse());
                        requireActivity().runOnUiThread(() -> yandexMusicResultView.setText(getYandexResponse()));
                    }
                }
            }

            @Override
            public void onFailure(Call<YandexInfo> call, Throwable t) {
                if (isAdded() && getActivity() != null) {
                    saveYandexResponse(t.getMessage());
                    Log.e("Yandex response", getYandexResponse());
                    requireActivity().runOnUiThread(() -> yandexMusicResultView.setText(getYandexResponse()));
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