package com.example.bantaybakuna; // **** REPLACE PACKAGE NAME ****

// --- Imports ---
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.bantaybakuna.Data.AppNotification;

import java.util.ArrayList;
import java.util.List;

public class NotificationListActivity extends AppCompatActivity {

    private static final String TAG = "NotificationListActivity";

    private Toolbar toolbarNotificationList;
    private RecyclerView recyclerViewAppNotifications;

    private AppNotificationAdapter notificationAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);
        Log.d(TAG, "onCreate: Starting.");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        toolbarNotificationList = findViewById(R.id.toolbarNotificationList);
        recyclerViewAppNotifications = findViewById(R.id.recyclerViewAppNotifications); // Find RecyclerView
        Log.d(TAG, "onCreate: Views found.");

        setSupportActionBar(toolbarNotificationList);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Notification History");
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            Log.d(TAG, "User ID: " + currentUserId);

            setupRecyclerView();

            loadNotifications();
        } else {
            Log.e(TAG, "User is null! Cannot load notifications.");
            Toast.makeText(this, "Login required.", Toast.LENGTH_SHORT).show();
            finish();
        }

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



    private void setupRecyclerView() {
        Log.d(TAG, "Setting up notifications RecyclerView...");
        if (recyclerViewAppNotifications == null) { Log.e(TAG, "recyclerViewAppNotifications is null!"); return; }


        notificationAdapter = new AppNotificationAdapter(new AppNotificationAdapter.NotificationDiff());
        recyclerViewAppNotifications.setAdapter(notificationAdapter);
        recyclerViewAppNotifications.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, "Notifications RecyclerView setup complete.");
    }

    private void loadNotifications() {
        if (currentUserId == null) { Log.e(TAG, "Cannot load notifications, userId is null."); return; }
        Log.d(TAG, "Loading notifications for user: " + currentUserId);

        // Path: users/{userId}/app_notifications - Order by timestamp descending (newest first)
        db.collection("users").document(currentUserId).collection("app_notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50) // Optional: Limit number of notifications shown
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed for app_notifications.", error);
                        Toast.makeText(NotificationListActivity.this, "Error loading notifications.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<AppNotification> notificationList = new ArrayList<>();
                    if (snapshots != null) {
                        Log.d(TAG, "Processing " + snapshots.size() + " notification documents.");
                        for (QueryDocumentSnapshot doc : snapshots) {
                            AppNotification notification = doc.toObject(AppNotification.class);
                            if (notification != null) {
                                notification.documentId = doc.getId();
                                notificationList.add(notification);
                            } else {
                                Log.w(TAG, "Failed to parse notification document: " + doc.getId());
                            }
                        }
                    } else {
                        Log.d(TAG, "Notifications snapshot is null.");
                    }

                    // Update the adapter
                    if (notificationAdapter != null) {
                        notificationAdapter.submitList(notificationList);
                        Log.d(TAG, "Submitted " + notificationList.size() + " notifications to adapter.");
                    } else {
                        Log.e(TAG, "notificationAdapter is null!");
                    }
                });
    }

}