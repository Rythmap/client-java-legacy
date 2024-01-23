package com.qylex.melomap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.qylex.melomap.LoginActivity.errorDescriptions;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.qylex.melomap.responses.account.AccountApi;
import com.qylex.melomap.responses.account.AccountInfo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private TokenManager tokenManager;
    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bnv = findViewById(R.id.bottom_navigation);

        tokenManager = new TokenManager(this);
        tokenValidation();

        fm = getSupportFragmentManager();

        bnv.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.request) {
                loadFragment(new RequestFragment());
                return true;
            } else if (item.getItemId() == R.id.account) {
                loadFragment(new AccountFragment());
                return true;
            }
            return false;
        });
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragmentContainerView, fragment);
        transaction.commit();
    }

    private void tokenValidation() {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            runOnUiThread(() -> {
                Toast.makeText(this, "token is empty", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        } else {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://melomap-production.up.railway.app/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            AccountApi accountApi = retrofit.create(AccountApi.class);

            Call<AccountInfo> accountInfoCall = accountApi.getAccountInfo(token);
            accountInfoCall.enqueue(new Callback<AccountInfo>() {
                @Override
                public void onResponse(Call<AccountInfo> call, Response<AccountInfo> response) {
                    int responseCode = response.code();
                    if (errorDescriptions.containsKey(responseCode)) {
                        if (responseCode == 200) {
                            Log.d("Token", tokenManager.getToken());
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "ok", Toast.LENGTH_SHORT).show());
                        } else {
                            Log.d("Token validation", errorDescriptions.get(responseCode));
                            tokenManager.saveToken(null);

                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, errorDescriptions.get(responseCode), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        }
                    } else {
                        Log.d("Token validation", String.valueOf(responseCode));
                        tokenManager.saveToken(null);

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, getString(R.string.error_checking_token_validity) + responseCode, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }
                }

                @Override
                public void onFailure(Call<AccountInfo> call, Throwable t) {
                    Log.d("Token validation", t.getMessage());
                    tokenManager.saveToken(null);

                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }
            });
        }
    }
}