package com.example.game;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Scalar;
import java.util.ArrayList;
import java.util.List;

public class ProcessExamPapersActivity extends Activity {
    private Button countMarksButton;
    private Bitmap photoBitmap;
    private ArrayList<Bitmap> templateBitmaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_exam_papers);

        // Retrieve the photo bitmap from the intent extras
        photoBitmap = getIntent().getParcelableExtra("photoBitmap");
        if (photoBitmap == null) {
            Toast.makeText(this, "Failed to retrieve photo bitmap", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Retrieve the selected templates from the Intent extras
        templateBitmaps = getIntent().getParcelableArrayListExtra("selectedTemplates");
        if (templateBitmaps == null || templateBitmaps.isEmpty()) {
            Toast.makeText(this, "No templates selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        countMarksButton = findViewById(R.id.countMarksButton);
        countMarksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int totalMarks = 0;
                for (Bitmap templateBitmap : templateBitmaps) {
                    int markCount = countMarks(photoBitmap, templateBitmap);
                    totalMarks += markCount;
                }
                Toast.makeText(ProcessExamPapersActivity.this, "Total marks: " + totalMarks, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public int countMarks(Bitmap imageBitmap, Bitmap templateBitmap) {
        // Convert Bitmaps to Mats
        Mat imageMat = new Mat();
        Mat templateMat = new Mat();
        Utils.bitmapToMat(imageBitmap, imageMat);
        Utils.bitmapToMat(templateBitmap, templateMat);

        // Convert images to grayscale
        Mat grayImage = new Mat();
        Mat grayTemplate = new Mat();
        Imgproc.cvtColor(imageMat, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(templateMat, grayTemplate, Imgproc.COLOR_BGR2GRAY);

        // Use template matching
        Mat result = new Mat();
        Imgproc.matchTemplate(grayImage, grayTemplate, result, Imgproc.TM_CCOEFF_NORMED);

        // Define threshold
        double threshold = 0.8;
        Mat mask = new Mat();
        Core.compare(result, new Scalar(threshold), mask, Core.CMP_GT);

        // Count the number of matches
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        return contours.size(); // Number of marks detected
    }
}
