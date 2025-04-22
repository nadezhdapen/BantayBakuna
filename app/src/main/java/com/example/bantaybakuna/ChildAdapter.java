package com.example.bantaybakuna; // **** REPLACE WITH YOUR PACKAGE NAME ****

// --- Imports ---
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bantaybakuna.Data.Child;
import com.example.bantaybakuna.R;
import java.util.Date;

public class ChildAdapter extends ListAdapter<Child, ChildAdapter.ChildViewHolder> {

    private final OnChildClickListener listener;

    public interface OnChildClickListener {
        void onChildClick(Child child);
    }

    public ChildAdapter(@NonNull DiffUtil.ItemCallback<Child> diffCallback, OnChildClickListener listener) {
        super(diffCallback);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // (create) the layout for a single list item (child_list_item.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.child_list_item, parent, false);
        // Create and return a new ViewHolder
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        // Get the Child data object for the current position
        Child currentChild = getItem(position);
        // Pass the Child data and the listener to the ViewHolder's bind method
        // Add null check for safety before binding
        if (currentChild != null) {
            holder.bind(currentChild, listener);
        } else {
            Log.e("ChildAdapter", "Attempting to bind null Child object at position: " + position);

        }
    }

    // Holds the references to the views inside each list item row (like TextView, ImageView)
    static class ChildViewHolder extends RecyclerView.ViewHolder {
        // Declare the view variables for this list item
        private final ImageView imageViewChildIcon;
        private final TextView textViewChildName;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView); // Call the parent constructor
            imageViewChildIcon = itemView.findViewById(R.id.imageViewChildIcon);
            textViewChildName = itemView.findViewById(R.id.textViewChildName);
        }

        // Method to bind the specific Child data to the views in this row
        public void bind(final Child child, final OnChildClickListener listener) {
            // Basic null check for safety
            if (child == null) {
                textViewChildName.setText("Error: Null Child Data");
                return;
            }

            // Set the child's name (with null check for the name itself)
            if (child.getName() != null) {
                textViewChildName.setText(child.getName());
            } else {
                textViewChildName.setText("Unnamed Child"); // Fallback text
            }

            itemView.setOnClickListener(v -> {

                if (listener != null && child.getDocumentId() != null) {
                    Log.d("ChildAdapter", "Item row clicked: " + child.getName());
                    // Call the listener's onChildClick method, passing the clicked child object
                    // This notifies MainActivity that this specific child was clicked.
                    listener.onChildClick(child);
                } else {
                    Log.e("ChildAdapter", "Item click listener error: listener or documentId null");
                }
            });
        }
    }

    public static class ChildDiff extends DiffUtil.ItemCallback<Child> {

        @Override
        public boolean areItemsTheSame(@NonNull Child oldItem, @NonNull Child newItem) {
            // Items are the same if their Firestore document IDs exist and are equal
            return oldItem.getDocumentId() != null && oldItem.getDocumentId().equals(newItem.getDocumentId());
        }

        // Called to check if the CONTENTS of two items (that represent the same object) have changed
        @Override
        public boolean areContentsTheSame(@NonNull Child oldItem, @NonNull Child newItem) {
            // Check if name is the same (handle nulls)
            boolean nameMatch = (oldItem.getName() == null && newItem.getName() == null) ||
                    (oldItem.getName() != null && oldItem.getName().equals(newItem.getName()));
            // Check if date of birth is the same (handle nulls)
            boolean dobMatch = (oldItem.getDateOfBirth() == null && newItem.getDateOfBirth() == null) ||
                    (oldItem.getDateOfBirth() != null && oldItem.getDateOfBirth().equals(newItem.getDateOfBirth()));
            // Contents are the same if both name and DOB match
            return nameMatch && dobMatch;
        }
    }

}