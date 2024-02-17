package com.example.game;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private EditText emailEditText;
    private EditText topicEditText;
    private EditText markEditText;
    private Button capturePhotoButton;
    private Button submitButton;

    private ArrayList<Uri> capturedPhotos;
    private ArrayList<String> topics;
    private HashMap<String, Integer> marksMap;
    private String markString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.emailEditText);
        topicEditText = findViewById(R.id.topicEditText);
        markEditText = findViewById(R.id.markEditText);
        capturePhotoButton = findViewById(R.id.capturePhotoButton);
        submitButton = findViewById(R.id.submitButton);

        capturedPhotos = new ArrayList<>();
        topics = new ArrayList<>();
        marksMap = new HashMap<>();

        capturePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String topic = topicEditText.getText().toString();
                String markString = markEditText.getText().toString();

                if (isValidEmail(email) && isValidTopic(topic) && isValidMark(markString)) {
                    int mark = Integer.parseInt(markString);
                    topics.add(topic);
                    marksMap.put(topic, mark);
                    Toast.makeText(MainActivity.this, "Topic and mark added", Toast.LENGTH_SHORT).show();

                    // Clear input fields
                    topicEditText.getText().clear();
                    markEditText.getText().clear();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidTopic(String topic) {
        return !TextUtils.isEmpty(topic);
    }

    private boolean isValidMark(String markString) {
        this.markString = markString;
        if (TextUtils.isEmpty(markString)) {
            return false;
        }
        try {
            int mark = Integer.parseInt(markString);
            return mark >= 0; // Assuming marks cannot be negative
        } catch (NumberFormatException e) {
            return false;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bitmap photoBitmap = (Bitmap) data.getExtras().get("data");
            if (photoBitmap != null) {
                Uri photoUri = savePhotoToCache(photoBitmap);
                if (photoUri != null) {
                    capturedPhotos.add(photoUri);
                    Toast.makeText(this, "Photo captured", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Uri savePhotoToCache(Bitmap photoBitmap) {
        File cacheDir = getCacheDir();
        File photoFile = new File(cacheDir, "temp_photo.jpg");

        try {
            FileOutputStream fos = new FileOutputStream(photoFile);
            photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return Uri.fromFile(photoFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
