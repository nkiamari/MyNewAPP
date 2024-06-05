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

    private Button captureImageButton;
    private Button countMarksButton;
    private Button finalizeButton;

    private EditText studentNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selecttemplate_activity);

        OpenCVLoader.initDebug();

        studentNameEditText = findViewById(R.id.studentNameEditText);
        captureImageButton = findViewById(R.id.captureImageButton);
        countMarksButton = findViewById(R.id.countMarksButton);


        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            numberOfPages = intent.getIntExtra("numberOfPages", 0);
            topics = intent.getStringArrayListExtra("topics");
        }

        if (numberOfPages == 0 || topics == null || topics.isEmpty()) {
            promptForExamInfo();
        } else {
            initializeUI();
        }
    }

    private void promptForExamInfo() {
        setContentView(R.layout.exam_info_layout);
        EditText numberOfPagesEditText = findViewById(R.id.numberOfPagesEditText);
        EditText topicNameEditText = findViewById(R.id.topicNameEditText);
        Button addPageButton = findViewById(R.id.addPageButton);
        Button nextTopicButton = findViewById(R.id.nextTopicButton);
        Button finalizeButton = findViewById(R.id.finalizeButton);

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

                topics.add(topicName);
                topicNameEditText.setText("");
                Toast.makeText(SelectTemplateActivity.this, "Topic added: " + topicName, Toast.LENGTH_SHORT).show();
            }
        });

        finalizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topicName = topicNameEditText.getText().toString().trim();
                if (!topicName.isEmpty()) {
                    topics.add(topicName);
                    topicNameEditText.setText("");
                }
                if (numberOfPages == 0) {
                    Toast.makeText(SelectTemplateActivity.this, "Enter number of pages", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (topics.isEmpty()) {
                    Toast.makeText(SelectTemplateActivity.this, "Add at least one topic", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(SelectTemplateActivity.this, "List of topics finalized, Capture all pages of exam paper", Toast.LENGTH_SHORT).show();
                setContentView(R.layout.selecttemplate_activity);
                initializeUI();
            }
        });
    }

    private void initializeUI() {
        studentNameEditText = findViewById(R.id.studentNameEditText);
        captureImageButton = findViewById(R.id.captureImageButton);
        countMarksButton = findViewById(R.id.countMarksButton);
        finalizeButton = findViewById(R.id.finalizeButton);

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
                currentPage = 1;
                capturedImages.clear();
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
        List<MatOfPoint> contours = new ArrayList<>();
        List<Rect> boundingBoxes = new ArrayList<>();

        int index = 0;
        int count;

        for (String topic : topics) {
            marksByTopic.put(topic, 0);
        }

        for (Bitmap image : capturedImages) {
            if (image == null) {
                Log.e("countMarksInPage", "Null bitmap detected");
                continue;
            }
            boundingBoxes.clear();
            contours.clear();

            Mat imageMat = new Mat();
            Utils.bitmapToMat(image, imageMat);

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

            Mat hierarchy = new Mat();
            Imgproc.findContours(binaryMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            Mat resultMat = new Mat(imageMat.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));

            for (MatOfPoint contour : contours) {
                Rect boundingBox = Imgproc.boundingRect(contour);
                double aspectRatio = (double) boundingBox.width / boundingBox.height;
                double area = Imgproc.contourArea(contour);
                if (aspectRatio < 1.2 && area > 8) {
                    boundingBoxes.add(boundingBox);
                    Imgproc.rectangle(resultMat, boundingBox.tl(), boundingBox.br(), new Scalar(0, 0, 0), 2);
                }
            }

            boundingBoxes.sort(Comparator.comparingInt(r -> r.y));

            for (int i = 0; i < boundingBoxes.size(); i += count) {
                count = 0;

                for (int j = i; j < boundingBoxes.size(); j++) {
                    if (Math.abs(boundingBoxes.get(i).y - boundingBoxes.get(j).y) < 10) {
                        count++;
                    } else {
                        break;
                    }
                }

                if (index < topics.size()) {
                    String topic = topics.get(index);
                    marksByTopic.put(topic, marksByTopic.get(topic) + count);
                    index++;
                }
            }
        }

        String studentName = studentNameEditText.getText().toString().trim();
        if (!studentName.isEmpty()) {
            createExcelFile(studentName, marksByTopic);
        } else {
            Toast.makeText(this, "Enter student name to create Excel file", Toast.LENGTH_SHORT).show();
        }
    }
}



