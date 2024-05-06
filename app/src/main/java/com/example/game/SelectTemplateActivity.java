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
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class SelectTemplateActivity extends Activity {

    private static final int REQUEST_SELECT_TEMPLATES = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private ArrayList<Bitmap> selectedTemplates = new ArrayList<>();

    private Bitmap capturedImage;

    private Button selectTemplatesButton;
    private Button captureImageButton;

    private Button countMarksButton;

    private ImageView imageView;


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
                countMarksAndProcessImage();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
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


    private void countMarksAndProcessImage() {
        if (capturedImage != null) {
            // Convert captured image to Mat
            Mat imageMat = new Mat();
            Utils.bitmapToMat(capturedImage, imageMat);

            // Convert the captured image bitmap from ARGB to BGR format
            Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGBA2BGR);

            // Convert the image to grayscale
            Mat grayMat = new Mat();
            Imgproc.cvtColor(imageMat, grayMat, Imgproc.COLOR_BGR2GRAY);

            // Threshold the grayscale image to get binary image
            Mat binaryMat = new Mat();
            Imgproc.threshold(grayMat, binaryMat, 0, 255, Imgproc.THRESH_BINARY);

            // Find contours
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(binaryMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Display the number of contours found
            Toast.makeText(this, "Number of contours: " + contours.size(), Toast.LENGTH_SHORT).show();

            // Convert red parts of the image to white
            Scalar colorWhite = new Scalar(255, 255, 255); // BGR color format
            for (MatOfPoint contour : contours) {
                Rect rect = Imgproc.boundingRect(contour);
                Imgproc.rectangle(imageMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), colorWhite, -1);
            }

            // Convert the processed Mat back to Bitmap for display
            Bitmap processedBitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imageMat, processedBitmap);

            // Display the processed image in the ImageView
            imageView.setImageBitmap(processedBitmap);
            imageView.setVisibility(View.VISIBLE); // Show the ImageView
        } else {
            Toast.makeText(this, "Please capture an image first", Toast.LENGTH_SHORT).show();
        }
    }

}
