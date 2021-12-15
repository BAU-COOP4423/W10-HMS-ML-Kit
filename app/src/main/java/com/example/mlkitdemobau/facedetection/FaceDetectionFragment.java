package com.example.mlkitdemobau.facedetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mlkitdemobau.R;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.LensEngine;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting;
import com.huawei.hms.mlsdk.face.face3d.ML3DFaceAnalyzerSetting;

import java.io.IOException;

public class FaceDetectionFragment extends Fragment {
    private MLFaceAnalyzer analyzer;
    private LensEngine lensEngine;
    private SurfaceHolder surfaceHolderOverlay;
    private FaceAnalyzerTransactor mFaceAnalyzerTransactor;
    private SurfaceView surfaceHolderCameraView;
    private final String TAG = "FaceDetectionFragment";

    public FaceDetectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_face_detection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        surfaceHolderCameraView = requireView().findViewById(R.id.surfaceViewCamera);
        if (checkPermission()) {
            init();

        } else {
            requestPermission();
        }

    }

    private boolean checkPermission() {
        // Permission is not granted
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.CAMERA},
                0);
    }

    private MLFaceAnalyzer createAnalyzer() {
        // Use custom parameter settings, and enable the speed preference mode and face tracking function to obtain a faster speed.
        MLFaceAnalyzerSetting setting = new MLFaceAnalyzerSetting.Factory()
                // Set the preference mode of the analyzer.
                // ML3DFaceAnalyzerSetting.TYPE_SPEED: speed preference mode.
                // ML3DFaceAnalyzerSetting.TYPE_PRECISION: precision preference mode.
                .setPerformanceType(ML3DFaceAnalyzerSetting.TYPE_SPEED)
                // Set whether to enable face tracking in a specific mode.
                // Input parameter 1: true, indicating that the face tracking function is enabled.
                // Input parameter 1: false, indicating that the face tracking function is disabled.
                .setTracingAllowed(true)
                .create();
        return MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting);

    }

    private void prepareViews() {
        surfaceHolderCameraView = requireView().findViewById(R.id.surfaceViewCamera);
        SurfaceHolder surfaceHolderCamera = surfaceHolderCameraView.getHolder();
        SurfaceView surfaceHolderOverlayView = requireView().findViewById(R.id.surfaceViewOverlay);
        surfaceHolderOverlay = surfaceHolderOverlayView.getHolder();

        surfaceHolderOverlay.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolderCamera.addCallback(getSurfaceHolderCallback());


    }

    public SurfaceHolder.Callback getSurfaceHolderCallback() {
        // i1 = width, i2 = height
        return new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                if (lensEngine == null) {
                    lensEngine = createLensEngine(i1, i2); // i1 = width, i2 = height
                }
                if (surfaceHolderOverlay != null) {
                    mFaceAnalyzerTransactor.setOverlay(surfaceHolderOverlay);
                }
                try {
                    lensEngine.run(surfaceHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                // When camera surface view is destroyed, release the detection resources:
                releaseFaceDetectionResources();
            }
        };
    }

    private LensEngine createLensEngine(int width, int height) {

        lensEngine = new LensEngine.Creator(requireActivity().getApplicationContext(), analyzer)
                .setLensType(LensEngine.BACK_LENS)
                .applyDisplayDimension(height, width)
                .applyFps(30.0f)
                .enableAutomaticFocus(true)
                .create();

        return lensEngine;
    }

    private void init() {
        analyzer = createAnalyzer();

        mFaceAnalyzerTransactor = new FaceAnalyzerTransactor();
        analyzer.setTransactor(mFaceAnalyzerTransactor);
        prepareViews();
    }

    private void releaseFaceDetectionResources() {
        if (analyzer != null) {
            try {
                analyzer.stop();
            } catch (IOException e) {
                // Exception handling.
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
        if (lensEngine != null) {
            lensEngine.release();
        }
    }
}
