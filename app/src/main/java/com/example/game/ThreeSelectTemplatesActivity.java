package com.example.game;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.net.Uri;
import java.util.ArrayList;
import android.content.ClipData;
import android.widget.Toast;


public class ThreeSelectTemplatesActivity extends Activity {

    private static final int REQUEST_PICK_IMAGES = 1;
    private Bitmap photoBitmap;
    private Button selectTemplatesButton;
    private Button proceedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_templates);

        proceedButton = findViewById(R.id.proceedButton);
        selectTemplatesButton = findViewById(R.id.selectTemplatesButton);
        selectTemplatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch gallery to select templates
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_PICK_IMAGES);
            }
        });


        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Proceed to the next activity
                Intent intent = new Intent(ThreeSelectTemplatesActivity.this, ProcessExamPapersActivity.class);
                startActivity(intent);


            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            // Handle selected images from the gallery
            ArrayList<Uri> selectedImages = new ArrayList<>();
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri selectedImageUri = clipData.getItemAt(i).getUri();
                    selectedImages.add(selectedImageUri);
                }
            } else {
                // Handle single selection
                Uri selectedImageUri = data.getData();
                selectedImages.add(selectedImageUri);
            }

            // Retrieve photoBitmap
            photoBitmap = getIntent().getParcelableExtra("photoBitmap");
            if (photoBitmap == null) {
                Toast.makeText(this, "Failed to retrieve photo bitmap", Toast.LENGTH_SHORT).show();
                finish(); // Finish activity if photoBitmap is null
                return;
            }

            // Proceed to ProcessExamPapersActivity and pass the selected templates as extras
            Intent intent = new Intent(ThreeSelectTemplatesActivity.this, ProcessExamPapersActivity.class);
            intent.putParcelableArrayListExtra("selectedTemplates", selectedImages);
            intent.putExtra("photoBitmap", photoBitmap); // Pass photoBitmap to next activity
            startActivity(intent);
        }
    }

}

