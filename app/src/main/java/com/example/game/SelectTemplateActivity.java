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
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class SelectTemplateActivity extends Activity {

    private static final int REQUEST_SELECT_TEMPLATE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private Bitmap selectedTemplate;

    private Bitmap capturedImage;

    private Button selectTemplatesButton;
    private Button captureImageButton;

    private Button countMarksButton;


    private ImageView templatekeypointsImageView;
    private ImageView capturedkeypointsImageView;
    private ImageView matchingImageView;

    private ImageView resultImageView;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selecttemplate_activity);
        //loadLibrary("opencv_java");
        OpenCVLoader.initDebug();

        selectTemplatesButton = findViewById(R.id.selectTemplatesButton);
        captureImageButton = findViewById(R.id.captureImageButton);
        countMarksButton = findViewById(R.id.countMarksButton);
        templatekeypointsImageView = findViewById(R.id.templatekeypointsImageView);
        capturedkeypointsImageView = findViewById(R.id.capturedkeypointsImageView);
        matchingImageView = findViewById(R.id.matchingImageView);
        resultImageView = findViewById(R.id.resultImageView);

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
                Toast.makeText(this, "Template selected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to load template image", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.containsKey("data")) {
                capturedImage = (Bitmap) extras.get("data");
                // Display the captured image in the ImageView
                //imageView.setImageBitmap(capturedImage);
                //imageView.setVisibility(View.VISIBLE); // Show the ImageView
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

        // Convert the images to grayscale
        Mat grayTemplate = new Mat();
        Imgproc.cvtColor(templateMat, grayTemplate, Imgproc.COLOR_BGR2GRAY);
        Mat grayImage = new Mat();
        Imgproc.cvtColor(imageMat, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Resize the template image to match the size of the captured image
        Mat resizedTemplateMat = new Mat();
        Imgproc.resize(grayTemplate, resizedTemplateMat, grayImage.size());

        // Initialize ORB detector
        ORB orb = ORB.create();

        // Detect keypoints and compute descriptors for the resized template
        MatOfKeyPoint keypointsTemplate = new MatOfKeyPoint();
        Mat descriptorsTemplate = new Mat();
        orb.detectAndCompute(resizedTemplateMat, new Mat(), keypointsTemplate, descriptorsTemplate);

        // Detect keypoints and compute descriptors for the input image
        MatOfKeyPoint keypointsImage = new MatOfKeyPoint();
        Mat descriptorsImage = new Mat();
        orb.detectAndCompute(grayImage, new Mat(), keypointsImage, descriptorsImage);

        // Match descriptors using Brute Force matcher
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptorsTemplate, descriptorsImage, matches);

        // Filter matches based on distance
        List<DMatch> matchesList = matches.toList();
        List<DMatch> filteredMatches = matchesList.stream().filter(m -> m.distance < 55).collect(Collectors.toList());


        // Draw keypoints on the template and captured images
        Mat outputTemplateKeypoints = new Mat();
        Mat outputCapturedKeypoints = new Mat();
        Features2d.drawKeypoints(templateMat, keypointsTemplate, outputTemplateKeypoints, new Scalar(0, 255, 0), Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);
        Features2d.drawKeypoints(imageMat, keypointsImage, outputCapturedKeypoints, new Scalar(0, 255, 0));

        // Convert and display the template image with keypoints
        Bitmap templateBitmapWithKeypoints = Bitmap.createBitmap(outputTemplateKeypoints.cols(), outputTemplateKeypoints.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputTemplateKeypoints, templateBitmapWithKeypoints);
        templatekeypointsImageView.setImageBitmap(templateBitmapWithKeypoints);
        templatekeypointsImageView.setVisibility(View.VISIBLE);


        // Convert and display the captured image with keypoints
        Bitmap capturedBitmapWithKeypoints = Bitmap.createBitmap(outputCapturedKeypoints.cols(), outputCapturedKeypoints.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputCapturedKeypoints, capturedBitmapWithKeypoints);
        capturedkeypointsImageView.setImageBitmap(capturedBitmapWithKeypoints);
        capturedkeypointsImageView.setVisibility(View.VISIBLE);




        // Ensure matches are valid
        List<KeyPoint> keypointsTemplateList = keypointsTemplate.toList();
        List<KeyPoint> keypointsImageList = keypointsImage.toList();

        if (filteredMatches.size() < 5) {
            Log.d("findTemplateAndDisplayInCapturedPhoto", "Template not found in the image or not enough matches");
        } else {
            List<DMatch> validMatches = new ArrayList<>();
            for (DMatch match : filteredMatches) {
                if (match.queryIdx >= 0 && match.queryIdx < keypointsTemplateList.size() &&
                        match.trainIdx >= 0 && match.trainIdx < keypointsImageList.size()) {
                    validMatches.add(match);
                }
            }

            if (validMatches.isEmpty()) {
                Log.d("findTemplateAndDisplayInCapturedPhoto", "No valid matches found");
            } else {
                MatOfDMatch goodMatches = new MatOfDMatch();
                goodMatches.fromList(validMatches);

                // Combine the resized template and captured images side by side
                Mat combinedImage = new Mat();
                Core.hconcat(Arrays.asList(resizedTemplateMat, grayImage), combinedImage);

                // Draw matches on the combined image
                Mat outputTemplateMatches = new Mat();
                Features2d.drawMatches(resizedTemplateMat, keypointsTemplate, grayImage, keypointsImage, goodMatches, outputTemplateMatches, Scalar.all(-1), Scalar.all(-1), new MatOfByte(), Features2d.DrawMatchesFlags_NOT_DRAW_SINGLE_POINTS);

                // Convert the combined image with matches to bitmap
                Bitmap templateMatchesBitmap = Bitmap.createBitmap(outputTemplateMatches.cols(), outputTemplateMatches.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(outputTemplateMatches, templateMatchesBitmap);

                // Display the combined image with matches
                matchingImageView.setImageBitmap(templateMatchesBitmap);
                matchingImageView.setVisibility(View.VISIBLE);
                Log.d("findTemplateAndDisplayInCapturedPhoto", "Template was found in the image");

                // Find the bounding box around the matched region
                Rect boundingBox = new Rect();
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
            }
        }

        // Log keypoints sizes
        Log.d("Keypoints Size", "Template Keypoints Size: " + keypointsTemplate.rows());
        Log.d("Keypoints Size", "Captured Keypoints Size: " + keypointsImage.rows());
        Log.d("Keypoints Size", "Matched Keypoints Size: " + matches.rows());
        Log.d("Keypoints Size", "Filtered Matches Keypoints Size: " + filteredMatches.size());
    }





}


