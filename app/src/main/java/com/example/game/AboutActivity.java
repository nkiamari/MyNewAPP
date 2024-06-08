package com.example.game;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.text.Html;


public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        TextView aboutTextView = findViewById(R.id.aboutTextView);
        aboutTextView.setText(Html.fromHtml("This app is developed by <b>Niloofar Kiamari</b>. " +
                "The goal of this app is to scan exam papers and automatically count marks. " +
                "Here's how the buttons work:<br/><br/>" +
                "<b>1. Capture Image:</b> The user takes a photo of each page of the exam. " +
                "Captured images do not need to accurately include the entire page, but should include all marks within the page and exclude anything outside of the exam paper. Images should be captured only from exam papers, which should be black and white with colorful marks.<br/>" +
                "<b>2. Count Marks:</b> The app counts the marks on the captured exam page. Marks should be drawn with thick and colored markers for accurate detection. One line should be drawn for each mark.<br/>" +
                "<b>3. Next Topic:</b> The user moves to the next topic in the exam.<br/>" +
                "<b>4. Finalize:</b> The app finalizes the current exam and prepares it for analysis.<br/>" +
                "<b>5. New Exam:</b> The user starts a new exam session.<br/>" +
                "<b>6. Exit:</b> The user can exit the application by pressing the Exit button."
        ));


    }
}