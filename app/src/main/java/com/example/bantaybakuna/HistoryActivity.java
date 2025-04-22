package com.example.bantaybakuna; // **** REPLACE PACKAGE NAME ****

// --- Imports ---
import androidx.appcompat.app.AlertDialog; // Import AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context; // Import Context for SharedPreferences
import android.content.DialogInterface; // Import DialogInterface
import android.content.SharedPreferences; // Import SharedPreferences
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

// Firebase & Data
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.bantaybakuna.Data.VaccineLog;
import com.example.bantaybakuna.R;
import com.example.bantaybakuna.VaccineLogAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity implements VaccineLogAdapter.OnLogInteractionListener {

    private static final String TAG = "HistoryActivity";
    private Toolbar toolbarHistory;
    private RecyclerView recyclerViewHistoryList;

    //Data & Firebase Variables
    private VaccineLogAdapter logAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String childDocId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Log.d(TAG, "onCreate: Starting.");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        toolbarHistory = findViewById(R.id.toolbarHistory);
        recyclerViewHistoryList = findViewById(R.id.recyclerViewHistoryList);
        Log.d(TAG, "onCreate: Views found.");

        setSupportActionBar(toolbarHistory);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        childDocId = getIntent().getStringExtra("CHILD_DOC_ID");
        if (childDocId == null || childDocId.isEmpty()) { /* Handle error */ finish(); return; }
        Log.d(TAG, "Received Child ID: " + childDocId);

        setupRecyclerView();
        loadHistoryData();

        Log.d(TAG, "onCreate: Setup complete.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up history RecyclerView...");
        if (recyclerViewHistoryList == null) { Log.e(TAG, "recyclerViewHistoryList not found!"); return; }
        // Initialize adapter, passing 'this' as the listener
        logAdapter = new VaccineLogAdapter(new VaccineLogAdapter.LogDiff(), this);         recyclerViewHistoryList.setAdapter(logAdapter);
        recyclerViewHistoryList.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, "History RecyclerView setup complete.");
    }

    private void loadHistoryData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || childDocId == null) { /*...*/ return; }
        String userId = currentUser.getUid();
        Log.d(TAG, "Loading vaccine logs for user: " + userId + ", child: " + childDocId);

        db.collection("users").document(userId).collection("vaccine_logs")
                .whereEqualTo("childId", childDocId)
                .orderBy("dateAdministered", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) { /* Handle error */ return; }
                    List<VaccineLog> logsList = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            VaccineLog log = doc.toObject(VaccineLog.class);
                            if(log != null) {
                                log.setDocumentId(doc.getId()); // *** STORE THE ID ***
                                logsList.add(log);
                            } else { Log.w(TAG, "Failed to parse log: " + doc.getId()); }
                        }
                    }
                    if (logAdapter != null) logAdapter.submitList(logsList);
                    else Log.e(TAG, "logAdapter is null!");
                });
    }
    @Override
    public void onDeleteLogClick(VaccineLog logToDelete) {
        Log.d(TAG, "onDeleteLogClick triggered for log: " + logToDelete.getDocumentId());
        // Show confirmation dialog before actually deleting
        confirmAndDeleteLog(logToDelete);
    }


    private void confirmAndDeleteLog(final VaccineLog logToDelete) { // Make param final for listener
        if (logToDelete == null || logToDelete.getDocumentId() == null || logToDelete.getDocumentId().isEmpty()) {
            Log.e(TAG, "Cannot delete - log data invalid.");
            return;
        }

        // Build the confirmation message
        String vaccineName = logToDelete.getVaccineName() != null ? logToDelete.getVaccineName() : "this vaccine";
        String dateStr = "an unknown date";
        if(logToDelete.getDateAdministered() != null){
            dateStr = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(logToDelete.getDateAdministered());
        }
        String message = "Are you sure you want to delete the log for '" + vaccineName + "' on " + dateStr + "?";

        new AlertDialog.Builder(this)
                .setTitle("Delete Log Entry?")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert) // Warning icon
                .setPositiveButton("DELETE", (dialog, whichButton) -> {
                    // User clicked DELETE
                    Log.d(TAG, "User confirmed log deletion for: " + logToDelete.getDocumentId());
                    deleteLogFromFirestore(logToDelete); // Call the actual delete method
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User clicked Cancel
                    Log.d(TAG, "User cancelled log deletion.");
                    dialog.dismiss(); // Just close the dialog
                })
                .show(); // Display the dialog
    }


    //Delete Vaccine Log Document from Firestore
    private void deleteLogFromFirestore(VaccineLog logToDelete) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String logDocId = logToDelete.getDocumentId();

        // Check necessary IDs
        if (currentUser == null || childDocId == null || logDocId == null || logDocId.isEmpty()) {
            Log.e(TAG, "Cannot delete log: User/ChildID/LogID missing.");
            Toast.makeText(this, "Error: Could not delete log.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        String vaccineNameForToast = logToDelete.getVaccineName() != null ? logToDelete.getVaccineName() : "Log";

        Log.d(TAG, "Attempting Firestore delete: users/" + userId + "/vaccine_logs/" + logDocId);

        // Get reference and delete
        db.collection("users").document(userId).collection("vaccine_logs").document(logDocId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Vaccine log successfully deleted from Firestore!");
                    Toast.makeText(HistoryActivity.this, vaccineNameForToast + " log deleted.", Toast.LENGTH_SHORT).show();
                    // Clear notification flags associated with this deleted log
                    clearNotificationFlags(logDocId);
                    // List updates automatically due to the SnapshotListener in loadHistoryData
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting vaccine log from Firestore", e);
                    Toast.makeText(HistoryActivity.this, "Error deleting log.", Toast.LENGTH_SHORT).show();
                });
    }
    private void clearNotificationFlags(String logDocId) {
        if (logDocId == null || logDocId.isEmpty()) return;

        Log.d(TAG, "Clearing notification flags for deleted log ID: " + logDocId);
        // Use the same SharedPreferences file as the worker
        SharedPreferences notifPrefs = getSharedPreferences("BANTAY_BAKUNA_NOTIF_STATUS", MODE_PRIVATE);
        // Remove all potential flags for this log ID
        notifPrefs.edit()
                .remove("sent_" + logDocId + "_3h")
                .remove("sent_" + logDocId + "_1h")
                .remove("sent_" + logDocId + "_15m")
                .remove("sent_" + logDocId + "_5m")
                .apply();
    }

}