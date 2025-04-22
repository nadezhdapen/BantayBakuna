package com.example.bantaybakuna;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bantaybakuna.Data.VaccineLog;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class VaccineLogAdapter extends ListAdapter<VaccineLog, VaccineLogAdapter.LogViewHolder> {

    private final OnLogInteractionListener listener;


    public interface OnLogInteractionListener {
        void onDeleteLogClick(VaccineLog logToDelete);
    }

    public VaccineLogAdapter(@NonNull DiffUtil.ItemCallback<VaccineLog> diffCallback, OnLogInteractionListener listener) {
        super(diffCallback);
        this.listener = listener;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vaccine_history_item, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        VaccineLog currentLog = getItem(position);
        Log.d("VaccineLogAdapter", "Binding position: " + position + ", Log ID: " + (currentLog != null ? currentLog.getDocumentId() : "null"));
        if (currentLog != null) {
            holder.bind(currentLog, listener);
        }
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewHistoryIcon;
        private final TextView textViewHistoryVaccineName;
        private final TextView textViewHistoryDate;
        private final TextView textViewHistoryOptionalBatch;
        private final TextView textViewHistoryOptionalClinic;
        private final ImageButton buttonDeleteLogItem;
        private final SimpleDateFormat dateFormatter;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewHistoryIcon = itemView.findViewById(R.id.imageViewHistoryIcon);
            textViewHistoryVaccineName = itemView.findViewById(R.id.textViewHistoryVaccineName);
            textViewHistoryDate = itemView.findViewById(R.id.textViewHistoryDate);
            textViewHistoryOptionalBatch = itemView.findViewById(R.id.textViewHistoryOptionalBatch);
            textViewHistoryOptionalClinic = itemView.findViewById(R.id.textViewHistoryOptionalClinic);
            buttonDeleteLogItem = itemView.findViewById(R.id.buttonDeleteLogItem);
            dateFormatter = new SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault());
        }

        public void bind(final VaccineLog log, final OnLogInteractionListener listener) {
            if (log == null) return;

            textViewHistoryVaccineName.setText(log.vaccineName != null ? log.vaccineName : "N/A");

            if (log.dateAdministered != null) {
                boolean isFuture = log.dateAdministered.after(new Date());
                String prefix = isFuture ? "Scheduled: " : "Administered: ";
                textViewHistoryDate.setText(prefix + dateFormatter.format(log.dateAdministered));
                textViewHistoryDate.setVisibility(View.VISIBLE);
            } else { textViewHistoryDate.setVisibility(View.GONE); }

            if (log.batchNumber != null && !log.batchNumber.trim().isEmpty()) {
                textViewHistoryOptionalBatch.setText("Batch: " + log.batchNumber);
                textViewHistoryOptionalBatch.setVisibility(View.VISIBLE);
            } else { textViewHistoryOptionalBatch.setVisibility(View.GONE); }

            if (log.clinicDoctor != null && !log.clinicDoctor.trim().isEmpty()) {
                textViewHistoryOptionalClinic.setText("Clinic/Dr: " + log.clinicDoctor);
                textViewHistoryOptionalClinic.setVisibility(View.VISIBLE);
            } else { textViewHistoryOptionalClinic.setVisibility(View.GONE); }

            if (buttonDeleteLogItem != null && listener != null) {
                buttonDeleteLogItem.setOnClickListener(v -> {
                    if (log.getDocumentId() != null && !log.getDocumentId().isEmpty()) {
                        Log.d("VaccineLogAdapter", "Delete icon clicked for log ID: " + log.getDocumentId());
                        listener.onDeleteLogClick(log); // Notify HistoryActivity
                    } else {
                        Log.e("VaccineLogAdapter", "Cannot delete log: Document ID is missing from log object!");
                    }
                });
            }

        }
    }
    public static class LogDiff extends DiffUtil.ItemCallback<VaccineLog> {
        @Override
        public boolean areItemsTheSame(@NonNull VaccineLog oldItem, @NonNull VaccineLog newItem) {
            return oldItem.getDocumentId() != null && oldItem.getDocumentId().equals(newItem.getDocumentId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull VaccineLog oldItem, @NonNull VaccineLog newItem) {
            return Objects.equals(oldItem.vaccineName, newItem.vaccineName) &&
                    Objects.equals(oldItem.dateAdministered, newItem.dateAdministered) &&
                    Objects.equals(oldItem.batchNumber, newItem.batchNumber) &&
                    Objects.equals(oldItem.clinicDoctor, newItem.clinicDoctor) &&
                    Objects.equals(oldItem.childId, newItem.childId);
        }
    }

}