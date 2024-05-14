package com.example.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.util.Log;

import android.content.ClipData;
import android.net.Uri;
import java.io.IOException;


public class SelectTemplateActivity extends Activity {

    private static final int REQUEST_SELECT_TEMPLATE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private Bitmap selectedTemplate;

    private Bitmap capturedImage;

    private Button selectTemplatesButton;
    private Button captureImageButton;

    private Button countMarksButton;

    private ImageView imageView;

    private ImageView resultImageView;

    private ImageView templateImageView;

    private ImageView capturedImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selecttemplate_activity);
        //loadLibrary("opencv_java");
        OpenCVLoader.initDebug();

        selectTemplatesButton = findViewById(R.id.selectTemplatesButton);
        captureImageButton = findViewById(R.id.captureImageButton);
        countMarksButton = findViewById(R.id.countMarksButton);
        imageView = findViewById(R.id.imageView);
        resultImageView = findViewById(R.id.resultImageView);
        templateImageView = findViewById(R.id.templateImageView);
        capturedImageView = findViewById(R.id.capturedImageView);

        selectTemplatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, REQUEST_SELECT_TEMPLATE);
            }
        });

        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }

        });

        countMarksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (capturedImage != null && selectedTemplate != null) {
                    //int contours = countMarkssingletemplate(capturedImage, templateBitmap);
                    countMarkssingletemplate(capturedImage, selectedTemplate);

                    //Toast.makeText(SelectTemplateActivity.this, "Number of contours: " + contours, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SelectTemplateActivity.this, "Please capture an image and select templates first", Toast.LENGTH_SHORT).show();
                }
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

    // Helper method to convert Uri to Bitmap
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
                templateImageView.setImageBitmap(selectedTemplate);
                templateImageView.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Template selected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to load template image", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.containsKey("data")) {
                capturedImage = (Bitmap) extras.get("data");
                // Display the captured image in the ImageView
                imageView.setImageBitmap(capturedImage);
                imageView.setVisibility(View.VISIBLE); // Show the ImageView
                Toast.makeText(this, "Image captured", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
    }









    public void countMarkssingletemplate(Bitmap photoBitmap, Bitmap templateBitmap) {
        // Check if input bitmaps are valid
        if (photoBitmap == null || templateBitmap == null) {
            Log.e("findTemplateAndDisplayInCapturedPhoto", "Null bitmap detected");
            return;
        }

        // Convert Bitmaps to Mats
        Mat imageMat = new Mat();
        Mat templateMat = new Mat();
        Utils.bitmapToMat(photoBitmap, imageMat);
        Utils.bitmapToMat(templateBitmap, templateMat);

        // Convert the template image to grayscale
        Mat grayTemplate = new Mat();
        Imgproc.cvtColor(templateMat, grayTemplate, Imgproc.COLOR_BGR2GRAY);

        // Convert the input image to grayscale
        Mat grayImage = new Mat();
        Imgproc.cvtColor(imageMat, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Initialize ORB detector
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);

        // Detect keypoints and compute descriptors for the template
        MatOfKeyPoint keypointsTemplate = new MatOfKeyPoint();
        detector.detect(grayTemplate, keypointsTemplate);
        Mat descriptorsTemplate = new Mat();
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        extractor.compute(grayTemplate, keypointsTemplate, descriptorsTemplate);

        // Detect keypoints and compute descriptors for the input image
        MatOfKeyPoint keypointsImage = new MatOfKeyPoint();
        detector.detect(grayImage, keypointsImage);
        Mat descriptorsImage = new Mat();
        extractor.compute(grayImage, keypointsImage, descriptorsImage);

        // Match descriptors using Brute Force matcher
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptorsTemplate, descriptorsImage, matches);

        // Filter matches based on distance
        List<DMatch> matchesList = matches.toList();
        double minDist = Double.MAX_VALUE;
        for (DMatch match : matchesList) {
            double dist = match.distance;
            if (dist < minDist) {
                minDist = dist;
            }
        }

        // Filtered matches will be those with distance less than 3 times the minimum distance
        List<DMatch> filteredMatches = new ArrayList<>();
        double thresholdDist = 3 * minDist;
        for (DMatch match : matchesList) {
            if (match.distance < thresholdDist) {
                filteredMatches.add(match);
            }
        }

        // If enough good matches are found, the template is considered found
        if (filteredMatches.size() > 10) { // Adjust this threshold as needed
            Log.d("findTemplateAndDisplayInCapturedPhoto", "Template found in the image");

            // Find the bounding box around the matched region
            Rect boundingBox = new Rect();
            List<KeyPoint> keypointsTemplateList = keypointsTemplate.toList();
            List<KeyPoint> keypointsImageList = keypointsImage.toList();
            List<Point> matchedPoints = new ArrayList<>();
            for (DMatch match : filteredMatches) {
                Point imgPoint = keypointsImageList.get(match.trainIdx).pt;
                matchedPoints.add(imgPoint);
            }

            // Find the min and max points to create the bounding box
            Point minPoint = new Point(Double.MAX_VALUE, Double.MAX_VALUE);
            Point maxPoint = new Point(Double.MIN_VALUE, Double.MIN_VALUE);
            for (Point point : matchedPoints) {
                minPoint.x = Math.min(minPoint.x, point.x);
                minPoint.y = Math.min(minPoint.y, point.y);
                maxPoint.x = Math.max(maxPoint.x, point.x);
                maxPoint.y = Math.max(maxPoint.y, point.y);
            }

            // Create the bounding box
            boundingBox.x = (int) minPoint.x;
            boundingBox.y = (int) minPoint.y;
            boundingBox.width = (int) (maxPoint.x - minPoint.x);
            boundingBox.height = (int) (maxPoint.y - minPoint.y);

            // Crop the original image using the bounding box
            Mat croppedImage = new Mat(imageMat, boundingBox);

            // Convert the resulting image to bitmap
            Bitmap resultBitmap = Bitmap.createBitmap(croppedImage.cols(), croppedImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(croppedImage, resultBitmap);

            // Display the resultBitmap
            resultImageView.setImageBitmap(resultBitmap);
            resultImageView.setVisibility(View.VISIBLE); // Show the ImageView
        } else {
            // Template not found, handle accordingly
            Log.e("findTemplateAndDisplayInCapturedPhoto", "Template not found in the image");
        }






        // Draw keypoints on the template image
        Mat outputTemplate = new Mat();
        Features2d.drawKeypoints(templateMat, keypointsTemplate, outputTemplate, new Scalar(0, 255, 0), Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);

// Convert the resulting template image to bitmap
        Bitmap templateBitmapWithKeypoints = Bitmap.createBitmap(outputTemplate.cols(), outputTemplate.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputTemplate, templateBitmapWithKeypoints);

// Display the templateBitmapWithKeypoints
        templateImageView.setImageBitmap(templateBitmapWithKeypoints);
        templateImageView.setVisibility(View.VISIBLE); // Show the ImageView

// Draw keypoints on the captured image
        Mat outputCaptured = new Mat();
        Features2d.drawKeypoints(imageMat, keypointsImage, outputCaptured, new Scalar(0, 255, 0));

// Convert the resulting captured image to bitmap
        Bitmap capturedBitmapWithKeypoints = Bitmap.createBitmap(outputCaptured.cols(), outputCaptured.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputCaptured, capturedBitmapWithKeypoints);

// Display the capturedBitmapWithKeypoints
        capturedImageView.setImageBitmap(capturedBitmapWithKeypoints);
        capturedImageView.setVisibility(View.VISIBLE); // Show the ImageView





    }


}
