package com.mvnh.rythmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.mvnh.rythmap.databinding.FragmentTestAccountBinding;
import com.mvnh.rythmap.responses.ServiceGenerator;
import com.mvnh.rythmap.responses.account.AccountApi;
import com.mvnh.rythmap.responses.account.entities.AccountInfo;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestAccountFragment extends Fragment {

    private FragmentTestAccountBinding binding;
    private TokenManager tokenManager;
    ActivityResultLauncher<PickVisualMediaRequest> pickVisualMediaRequestActivityResultLauncher;
    AccountApi accountApi;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTestAccountBinding.inflate(inflater, container, false);

        tokenManager = new TokenManager(requireContext());

        accountApi = ServiceGenerator.createService(AccountApi.class);

        retrieveAccountInfo(tokenManager.getToken());

        pickVisualMediaRequestActivityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        try {
                            uploadMediaToServer(tokenManager.getToken(), uri);
                        } catch (IOException e) {
                            Log.e("Rythmap", e.toString());
                        }
                    } else {
                        Log.e("Rythmap", "smth happened");
                    }
        });

        binding.profilePfp.setOnClickListener(v -> {
            pickVisualMediaRequestActivityResultLauncher
                    .launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        return binding.getRoot();
    }

    String nickname = null;
    public void retrieveAccountInfo(String token) {
        Call<AccountInfo> call = accountApi.getAccountInfo(token);
        call.enqueue(new Callback<AccountInfo>() {
            @Override
            public void onResponse(Call<AccountInfo> call, Response<AccountInfo> response) {
                if (isAdded() && getActivity() != null) {
                    AccountInfo body = response.body();
                    if (response.isSuccessful()) {
                        binding.nameTextView.setText(body.getNickname());
                        nickname = body.getNickname();
                        fetchAvatar(nickname);
                    } else {
                        binding.nameTextView.setText(response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<AccountInfo> call, Throwable t) {

            }
        });
    }

    private void uploadMediaToServer(String token, Uri uri) throws IOException {
        InputStream uriInputStream = requireContext().getContentResolver().openInputStream(uri);
        byte[] uriISBytes = IOUtils.toByteArray(uriInputStream);
        RequestBody requestBody = RequestBody.create(uriISBytes, MediaType.parse("image/jpeg"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "avatar-" + token, requestBody);

        Call<ResponseBody> call = accountApi.uploadAvatar(token, filePart);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    fetchAvatar(nickname);
                } else {
                    Log.e("Rythmap", String.valueOf(response.code()));
                    try {
                        Log.e("Rythmap", response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("Rythmap", e.toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                Log.e("Rythmap", throwable.getMessage());
            }
        });
    }

    private void fetchAvatar(String username) {
        Call<ResponseBody> call = accountApi.getAvatar(nickname);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        byte[] imageBytes = response.body().bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes != null ? imageBytes.length : 0);
                        binding.profilePfp.setImageBitmap(bitmap);
                        Log.d("Rythmap", "here");
                    } catch (IOException e) {
                        Log.d("Rythmap", e.toString());
                    }
                } else {
                    Toast.makeText(requireContext(), "smth happened", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}