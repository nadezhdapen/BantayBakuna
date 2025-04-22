package com.example.bantaybakuna.Data;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class AppNotification {

    @Exclude
    public String documentId;

    public String message;
    @ServerTimestamp
    public Date timestamp;
    public boolean isRead;
    public String relatedChildId;
    public String relatedVaccine;

    public AppNotification() {}

    public AppNotification(String message, boolean isRead, String relatedChildId, String relatedVaccine) {
        this.message = message;
        this.isRead = isRead;
        this.relatedChildId = relatedChildId;
        this.relatedVaccine = relatedVaccine;
    }

    // Getters (Optional but good practice)
    public String getDocumentId() { return documentId; }
    public String getMessage() { return message; }
    public Date getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
    public String getRelatedChildId() { return relatedChildId; }
    public String getRelatedVaccine() { return relatedVaccine; }

    // Setters might be needed if you update 'isRead' later
    public void setRead(boolean read) { isRead = read; }
}