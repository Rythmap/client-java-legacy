package com.mvnh.melodymap;

import static com.mvnh.melodymap.auth.AuthActivity.errorDescriptions;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mvnh.melodymap.auth.AuthActivity;
import com.mvnh.melodymap.databinding.FragmentAccountBinding;
import com.mvnh.melodymap.responses.ServiceGenerator;
import com.mvnh.melodymap.responses.account.AccountApi;
import com.mvnh.melodymap.responses.account.AccountInfo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private AccountViewModel accountVM;
    private TokenManager tokenManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        accountVM = new ViewModelProvider(getActivity()).get(AccountViewModel.class);
        binding.setViewModel(accountVM);

        tokenManager = new TokenManager(requireContext());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.retriveAccountInfoButton.setOnClickListener(v -> retrieveAccountInfo(tokenManager.getToken()));

        binding.logoutButton.setOnClickListener(v -> {
            tokenManager.clearToken();
            Log.d("Melodymap", "Logout");
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            startActivity(intent);
            requireActivity().finishAffinity();
        });
    }

    public void retrieveAccountInfo(String token) {
        AccountApi accountApi = ServiceGenerator.createService(AccountApi.class);
        Call<AccountInfo> accountInfoCall = accountApi.getAccountInfo(token);
        accountInfoCall.enqueue(new Callback<AccountInfo>() {
            @Override
            public void onResponse(@NonNull Call<AccountInfo> call, @NonNull Response<AccountInfo> response) {
                if (isAdded() && getActivity() != null) {
                    if (response.isSuccessful()) {
                        Log.d("Melodymap", "response code" + response.code());
                        AccountInfo body = response.body();
                        Log.d("Melodymap", "body" + body.toString());
                        String resultString = "Token valid: " + body.isTokenValid() + "\n" +
                                "Username: " + body.getUsername() + "\n" +
                                "Email: " + body.getEmail() + "\n" +
                                "Email confirmed: " + body.isEmailConfirmed();
                        Log.d("Melodymap", resultString);
                        accountVM.setAccountInfo(resultString);
                    } else {
                        Log.d("Melodymap", "response code" + response.code());
                        if (errorDescriptions.containsKey(response.code())) {
                            accountVM.setAccountInfo(errorDescriptions.get(response.code()));
                            Log.e("Melodymap", errorDescriptions.get(response.code()));
                        } else {
                            accountVM.setAccountInfo(getString(R.string.unknown_error) + response.code());
                            Log.e("Melodymap", String.valueOf(response.code()));
                        }
                        requireActivity().finishAffinity();
                    }
                }
            }

            @Override
            public void onFailure(Call<AccountInfo> call, Throwable t) {
                if (isAdded() && getActivity() != null) {
                    accountVM.setAccountInfo(t.getMessage());
                    Log.e("Melodymap", t.getMessage());
                }
            }
        });

        accountVM.getAccountInfo().observe(getViewLifecycleOwner(), accountInfo -> binding.accountInfoView.setText(accountInfo));
    }
}