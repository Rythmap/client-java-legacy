package com.mvnh.melodymap;

import static com.mvnh.melodymap.auth.AuthActivity.errorDescriptions;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mvnh.melodymap.auth.AuthActivity;
import com.mvnh.melodymap.responses.account.AccountApi;
import com.mvnh.melodymap.responses.account.AccountInfo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AccountFragment extends Fragment {

    SwipeRefreshLayout swipeRefreshLayout;

    private TokenManager tokenManager;

    private TextView accountInfo;

    private Button logout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        tokenManager = new TokenManager(requireContext());
        String token = tokenManager.getToken();

        accountInfo = view.findViewById(R.id.accountInfoView);
        logout = view.findViewById(R.id.logoutButton);

        String savedAccountInfo = getSavedAccountInfo();
        if (savedAccountInfo != null) {
            accountInfo.setText(savedAccountInfo);
        } else {
            getAccountInfo(token);
        }

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getAccountInfo(token);
            swipeRefreshLayout.setRefreshing(false);
        });

        logout.setOnClickListener(v -> {
            tokenManager.saveToken(null);
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            startActivity(intent);
            requireActivity().finishAffinity();
        });

        return view;
    }

    private void getAccountInfo(String token) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://melomap-production.up.railway.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AccountApi accountApi = retrofit.create(AccountApi.class);
        Call<AccountInfo> accountInfoCall = accountApi.getAccountInfo(token);
        accountInfoCall.enqueue(new Callback<AccountInfo>() {
            @Override
            public void onResponse(Call<AccountInfo> call, Response<AccountInfo> response) {
                if (isAdded() && getActivity() != null) {
                    if (response.isSuccessful()) {
                        AccountInfo accountInfoModel = response.body();
                        if (accountInfoModel != null) {
                            String resultString = "Token valid: " + accountInfoModel.isTokenValid() + "\n" +
                                    "Username: " + accountInfoModel.getUsername() + "\n" +
                                    "Email: " + accountInfoModel.getEmail() + "\n" +
                                    "Email confirmed: " + accountInfoModel.isEmailConfirmed();
                            saveAccountInfo(resultString);

                            requireActivity().runOnUiThread(() -> accountInfo.setText(resultString));
                        } else {
                            int responseCode = response.code();
                            if (errorDescriptions.containsKey(responseCode)) {
                                requireActivity().runOnUiThread(() -> accountInfo.setText(errorDescriptions.get(responseCode)));
                                Log.d("Error", errorDescriptions.get(responseCode));
                            } else {
                                requireActivity().runOnUiThread(() -> accountInfo.setText(getString(R.string.unknown_error) + responseCode));
                                Log.d("Error", String.valueOf(responseCode));
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AccountInfo> call, Throwable t) {
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> accountInfo.setText(t.getMessage()));
                    Log.d("Retrofit Exception", t.getMessage());
                }
            }
        });
    }

    private String getSavedAccountInfo() {
        SharedPreferences prefs = requireContext().getSharedPreferences("AccountInfoPrefs", Context.MODE_PRIVATE);
        return prefs.getString("AccountInfoKey", null);
    }

    private void saveAccountInfo(String accountInfo) {
        SharedPreferences prefs = requireContext().getSharedPreferences("AccountInfoPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("AccountInfoKey", accountInfo).apply();
    }
}