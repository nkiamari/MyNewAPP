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

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class SelectTemplateActivity extends Activity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private int numberOfPages;
    private int currentPage = 1;
    private List<Bitmap> capturedImages = new ArrayList<>();
    private Map<String, List<Integer>> topicsWithQuestions = new HashMap<>();

    private Map<String, Integer> marksByTopic = new HashMap<>();


    private Bitmap capturedImage;

    private Button addPageButton;
    private Button captureImageButton;
    private Button countMarksButton;
    private Button setTopicAndQuestionButton;

    private EditText numberOfPagesEditText;
    private EditText studentNameEditText;
    private EditText topicNameEditText;
    private EditText questionNumbersEditText;

    private ImageView resultImageView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selecttemplate_activity);

        OpenCVLoader.initDebug();

        resultImageView = findViewById(R.id.resultImageView);
        numberOfPagesEditText = findViewById(R.id.numberOfPagesEditText);
        studentNameEditText = findViewById(R.id.studentNameEditText);
        topicNameEditText = findViewById(R.id.topicNameEditText);
        questionNumbersEditText = findViewById(R.id.questionNumbersEditText);

        addPageButton = findViewById(R.id.addPageButton);
        captureImageButton = findViewById(R.id.captureImageButton);
        countMarksButton = findViewById(R.id.countMarksButton);
        setTopicAndQuestionButton = findViewById(R.id.setTopicAndQuestionButton); // Corrected button ID here

        addPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numberOfPagesStr = numberOfPagesEditText.getText().toString().trim();
                Log.d("DEBUG", "Number of Pages: '" + numberOfPagesStr + "'");
                if (numberOfPagesStr.isEmpty()) {
                    Toast.makeText(SelectTemplateActivity.this, "Enter the number of pages", Toast.LENGTH_SHORT).show();
                    return;
                }

                numberOfPages = Integer.parseInt(numberOfPagesStr);
                currentPage = 1;
                capturedImages.clear();
                topicsWithQuestions.clear(); // Clear topics and related questions
                Toast.makeText(SelectTemplateActivity.this, "Number of pages set to " + numberOfPages, Toast.LENGTH_SHORT).show();
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

        setTopicAndQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topicName = topicNameEditText.getText().toString().trim();
                String questionNumbersText = questionNumbersEditText.getText().toString().trim();

                if (topicName.isEmpty()) {
                    Toast.makeText(SelectTemplateActivity.this, "Enter topic name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (questionNumbersText.isEmpty()) {
                    Toast.makeText(SelectTemplateActivity.this, "Enter question numbers", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] questionNumbers = questionNumbersText.split(",");
                List<Integer> questions = new ArrayList<>();
                for (String number : questionNumbers) {
                    try {
                        int questionNumber = Integer.parseInt(number.trim());
                        questions.add(questionNumber);
                    } catch (NumberFormatException e) {
                        Toast.makeText(SelectTemplateActivity.this, "Invalid question number: " + number, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Add the topic and its related question numbers to the map
                topicsWithQuestions.put(topicName, questions);

                // Clear input fields after adding the topic
                topicNameEditText.setText("");
                questionNumbersEditText.setText("");

                Toast.makeText(SelectTemplateActivity.this, "Topic added: " + topicName, Toast.LENGTH_SHORT).show();
            }
        });
    }





    private void createExcelFile(String studentName) {
        try {
            File outputFile = new File(getExternalFilesDir(null), studentName + ".xls");
            WritableWorkbook workbook = Workbook.createWorkbook(outputFile);

            WritableSheet sheet = workbook.createSheet("Marks", 0);

            sheet.addCell(new Label(0, 0, "Topic"));
            sheet.addCell(new Label(1, 0, "Total Marks"));

            int row = 1;
            for (Map.Entry<String, Integer> entry : marksByTopic.entrySet()) {
                sheet.addCell(new Label(0, row, entry.getKey()));
                sheet.addCell(new jxl.write.Number(1, row, entry.getValue()));
                row++;
            }

            workbook.write();
            workbook.close();

            Toast.makeText(this, "Excel file created for " + studentName, Toast.LENGTH_SHORT).show();

        } catch (IOException | WriteException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create Excel file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
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
        Map<String, Integer> topicMarks = new HashMap<>();
        for (String topic : topicsWithQuestions.keySet()) {
            topicMarks.put(topic, 0);
        }

        int questionIndex = 0; // Index for tracking the current question
        int pageCounter = 0; // Counter for tracking the current page
        int currentPageMarks = 0; // Marks counted in the current page

        for (int pageNumber = 1; pageNumber <= numberOfPages; pageNumber++) {
            Bitmap capturedPage = capturedImages.get(pageNumber - 1);
            Map<Integer, Integer> pageMarks = countMarksInPage(capturedPage);

            for (Map.Entry<String, List<Integer>> entry : topicsWithQuestions.entrySet()) {
                String topic = entry.getKey();
                List<Integer> questionNumbers = entry.getValue();

                for (Integer questionNumber : questionNumbers) {
                    // Skip questions until reaching the current question index
                    if (questionIndex > 0) {
                        questionIndex--;
                        continue;
                    }

                    // If marks for the current page's questions have been counted, move to the next page
                    if (pageCounter > 0) {
                        pageCounter--;
                        continue;
                    }

                    if (pageMarks.containsKey(questionNumber)) {
                        int marks = pageMarks.get(questionNumber);
                        topicMarks.put(topic, topicMarks.get(topic) + marks);
                        currentPageMarks += marks;
                        questionIndex++; // Move to the next question
                    }

                    // If marks for all questions in the current page are counted, move to the next page
                    if (questionIndex >= questionNumbers.size()) {
                        pageCounter = 1; // Set the counter for the next page
                        break; // Move to the next page
                    }
                }
            }

            // If marks for the current page's questions have been counted, reset the counter
            if (currentPageMarks > 0) {
                currentPageMarks = 0;
            } else {
                // If no marks counted in the current page, no questions found, move to the next page
                pageCounter = 1;
            }
        }

        marksByTopic = topicMarks;
        displayMarksByTopic();
        createExcelFile(studentNameEditText.getText().toString());
    }


    private Map<Integer, Integer> countMarksInPage(Bitmap capturedPage) {
        Map<Integer, Integer> marksByQuestion = new HashMap<>();

        if (capturedPage == null) {
            Log.e("countMarksInPage", "Null bitmap detected");
            return marksByQuestion;
        }

        Mat imageMat = new Mat();
        Utils.bitmapToMat(capturedPage, imageMat);

        // Create a binary mask based on color differences
        Mat binaryMat = new Mat(imageMat.size(), CvType.CV_8UC1);
        for (int y = 0; y < imageMat.rows(); y++) {
            for (int x = 0; x < imageMat.cols(); x++) {
                double[] pixel = imageMat.get(y, x);
                double blue = pixel[0], green = pixel[1], red = pixel[2];
                if (Math.abs(red - green) > 50 || Math.abs(red - blue) > 50 || Math.abs(green - blue) > 50) {
                    binaryMat.put(y, x, 255);
                } else {
                    binaryMat.put(y, x, 0);
                }
            }
        }

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binaryMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Create an image for drawing bounding boxes
        Mat resultMat = new Mat(imageMat.size(), CvType.CV_8UC3, new Scalar(255, 255, 255)); // Create a white image

        // Filter contours based on aspect ratio and area, then draw bounding boxes
        List<Rect> boundingBoxes = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect boundingBox = Imgproc.boundingRect(contour);
            double aspectRatio = (double) boundingBox.width / boundingBox.height;
            double area = Imgproc.contourArea(contour);
            if (aspectRatio < 0.8 && area > 15) {
                boundingBoxes.add(boundingBox);
                Imgproc.rectangle(resultMat, boundingBox.tl(), boundingBox.br(), new Scalar(0, 0, 0), 2); // Draw black rectangle
            }
        }

        // Convert the resultMat to bitmap
        Bitmap resultBitmapWithRectangle = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resultMat, resultBitmapWithRectangle);

        // Display the bitmap with rectangles
        resultImageView.setImageBitmap(resultBitmapWithRectangle);
        resultImageView.setVisibility(View.VISIBLE);

        // Group bounding boxes that are close to each other
        List<Rect> groupedBoundingBoxes = new ArrayList<>();
        boolean[] visited = new boolean[boundingBoxes.size()];

        for (int i = 0; i < boundingBoxes.size(); i++) {
            if (visited[i]) continue;

            Rect boxA = boundingBoxes.get(i);
            Rect combinedBox = new Rect(boxA.tl(), boxA.br()); // Initialize with the same coordinates
            visited[i] = true;

            for (int j = 0; j < boundingBoxes.size(); j++) {
                if (i == j || visited[j]) continue;

                Rect boxB = boundingBoxes.get(j);
                int distance = (int) Math.sqrt(Math.pow((boxA.x + boxA.width / 2) - (boxB.x + boxB.width / 2), 2) +
                        Math.pow((boxA.y + boxA.height / 2) - (boxB.y + boxB.height / 2), 2));

                if (distance < 10) {
                    combinedBox = union(combinedBox, boxB);
                    visited[j] = true;
                }
            }
            groupedBoundingBoxes.add(combinedBox);
        }

        // Sort grouped bounding boxes by their y-coordinate
        Collections.sort(groupedBoundingBoxes, new Comparator<Rect>() {
            @Override
            public int compare(Rect r1, Rect r2) {
                return Integer.compare(r1.y, r2.y);
            }
        });

        // Count thick lines inside each new bounding box and assign to questions
        for (Rect boundingBox : groupedBoundingBoxes) {
            int count = 0;
            for (Rect box : boundingBoxes) {
                if (boundingBox.contains(box.tl()) && boundingBox.contains(box.br())) {
                    count++;
                }
            }

            // Assuming question numbers are represented by the y-coordinate of the bounding box
            marksByQuestion.put(boundingBox.y, count);
        }

        return marksByQuestion;
    }

    // Utility function to find the union of two rectangles
    private static Rect union(Rect r1, Rect r2) {
        int x = Math.min(r1.x, r2.x);
        int y = Math.min(r1.y, r2.y);
        int width = Math.max(r1.x + r1.width, r2.x + r2.width) - x;
        int height = Math.max(r1.y + r1.height, r2.y + r2.height) - y;
        return new Rect(x, y, width, height);
    }

    private void displayMarksByTopic() {
        StringBuilder marksSummary = new StringBuilder();
        for (Map.Entry<String, Integer> entry : marksByTopic.entrySet()) {
            marksSummary.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        Toast.makeText(this, marksSummary.toString(), Toast.LENGTH_LONG).show();
    }



}

