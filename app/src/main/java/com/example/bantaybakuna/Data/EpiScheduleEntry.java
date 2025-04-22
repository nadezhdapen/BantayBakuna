package com.example.bantaybakuna.Data;

public class EpiScheduleEntry {
    public String milestoneLabel;
    public String vaccineName;
    public int dueWeeks;

    public EpiScheduleEntry(String milestoneLabel, String vaccineName, int dueWeeks) {
        this.milestoneLabel = milestoneLabel;
        this.vaccineName = vaccineName;
        this.dueWeeks = dueWeeks;
    }
}