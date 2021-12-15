package com.example.mlkitdemobau.imageclassification;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mlkitdemobau.R;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.classification.MLImageClassification;
import com.huawei.hms.mlsdk.classification.MLImageClassificationAnalyzer;
import com.huawei.hms.mlsdk.classification.MLLocalClassificationAnalyzerSetting;
import com.huawei.hms.mlsdk.common.MLFrame;

import java.io.IOException;
import java.util.List;

public class ImageDetectionFragment extends Fragment {
    private MLImageClassificationAnalyzer analyzer;
    private final String TAG = "ImageDetectionFragment";

    public ImageDetectionFragment() {
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
        return inflater.inflate(R.layout.fragment_image_detection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.catimage);
        ImageView myImage = requireView().findViewById(R.id.detected_image_iv);
        myImage.setImageBitmap(bitmap);

        Button imageClassificationButton = requireView().findViewById(R.id.image_classification_btn);
        TextView detectedClassesTv = requireView().findViewById(R.id.detected_image_class_tv);

        // Create the image analyzer:
        analyzer = createImageAnalyzer();
        // Create an MLFrame object using the bitmap, which is the image data in bitmap format:
        MLFrame frame = MLFrame.fromBitmap(bitmap);
        imageClassificationButton.setOnClickListener(view1 -> {
            Task<List<MLImageClassification>> task = analyzer.asyncAnalyseFrame(frame);
            task.addOnSuccessListener(classifications -> {
                // Recognition success.
                StringBuilder sb = new StringBuilder();
                sb.append("Results: \n\n");
                // All classification results will be added to StringBuilder object in a for loop:
                for (int i = 0; i < classifications.size(); i++) {
                    sb.append("[")
                            .append(i)
                            .append("] ")
                            .append(classifications.get(i).getName())
                            .append("\n");

                }
                detectedClassesTv.setText(sb.toString());
                // After recognition is completed, release the detection resources:
                releaseImageDetectionResources();

            }).addOnFailureListener(e -> {
                // Recognition failure.
                Log.e(TAG, e.getLocalizedMessage());

            });
        });

    }

    private MLImageClassificationAnalyzer createImageAnalyzer() {
        MLLocalClassificationAnalyzerSetting setting =
                new MLLocalClassificationAnalyzerSetting.Factory()
                        .setMinAcceptablePossibility(0.8f) // Higher min acceptable possibility results in higher accuracy in classification results.
                        .create();
        return MLAnalyzerFactory.getInstance().getLocalImageClassificationAnalyzer(setting);
    }

    private void releaseImageDetectionResources() {
        try {
            if (analyzer != null) {
                analyzer.stop();
            }
        } catch (IOException e) {
            // Exception handling.
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

}