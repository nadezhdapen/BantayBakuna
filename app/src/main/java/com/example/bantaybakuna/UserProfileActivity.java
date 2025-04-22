package com.example.bantaybakuna;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

// Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
// Java Utilities
import java.util.HashMap;
import java.util.Map;


public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";

    private Toolbar toolbarUserProfile;
    private TextInputEditText editTextProfileName, editTextProfileEmail, editTextProfilePhone, editTextProfileAge;
    private Button buttonSaveProfile, buttonLogout;
    // private com.google.android.material.imageview.ShapeableImageView imageViewProfilePic;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId = null; // Store the logged-in user's ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Log.d(TAG, "onCreate: Starting.");

        // --- Initialize Firebase ---
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        Log.d(TAG, "onCreate: Finding views...");
        toolbarUserProfile = findViewById(R.id.toolbarUserProfile);
        editTextProfileName = findViewById(R.id.editTextProfileName);
        editTextProfileEmail = findViewById(R.id.editTextProfileEmail);
        editTextProfilePhone = findViewById(R.id.editTextProfilePhone);
        editTextProfileAge = findViewById(R.id.editTextProfileAge);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        buttonLogout = findViewById(R.id.buttonLogout);
        // imageViewProfilePic = findViewById(R.id.imageViewProfilePic);
        Log.d(TAG, "onCreate: Views found.");

        // --- Setup Toolbar ---
        setSupportActionBar(toolbarUserProfile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Your Profile");
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            if(editTextProfileEmail != null) {
                editTextProfileEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            }
            Log.d(TAG, "Current User ID: " + currentUserId + ", Email: " + currentUser.getEmail());
            loadUserProfile();
        } else {
            Log.e(TAG, "User is null, cannot load profile. Redirecting to login.");
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            goToLogin();
            return;
        }

        setupButtonClickListeners();

        Log.d(TAG, "onCreate: Setup complete.");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserProfile() {
        if (currentUserId == null) { Log.e(TAG,"UserID is null, cannot load profile."); return; }
        Log.d(TAG, "Loading profile from Firestore for user: " + currentUserId);

        DocumentReference userRef = db.collection("users").document(currentUserId);

        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Profile document exists. Populating fields.");
                        String name = documentSnapshot.getString("displayName");
                        String phone = documentSnapshot.getString("phoneNumber");
                        Long ageLong = documentSnapshot.getLong("userAge");

                        if (editTextProfileName != null) editTextProfileName.setText(name != null ? name : "");
                        if (editTextProfilePhone != null) editTextProfilePhone.setText(phone != null ? phone : "");
                        if (editTextProfileAge != null) editTextProfileAge.setText(ageLong != null ? String.valueOf(ageLong) : ""); // Convert Long to String for EditText

                    } else {
                        Log.d(TAG, "No profile document found for this user yet.");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors loading data
                    Log.e(TAG, "Error loading profile data from Firestore", e);
                    Toast.makeText(UserProfileActivity.this, "Could not load profile.", Toast.LENGTH_SHORT).show();
                });
    }
    private void setupButtonClickListeners() {
        Log.d(TAG, "Setting up button listeners...");

        if (buttonSaveProfile != null) {
            buttonSaveProfile.setOnClickListener(v -> {
                Log.d(TAG, "Save Profile button clicked.");
                saveUserProfile(); // Call the save function
            });
        } else { Log.e(TAG,"buttonSaveProfile is null!"); }

        if (buttonLogout != null) {
            buttonLogout.setOnClickListener(v -> {
                Log.d(TAG, "Logout button clicked.");
                // Clear saved User ID first
                getSharedPreferences("BANTAY_BAKUNA_PREFS", MODE_PRIVATE)
                        .edit()
                        .remove("CURRENT_USER_ID")
                        .apply();
                Log.d(TAG, "User ID removed from SharedPreferences.");

                // Sign out from Firebase Auth
                mAuth.signOut();
                Toast.makeText(UserProfileActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();


                goToLogin();
            });
        } else { Log.e(TAG, "buttonLogout is null!"); }

        Log.d(TAG, "Button listeners setup complete.");
    }
    private void saveUserProfile() {
        // Ensure user ID is available
        if (currentUserId == null) {
            Toast.makeText(this, "Error: Cannot save profile. User ID missing.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot save profile, currentUserId is null.");
            return;
        }
        Log.d(TAG, "Attempting to save profile for user: " + currentUserId);

        String name = "";
        String phone = "";
        String ageStr = "";
        if (editTextProfileName != null) name = editTextProfileName.getText().toString().trim();
        if (editTextProfilePhone != null) phone = editTextProfilePhone.getText().toString().trim();
        if (editTextProfileAge != null) ageStr = editTextProfileAge.getText().toString().trim();

        // 2. Basic Validation (Name required)
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Display Name cannot be empty.", Toast.LENGTH_SHORT).show();
            if (editTextProfileName != null) editTextProfileName.setError("Required");
            return;
        }

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("displayName", name);
        profileData.put("phoneNumber", phone); // Save phone number

        // 4. Convert age String to Long (Number) for Firestore, handle errors/empty
        try {
            if (!ageStr.isEmpty()) {
                long age = Long.parseLong(ageStr);
                profileData.put("userAge", age); // Store as Number type
            } else {
                profileData.put("userAge", null);

            }
        } catch (NumberFormatException e) {
            // If the text entered in the age field is not a valid number
            Toast.makeText(this, "Please enter a valid whole number for age.", Toast.LENGTH_SHORT).show();
            if (editTextProfileAge != null) editTextProfileAge.setError("Invalid number");
            Log.w(TAG,"Invalid age input format: '" + ageStr + "'");
            return; // Stop saving if age format is wrong
        }

        // 5. Get Firestore reference and save data
        // Path: users/{userId}
        DocumentReference userRef = db.collection("users").document(currentUserId);


        userRef.set(profileData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Success!
                    Log.d(TAG, "Profile data successfully saved/merged!");
                    Toast.makeText(UserProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    // Stay on the screen after successful save
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error writing profile document to Firestore", e);
                    Toast.makeText(UserProfileActivity.this, "Error saving profile. Check logs.", Toast.LENGTH_SHORT).show();
                });
    }


    private void goToLogin() {
        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}