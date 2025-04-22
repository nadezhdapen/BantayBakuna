package com.example.bantaybakuna.Data;
import com.example.bantaybakuna.Data.EpiScheduleEntry;
import java.util.ArrayList;
import java.util.List;

public class EpiScheduleHelper {

    private static final List<EpiScheduleEntry> schedule = new ArrayList<>();

    static {
        // At Birth (Week 0)
        schedule.add(new EpiScheduleEntry("At Birth", "BCG Vaccine (1 Dose)", 0));
        schedule.add(new EpiScheduleEntry("At Birth", "Hepatitis B Vaccine (Dose 1)", 0));
        // 6 Weeks
        schedule.add(new EpiScheduleEntry("6 Weeks", "Pentavalent Vaccine (Dose 1)", 6));
        schedule.add(new EpiScheduleEntry("6 Weeks", "Oral Polio Vaccine (OPV) (Dose 1)", 6));
        schedule.add(new EpiScheduleEntry("6 Weeks", "Pneumococcal Conjugate Vaccine (PCV) (Dose 1)", 6));
        // 10 Weeks
        schedule.add(new EpiScheduleEntry("10 Weeks", "Pentavalent Vaccine (Dose 2)", 10));
        schedule.add(new EpiScheduleEntry("10 Weeks", "Oral Polio Vaccine (OPV) (Dose 2)", 10));
        schedule.add(new EpiScheduleEntry("10 Weeks", "Pneumococcal Conjugate Vaccine (PCV) (Dose 2)", 10));
        // 14 Weeks
        schedule.add(new EpiScheduleEntry("14 Weeks", "Pentavalent Vaccine (Dose 3)", 14));
        schedule.add(new EpiScheduleEntry("14 Weeks", "Oral Polio Vaccine (OPV) (Dose 3)", 14));
        schedule.add(new EpiScheduleEntry("14 Weeks", "Inactivated Polio Vaccine (IPV) (Dose 1)", 14));
        schedule.add(new EpiScheduleEntry("14 Weeks", "Pneumococcal Conjugate Vaccine (PCV) (Dose 3)", 14));
        // 9 Months (~39 Weeks)
        schedule.add(new EpiScheduleEntry("9 Months", "MMR Vaccine (Dose 1)", 39));
        // 12 Months (~52 Weeks)
        schedule.add(new EpiScheduleEntry("12 Months", "MMR Vaccine (Dose 2)", 52));

    }

    // Method to get the full schedule
    public static List<EpiScheduleEntry> getSchedule() {
        return schedule; // Return the populated list
    }
}