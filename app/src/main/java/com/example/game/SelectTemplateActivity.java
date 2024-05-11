package com.example.game;

import android.app.Activity;
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
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import android.content.ClipData;
import android.net.Uri;
import java.io.IOException;


public class SelectTemplateActivity extends Activity {

    private static final int REQUEST_SELECT_TEMPLATES = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private ArrayList<Bitmap> selectedTemplates = new ArrayList<>();

    private Bitmap capturedImage;

    private Button selectTemplatesButton;
    private Button captureImageButton;

    private Button countMarksButton;

    private ImageView imageView;

    private ImageView resultImageView;


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

        selectTemplatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, REQUEST_SELECT_TEMPLATES);
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
                if (capturedImage != null && !selectedTemplates.isEmpty()) {
                    // Assuming you want to count contours for the first selected template
                    Bitmap templateBitmap = selectedTemplates.get(0);
                    //int contours = countMarkssingletemplate(capturedImage, templateBitmap);
                    countMarkssingletemplate(capturedImage, templateBitmap);
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

        if (requestCode == REQUEST_SELECT_TEMPLATES && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                // Handle single image selection
                Bitmap selectedTemplateBitmap = getBitmapFromUri(data.getData());
                if (selectedTemplateBitmap != null) {
                    selectedTemplates.add(selectedTemplateBitmap);
                } else {
                    Toast.makeText(this, "Failed to load template image", Toast.LENGTH_SHORT).show();
                }
            } else if (data.getClipData() != null) {
                // Handle multiple image selection
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    Bitmap selectedTemplateBitmap = getBitmapFromUri(uri);
                    if (selectedTemplateBitmap != null) {
                        selectedTemplates.add(selectedTemplateBitmap);
                    } else {
                        Toast.makeText(this, "Failed to load template image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            Toast.makeText(this, "Template(s) selected", Toast.LENGTH_SHORT).show();
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
            Log.e("findAndDisplayTemplate", "Null bitmap detected");
            return;
        }

        // Convert Bitmaps to Mats
        Mat imageMat = new Mat();
        Mat templateMat = new Mat();
        Utils.bitmapToMat(photoBitmap, imageMat);
        Utils.bitmapToMat(templateBitmap, templateMat);

        // Convert the input image bitmap from ARGB to BGR format
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGBA2BGR);
        // Convert the input template bitmap from ARGB to BGR format
        Imgproc.cvtColor(templateMat, templateMat, Imgproc.COLOR_RGBA2BGR);

        // Perform template matching
        Mat result = new Mat();
        Imgproc.matchTemplate(imageMat, templateMat, result, Imgproc.TM_CCOEFF);




        // Define threshold for template matching
        double threshold = -0.5;
        Core.compare(result, new Scalar(threshold), result, Core.CMP_LT);

        // Convert the result to the HSV color space
        //Mat hsvMat = new Mat();
        //Imgproc.cvtColor(result, hsvMat, Imgproc.COLOR_BGR2HSV);



        // Convert the result of template matching and the processed image to bitmaps for display
        Bitmap resultBitmapTemplateMatch = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, resultBitmapTemplateMatch);

        // Display the resultBitmapTemplateMatch
        resultImageView.setImageBitmap(resultBitmapTemplateMatch);
        resultImageView.setVisibility(View.VISIBLE); // Show the ImageView



    }


    }
