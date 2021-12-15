package com.example.mlkitdemobau.textdetectiontranslation;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mlkitdemobau.R;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLException;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.model.download.MLModelDownloadListener;
import com.huawei.hms.mlsdk.model.download.MLModelDownloadStrategy;
import com.huawei.hms.mlsdk.text.MLLocalTextSetting;
import com.huawei.hms.mlsdk.text.MLText;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;
import com.huawei.hms.mlsdk.translate.MLTranslateLanguage;
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory;
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslateSetting;
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslator;

import java.io.IOException;
import java.util.Set;


public class TextDetectionFragment extends Fragment {

    private MLTextAnalyzer analyzer;
    private Bitmap bitmap;
    private MLLocalTranslator mlLocalTranslator;
    private TextView detectedTextTv;
    private TextView translatedTextTv;
    private Button translateTextBtn;
    private final String TAG = "TextDetectionFragment";

    public TextDetectionFragment() {
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
        return inflater.inflate(R.layout.fragment_text_detection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.poemimage);
        ImageView myImage = requireView().findViewById(R.id.detected_text_iv);
        myImage.setImageBitmap(bitmap);

        Button detectTextBtn = requireView().findViewById(R.id.detect_btn);
        translateTextBtn = requireView().findViewById(R.id.translate_btn);
        detectedTextTv = requireView().findViewById(R.id.detected_text_tv);
        translatedTextTv = requireView().findViewById(R.id.translated_text_tv);

        createTextAnalyzer(); // For text recognition

        queryLanguagesSupported(); // This method returns the language codes supported (e.g. en, tr) in the logs for on-device translation

        createOfflineTranslator(); // For on-device translation
        downloadTranslationModel(); // For on-device translation

        detectTextBtn.setOnClickListener(view12 -> {
            // If detected text TextView is empty, detectText() function will be called:
            if (detectedTextTv.getText().toString().equals("")) {
                detectText();
            }
        });
    }

    private void createTextAnalyzer() {
        // Use the customized parameter MLLocalTextSetting to configure the text analyzer on the device.
        MLLocalTextSetting setting = new MLLocalTextSetting.Factory()
                .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
                // Specify languages that can be recognized.
                .setLanguage("en")
                .create();
        analyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting);
    }

    private void detectText() {
        // Create an MLFrame object using the bitmap, which is the image data in bitmap format.
        MLFrame frame = MLFrame.fromBitmap(bitmap);
        Task<MLText> task = analyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(text -> {
            detectedTextTv.setText(text.getStringValue());
            releaseTextRecognitionResources();
        }).addOnFailureListener(e -> Toast.makeText(requireActivity().getApplicationContext(), "Detection failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    private void createOfflineTranslator() {
        // Create an offline translator.
        MLLocalTranslateSetting setting = new MLLocalTranslateSetting.Factory()
                // Set the source language code, which complies with the ISO 639-1 standard. This parameter is mandatory. If this parameter is not set, an error may occur.
                .setSourceLangCode("en")
                // Set the target language code, which complies with the ISO 639-1 standard. This parameter is mandatory. If this parameter is not set, an error may occur.
                .setTargetLangCode("tr")
                .create();
        mlLocalTranslator = MLTranslatorFactory.getInstance().getLocalTranslator(setting);
    }

    private void queryLanguagesSupported() {
        // Sample code for calling the synchronous method.
        Set<String> result = MLTranslateLanguage.syncGetLocalAllLanguages();
        Log.i(TAG, "Supported languages for translation: " + result.toString());
    }

    private void downloadTranslationModel() {

        // Set the model download policy.
        MLModelDownloadStrategy downloadStrategy = new MLModelDownloadStrategy.Factory()
                .needWifi() // It is recommended that you download the package in a Wi-Fi environment.
                .create();
        // Create a download progress listener.
        MLModelDownloadListener modelDownloadListener = (alreadyDownLength, totalLength) -> requireActivity().runOnUiThread(() -> {
            // Display the download progress or perform other operations if you need.
        });
        mlLocalTranslator.preparedModel(downloadStrategy, modelDownloadListener).
                addOnSuccessListener(aVoid -> {
                    // Called when the model package is successfully downloaded.
                    Log.i(TAG, "Translation model package has been downloaded successfully.");
                    translateTextBtn.setOnClickListener(view1 -> translate(detectedTextTv.getText().toString()));
                }).addOnFailureListener(e -> {
            // Called when the model package fails to be downloaded.
            Log.e(TAG, "Translation model download failed: " + e.getLocalizedMessage());
        });
    }

    private void translate(String input) {
        try {
            // input is a string of less than 5000 characters.
            String output = mlLocalTranslator.syncTranslate(input);
            // Processing logic for detection success.
            translatedTextTv.setText(output);
            // After translation is completed, release the detection resources:
            releaseTranslationResources();
        } catch (MLException e) {
            // Processing logic for detection failure.
            Toast.makeText(requireActivity().getApplicationContext(), "Translation failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void releaseTextRecognitionResources() {
        if (analyzer != null) {
            try {
                analyzer.stop();
            } catch (IOException e) {
                // Exception handling.
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    private void releaseTranslationResources() {
        if (mlLocalTranslator != null) {
            mlLocalTranslator.stop();
        }
    }
}