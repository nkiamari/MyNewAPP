package com.example.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;




public class OneMainActivity extends Activity {
    private EditText emailEditText;
    private Button proceedButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        emailEditText = findViewById(R.id.emailEditText);
        proceedButton = findViewById(R.id.proceedButton);

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                if (isValidEmail(email)) {
                    // Proceed to EnterExamDetailsActivity
                    startActivity(new Intent(OneMainActivity.this, TwoEnterExamDetailsActivity.class));
                } else {
                    Toast.makeText(OneMainActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}



















