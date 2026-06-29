package com.donationapp.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.donationmanagementapp.R;
import com.donationapp.model.Donation;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Displays the full details of a single donation.
 * Allows an NGO to mark the donation as picked up from this screen as well.
 */
public class DonationDetailActivity extends AppCompatActivity {

    private static final String TAG        = "DonationDetail";
    public  static final String EXTRA_DONATION_ID = "extra_donation_id";
    private static final String COLLECTION = "donations";

    // ── Views ──────────────────────────────────────────────────────────────
    private TextView    tvItemName;
    private TextView    tvDescription;
    private TextView    tvQuantity;
    private TextView    tvLocation;
    private TextView    tvContact;
    private TextView    tvStatus;
    private TextView    tvCreatedAt;
    private Button      btnMarkPickedUp;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String            donationId;
    private Donation          currentDonation;

    // ──────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_detail);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db         = FirebaseFirestore.getInstance();
        donationId = getIntent().getStringExtra(EXTRA_DONATION_ID);

        bindViews();
        loadDonation();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ── UI ─────────────────────────────────────────────────────────────────

    private void bindViews() {
        tvItemName      = findViewById(R.id.tvItemName);
        tvDescription   = findViewById(R.id.tvDescription);
        tvQuantity      = findViewById(R.id.tvQuantity);
        tvLocation      = findViewById(R.id.tvLocation);
        tvContact       = findViewById(R.id.tvContact);
        tvStatus        = findViewById(R.id.tvStatus);
        tvCreatedAt     = findViewById(R.id.tvCreatedAt);
        btnMarkPickedUp = findViewById(R.id.btnMarkPickedUp);
        progressBar     = findViewById(R.id.progressBar);
    }

    private void populateViews(Donation donation) {
        tvItemName.setText(donation.getItemName());
        tvDescription.setText(donation.getDescription());
        tvQuantity.setText(getString(R.string.quantity_label, donation.getQuantity()));
        tvLocation.setText(getString(R.string.location_label, donation.getPickupLocation()));
        tvContact.setText(getString(R.string.contact_label, donation.getContactInfo()));

        if (donation.getCreatedAt() != null) {
            tvCreatedAt.setText(getString(R.string.listed_on,
                    donation.getCreatedAt().toDate().toString()));
        }

        updateStatusUI(donation.isPickedUp());

        btnMarkPickedUp.setOnClickListener(v -> markAsPickedUp());
    }

    private void updateStatusUI(boolean pickedUp) {
        if (pickedUp) {
            tvStatus.setText(R.string.status_picked_up);
            tvStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_picked));
            btnMarkPickedUp.setEnabled(false);
            btnMarkPickedUp.setText(R.string.already_collected);
            btnMarkPickedUp.setAlpha(0.5f);
        } else {
            tvStatus.setText(R.string.status_available);
            tvStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_available));
            btnMarkPickedUp.setEnabled(true);
            btnMarkPickedUp.setText(R.string.mark_as_picked_up);
            btnMarkPickedUp.setAlpha(1f);
        }
    }

    // ── Firestore ──────────────────────────────────────────────────────────

    private void loadDonation() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection(COLLECTION)
          .document(donationId)
          .get()
          .addOnSuccessListener(snapshot -> {
              progressBar.setVisibility(View.GONE);
              if (snapshot.exists()) {
                  currentDonation = snapshot.toObject(Donation.class);
                  if (currentDonation != null) {
                      currentDonation.setId(snapshot.getId());
                      populateViews(currentDonation);
                  }
              } else {
                  Toast.makeText(this, R.string.error_not_found, Toast.LENGTH_SHORT).show();
                  finish();
              }
          })
          .addOnFailureListener(e -> {
              progressBar.setVisibility(View.GONE);
              Log.e(TAG, "Error loading donation", e);
              Toast.makeText(this, R.string.error_load, Toast.LENGTH_SHORT).show();
              finish();
          });
    }

    private void markAsPickedUp() {
        db.collection(COLLECTION)
          .document(donationId)
          .update("pickedUp", true)
          .addOnSuccessListener(unused -> {
              if (currentDonation != null) currentDonation.setPickedUp(true);
              updateStatusUI(true);
              Toast.makeText(this, R.string.marked_picked_up, Toast.LENGTH_SHORT).show();
          })
          .addOnFailureListener(e -> {
              Log.e(TAG, "Failed to update", e);
              Toast.makeText(this, R.string.error_update, Toast.LENGTH_SHORT).show();
          });
    }
}
