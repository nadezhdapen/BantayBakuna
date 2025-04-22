package com.example.bantaybakuna.Data;

public class VaccineStatusItem {
    public String milestoneLabel;
    public String vaccineName;
    public String status; // e.g., "DONE", "UPCOMING", "MISSED", "FUTURE", "SCHEDULED"
    public String dateInfo; // e.g., "Administered: Apr 22", "Due: May 10"

    public VaccineStatusItem(String milestoneLabel, String vaccineName, String status, String dateInfo) {
        this.milestoneLabel = milestoneLabel;
        this.vaccineName = vaccineName;
        this.status = status;
        this.dateInfo = dateInfo;
    }

}