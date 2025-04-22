package com.example.bantaybakuna;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bantaybakuna.Data.AppNotification;
import com.example.bantaybakuna.R;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class AppNotificationAdapter extends ListAdapter<AppNotification, AppNotificationAdapter.NotificationViewHolder> {

    public AppNotificationAdapter(@NonNull DiffUtil.ItemCallback<AppNotification> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        AppNotification currentNotification = getItem(position);
        if (currentNotification != null) {
            holder.bind(currentNotification);
        }
    }

    // --- ViewHolder ---
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewNotificationMessage;
        private final TextView textViewNotificationTimestamp;
        private final SimpleDateFormat timestampFormatter;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNotificationMessage = itemView.findViewById(R.id.textViewNotificationMessage);
            textViewNotificationTimestamp = itemView.findViewById(R.id.textViewNotificationTimestamp);
            // Format for timestamp display e.g., "Apr 22, 2025 10:30 AM"
            timestampFormatter = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        }

        public void bind(AppNotification notification) {
            if (notification == null) return;
            textViewNotificationMessage.setText(notification.message != null ? notification.message : "No message");
            if (notification.timestamp != null) {
                textViewNotificationTimestamp.setText("Received: " + timestampFormatter.format(notification.timestamp));
                textViewNotificationTimestamp.setVisibility(View.VISIBLE);
            } else {
                textViewNotificationTimestamp.setVisibility(View.GONE);
            }
            // TODO: Change background/text style if notification.isRead is true?
        }
    }


    public static class NotificationDiff extends DiffUtil.ItemCallback<AppNotification> {
        @Override
        public boolean areItemsTheSame(@NonNull AppNotification oldItem, @NonNull AppNotification newItem) {
            return oldItem.getDocumentId() != null && oldItem.getDocumentId().equals(newItem.getDocumentId());

        }

        @Override
        public boolean areContentsTheSame(@NonNull AppNotification oldItem, @NonNull AppNotification newItem) {
            // Compare all relevant fields
            return Objects.equals(oldItem.message, newItem.message) &&
                    Objects.equals(oldItem.timestamp, newItem.timestamp) &&
                    oldItem.isRead == newItem.isRead; // Compare boolean directly
        }
    }
}