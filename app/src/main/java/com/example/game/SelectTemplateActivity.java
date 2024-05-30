package com.example.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
    private Map<Integer, List<Map<String, Object>>> templatesMap = new HashMap<>();
    private List<Bitmap> capturedImages = new ArrayList<>();

    private List<Map<String, Object>> templatesList = new ArrayList<>();

    private Map<String, Integer> marksByTopic = new HashMap<>();

    private Bitmap selectedTemplate;
    private Bitmap capturedImage;

    private Button addPageButton;

    private Button setPageNumberButton;
    private Button setNumberOfQuestionsButton;
    private Button setTopicAndTemplateButton;


    private Button selectTemplatesButton;
    private Button captureImageButton;
    private Button countMarksButton;
    private EditText numberOfQuestionsPerPageEditText;
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
        numberOfQuestionsPerPageEditText = findViewById(R.id.numberOfQuestionsPerPageEditText);
        pageNumberEditText = findViewById(R.id.pageNumberEditText);
        topicEditText = findViewById(R.id.topicEditText);

        addPageButton = findViewById(R.id.addPageButton);
        selectTemplatesButton = findViewById(R.id.selectTemplatesButton);
        captureImageButton = findViewById(R.id.captureImageButton);
        countMarksButton = findViewById(R.id.countMarksButton);
        setNumberOfQuestionsButton = findViewById(R.id.setNumberOfQuestionsButton);
        setPageNumberButton = findViewById(R.id.setPageNumberButton);
        setTopicAndTemplateButton = findViewById(R.id.setTopicAndTemplateButton);

        resultImageView = findViewById(R.id.resultImageView);

        addPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOfPagesEditText.getText().toString().isEmpty()) {
                    Toast.makeText(SelectTemplateActivity.this, "Enter the number of pages", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the number of questions for the current page
                //int numberOfQuestions = Integer.parseInt(numberOfQuestionsPerPageEditText.getText().toString());
                Toast.makeText(SelectTemplateActivity.this, "Enter page number", Toast.LENGTH_SHORT).show();

                numberOfPages = Integer.parseInt(numberOfPagesEditText.getText().toString());
                currentPage = 1;
                templatesMap.clear();
                capturedImages.clear();
                marksByTopic.clear();
                //Toast.makeText(SelectTemplateActivity.this, "Number of pages set to " + numberOfPages, Toast.LENGTH_SHORT).show();
            }
        });

        selectTemplatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pageNumberEditText.getText().toString().isEmpty() || topicEditText.getText().toString().isEmpty() || numberOfQuestionsPerPageEditText.getText().toString().isEmpty()) {
                    Toast.makeText(SelectTemplateActivity.this, "Enter page number, topic, and number of questions per page", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get page number and topic from the EditText fields
                final int pageNumber = Integer.parseInt(pageNumberEditText.getText().toString());
                String topic = topicEditText.getText().toString();

                // Start the intent to select a template image
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_SELECT_TEMPLATE);
            }
        });


        setPageNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SelectTemplateActivity.this, "Enter number of questions in current page", Toast.LENGTH_SHORT).show();
            }
        });


        setNumberOfQuestionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SelectTemplateActivity.this, "Enter topic and select template for current question", Toast.LENGTH_SHORT).show();
            }
        });





        setTopicAndTemplateButton.setOnClickListener(new View.OnClickListener() {
            private int remainingQuestions; // Track remaining questions per page
            private int currentQuestionIndex = 0; // Track the index of the current question

            @Override
            public void onClick(View v) {
                // Initialize remainingQuestions if not already initialized
                if (remainingQuestions == 0) {
                    remainingQuestions = Integer.parseInt(numberOfQuestionsPerPageEditText.getText().toString());
                }

                // Check if topic is not entered
                if (topicEditText.getText().toString().isEmpty()) {
                    Toast.makeText(SelectTemplateActivity.this, "Topic not entered for current question", Toast.LENGTH_LONG).show();
                    return; // Ensure no further execution
                }

                // Check if template is not selected for the current question on the current page
                int currentPageNumber = Integer.parseInt(pageNumberEditText.getText().toString());
                List<Map<String, Object>> templatesForCurrentPage = templatesMap.get(currentPageNumber);

                if (templatesForCurrentPage == null || currentQuestionIndex >= templatesForCurrentPage.size() || templatesForCurrentPage.get(currentQuestionIndex).isEmpty()) {
                    Toast.makeText(SelectTemplateActivity.this, "Template not selected for current question", Toast.LENGTH_LONG).show();
                    return; // Ensure no further execution
                }

                // Decrement the number of questions on the current page
                remainingQuestions--;
                currentQuestionIndex++; // Move to the next question

                if (remainingQuestions > 0) {
                    // Show toast for each question until the last one
                    Toast.makeText(SelectTemplateActivity.this, "Saved successfully! Enter topic and select template for next question.", Toast.LENGTH_SHORT).show();
                } else if (remainingQuestions == 0) {
                    // Check if the current page is the last page
                    if (pageNumberEditText.getText().toString().equals(numberOfPagesEditText.getText().toString())) {
                        // Show toast to capture all pages of the exam paper
                        Toast.makeText(SelectTemplateActivity.this, "Capture all pages of exam paper", Toast.LENGTH_SHORT).show();
                    } else {
                        // Show toast to enter the next page number when the last question is reached
                        Toast.makeText(SelectTemplateActivity.this, "Enter next page number", Toast.LENGTH_SHORT).show();
                    }
                    // Reset for the next page
                    remainingQuestions = 0; // Reset remainingQuestions
                    currentQuestionIndex = 0; // Reset question index
                    numberOfQuestionsPerPageEditText.setText(""); // Clear the number of questions EditText
                }

                // Update the number of questions EditText with the decremented value if not zero
                if (remainingQuestions > 0) {
                    numberOfQuestionsPerPageEditText.setText(String.valueOf(remainingQuestions));
                }

                // Clear the topic EditText
                topicEditText.setText("");
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

                if (!templatesMap.containsKey(pageNumber)) {
                    templatesMap.put(pageNumber, new ArrayList<>());
                }
                templatesMap.get(pageNumber).add(templateData);

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
            List<Map<String, Object>> templatesForPage = templatesMap.get(pageNumber);
            if (templatesForPage != null) {
                for (Map<String, Object> templateData : templatesForPage) {
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
            Log.e("countMarksSingleTemplate", "Null bitmap detected");
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

        Log.d("countMarksSingleTemplate", "Number of thick lines: " + thickLineCount);

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
