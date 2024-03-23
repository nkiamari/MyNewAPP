package com.example.game;

import android.adservices.topics.Topic;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
C:\Users\nkiam\AndroidStudioProjects\Game2\app\src\main\java\com\example
import java.util.ArrayList;
import java.util.Arrays;

public class TwoEnterExamDetailsActivity extends Activity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SELECT_QUESTIONS = 2;

    // Declare EditText for questions
    private EditText questionsEditText;
    private EditText numQuestionsEditText;
    private EditText numPagesEditText;
    private Button proceedButton;
    private Button addTopicButton;
    private LinearLayout topicContainer;

    // Declare topics ArrayList to store topics entered by the user
    private ArrayList<String> topics = new ArrayList<>();
    // Declare questionsPerTopic ArrayList to store questions for each topic
    private ArrayList<ArrayList<String>> questionsPerTopic = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_exam_details);

        numQuestionsEditText = findViewById(R.id.numQuestionsEditText);
        numPagesEditText = findViewById(R.id.numPagesEditText);
        proceedButton = findViewById(R.id.proceedButton2);
        addTopicButton = findViewById(R.id.addAnotherTopicButton);
        topicContainer = findViewById(R.id.topicContainer);
        // Initialize EditText
        questionsEditText = findViewById(R.id.questionsEditText);
        // Set input type to multiline text
        questionsEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);



        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numQuestions = numQuestionsEditText.getText().toString();
                String numPages = numPagesEditText.getText().toString();

                if (!TextUtils.isEmpty(numQuestions) && !TextUtils.isEmpty(numPages)) {
                    int numQuestionsValue = Integer.parseInt(numQuestions);
                    int numPagesValue = Integer.parseInt(numPages);

                    Intent intent = new Intent(TwoEnterExamDetailsActivity.this, ThreeSelectTemplatesActivity.class);
                    intent.putExtra("numQuestions", numQuestionsValue);
                    intent.putExtra("numPages", numPagesValue);
                    startActivityForResult(intent, REQUEST_SELECT_QUESTIONS);
                } else {
                    Toast.makeText(TwoEnterExamDetailsActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTopic();
            }
        });
    }

    private void addTopic() {
        EditText topicEditText = findViewById(R.id.topicEditText);
        EditText questionsEditText = findViewById(R.id.questionsEditText);

        String topic = topicEditText.getText().toString().trim();
        String questionsString = questionsEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(topic) && !TextUtils.isEmpty(questionsString)) {
            // Split the entered questions by newline
            String[] questionArray = questionsString.split(",");
            ArrayList<String> questionsList = new ArrayList<>(Arrays.asList(questionArray));

            // Validate each question in the list
            boolean isValidQuestions = true;
            for (String question : questionsList) {
                // Check if the question is a valid number
                try {
                    Integer.parseInt(question);
                } catch (NumberFormatException e) {
                    // If the question is not a valid number, set isValidQuestions to false and break the loop
                    isValidQuestions = false;
                    break;
                }
            }

            if (isValidQuestions) {
                // If all questions are valid numbers, add the topic and questions to the global list
                topics.add(topic);
                questionsPerTopic.add(questionsList);

                // Clear the EditText fields for the next entry
                topicEditText.setText("");
                questionsEditText.setText("");
            } else {
                // If any question is not a valid number, display an error message
                Toast.makeText(TwoEnterExamDetailsActivity.this, "Please enter valid question numbers", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Notify the user if topic or questions are empty
            Toast.makeText(TwoEnterExamDetailsActivity.this, "Please enter topic and questions", Toast.LENGTH_SHORT).show();
        }
    }






    public void captureImage() {
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
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if (imageBitmap != null) {
                Intent intent = new Intent(TwoEnterExamDetailsActivity.this, ThreeSelectTemplatesActivity.class);
                intent.putExtra("photoBitmap", imageBitmap);
                startActivityForResult(intent, REQUEST_SELECT_QUESTIONS);
            } else {
                Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_SELECT_QUESTIONS && resultCode == RESULT_OK && data != null) {
            ArrayList<ArrayList<String>> questionsPerTopic = (ArrayList<ArrayList<String>>) data.getSerializableExtra("questionsPerTopic");
            if (questionsPerTopic != null && !questionsPerTopic.isEmpty()) {
                // Handle the data received from ThreeSelectTemplatesActivity
                // For example, you can proceed to the next activity or perform other actions
            } else {
                Toast.makeText(this, "No questions selected", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

