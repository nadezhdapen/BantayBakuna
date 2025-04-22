package com.example.bantaybakuna.Data;

import com.google.firebase.firestore.Exclude;
import java.util.Date;

public class VaccineLog {
    @Exclude
    public String documentId;

    public String childId;
    public String vaccineName;
    public Date dateAdministered;
    public String batchNumber;
    public String clinicDoctor;

    public VaccineLog() {}

    public VaccineLog(String childId, String vaccineName, Date dateAdministered, String batchNumber, String clinicDoctor) {
        this.childId = childId;
        this.vaccineName = vaccineName;
        this.dateAdministered = dateAdministered;
        this.batchNumber = batchNumber;
        this.clinicDoctor = clinicDoctor;
    }

    public String getDocumentId() { return documentId; }
    public String getChildId() { return childId; }
    public String getVaccineName() { return vaccineName; }
    public Date getDateAdministered() { return dateAdministered; }
    public String getBatchNumber() { return batchNumber; }
    public String getClinicDoctor() { return clinicDoctor; }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}