package com.example.bantaybakuna;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Firebase Auth
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText editTextLoginEmail;
    private EditText editTextLoginPassword; // Declaration for password field

    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Link to the layout file (ensure it uses the corrected IDs)
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        editTextLoginEmail = findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword);
        // Declaration for login button
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textViewGoToRegister = findViewById(R.id.textGoToRegister);
        // --- END Find Views ---

        Log.d(TAG, "LoginActivity onCreate: Views initialized.");

        if (buttonLogin != null) {
            buttonLogin.setOnClickListener(v -> {
                // Check if fields were found before getting text
                if (editTextLoginEmail == null || editTextLoginPassword == null) {
                    Log.e(TAG, "Email or Password EditText not found!");
                    Toast.makeText(LoginActivity.this, "Login Error.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String email = editTextLoginEmail.getText().toString().trim();
                String password = editTextLoginPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(LoginActivity.this, "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "Attempting login for: " + email);
                // Sign in with Firebase Auth
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                // Save User ID to SharedPreferences for Background Worker
                                if (user != null) {
                                    getSharedPreferences("BANTAY_BAKUNA_PREFS", MODE_PRIVATE)
                                            .edit()
                                            .putString("CURRENT_USER_ID", user.getUid())
                                            .apply(); // Save the ID
                                    Log.d(TAG, "User ID saved to SharedPreferences: " + user.getUid());
                                } else {
                                    Log.w(TAG,"Login successful but currentUser is null?"); // Should not happen
                                }

                                Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            } else {
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed: " + Objects.requireNonNull(task.getException()).getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            });
        } else {
            Log.e(TAG, "Login Button is null! Check XML ID and findViewById.");
            Toast.makeText(this,"Error initializing login button.", Toast.LENGTH_LONG).show();
        }


        if (textViewGoToRegister != null) {
            textViewGoToRegister.setOnClickListener(v -> {
                Log.d(TAG, "GoToRegister text clicked.");
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "GoToRegister TextView is null!");
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // If user is already logged in, save ID again (doesn't hurt) and go to MainActivity
            Log.d(TAG, "User already logged in (onStart), ensuring ID is saved, going to MainActivity.");
            getSharedPreferences("BANTAY_BAKUNA_PREFS", MODE_PRIVATE)
                    .edit()
                    .putString("CURRENT_USER_ID", currentUser.getUid())
                    .apply();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Log.d(TAG, "No user logged in (onStart).");
        }
    }

}