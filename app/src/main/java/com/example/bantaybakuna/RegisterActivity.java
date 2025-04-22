package com.example.bantaybakuna;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";


    EditText etRegisterFullName, etRegisterEmail, etRegisterPassword;
    Button btnRegister;
    TextView tvGoToLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        etRegisterFullName = findViewById(R.id.registerFullName);
        etRegisterEmail = findViewById(R.id.registerEmail);
        etRegisterPassword = findViewById(R.id.registerPassword);
        btnRegister = findViewById(R.id.buttonRegister);
        tvGoToLogin = findViewById(R.id.textGoToLogin);


        btnRegister.setOnClickListener(v -> {
            registerUser(); // Call the registration method
        });

        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);

            startActivity(intent);

            finish();
        });
    }

    private void registerUser() {
        String fullName = etRegisterFullName.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim();
        String password = etRegisterPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etRegisterFullName.setError("Full Name is required.");
            etRegisterFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etRegisterEmail.setError("Email is required.");
            etRegisterEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegisterEmail.setError("Please enter a valid email address.");
            etRegisterEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etRegisterPassword.setError("Password is required.");
            etRegisterPassword.requestFocus();
            return;
        }
        if (password.length() < 6) { // Firebase requirement
            etRegisterPassword.setError("Password must be at least 6 characters long.");
            etRegisterPassword.requestFocus();
            return;
        }

        // Use Firebase Auth to create the new user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Update the user's profile to include their Full Name
                        updateUserProfile(user, fullName);
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Update User Profile (Called after registration) ---
    private void updateUserProfile(FirebaseUser user, String fullName) {
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated successfully with name: " + fullName);
                            navigateToMain();
                        } else {
                            Log.w(TAG, "Failed to update user profile.", task.getException());
                            Toast.makeText(RegisterActivity.this, "Profile update failed, logging in.", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        }
                    });
        } else {
            Log.e(TAG, "Cannot update profile, user is null after registration.");
            Toast.makeText(RegisterActivity.this, "Registration complete, but profile update failed.", Toast.LENGTH_SHORT).show();
            navigateToMain();
        }
    }

    private void navigateToMain() {
        // Make sure MainActivity exists and is correctly set up
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        // Clear the activity stack so the user goes directly to main app screen
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}