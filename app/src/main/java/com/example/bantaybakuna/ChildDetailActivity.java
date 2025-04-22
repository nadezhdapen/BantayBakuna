package com.example.bantaybakuna;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// Firebase & Data Classes
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.bantaybakuna.Data.Child;


// Java Date/Time Utilities
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ChildDetailActivity extends AppCompatActivity {

    private static final String TAG = "ChildDetailActivity";

    // --- UI Variables ---
    private Toolbar toolbarChildDetail;
    private TextView textViewDetailName, textViewDetailAge, textViewDetailDob;
    private Button buttonGoToLog, buttonGoToHistory, buttonGoToSummary, buttonDeleteThisChild;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String childDocId;
    private Child currentChild = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_detail);
        Log.d(TAG, "onCreate: Starting.");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Log.d(TAG, "onCreate: Finding views...");
        toolbarChildDetail = findViewById(R.id.toolbarChildDetail);
        textViewDetailName = findViewById(R.id.textViewDetailName);
        textViewDetailAge = findViewById(R.id.textViewDetailAge);
        textViewDetailDob = findViewById(R.id.textViewDetailDob);
        buttonGoToLog = findViewById(R.id.buttonGoToLog);
        buttonGoToHistory = findViewById(R.id.buttonGoToHistory);
        buttonGoToSummary = findViewById(R.id.buttonGoToSummary);
        buttonDeleteThisChild = findViewById(R.id.buttonDeleteThisChild);
        // REMOVED: findViewById for recyclerViewVaccineStatus
        Log.d(TAG, "onCreate: Views found.");

        setSupportActionBar(toolbarChildDetail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } else { Log.e(TAG, "Toolbar not found!"); }

        childDocId = getIntent().getStringExtra("CHILD_DOC_ID");
        if (childDocId == null || childDocId.isEmpty()) { /* Handle error, finish() */ return; }
        Log.d(TAG, "Received Child ID: " + childDocId);


        setupButtonClickListeners();
        loadChildData(); // Just load basic info now


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


    private void setupButtonClickListeners() {
        Log.d(TAG, "Setting up button listeners...");

        if(buttonGoToLog != null) {
            buttonGoToLog.setOnClickListener(v -> { /* Start LogVaccineActivity Intent */
                if (childDocId != null) {
                    Intent intent = new Intent(ChildDetailActivity.this, LogVaccineActivity.class);
                    intent.putExtra("CHILD_DOC_ID", childDocId);
                    startActivity(intent);
                } else { Toast.makeText(this, "Child ID missing.", Toast.LENGTH_SHORT).show(); }
            });
        }

        if(buttonGoToHistory != null) {
            buttonGoToHistory.setOnClickListener(v -> { /* Start HistoryActivity Intent */
                Log.d(TAG, "History button clicked.");
                if (childDocId != null) {
                    Intent intent = new Intent(ChildDetailActivity.this, HistoryActivity.class);
                    intent.putExtra("CHILD_DOC_ID", childDocId);
                    startActivity(intent);
                } else { Toast.makeText(this, "Child ID missing.", Toast.LENGTH_SHORT).show(); }
            });
        }

        if(buttonGoToSummary != null) {
            buttonGoToSummary.setOnClickListener(v -> {
                Log.d(TAG, "Summary button clicked. Starting SummaryActivity.");
                if (childDocId != null) {
                    // Create Intent for the NEW SummaryActivity
                    Intent intent = new Intent(ChildDetailActivity.this, SummaryActivity.class);
                    // Pass the Child ID
                    intent.putExtra("CHILD_DOC_ID", childDocId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Child ID missing.", Toast.LENGTH_SHORT).show();
                }
            });
        } else { Log.e(TAG, "buttonGoToSummary is null!"); }
        // --- End Updated Summary Button ---

        if(buttonDeleteThisChild != null) {
            buttonDeleteThisChild.setOnClickListener(v -> { /* Call confirmAndDeleteChild() */
                Log.d(TAG, "Delete button clicked.");
                confirmAndDeleteChild();
            });
        }

        Log.d(TAG, "Button listeners setup complete.");
    }

    // Load Specific Child's Basic Data - from userrrrrr

    private void loadChildData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || childDocId == null) { /*...*/ return; }
        String userId = currentUser.getUid();
        Log.d(TAG, "Loading child data from path: users/" + userId + "/children/" + childDocId);
        db.collection("users").document(userId).collection("children").document(childDocId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) { /*...*/ return; }
                    if (snapshot != null && snapshot.exists()) {
                        currentChild = snapshot.toObject(Child.class);
                        if (currentChild != null) {
                            currentChild.documentId = snapshot.getId();
                            updateChildInfoUI(); // Update TextViews
                        } else { /*...*/ }
                    } else { /*...*/
                        updateChildInfoUI();
                        if(buttonDeleteThisChild != null) buttonDeleteThisChild.setEnabled(false);
                    }
                });
    }

    private void updateChildInfoUI() {
        if (currentChild != null) {
            if(textViewDetailName!=null) textViewDetailName.setText(currentChild.getName() != null ? currentChild.getName() : "N/A");
            if(textViewDetailDob!=null) textViewDetailDob.setText("DOB: " + formatDate(currentChild.getDateOfBirth()));
            if(textViewDetailAge!=null) textViewDetailAge.setText(calculateAge(currentChild.getDateOfBirth()));
            if (getSupportActionBar() != null) { getSupportActionBar().setTitle(currentChild.getName() != null ? currentChild.getName() : "Child Details"); }
            if(buttonDeleteThisChild != null) buttonDeleteThisChild.setEnabled(true); // Re-enable if data loads
        } else {
            if(textViewDetailName!=null) textViewDetailName.setText("Child Not Found");
            if(textViewDetailDob!=null) textViewDetailDob.setText("DOB: N/A");
            if(textViewDetailAge!=null) textViewDetailAge.setText("Age: N/A");
            if (getSupportActionBar() != null) { getSupportActionBar().setTitle("Child Details"); }
            if(buttonDeleteThisChild != null) buttonDeleteThisChild.setEnabled(false);
        }
    }


    private void confirmAndDeleteChild() { /* ... Same as before ... */
        if (currentChild == null || childDocId == null) { return; }
        String childName = currentChild.getName() != null ? currentChild.getName() : "this child";
        new AlertDialog.Builder(this)
                .setTitle("Delete Child")
                .setMessage("Are you sure you want to delete " + childName + "?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("DELETE", (dialog, whichButton) -> deleteChildFromFirestore())
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void deleteChildFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || childDocId == null) { return; }
        String userId = currentUser.getUid();
        String nameForToast = (currentChild != null && currentChild.getName() != null) ? currentChild.getName() : "Child";
        Log.w(TAG, "Reminder: Associated vaccine logs are NOT being deleted yet!");
        db.collection("users").document(userId).collection("children").document(childDocId)
                .delete()
                .addOnSuccessListener(aVoid -> { finish(); Toast.makeText(this, nameForToast + " deleted.", Toast.LENGTH_SHORT).show(); })
                .addOnFailureListener(e -> { /* Error Toast */ });
    }

    private String calculateAge(Date birthDate) {
        if (birthDate == null) return "Age N/A";
        Calendar dob = Calendar.getInstance(); dob.setTime(birthDate);
        Calendar today = Calendar.getInstance();
        int years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        int months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
        if (today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) { months--; }
        if (months < 0) { years--; months += 12; }
        if (years > 0) return years + " yr" + (years > 1 ? "s" : "");
        if (months > 0) return months + " mo" + (months > 1 ? "s" : "");
        long diffInMillis = today.getTimeInMillis() - dob.getTimeInMillis();
        long days = TimeUnit.MILLISECONDS.toDays(diffInMillis); if (days < 0) days = 0;
        return days + " day" + (days != 1 ? "s" : "") + " old";
    }
    private String formatDate(Date date) {
        if (date == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        return sdf.format(date);
    }

}