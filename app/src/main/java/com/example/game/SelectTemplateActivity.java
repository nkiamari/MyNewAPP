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


// Determine the ratio of the captured image's width and height
        float captureRatio = (float) templateBitmap.getWidth() / photoBitmap.getWidth();


// Resize the template image based on the capture ratio
        int newWidth = (int) (imageMat.cols());
        int newHeight = (int) (templateBitmap.getHeight() / captureRatio);

        Mat resizedTemplateMat = new Mat();
        Imgproc.resize(templateMat, resizedTemplateMat, new Size(newWidth, newHeight));






        Log.d("Template Matching", "Image cols: " + imageMat.cols() + ", rows: " + imageMat.rows());
        Log.d("Template Matching", "template cols: " + resizedTemplateMat.cols() + ", rows: " + resizedTemplateMat.rows());







// Convert the images to grayscale
        Mat grayTemplate = new Mat();
        Imgproc.cvtColor(resizedTemplateMat, grayTemplate, Imgproc.COLOR_BGR2GRAY);
        Mat grayImage = new Mat();
        Imgproc.cvtColor(imageMat, grayImage, Imgproc.COLOR_BGR2GRAY);

// Use matchTemplate method for template matching
        Mat result = new Mat();
        Imgproc.matchTemplate(grayImage, grayTemplate, result, Imgproc.TM_CCOEFF_NORMED);

// Normalize the result
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());



// Localize the best match with minMaxLoc
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc = mmr.maxLoc;

// Draw rectangle on the detected template
        int templateWidth = resizedTemplateMat.cols();
        int templateHeight = resizedTemplateMat.rows();
        Imgproc.rectangle(imageMat, matchLoc, new Point(matchLoc.x + templateWidth, matchLoc.y + templateHeight), new Scalar(0, 255, 0), 2);




// Convert the imageMat with the rectangle to a bitmap
        Bitmap resultBitmapWithRectangle = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageMat, resultBitmapWithRectangle);

// Display the imageMat with the rectangle
        resultImageView.setImageBitmap(resultBitmapWithRectangle);
        resultImageView.setVisibility(View.VISIBLE);









        double maxValue = mmr.maxVal;
        Log.d("Template Matching", "Maximum value in result matrix: " + maxValue);

        Point maxLoc = mmr.maxLoc;
// Log the coordinates of the maximum value
        int maxX = (int) maxLoc.x;
        int maxY = (int) maxLoc.y;
        Log.d("Template Matching", "Coordinates of maximum value: (" + maxX + ", " + maxY + ")");


        Log.d("Template Matching", "Match location: {" + matchLoc.x + ", " + matchLoc.y + "}");
    }



}

