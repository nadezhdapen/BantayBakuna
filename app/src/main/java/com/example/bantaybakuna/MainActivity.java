package com.example.bantaybakuna; // **** REPLACE WITH YOUR PACKAGE NAME ****

// --- Imports ---
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy; // For Scheduling Policy
import androidx.work.OneTimeWorkRequest;        // For Test Button Action
import androidx.work.PeriodicWorkRequest;        // For Regular Scheduling
import androidx.work.WorkManager;               // For Scheduling

// Android OS / Utilities
import android.Manifest; // For Permissions
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.bantaybakuna.Data.Child;

// Java Utilities
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String VACCINE_REMINDER_CHANNEL_ID = "BANTAY_BAKUNA_VACCINE_REMINDERS";
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    private RecyclerView recyclerViewChildren;
    private CardView cardViewAddChild;
    private BottomNavigationView bottomNavigationView;
    private Button buttonTestWorker; // Temporary Test Button

    // --- Adapter and Firebase Variables ---
    private ChildAdapter childAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Activity starting.");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        Log.d(TAG, "onCreate: Finding views...");
        recyclerViewChildren = findViewById(R.id.recyclerViewChildren);
        cardViewAddChild = findViewById(R.id.cardViewAddChild);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        buttonTestWorker = findViewById(R.id.buttonTestWorker);
        Log.d(TAG, "onCreate: Views found.");


        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No user logged in! Redirecting to Login.");
            goToLogin(); return;
        }

        createNotificationChannel();
        requestNotificationPermissionIfNeeded();

        setupRecyclerView();
        setupButtonClickListeners();
        setupBottomNavigation();

        loadChildren();

        scheduleVaccineCheckWorker();

        Log.d(TAG, "onCreate: Setup complete.");
    }
    private void setupRecyclerView() {
        // ... (Keep this method exactly the same as response a487) ...
        Log.d(TAG, "Setting up RecyclerView...");
        if (recyclerViewChildren == null) { Log.e(TAG, "RecyclerView not found!"); return; }
        childAdapter = new ChildAdapter(new ChildAdapter.ChildDiff(), child -> {
            if (child != null && child.getDocumentId() != null && !child.getDocumentId().isEmpty()) {
                Intent intent = new Intent(MainActivity.this, ChildDetailActivity.class);
                intent.putExtra("CHILD_DOC_ID", child.getDocumentId());
                startActivity(intent);
            } else {  }
        });
        recyclerViewChildren.setAdapter(childAdapter);
        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, "RecyclerView setup complete.");
    }


    private void setupButtonClickListeners() {
        // Add Child Listener
        if (cardViewAddChild != null) {
            cardViewAddChild.setOnClickListener(v -> {
                Log.d(TAG, "Add Child card clicked!");
                Intent intent = new Intent(MainActivity.this, AddChildActivity.class);
                startActivity(intent);
            });
        } else { Log.e(TAG, "cardViewAddChild is null!"); }

        // Test Worker Button Listener
        if (buttonTestWorker != null) {
            buttonTestWorker.setOnClickListener(v -> {
                Log.d(TAG, "Test Worker button clicked. Enqueuing ONE-TIME work.");
                Toast.makeText(this, "Triggering background check (one time)...", Toast.LENGTH_SHORT).show();
                // Create a ONE-TIME request with NO delay
                OneTimeWorkRequest testWorkRequest =
                        new OneTimeWorkRequest.Builder(VaccineReminderWorker.class)
                                .build();
                // Enqueue it using the application context
                WorkManager.getInstance(this.getApplicationContext()).enqueue(testWorkRequest);
            });
        } else { Log.e(TAG,"buttonTestWorker is null!"); }
    }
    private void loadChildren() {
        Log.d(TAG, "Loading children from Firestore...");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) { /* ... */ return; }
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("children")
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) { /* ... */ return; }
                    List<Child> childrenList = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Child child = doc.toObject(Child.class);
                            if (child != null) { // Null check
                                child.documentId = doc.getId();
                                childrenList.add(child);
                            } else { /* Log warning */ }
                        }
                    }
                    if (childAdapter != null) childAdapter.submitList(childrenList);
                    else { /* Log error */ }
                });
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView != null) {
            Log.d(TAG, "Setting up Bottom Navigation.");
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId(); Intent intent = null; Class<?> currentClass = this.getClass();
                if (itemId == R.id.navigation_home) { if (!currentClass.equals(MainActivity.class)) { /* ... */ } else { return true; } }
                else if (itemId == R.id.navigation_messages) { if (!currentClass.equals(MessagesActivity.class)) { intent = new Intent(this, MessagesActivity.class); intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); } else { return true; } }
                else if (itemId == R.id.navigation_notifications) { if (!currentClass.equals(NotificationListActivity.class)) { intent = new Intent(this, NotificationListActivity.class); intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); } else { return true; } }
                else if (itemId == R.id.navigation_user) { if (!currentClass.equals(UserProfileActivity.class)) { intent = new Intent(this, UserProfileActivity.class); intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); } else { return true; } }
                if (intent != null) { startActivity(intent); }
                return true;
            });
        } else { Log.e(TAG, "bottomNavigationView is null!"); }
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(VACCINE_REMINDER_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int rCode, @NonNull String[] p, @NonNull int[] g) { /* ... Same as before ... */
        super.onRequestPermissionsResult(rCode, p, g);
        if (rCode == NOTIFICATION_PERMISSION_CODE) { if (g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) { /* Granted */ } else { /* Denied */ } }
    }

    private void scheduleVaccineCheckWorker() {
        Log.d(TAG, "Enqueueing periodic worker (15 mins with REPLACE)..."); // Update log

        // Use minimum interval for faster testing
        PeriodicWorkRequest reminderWorkRequest =
                new PeriodicWorkRequest.Builder(VaccineReminderWorker.class,
                        PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS) // 15 minutes
                        .build();

        WorkManager.getInstance(this.getApplicationContext()).enqueueUniquePeriodicWork(
                "vaccineReminderCheck",
                ExistingPeriodicWorkPolicy.REPLACE,
                // ExistingPeriodicWorkPolicy.KEEP,
                reminderWorkRequest);

        Log.d(TAG, "Worker enqueued (15 mins interval with REPLACE policy)."); // Update log
    }



    private void goToLogin() { /* ... Same as before ... */
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}