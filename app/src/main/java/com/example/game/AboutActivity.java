package com.example.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View; // Import View class
import android.widget.Button;
import android.widget.TextView;
import android.text.Html;
import android.widget.ImageView;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        // Set up the back button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Navigate back to previous activity
            }
        });


        ImageView exampleImageView = findViewById(R.id.exampleImageView);
        exampleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePopup();
            }
        });

        TextView aboutTextView = findViewById(R.id.aboutTextView);
        aboutTextView.setText(Html.fromHtml("Exam Scanner V1.0 is developed by <b>Niloofar Kiamari</b>. " +
                "The goal of this app is to streamline the process of providing detailed feedback to students by scaning exam papers and automatically counting marks related to each topic. " +
                "Here's how the buttons work:<br/><br/>" +
                "<b>1. Capture Image:</b> The user takes a photo of each page of the exam. " +
                "Captured images do not need to include the entire page accurately, but should include all marks within the page and exclude anything outside of the exam paper. Images should be captured only from exam papers, which should be black and white with colorful marks.<br/>" +
                "<b>2. Count Marks:</b> The app automatically counts the marks related to each topic on the captured exam page. Marks should be drawn with thick and colored markers for accurate detection. One line should be drawn for each mark.<br/>" +
                "<b>3. Next Topic:</b> The user enters the topic related to the next question in the exam.<br/>" +
                "<b>4. Finalize:</b> The app finalizes the current exam and prepares it for analysis.<br/>" +
                "<b>5. New Exam:</b> The user starts a new exam paper.<br/>" +
                "<b>6. Exit:</b> The user can exit the application by pressing the Exit button.<br/>" +
                "* If you have any question, leave them in the comment section and they will be answered accordingly."
        ));


    }

    private void showImagePopup() {
        // Inflate the layout for the dialog
        View view = LayoutInflater.from(this).inflate(R.layout.popup_image_layout, null);
        ImageView imageView = view.findViewById(R.id.popupImageView);
        imageView.setImageResource(R.drawable.example_exam_image);

        // Create AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Set OnClickListener to dismiss dialog when clicked anywhere
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Show AlertDialog
        dialog.show();
    }

}
