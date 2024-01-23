package com.qylex.melomap;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.qylex.melomap.responses.yandex.YandexApi;
import com.qylex.melomap.responses.yandex.YandexInfo;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RequestFragment extends Fragment {

    private TextView yandexMusicResultView, soundCloudResultView;
    private String yandexMusicResultText = "";

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

        request.setOnClickListener(v -> {
            yandexMusicResultView.setText(R.string.wait);

            String yaToken = yandexMusicToken.getText().toString();
            String scOAuth = soundCloudOAuth.getText().toString();
            String scClientID = soundCloudClientID.getText().toString();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://melomap-production.up.railway.app/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            YandexApi yandexApi = retrofit.create(YandexApi.class);

            Call<YandexInfo> yandexCall = yandexApi.getCurrentTrack(yaToken);

            yandexCall.enqueue(new Callback<YandexInfo>() {
                @Override
                public void onResponse(Call<YandexInfo> call, Response<YandexInfo> response) {
                    if (isAdded() && getActivity() != null) {
                        if (response.isSuccessful()) {
                            YandexInfo yandexInfo = response.body();
                            if (yandexInfo != null) {
                                List<YandexInfo.Artist> yandexArtists = yandexInfo.getArtists();
                                StringBuilder yandexArtistsNames = new StringBuilder();
                                for (int i = 0; i < yandexArtists.size(); i++) {
                                    YandexInfo.Artist artist = yandexArtists.get(i);
                                    if (i != 0) {
                                        yandexArtistsNames.append(", ");
                                    }
                                    yandexArtistsNames.append(artist.getName());
                                }

                                String yaDisplayText = yandexArtistsNames + " - " + yandexInfo.getTitle();
                                yandexMusicResultText = yaDisplayText;

                                requireActivity().runOnUiThread(() -> yandexMusicResultView.setText(yaDisplayText));
                            }
                        } else {
                            int responseCode = response.code();
                            yandexMusicResultText = "connection failed " + responseCode;
                            requireActivity().runOnUiThread(() -> yandexMusicResultView.setText(yandexMusicResultText));
                        }
                    }
                }

                @Override
                public void onFailure(Call<YandexInfo> call, Throwable t) {
                    if (isAdded() && getActivity() != null) {
                        yandexMusicResultText = t.getMessage();
                        requireActivity().runOnUiThread(() -> yandexMusicResultView.setText(yandexMusicResultText));
                    }
                }
            });
        });

        return view;
    }
}