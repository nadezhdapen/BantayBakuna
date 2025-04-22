package com.example.bantaybakuna;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bantaybakuna.Data.VaccineStatusItem;
import java.util.Objects;

public class VaccineStatusAdapter extends ListAdapter<VaccineStatusItem, VaccineStatusAdapter.StatusViewHolder> {

    public VaccineStatusAdapter(@NonNull DiffUtil.ItemCallback<VaccineStatusItem> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vaccine_status_item, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        VaccineStatusItem currentItem = getItem(position);
        if (currentItem != null) {
            holder.bind(currentItem);
        }
    }


    static class StatusViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewStatusVaccineName;
        private final TextView textViewStatusInfo;
        private final TextView textViewStatusMilestone;
        private final View viewStatusColor;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStatusVaccineName = itemView.findViewById(R.id.textViewStatusVaccineName);
            textViewStatusInfo = itemView.findViewById(R.id.textViewStatusInfo);
            textViewStatusMilestone = itemView.findViewById(R.id.textViewStatusMilestone);
            viewStatusColor = itemView.findViewById(R.id.viewStatusColor);
        }

        public void bind(VaccineStatusItem item) {
            if (item == null) return;
            Context context = itemView.getContext();

            textViewStatusVaccineName.setText(item.vaccineName != null ? item.vaccineName : "N/A");
            textViewStatusInfo.setText(item.status + ": " + (item.dateInfo != null ? item.dateInfo : "N/A"));
            textViewStatusMilestone.setText("(" + (item.milestoneLabel != null ? item.milestoneLabel : "Unknown Milestone") + ")");

            int colorResId;
            switch (item.status.toUpperCase()) {
                case "DONE":
                    colorResId = R.color.status_done;
                    break;
                case "UPCOMING":
                    colorResId = R.color.status_upcoming;
                    break;
                case "MISSED":
                    colorResId = R.color.status_missed;
                    break;
                case "FUTURE":
                default:
                    colorResId = R.color.status_future;
                    break;
            }
            viewStatusColor.setBackgroundColor(ContextCompat.getColor(context, colorResId));
        }
    }
    public static class StatusDiff extends DiffUtil.ItemCallback<VaccineStatusItem> {
        @Override
        public boolean areItemsTheSame(@NonNull VaccineStatusItem oldItem, @NonNull VaccineStatusItem newItem) {
            return Objects.equals(oldItem.vaccineName, newItem.vaccineName) && Objects.equals(oldItem.milestoneLabel, newItem.milestoneLabel);
        }

        @Override
        public boolean areContentsTheSame(@NonNull VaccineStatusItem oldItem, @NonNull VaccineStatusItem newItem) {
            return Objects.equals(oldItem.vaccineName, newItem.vaccineName) &&
                    Objects.equals(oldItem.milestoneLabel, newItem.milestoneLabel) &&
                    Objects.equals(oldItem.status, newItem.status) &&
                    Objects.equals(oldItem.dateInfo, newItem.dateInfo);
        }
    }

}