package com.example.mlkitdemobau.facedetection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.SparseArray;
import android.view.SurfaceHolder;

import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.common.MLPosition;
import com.huawei.hms.mlsdk.face.MLFace;

import java.util.ArrayList;
import java.util.List;

public class FaceAnalyzerTransactor implements MLAnalyzer.MLTransactor<MLFace> {
    private SurfaceHolder mOverlay;

    public void setOverlay(SurfaceHolder surfaceHolder) {
        mOverlay = surfaceHolder;
    }

    @Override
    public void transactResult(MLAnalyzer.Result<MLFace> result) {
        draw(result.getAnalyseList());
    }

    // draw() method is used for drawing yellow face lines and "SMILING","NOT SMILING" strings on the surface overlay
    private void draw(SparseArray<MLFace> faces) {
        Canvas canvas = mOverlay.lockCanvas();

        if (canvas != null && faces != null) {

            //Clear the canvas
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);

            for (MLFace face : asList(faces)) {

                //Draw all 855 points of the face. If Front Lens is selected, change x points side.
                for (MLPosition point : face.getAllPoints()) {
                    float x = mOverlay.getSurfaceFrame().right - point.getX();
                    Paint paint = new Paint();
                    paint.setColor(Color.YELLOW);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setStrokeWidth(16F);
                    canvas.drawPoint(x, point.getY(), paint);
                }

                //Prepare a string to show if the user smiles or not and draw a text on the canvas.
                String smilingString;
                if (face.getEmotions().getSmilingProbability() > 0.5) {
                    smilingString = "SMILING";
                } else {
                    smilingString = "NOT SMILING";
                }

                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setTextSize(60F);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(smilingString, face.getBorder().exactCenterX(), face.getBorder().exactCenterY(), paint);

            }

            mOverlay.unlockCanvasAndPost(canvas);
        }

    }

    public static <T> List<T> asList(SparseArray<T> sparseArray) {
        if (sparseArray == null) return null;
        List<T> arrayList = new ArrayList<>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }

    @Override
    public void destroy() {

    }


}
