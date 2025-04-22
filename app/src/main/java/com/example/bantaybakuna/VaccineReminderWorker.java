package com.example.bantaybakuna;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.bantaybakuna.Data.AppNotification;
import com.example.bantaybakuna.Data.VaccineLog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class VaccineReminderWorker extends Worker {

    private static final String TAG = "VaccineReminderWorker";
    private Context context;
    private SharedPreferences notifPrefs;


    private static final long MIN_5_MS = TimeUnit.MINUTES.toMillis(5);
    private static final long MIN_15_MS = TimeUnit.MINUTES.toMillis(15);
    private static final long HOUR_1_MS = TimeUnit.HOURS.toMillis(1);
    private static final long HOUR_3_MS = TimeUnit.HOURS.toMillis(3);

    public VaccineReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.notifPrefs = context.getSharedPreferences("BANTAY_BAKUNA_NOTIF_STATUS", Context.MODE_PRIVATE);
        Log.i(TAG, "Worker constructor called.");
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "doWork() method STARTED.");

        // 1. Get User ID
        String userId = getUserIdFromPrefs();
        if (userId == null) { Log.w(TAG, "Worker stopping: No User ID."); return Result.success(); }
        Log.d(TAG, "Checking vaccines for user ID: " + userId);

        Calendar calStart = Calendar.getInstance();
        Date startDate = calStart.getTime();


        Log.d(TAG, "Checking logs scheduled after: " + formatDateForLog(startDate));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("users").document(userId).collection("vaccine_logs")
                .whereGreaterThan("dateAdministered", startDate)
                .orderBy("dateAdministered", Query.Direction.ASCENDING);

        try {
            Log.d(TAG, "Executing Firestore query for future logs...");
            Task<QuerySnapshot> task = query.get();
            QuerySnapshot snapshots = Tasks.await(task);

            if (snapshots == null || snapshots.isEmpty()) {
                Log.i(TAG, "No future scheduled vaccine logs found for user " + userId);
                return Result.success();
            }

            Log.i(TAG, "Found " + snapshots.size() + " future logs. Checking reminder windows...");
            int notificationIdCounter = 7000; // Base ID

            // 4. Process each future log
            for (QueryDocumentSnapshot doc : snapshots) {
                VaccineLog log = doc.toObject(VaccineLog.class);
                if (log != null) {
                    log.setDocumentId(doc.getId());
                    checkAndSendReminders(log, notificationIdCounter++, db, userId); // Pass db and userId
                } else {
                    Log.w(TAG, "Failed to parse future log document: " + doc.getId());
                }
            }

            Log.i(TAG, "Worker finished processing successfully.");
            return Result.success();

        } catch (ExecutionException | InterruptedException e) { /* Log errors, check index */ Log.e(TAG,"Query error", e); return Result.failure(); }
        catch (Exception e) { Log.e(TAG, "Unexpected error in worker.", e); return Result.failure(); }

    }

    private void checkAndSendReminders(VaccineLog log, int baseNotificationId, FirebaseFirestore db, String userId) {
        if (log == null || log.dateAdministered == null || log.documentId == null) {
            Log.w(TAG,"Skipping reminder check: Invalid log data.");
            return;
        }

        long nowMillis = System.currentTimeMillis();
        long scheduledMillis = log.dateAdministered.getTime();
        long millisUntil = scheduledMillis - nowMillis;

        if (millisUntil <= 0) return;

        String logDocId = log.getDocumentId();



        // 3 Hour Window Check
        String key3h = "sent_" + logDocId + "_3h"; // Use logDocId
        if (millisUntil <= HOUR_3_MS && !notifPrefs.getBoolean(key3h, false)) {
            Log.d(TAG,"Within 3 hour window for " + log.vaccineName);
            showNotification(log, "in about 3 hours", baseNotificationId + 1, db, userId); // Pass db, userId
            notifPrefs.edit().putBoolean(key3h, true).apply();
            return;
        }

        // 1 Hour Window Check
        String key1h = "sent_" + logDocId + "_1h"; // Use logDocId
        if (millisUntil <= HOUR_1_MS && !notifPrefs.getBoolean(key1h, false)) {
            Log.d(TAG,"Within 1 hour window for " + log.vaccineName);
            showNotification(log, "in about 1 hour", baseNotificationId + 2, db, userId); // Pass db, userId
            notifPrefs.edit().putBoolean(key1h, true).apply();
            notifPrefs.edit().putBoolean(key3h, true).apply(); // Also mark 3h sent
            return;
        }

        // 15 Minute Window Check
        String key15m = "sent_" + logDocId + "_15m"; // Use logDocId
        if (millisUntil <= MIN_15_MS && !notifPrefs.getBoolean(key15m, false)) {
            Log.d(TAG,"Within 15 min window for " + log.vaccineName);
            showNotification(log, "in 15 minutes", baseNotificationId + 3, db, userId); // Pass db, userId
            notifPrefs.edit().putBoolean(key15m, true).apply();
            notifPrefs.edit().putBoolean(key1h, true).putBoolean(key3h, true).apply(); // Mark previous sent
            return;
        }

        // 5 Minute Window Check
        String key5m = "sent_" + logDocId + "_5m"; // Use logDocId
        if (millisUntil <= MIN_5_MS && !notifPrefs.getBoolean(key5m, false)) {
            Log.d(TAG,"Within 5 min window for " + log.vaccineName);
            showNotification(log, "in 5 minutes", baseNotificationId + 4, db, userId); // Pass db, userId
            notifPrefs.edit().putBoolean(key5m, true).apply();
            notifPrefs.edit().putBoolean(key15m, true).putBoolean(key1h, true).putBoolean(key3h, true).apply(); // Mark previous sent
            return;
        }
    }
    private void showNotification(VaccineLog log, String timeText, int notificationId, FirebaseFirestore db, String userId) {
        if (context == null || log == null || log.childId == null || log.documentId == null) { return; }
        Log.d(TAG,"Preparing notification (" + timeText + ") for: " + log.vaccineName);

        // Create Message String
        SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String timeString = sdfTime.format(log.dateAdministered);
        String message = log.vaccineName + " due " + timeText + " (around " + timeString + ")";

        // 1. Save Record to Firestore (pass db and userId)
        saveNotificationRecord(message, log, db, userId);

        // 2. Show System Notification
        Intent intent = new Intent(context, ChildDetailActivity.class);
        intent.putExtra("CHILD_DOC_ID", log.childId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.VACCINE_REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("Vaccine Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // Check Permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Permission denied. Cannot show system notification.");
            return;
        }
        int uniqueSystemNotificationId = (log.documentId + "_" + timeText).hashCode();
        notificationManager.notify(uniqueSystemNotificationId, builder.build());
        Log.d(TAG, "System Notification shown for: " + log.vaccineName + " ("+timeText+") ID: " + uniqueSystemNotificationId);
    }

    private void saveNotificationRecord(String message, VaccineLog relatedLog, FirebaseFirestore db, String userId) {
        // Use the passed 'db' and 'userId'
        if (userId != null && message != null && relatedLog != null && relatedLog.childId != null) {
            AppNotification notificationRecord = new AppNotification(message, false, relatedLog.childId, relatedLog.vaccineName);
            db.collection("users").document(userId).collection("app_notifications")
                    .add(notificationRecord)
                    .addOnSuccessListener(docRef -> Log.d(TAG, "Notif record saved: " + docRef.getId()))
                    .addOnFailureListener(e -> Log.e(TAG, "Error saving notif record", e));
        } else { Log.w(TAG, "Could not save notification record: User/Msg/Log null."); }
    }

    private String getUserIdFromPrefs() {
        SharedPreferences mainPrefs = context.getSharedPreferences("BANTAY_BAKUNA_PREFS", Context.MODE_PRIVATE);
        return mainPrefs.getString("CURRENT_USER_ID", null);
    }


    private long daysBetween(Date date1, Date date2) {
        return 0;
    }


    private String formatDateForLog(Date date) {
        return "";
    }

}