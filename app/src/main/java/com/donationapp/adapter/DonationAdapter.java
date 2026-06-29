package com.donationapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.donationapp.activity.*;
import com.donationapp.model.Donation;
import com.example.donationmanagementapp.R;

import java.util.List;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.DonationViewHolder> {

    public interface OnPickedUpClickListener {
        void onPickedUpClick(Donation donation, int position);
    }

    public interface OnItemClickListener {
        void onItemClick(Donation donation);
    }

    private final List<Donation>          donations;
    private final Context                 context;
    private final OnPickedUpClickListener pickedUpListener;
    private final OnItemClickListener     itemClickListener;

    public DonationAdapter(Context context,
                           List<Donation> donations,
                           OnPickedUpClickListener pickedUpListener,
                           OnItemClickListener itemClickListener) {
        this.context           = context;
        this.donations         = donations;
        this.pickedUpListener  = pickedUpListener;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_donation, parent, false);
        return new DonationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationViewHolder holder, int position) {
        holder.bind(donations.get(position), position);
    }

    @Override
    public int getItemCount() { return donations.size(); }

    public void updateList(List<Donation> newList) {
        donations.clear();
        donations.addAll(newList);
        notifyDataSetChanged();
    }

    class DonationViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvItemName;
        private final TextView tvDescription;
        private final TextView tvQuantity;
        private final TextView tvLocation;
        private final TextView tvContact;
        private final TextView tvStatus;
        private final Button   btnMarkPickedUp;

        DonationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName      = itemView.findViewById(R.id.tvItemName);
            tvDescription   = itemView.findViewById(R.id.tvDescription);
            tvQuantity      = itemView.findViewById(R.id.tvQuantity);
            tvLocation      = itemView.findViewById(R.id.tvLocation);
            tvContact       = itemView.findViewById(R.id.tvContact);
            tvStatus        = itemView.findViewById(R.id.tvStatus);
            btnMarkPickedUp = itemView.findViewById(R.id.btnMarkPickedUp);
        }

        void bind(Donation donation, int position) {
            tvItemName.setText(donation.getItemName());
            tvDescription.setText(donation.getDescription());
            tvQuantity.setText(context.getString(R.string.quantity_label, donation.getQuantity()));
            tvLocation.setText(context.getString(R.string.location_label, donation.getPickupLocation()));
            tvContact.setText(context.getString(R.string.contact_label, donation.getContactInfo()));

            if (donation.isPickedUp()) {
                tvStatus.setText(R.string.status_picked_up);
                tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_picked));
                btnMarkPickedUp.setEnabled(false);
                btnMarkPickedUp.setText(R.string.already_collected);
                btnMarkPickedUp.setAlpha(0.5f);
            } else {
                tvStatus.setText(R.string.status_available);
                tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_available));
                btnMarkPickedUp.setEnabled(true);
                btnMarkPickedUp.setText(R.string.mark_as_picked_up);
                btnMarkPickedUp.setAlpha(1f);
            }

            btnMarkPickedUp.setOnClickListener(v -> {
                if (pickedUpListener != null) pickedUpListener.onPickedUpClick(donation, position);
            });

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) itemClickListener.onItemClick(donation);
            });
        }
    }
}