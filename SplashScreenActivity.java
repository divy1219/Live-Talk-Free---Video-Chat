package com.livetalk.randomvideocall.joinlivetalk.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.livetalk.randomvideocall.joinlivetalk.R;
import com.livetalk.randomvideocall.joinlivetalk.models.AdsModel;
import com.livetalk.randomvideocall.joinlivetalk.retro.ApiClient;
import com.livetalk.randomvideocall.joinlivetalk.retro.ApiInterface;
import com.skyfishjy.library.RippleBackground;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.livetalk.randomvideocall.joinlivetalk.utils.Utils.ADS_MODEL;
import static com.livetalk.randomvideocall.joinlivetalk.utils.Utils.isConnected;
import static com.livetalk.randomvideocall.joinlivetalk.utils.Utils.isFirstRunApp;

public class SplashScreenActivity extends BaseActivity {
    private static final int REQUEST_PERMISSIONS = 1;
    RippleBackground rippleBackground;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_activity);
        rippleBackground = findViewById(R.id.content);
        imageView = findViewById(R.id.centerImage);
        rippleBackground.startRippleAnimation();
        if (isConnected(this)) {
            getAppDetail();
        } else {
            showNetworkDialog();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSIONS) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else if (grantResults.length == 4 && grantResults[0] == 0 && grantResults[1] == 0 && grantResults[2] == 0 && grantResults[3] == 0) {
            openNextActivity();
        } else {
            requestPermissions(new String[]{"android.permission.READ_PHONE_STATE", "android.permission.CAMERA", "android.permission.MODIFY_AUDIO_SETTINGS", "android.permission.RECORD_AUDIO"}, REQUEST_PERMISSIONS);
        }
    }

    void getAppDetail() {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<AdsModel> call = apiService.getAppInformation(getPackageName());
        call.enqueue(new Callback<AdsModel>() {
            @Override
            public void onResponse(@Nullable Call<AdsModel> call, @Nullable Response<AdsModel> response) {

                assert response != null;
                ADS_MODEL = response.body();
                if (isConnected(SplashScreenActivity.this)) {
                    openNextActivity();
                } else {
                    showNetworkDialog();
                }


            }

            @Override
            public void onFailure(@Nullable Call<AdsModel> call, @Nullable Throwable t) {
                openNextActivity();
            }


        });
    }

    void openNextActivity() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int verCode = pInfo.versionCode;
            if (ADS_MODEL != null && ADS_MODEL.getVersion_code() <= verCode) {
                if (isConnected(this) && isFirstRunApp(this)) {
                    Intent intent = new Intent(SplashScreenActivity.this, ProfileSettingActivity.class);
                    intent.putExtra("isMain",false);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashScreenActivity.this, JoinLiveTalkMainActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                showDownloadLatestApp();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    void showNetworkDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getResources().getString(R.string.network_error_title));
        alertDialog.setMessage(getResources().getString(R.string.network_error_message));
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isConnected(SplashScreenActivity.this)) {
                    getAppDetail();
                } else {
                    showNetworkDialog();
                }
            }
        });
        alertDialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }
        });
        alertDialog.show();
    }

    void showDownloadLatestApp() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashScreenActivity.this);
        alertDialog.setTitle("New version available");
        alertDialog.setMessage("To enjoy new features of app. You need to update app please kindly update your app!");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String urlStrRateUs = "https://play.google.com/store/apps/details?id=" + getPackageName();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlStrRateUs)));
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        alertDialog.show();
    }
}
