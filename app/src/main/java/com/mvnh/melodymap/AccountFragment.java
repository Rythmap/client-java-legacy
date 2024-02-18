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
import com.mvnh.melodymap.responses.ServiceGenerator;
import com.mvnh.melodymap.responses.account.AccountApi;
import com.mvnh.melodymap.responses.account.AccountInfo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AccountFragment extends Fragment {

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

        getAccountInfo(token);
        Log.d("getAccountInfo method executed", token);
        accountInfo.setText(getSavedAccountInfo());

        logout.setOnClickListener(v -> {
            tokenManager.clearToken();
            Log.d("Logout", "Logout");
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            startActivity(intent);
            requireActivity().finishAffinity();
        });

        return view;
    }

    private void getAccountInfo(String token) {
        AccountApi accountApi = ServiceGenerator.createService(AccountApi.class);
        Call<AccountInfo> accountInfoCall = accountApi.getAccountInfo(token);
        accountInfoCall.enqueue(new Callback<AccountInfo>() {
            @Override
            public void onResponse(Call<AccountInfo> call, Response<AccountInfo> response) {
                if (isAdded() && getActivity() != null) {
                    if (response.isSuccessful()) {
                        Log.d("Response", String.valueOf(response.code()));
                        AccountInfo body = response.body();
                        Log.d("Account info", String.valueOf(body));
                        if (body != null) {
                            String resultString = "Token valid: " + body.isTokenValid() + "\n" +
                                    "Username: " + body.getUsername() + "\n" +
                                    "Email: " + body.getEmail() + "\n" +
                                    "Email confirmed: " + body.isEmailConfirmed();
                            Log.d("Result string", resultString);
                            saveAccountInfo(resultString);

                            requireActivity().runOnUiThread(() -> accountInfo.setText(resultString));
                        } else {
                            int responseCode = response.code();
                            Log.e("Response", String.valueOf(response.code()));
                            if (errorDescriptions.containsKey(responseCode)) {
                                requireActivity().runOnUiThread(() -> accountInfo.setText(errorDescriptions.get(responseCode)));
                                Log.e("Error", errorDescriptions.get(responseCode));
                            } else {
                                requireActivity().runOnUiThread(() -> accountInfo.setText(getString(R.string.unknown_error) + responseCode));
                                Log.e("Error", String.valueOf(responseCode));
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AccountInfo> call, Throwable t) {
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> accountInfo.setText(t.getMessage()));
                    Log.e("Error", t.getMessage());
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