package com.example.bantaybakuna;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Firebase &  Data Classes
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.bantaybakuna.Data.Child;
import com.example.bantaybakuna.R;

// Java Date/Time Utilities
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddChildActivity extends AppCompatActivity {

    private static final String TAG = "AddChildActivity";

    // --- Declare UI Variables ---
    private EditText editTextChildName;
    private TextView textViewSelectedDob;
    private Button buttonSelectDate;
    private Button buttonSaveChild;
    private Toolbar toolbarAddChild;

    private FirebaseFirestore db;
    private Calendar selectedDateCalendar;
    private FirebaseAuth mAuth;

    // onCreate Method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);
        Log.d(TAG, "onCreate: Activity started.");

        // Initialize
        db = FirebaseFirestore.getInstance();
        selectedDateCalendar = Calendar.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Setup Toolbar
        toolbarAddChild = findViewById(R.id.toolbarAddChild);
        setSupportActionBar(toolbarAddChild);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            Log.d(TAG, "Toolbar setup complete.");
        } else { Log.e(TAG, "Toolbar not found!"); }

        Log.d(TAG, "onCreate: Finding other views...");
        editTextChildName = findViewById(R.id.editTextChildName);
        textViewSelectedDob = findViewById(R.id.textViewSelectedDob);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonSaveChild = findViewById(R.id.buttonSaveChild);
        Log.d(TAG, "onCreate: Views found.");

        setupClickListeners();
        Log.d(TAG, "onCreate: Setup complete.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "Toolbar back arrow pressed.");
            onBackPressed(); // Go back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners...");
        if (buttonSelectDate != null) {
            buttonSelectDate.setOnClickListener(view -> {
                Log.d(TAG, "Select Date button clicked.");
                showDatePickerDialog();
            });
        } else { Log.e(TAG, "buttonSelectDate is null!"); }

        if (buttonSaveChild != null) {
            buttonSaveChild.setOnClickListener(view -> {
                Log.d(TAG, "Save Child button clicked.");
                saveChild();
            });
        } else { Log.e(TAG, "buttonSaveChild is null!"); }
        Log.d(TAG, "Click listeners setup complete.");
    }


    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            selectedDateCalendar.set(Calendar.YEAR, year);
            selectedDateCalendar.set(Calendar.MONTH, month);
            selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Log.d(TAG, "Date selected: " + (month + 1) + "/" + dayOfMonth + "/" + year);
            updateLabel();
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddChildActivity.this, dateSetListener,
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
        Log.d(TAG, "DatePickerDialog shown.");
    }



    private void updateLabel() {
        String myFormat = "MMMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        if (textViewSelectedDob != null) {
            textViewSelectedDob.setText(sdf.format(selectedDateCalendar.getTime()));
            Log.d(TAG, "Date label updated: " + textViewSelectedDob.getText());
        } else { Log.e(TAG, "textViewSelectedDob is null!"); }
    }


    private void saveChild() {
        Log.d(TAG, "Attempting to save child...");

        // 1. Get Name
        String name = "";
        if (editTextChildName != null) { name = editTextChildName.getText().toString().trim(); }

        // 2. Get Date
        Date dob = null;
        String dobHint = getString(R.string.select_dob_hint);
        if (textViewSelectedDob != null &&
                !textViewSelectedDob.getText().toString().isEmpty() &&
                !textViewSelectedDob.getText().toString().equals(dobHint)) {
            dob = selectedDateCalendar.getTime();
        }

        // 3. Validate
        if (name.isEmpty()) { /* Show error, return */ Toast.makeText(this,"Name required",Toast.LENGTH_SHORT).show(); return; }
        if (dob == null) { /* Show error, return */ Toast.makeText(this,"DOB required",Toast.LENGTH_SHORT).show(); return; }

        // --- Get Current Logged-in User ---
        FirebaseUser currentUser = mAuth.getCurrentUser(); // **** USE mAuth HERE ****
        if (currentUser == null) {
            Toast.makeText(this, "Not logged in. Cannot save.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User is null, cannot save child.");
            return;
        }
        String userId = currentUser.getUid();

        Child newChild = new Child(name, dob);

        Log.d(TAG, "Saving to Firestore for user: " + userId);
        db.collection("users").document(userId).collection("children") // Use the correct path
                .add(newChild)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Child saved successfully for user " + userId + "! ID: " + documentReference.getId());
                    Toast.makeText(AddChildActivity.this, "Child Saved!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to MainActivity
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving child for user " + userId, e);
                    Toast.makeText(AddChildActivity.this, "Error saving. Check logs.", Toast.LENGTH_LONG).show();
                });
    }

}