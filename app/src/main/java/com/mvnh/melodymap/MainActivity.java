package com.mvnh.melodymap;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.mvnh.melodymap.auth.AuthActivity.errorDescriptions;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.mvnh.melodymap.auth.AuthActivity;
import com.mvnh.melodymap.responses.ServiceGenerator;
import com.mvnh.melodymap.responses.account.AccountApi;
import com.mvnh.melodymap.responses.account.AccountInfo;

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
        EdgeToEdge.enable(this);

        BottomNavigationView bnv = findViewById(R.id.mainBottomNavigation);
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
            } else if (item.getItemId() == R.id.map) {
                loadFragment(new MapFragment());
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

    public void tokenValidation() {
        String token = tokenManager.getToken();
        Log.d("Token", token);
        if (token == null || token.isEmpty()) {
            runOnUiThread(() -> {
                Log.e("Token", tokenManager.getToken());
                Toast.makeText(this, "token is empty", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, AuthActivity.class));
                finish();
            });
        } else {
            AccountApi accountApi = ServiceGenerator.createService(AccountApi.class);
            Call<AccountInfo> accountInfoCall = accountApi.getAccountInfo(token);
            accountInfoCall.enqueue(new Callback<AccountInfo>() {
                @Override
                public void onResponse(Call<AccountInfo> call, Response<AccountInfo> response) {
                    Log.d("Melodymap", String.valueOf(response.code()));
                    if (errorDescriptions.containsKey(response.code())) {
                        if (response.code() == 200) {
                            Log.d("Melodymap", "token validation successful");
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "ok", Toast.LENGTH_SHORT).show());
                        } else {
                            tokenManager.clearToken();

                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, errorDescriptions.get(response.code()), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this, AuthActivity.class));
                                finish();
                            });
                            Log.e("Token validation", errorDescriptions.get(response.code()));
                        }
                    } else {
                        tokenManager.clearToken();

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, getString(R.string.error_checking_token_validity) + response.code(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, AuthActivity.class));
                            finish();
                        });
                        Log.e("Melodymap", getString(R.string.error_checking_token_validity));
                    }
                }

                @Override
                public void onFailure(Call<AccountInfo> call, Throwable t) {
                    tokenManager.clearToken();

                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, AuthActivity.class));
                        finish();
                    });
                    Log.e("Melodymap", t.getMessage());
                }
            });
        }
    }
}