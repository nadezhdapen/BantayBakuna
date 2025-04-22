package com.example.bantaybakuna;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

// Firebase & Data
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.bantaybakuna.Data.VaccineLog;

// Java Utilities
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LogVaccineActivity extends AppCompatActivity {

    private static final String TAG = "LogVaccineActivity";

    private Toolbar toolbarLogVaccine;
    private TextView textViewLogForChildName, textViewLogSelectedDate, textViewLogSelectedTime;
    private Spinner spinnerVaccineName;
    private Button buttonLogSelectDate, buttonLogSelectTime, buttonLogSaveVaccine;
    private EditText editTextBatchNumber, editTextClinicDoctor;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String childDocId;
    private Calendar selectedDateCalendar;
    private ArrayAdapter<String> vaccineAdapter;

    // Vaccine List
    private static final String[] VACCINE_NAMES = { "-- Select Vaccine --", "BCG Vaccine (1 Dose)", "Hepatitis B Vaccine (Dose 1)", "Pentavalent Vaccine (Dose 1)", "Oral Polio Vaccine (OPV) (Dose 1)", "Pneumococcal Conjugate Vaccine (PCV) (Dose 1)", "Pentavalent Vaccine (Dose 2)", "Oral Polio Vaccine (OPV) (Dose 2)", "Pneumococcal Conjugate Vaccine (PCV) (Dose 2)", "Pentavalent Vaccine (Dose 3)", "Oral Polio Vaccine (OPV) (Dose 3)", "Inactivated Polio Vaccine (IPV) (Dose 1)", "Pneumococcal Conjugate Vaccine (PCV) (Dose 3)", "MMR Vaccine (Dose 1)", "MMR Vaccine (Dose 2)", "Influenza (Seasonal)", "Varicella (Chickenpox)", "Hepatitis A", "Japanese Encephalitis (JE)", "Rotavirus", "DTP Booster", "OPV Booster", "Td / Tdap", "HPV"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_vaccine);
        Log.d(TAG, "onCreate: Starting.");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        selectedDateCalendar = Calendar.getInstance();

        Log.d(TAG, "onCreate: Finding views...");
        toolbarLogVaccine = findViewById(R.id.toolbarLogVaccine);
        textViewLogForChildName = findViewById(R.id.textViewLogForChildName);
        spinnerVaccineName = findViewById(R.id.spinnerVaccineName);
        textViewLogSelectedDate = findViewById(R.id.textViewLogSelectedDate);
        buttonLogSelectDate = findViewById(R.id.buttonLogSelectDate);
        textViewLogSelectedTime = findViewById(R.id.textViewLogSelectedTime);
        buttonLogSelectTime = findViewById(R.id.buttonLogSelectTime);
        editTextBatchNumber = findViewById(R.id.editTextBatchNumber);
        editTextClinicDoctor = findViewById(R.id.editTextClinicDoctor);
        buttonLogSaveVaccine = findViewById(R.id.buttonLogSaveVaccine);
        Log.d(TAG, "onCreate: Views found.");

        setSupportActionBar(toolbarLogVaccine);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        childDocId = getIntent().getStringExtra("CHILD_DOC_ID");
        if (childDocId == null || childDocId.isEmpty()) { /* Handle error */ finish(); return; }
        Log.d(TAG, "Received Child ID: " + childDocId);

        fetchAndDisplayChildName();
        populateVaccineSpinner();
        setupClickListeners();
        updateDateLabel(); // Initialize labels
        updateTimeLabel();

        Log.d(TAG, "onCreate: Setup complete.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchAndDisplayChildName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && childDocId != null) {  }
    }

    private void populateVaccineSpinner() {
        vaccineAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, VACCINE_NAMES);
        vaccineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerVaccineName != null) spinnerVaccineName.setAdapter(vaccineAdapter);
    }

    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners...");
        if (buttonLogSelectDate != null) {
            buttonLogSelectDate.setOnClickListener(view -> showDatePickerDialog());
        } else { Log.e(TAG, "buttonLogSelectDate is null!"); }

        // Listener for Time Button
        if (buttonLogSelectTime != null) {
            buttonLogSelectTime.setOnClickListener(view -> showTimePickerDialog());
        } else { Log.e(TAG, "buttonLogSelectTime is null!"); }

        if (buttonLogSaveVaccine != null) {
            buttonLogSaveVaccine.setOnClickListener(view -> saveVaccineLog());
        } else { Log.e(TAG, "buttonLogSaveVaccine is null!"); }
        Log.d(TAG, "Click listeners setup complete.");
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            int hour = selectedDateCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = selectedDateCalendar.get(Calendar.MINUTE);

            selectedDateCalendar.set(Calendar.YEAR, year);
            selectedDateCalendar.set(Calendar.MONTH, month);
            selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            selectedDateCalendar.set(Calendar.HOUR_OF_DAY, hour);
            selectedDateCalendar.set(Calendar.MINUTE, minute);

            Log.d(TAG, "Date selected: " + (month + 1) + "/" + dayOfMonth + "/" + year);
            updateDateLabel();
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                LogVaccineActivity.this, dateSetListener,
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH));
        // Allow future dates
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        int hour = selectedDateCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDateCalendar.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener timeSetListener = (view, selectedHour, selectedMinute) -> {
            selectedDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            selectedDateCalendar.set(Calendar.MINUTE, selectedMinute);
            selectedDateCalendar.set(Calendar.SECOND, 0);
            Log.d(TAG, "Time selected: " + selectedHour + ":" + selectedMinute);
            updateTimeLabel();
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, timeSetListener, hour, minute, false); // false = 12hr AM/PM
        timePickerDialog.show();
        Log.d(TAG, "TimePickerDialog shown.");
    }

    private void updateDateLabel() {
        String myFormat = "MMMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        if (textViewLogSelectedDate != null) { textViewLogSelectedDate.setText(sdf.format(selectedDateCalendar.getTime())); }
    }

    private void updateTimeLabel() {
        String timeFormat = "hh:mm a"; // e.g., 09:30 AM
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.getDefault());
        if (textViewLogSelectedTime != null) {
            textViewLogSelectedTime.setText(sdf.format(selectedDateCalendar.getTime()));
            Log.d(TAG, "Time label updated: " + textViewLogSelectedTime.getText());
        }
    }


    private void saveVaccineLog() {
        Log.d(TAG, "Attempting to save vaccine log...");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) { /*...*/ return; }
        String userId = currentUser.getUid();
        String vaccineName = "";
        if (spinnerVaccineName != null && spinnerVaccineName.getSelectedItemPosition() > 0) { vaccineName = spinnerVaccineName.getSelectedItem().toString(); }
        Date dateAdministered = null;
        String dateHint = getString(R.string.select_date_administered_hint);
        if (textViewLogSelectedDate != null && !textViewLogSelectedDate.getText().toString().isEmpty() && !textViewLogSelectedDate.getText().toString().equals(dateHint)) {

            String timeHint = "Tap button to set time (defaults to now)";
            if (textViewLogSelectedTime != null && !textViewLogSelectedTime.getText().toString().isEmpty() && !textViewLogSelectedTime.getText().toString().equals(timeHint)) {
                dateAdministered = selectedDateCalendar.getTime();
            } else {
                Calendar dateOnlyCalendar = Calendar.getInstance();
                dateOnlyCalendar.setTime(selectedDateCalendar.getTime());
                dateOnlyCalendar.set(Calendar.HOUR_OF_DAY, 12);
                dateOnlyCalendar.set(Calendar.MINUTE, 0);
                dateOnlyCalendar.set(Calendar.SECOND, 0);
                dateAdministered = dateOnlyCalendar.getTime();
                Log.d(TAG, "Time not explicitly set, using default time for date: " + dateAdministered);
            }
        }
        String batchNumber = editTextBatchNumber != null ? editTextBatchNumber.getText().toString().trim() : "";
        String clinicDoctor = editTextClinicDoctor != null ? editTextClinicDoctor.getText().toString().trim() : "";

        if (vaccineName.isEmpty()) {  return; }
        if (dateAdministered == null) { return; }

        VaccineLog newLog = new VaccineLog(childDocId, vaccineName, dateAdministered, batchNumber, clinicDoctor);

        Log.d(TAG, "Saving log to Firestore: " + newLog.vaccineName + " @ " + dateAdministered); // Log the full timestamp
        db.collection("users").document(userId).collection("vaccine_logs")
                .add(newLog)
                .addOnSuccessListener(documentReference -> { finish(); Toast.makeText(this,"Saved!",Toast.LENGTH_SHORT).show(); })
                .addOnFailureListener(e -> { Toast.makeText(this,"Error saving.",Toast.LENGTH_SHORT).show(); });
    }
}