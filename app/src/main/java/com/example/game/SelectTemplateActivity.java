package com.example.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.game.R;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectTemplateActivity extends Activity {

    private static final int REQUEST_SELECT_TEMPLATE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private int numberOfPages;
    private int currentPage = 1;
    private List<Map<String, Object>> templatesList = new ArrayList<>();
    private List<Bitmap> capturedImages = new ArrayList<>();
    private Map<String, Integer> marksByTopic = new HashMap<>();

    private Bitmap selectedTemplate;
    private Bitmap capturedImage;

    private Button addPageButton;
    private Button selectTemplatesButton;
    private Button captureImageButton;
    private Button countMarksButton;

    private EditText numberOfPagesEditText;
    private EditText pageNumberEditText;
    private EditText topicEditText;
    private ImageView resultImageView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selecttemplate_activity);

        OpenCVLoader.initDebug();

        numberOfPagesEditText = findViewById(R.id.numberOfPagesEditText);
        pageNumberEditText = findViewById(R.id.pageNumberEditText);
        topicEditText = findViewById(R.id.topicEditText);

        addPageButton = findViewById(R.id.addPageButton);
        selectTemplatesButton = findViewById(R.id.selectTemplatesButton);
        captureImageButton = findViewById(R.id.captureImageButton);
        countMarksButton = findViewById(R.id.countMarksButton);


        resultImageView = findViewById(R.id.resultImageView);

        addPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOfPagesEditText.getText().toString().isEmpty()) {
                    Toast.makeText(SelectTemplateActivity.this, "Enter the number of pages", Toast.LENGTH_SHORT).show();
                    return;
                }
                numberOfPages = Integer.parseInt(numberOfPagesEditText.getText().toString());
                currentPage = 1;
                templatesList.clear();
                capturedImages.clear();
                marksByTopic.clear();
                Toast.makeText(SelectTemplateActivity.this, "Number of pages set to " + numberOfPages, Toast.LENGTH_SHORT).show();
            }
        });

        selectTemplatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pageNumberEditText.getText().toString().isEmpty() || topicEditText.getText().toString().isEmpty()) {
                    Toast.makeText(SelectTemplateActivity.this, "Enter page number and topic", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_SELECT_TEMPLATE);
            }
        });

        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage > numberOfPages) {
                    Toast.makeText(SelectTemplateActivity.this, "All pages captured", Toast.LENGTH_SHORT).show();
                    return;
                }
                dispatchTakePictureIntent();
            }
        });

        countMarksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (capturedImages.size() < numberOfPages) {
                    Toast.makeText(SelectTemplateActivity.this, "Capture all pages first", Toast.LENGTH_SHORT).show();
                    return;
                }
                countMarksForAllPages();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SELECT_TEMPLATE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            selectedTemplate = getBitmapFromUri(uri);
            if (selectedTemplate != null) {
                String topic = topicEditText.getText().toString();
                int pageNumber = Integer.parseInt(pageNumberEditText.getText().toString());
                Map<String, Object> templateData = new HashMap<>();
                templateData.put("template", selectedTemplate);
                templateData.put("topic", topic);
                templateData.put("page", pageNumber);
                templatesList.add(templateData);
                Toast.makeText(this, "Template and topic added for page " + pageNumber, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to load template image", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.containsKey("data")) {
                capturedImage = (Bitmap) extras.get("data");
                capturedImages.add(capturedImage);
                currentPage++;
                Toast.makeText(this, "Image captured for page " + (currentPage - 1), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void countMarksForAllPages() {
        for (int pageNumber = 1; pageNumber <= numberOfPages; pageNumber++) {
            Bitmap capturedPage = capturedImages.get(pageNumber - 1);
            for (Map<String, Object> templateData : templatesList) {
                int templatePage = (int) templateData.get("page");
                if (templatePage == pageNumber) {
                    Bitmap template = (Bitmap) templateData.get("template");
                    String topic = (String) templateData.get("topic");
                    int marks = countMarksSingleTemplate(capturedPage, template);
                    marksByTopic.put(topic, marksByTopic.getOrDefault(topic, 0) + marks);
                }
            }
        }
        displayMarksByTopic();
    }

    private int countMarksSingleTemplate(Bitmap photoBitmap, Bitmap templateBitmap) {
        if (photoBitmap == null || templateBitmap == null) {
            Log.e("findTemplateAndDisplayInCapturedPhoto", "Null bitmap detected");
            return 0;
        }

        Mat imageMat = new Mat();
        Mat templateMat = new Mat();
        Utils.bitmapToMat(photoBitmap, imageMat);
        Utils.bitmapToMat(templateBitmap, templateMat);

        float captureRatio = (float) templateBitmap.getWidth() / photoBitmap.getWidth();

        int newWidth = imageMat.cols();
        int newHeight = (int) (templateBitmap.getHeight() / captureRatio);
        Mat resizedTemplateMat = new Mat();
        Imgproc.resize(templateMat, resizedTemplateMat, new Size(newWidth, newHeight));

        Log.d("Template Matching", "Image cols: " + imageMat.cols() + ", rows: " + imageMat.rows());
        Log.d("Template Matching", "Template cols: " + resizedTemplateMat.cols() + ", rows: " + resizedTemplateMat.rows());

        Mat result = new Mat();
        Imgproc.matchTemplate(imageMat, resizedTemplateMat, result, Imgproc.TM_CCOEFF_NORMED);

        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc = mmr.maxLoc;

        int templateWidth = resizedTemplateMat.cols();
        int templateHeight = resizedTemplateMat.rows();
        Rect roi = new Rect((int) matchLoc.x, (int) matchLoc.y, templateWidth, templateHeight);
        Mat roiMat = new Mat(imageMat, roi);

        Mat binaryRoiMat = new Mat(roiMat.size(), CvType.CV_8UC1);
        for (int y = 0; y < roiMat.rows(); y++) {
            for (int x = 0; x < roiMat.cols(); x++) {
                double[] pixel = roiMat.get(y, x);
                double blue = pixel[0], green = pixel[1], red = pixel[2];
                if (Math.abs(red - green) > 40 || Math.abs(red - blue) > 40 || Math.abs(green - blue) > 40) {
                    binaryRoiMat.put(y, x, 255);
                } else {
                    binaryRoiMat.put(y, x, 0);
                }
            }
        }

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binaryRoiMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        int thickLineCount = 0;
        for (MatOfPoint contour : contours) {
            Rect boundingBox = Imgproc.boundingRect(contour);
            double aspectRatio = (double) boundingBox.width / boundingBox.height;
            double area = Imgproc.contourArea(contour);
            if (aspectRatio < 0.8 && area > 15) {
                thickLineCount++;
                Imgproc.drawContours(roiMat, Collections.singletonList(contour), -1, new Scalar(255, 0, 0), 2);
            }
        }

        Log.d("Template Matching", "Number of thick lines: " + thickLineCount);

        Bitmap resultBitmapWithRectangle = Bitmap.createBitmap(roiMat.cols(), roiMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(roiMat, resultBitmapWithRectangle);
        resultImageView.setImageBitmap(resultBitmapWithRectangle);
        resultImageView.setVisibility(View.VISIBLE);

        return thickLineCount;
    }

    private void displayMarksByTopic() {
        StringBuilder marksDisplay = new StringBuilder();
        for (Map.Entry<String, Integer> entry : marksByTopic.entrySet()) {
            marksDisplay.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        Toast.makeText(this, marksDisplay.toString(), Toast.LENGTH_LONG).show();
    }
}
