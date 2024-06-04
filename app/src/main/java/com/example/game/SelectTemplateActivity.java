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
    private List<String> topics = new ArrayList<>();

    private Bitmap capturedImage;

    private Button addPageButton;
    private Button captureImageButton;
    private Button countMarksButton;
    private Button finalizeButton;
    Button nextTopicButton;

    private EditText numberOfPagesEditText;
    private EditText studentNameEditText;
    private EditText topicNameEditText;

    private ImageView resultImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selecttemplate_activity);

        OpenCVLoader.initDebug();

        resultImageView = findViewById(R.id.resultImageView);
        numberOfPagesEditText = findViewById(R.id.numberOfPagesEditText);
        studentNameEditText = findViewById(R.id.studentNameEditText);
        topicNameEditText = findViewById(R.id.topicNameEditText);


        nextTopicButton = findViewById(R.id.nextTopicButton);
        addPageButton = findViewById(R.id.addPageButton);
        captureImageButton = findViewById(R.id.captureImageButton);
        countMarksButton = findViewById(R.id.countMarksButton);
        finalizeButton = findViewById(R.id.finalizeButton);



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
                topics.clear();
                Toast.makeText(SelectTemplateActivity.this, "Number of pages set to " + numberOfPages, Toast.LENGTH_SHORT).show();
            }
        });


        nextTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topicName = topicNameEditText.getText().toString().trim();
                if (topicName.isEmpty()) {
                    Toast.makeText(SelectTemplateActivity.this, "Enter topic name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add the topic to the list
                topics.add(topicName);
                topicNameEditText.setText("");
                Toast.makeText(SelectTemplateActivity.this, "Topic added: " + topicName, Toast.LENGTH_SHORT).show();
            }
        });


        finalizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topicName = topicNameEditText.getText().toString().trim();
                // Add the topic to the list
                topics.add(topicName);
                topicNameEditText.setText("");

                Toast.makeText(SelectTemplateActivity.this, "List of topics finalized, Capture all pages of exam paper", Toast.LENGTH_SHORT).show();
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






    private void createExcelFile(String studentName, Map<String, Integer> totalMarksByTopic) {
        try {
            File outputFile = new File(getExternalFilesDir(null), studentName + ".xls");
            WritableWorkbook workbook = Workbook.createWorkbook(outputFile);

            WritableSheet sheet = workbook.createSheet("Marks", 0);

            sheet.addCell(new Label(0, 0, "Topic"));
            sheet.addCell(new Label(1, 0, "Marks"));

            int row = 1;
            for (Map.Entry<String, Integer> entry : totalMarksByTopic.entrySet()) {
                String topic = entry.getKey();
                int marks = entry.getValue();
                sheet.addCell(new Label(0, row, topic));
                sheet.addCell(new jxl.write.Number(1, row, marks));
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
        Map<String, Integer> marksByTopic = new HashMap<>();

        // Initialize marks for all topics to 0
        for (String topic : topics) {
            marksByTopic.put(topic, 0);
        }

        for (Bitmap image : capturedImages) {
            if (image == null) {
                Log.e("countMarksInPage", "Null bitmap detected");
                continue;
            }

            Mat imageMat = new Mat();
            Utils.bitmapToMat(image, imageMat);

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

            // Group bounding boxes that are close to each other and keep the number of bounding boxes in each group
            class BoundingBoxGroup {
                List<Rect> boundingBoxes;
                int count;

                BoundingBoxGroup(List<Rect> boundingBoxes) {
                    this.boundingBoxes = boundingBoxes;
                    this.count = boundingBoxes.size();
                }
                int getCount() {
                    return count;
                }
            }

            List<BoundingBoxGroup> groupedBoundingBoxes = new ArrayList<>();
            boolean[] visited = new boolean[boundingBoxes.size()];

            for (int i = 0; i < boundingBoxes.size(); i++) {
                if (visited[i]) continue;

                List<Rect> group = new ArrayList<>();
                group.add(boundingBoxes.get(i));
                visited[i] = true;

                for (int j = 0; j < boundingBoxes.size(); j++) {
                    if (i == j || visited[j]) continue;

                    if (Math.abs(boundingBoxes.get(i).x - boundingBoxes.get(j).x) < 10) {
                        group.add(boundingBoxes.get(j));
                        visited[j] = true;
                    }
                }

                groupedBoundingBoxes.add(new BoundingBoxGroup(group));
            }

            // Draw grouped bounding boxes
            for (BoundingBoxGroup group : groupedBoundingBoxes) {
                for (Rect boundingBox : group.boundingBoxes) {
                    Imgproc.rectangle(resultMat, boundingBox.tl(), boundingBox.br(), new Scalar(255, 0, 0), 2); // Draw red rectangle
                }
            }

            // Convert the resultMat to bitmap
            Bitmap resultBitmapWithRectangle = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(resultMat, resultBitmapWithRectangle);

            // Display the bitmap with rectangles
            resultImageView.setImageBitmap(resultBitmapWithRectangle);
            resultImageView.setVisibility(View.VISIBLE);

            // Sort bounding boxes based on their y-coordinate from smallest to biggest
            boundingBoxes.sort(Comparator.comparingInt(r -> r.y));

            // Initialize the count and index
            int index = 0;
            int count;

            // Loop through pages and process bounding boxes
            for (int page = 1; page <= numberOfPages; page++) {
                for (int i = 0; i < boundingBoxes.size(); i += count) {
                    count = 0;

                    // Count bounding boxes with y-coordinate difference less than 10 from the current bounding box
                    for (int j = i; j < boundingBoxes.size(); j++) {
                        if (Math.abs(boundingBoxes.get(i).y - boundingBoxes.get(j).y) < 10) {
                            count++;
                        } else {
                            break;
                        }
                    }

                    // Update marks for the current topic
                    if (index < topics.size()) {
                        String topic = topics.get(index);
                        marksByTopic.put(topic, marksByTopic.get(topic) + count );
                        index++;
                    }
                }
            }
        }

        // Log the marks by topic
        for (Map.Entry<String, Integer> entry : marksByTopic.entrySet()) {
            Log.d("MarksByTopic", "Topic: " + entry.getKey() + ", Marks: " + entry.getValue());
        }

        String studentName = studentNameEditText.getText().toString().trim();
        if (!studentName.isEmpty()) {
            createExcelFile(studentName, marksByTopic);
        } else {
            Toast.makeText(this, "Enter student name to create Excel file", Toast.LENGTH_SHORT).show();
        }
    }


    // Utility function to find the union of two rectangles
    private static Rect union(Rect r1, Rect r2) {
        int x = Math.min(r1.x, r2.x);
        int y = Math.min(r1.y, r2.y);
        int width = Math.max(r1.x + r1.width, r2.x + r2.width) - x;
        int height = Math.max(r1.y + r1.height, r2.y + r2.height) - y;
        return new Rect(x, y, width, height);
    }

    // Utility function to find the topic associated with a question number
    private String findTopicForQuestion(int questionNumber) {
        if (questionNumber - 1 < topics.size()) {
            return topics.get(questionNumber - 1);
        }
        return null;
    }
}







