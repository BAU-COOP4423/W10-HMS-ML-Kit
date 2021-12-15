package com.example.mlkitdemobau;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mlkitdemobau.facedetection.*;
import com.example.mlkitdemobau.imageclassification.*;
import com.example.mlkitdemobau.textdetectiontranslation.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.huawei.hms.mlsdk.common.MLApplication;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set API key for on-device translation in Activity's onCreate method:
        MLApplication.getInstance().setApiKey(getResources().getString(R.string.api_key));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // App starts with TextDetectionFragment initially
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new TextDetectionFragment()).commit();

        // When bottom navigation items are clicked, the corresponding fragment is set
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.text:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, new TextDetectionFragment()).commit();
                    return true;

                case R.id.face:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, new FaceDetectionFragment()).commit();
                    return true;

                case R.id.image:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, new ImageDetectionFragment()).commit();
                    return true;
            }
            return false;
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Reload current face detection fragment in order to display camera surface view when camera permission is granted
                // Without camera permission, camera surface view will not be able to displayed.
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new FaceDetectionFragment()).commit();
            }
        }
    }
}