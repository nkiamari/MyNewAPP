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

import java.util.ArrayList;

public class SelectTemplateActivity extends Activity {

    private static final int REQUEST_SELECT_TEMPLATES = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private ArrayList<Bitmap> selectedTemplates;
    private Bitmap capturedImage;

    private Button selectTemplatesButton;
    private Button captureImageButton;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selecttemplate_activity);
        //loadLibrary("opencv_java");
        OpenCVLoader.initDebug();

        selectTemplatesButton = findViewById(R.id.selectTemplatesButton);
        captureImageButton = findViewById(R.id.captureImageButton);
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
        if (requestCode == REQUEST_SELECT_TEMPLATES && resultCode == RESULT_OK && data != null) {
            // Handle selected templates
            // You can retrieve the selected images from the data Intent and store them in selectedTemplates
            // For simplicity, I'm skipping the implementation here
            Toast.makeText(this, "Templates selected", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.containsKey("data")) {
                capturedImage = (Bitmap) extras.get("data");
                // Display the captured image in the ImageView
                imageView.setImageBitmap(capturedImage);
                Toast.makeText(this, "Image captured", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}