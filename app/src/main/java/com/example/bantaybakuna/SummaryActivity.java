package com.example.bantaybakuna;

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
import com.example.bantaybakuna.Data.Child;
import com.example.bantaybakuna.Data.VaccineLog;
import com.example.bantaybakuna.Data.EpiScheduleEntry;
import com.example.bantaybakuna.Data.VaccineStatusItem;
import com.example.bantaybakuna.Data.EpiScheduleHelper;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class SummaryActivity extends AppCompatActivity {

    private static final String TAG = "SummaryActivity";

    private Toolbar toolbarSummary;
    private RecyclerView recyclerViewSummaryStatusList;

    private VaccineStatusAdapter statusAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String childDocId;
    private Child currentChild = null;
    private List<VaccineLog> currentLogs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        Log.d(TAG, "onCreate: Starting.");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        toolbarSummary = findViewById(R.id.toolbarSummary);
        recyclerViewSummaryStatusList = findViewById(R.id.recyclerViewSummaryStatusList);
        Log.d(TAG, "onCreate: Views found.");

        setSupportActionBar(toolbarSummary);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Vaccine Status Summary");
        } else { Log.e(TAG, "Toolbar not found!"); }

        childDocId = getIntent().getStringExtra("CHILD_DOC_ID");
        if (childDocId == null || childDocId.isEmpty()) {
            Log.e(TAG, "CRITICAL ERROR: CHILD_DOC_ID not passed.");
            Toast.makeText(this, "Error: Child ID missing.", Toast.LENGTH_LONG).show();
            finish(); return;
        }
        Log.d(TAG, "Received Child ID: " + childDocId);

        setupRecyclerView();

        loadChildDetails();
        loadVaccineLogs();

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
        Log.d(TAG, "Setting up status RecyclerView...");
        if (recyclerViewSummaryStatusList == null) { Log.e(TAG, "recyclerViewSummaryStatusList not found!"); return; }
        // Initialize the VaccineStatusAdapter (Make sure VaccineStatusAdapter.java exists!)
        statusAdapter = new VaccineStatusAdapter(new VaccineStatusAdapter.StatusDiff());
        recyclerViewSummaryStatusList.setAdapter(statusAdapter);
        recyclerViewSummaryStatusList.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, "Status RecyclerView setup complete.");
    }
    private void loadChildDetails() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || childDocId == null) { Log.e(TAG,"LoadChildDetails: User/ChildID null"); return; }
        String userId = currentUser.getUid();
        Log.d(TAG, "Summary: Loading child details...");

        db.collection("users").document(userId).collection("children").document(childDocId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && snapshot.exists()) {
                        currentChild = snapshot.toObject(Child.class);
                        if (currentChild != null) {
                            currentChild.documentId = snapshot.getId();
                            Log.d(TAG, "Summary: Child details loaded: " + currentChild.getName());
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(currentChild.getName() + " - Status");
                            }
                            calculateAndDisplayStatus(); // Attempt calculation now
                        } else { Log.e(TAG, "Summary: Failed to parse Child object"); currentChild=null; calculateAndDisplayStatus(); }
                    } else { Log.w(TAG,"Summary: Child document not found"); currentChild=null; calculateAndDisplayStatus(); }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Summary: Failed load child details", e);
                    currentChild = null; calculateAndDisplayStatus(); // Try calc with null child (will show future/missed)
                    Toast.makeText(this, "Error loading child info.", Toast.LENGTH_SHORT).show();
                });
    }
    private void loadVaccineLogs() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || childDocId == null) { Log.e(TAG,"LoadVaccineLogs: User/ChildID null"); return; }
        String userId = currentUser.getUid();
        Log.d(TAG, "Summary: Loading vaccine logs...");

        db.collection("users").document(userId).collection("vaccine_logs")
                .whereEqualTo("childId", childDocId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<VaccineLog> logsList = new ArrayList<>();
                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            VaccineLog log = doc.toObject(VaccineLog.class);
                            if(log != null) { logsList.add(log); }
                            else { Log.w(TAG, "Summary: Failed to parse log: " + doc.getId()); }
                        }
                    } else { Log.d(TAG, "Summary: No logs found for child."); }
                    currentLogs = logsList; // Store the logs
                    Log.d(TAG, "Summary: Vaccine logs loaded: " + currentLogs.size());
                    calculateAndDisplayStatus(); // Attempt calculation
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Summary: Failed load vaccine logs", e);
                    currentLogs = new ArrayList<>(); // Use empty list on error
                    calculateAndDisplayStatus(); // Still try calculation
                    Toast.makeText(this, "Error loading history data.", Toast.LENGTH_SHORT).show();
                });
    }
    private void calculateAndDisplayStatus() {
        Log.d(TAG, "Attempting to calculate vaccine status...");
        // Only run if both child (with DOB) and logs list are ready
        if (currentChild == null || currentLogs == null || currentChild.getDateOfBirth() == null) {
            Log.d(TAG, "Status calc skipped: Data not ready (Child:" + (currentChild!=null) + ", Logs:" + (currentLogs!=null) + ", DOB:" + (currentChild != null && currentChild.getDateOfBirth() != null)+")");
            return;
        }
        if (statusAdapter == null) { Log.e(TAG,"statusAdapter is null!"); return; }
        Log.d(TAG, "Calculating status for: " + currentChild.getName());

        List<VaccineStatusItem> statusItems = new ArrayList<>();
        Date dob = currentChild.getDateOfBirth();
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormatForInfo = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        List<EpiScheduleEntry> schedule = EpiScheduleHelper.getSchedule(); // Get schedule from helper

        // Loop through schedule
        for (EpiScheduleEntry scheduleEntry : schedule) {
            String targetVaccineName = scheduleEntry.vaccineName;
            boolean isDone = false;
            Date administeredDate = null;

            // 1. Check logs
            for (VaccineLog log : currentLogs) {
                if (log.vaccineName != null && log.vaccineName.equalsIgnoreCase(targetVaccineName)) {
                    isDone = true;
                    administeredDate = log.dateAdministered;
                    break;
                }
            }

            // 2. Determine Status
            String status; String dateInfo;
            Calendar approxDueDate = Calendar.getInstance(); approxDueDate.setTime(dob);
            approxDueDate.add(Calendar.WEEK_OF_YEAR, scheduleEntry.dueWeeks);
            Calendar gracePeriodEnd = Calendar.getInstance(); gracePeriodEnd.setTime(approxDueDate.getTime());
            gracePeriodEnd.add(Calendar.WEEK_OF_YEAR, 4); // Example grace period

            if (isDone && administeredDate != null) {
                if (administeredDate.after(today.getTime())) {
                    status = "SCHEDULED";
                    dateInfo = "Scheduled: " + dateFormatForInfo.format(administeredDate);
                } else {
                    status = "DONE";
                    dateInfo = "Administered: " + dateFormatForInfo.format(administeredDate);
                }
            } else if (today.after(gracePeriodEnd)) {
                status = "MISSED";
                dateInfo = "Was Due: " + dateFormatForInfo.format(approxDueDate.getTime());
            } else if (today.after(approxDueDate)) {
                status = "UPCOMING";
                dateInfo = "Due Around: " + dateFormatForInfo.format(approxDueDate.getTime());
            } else {
                status = "FUTURE";
                dateInfo = "Due Around: " + dateFormatForInfo.format(approxDueDate.getTime());
            }

            // 3. Add to list
            statusItems.add(new VaccineStatusItem(
                    scheduleEntry.milestoneLabel, scheduleEntry.vaccineName, status, dateInfo
            ));
        }
        Log.d(TAG, "Submitting " + statusItems.size() + " status items to adapter.");
        statusAdapter.submitList(statusItems);

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