package com.donationapp.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.donationmanagementapp.R;
import com.donationapp.model.Donation;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class SubmitDonationActivity extends AppCompatActivity {

    private static final String TAG        = "SubmitDonation";
    private static final String COLLECTION = "donations";

    private EditText    etItemName;
    private EditText    etDescription;
    private EditText    etQuantity;
    private EditText    etLocation;
    private EditText    etContact;
    private Button      btnSubmit;
    private ProgressBar progressBar;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_donation);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_submit);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // ── Fix: enable offline persistence so callbacks always fire ──────
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        bindViews();
        btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void bindViews() {
        etItemName    = findViewById(R.id.etItemName);
        etDescription = findViewById(R.id.etDescription);
        etQuantity    = findViewById(R.id.etQuantity);
        etLocation    = findViewById(R.id.etLocation);
        etContact     = findViewById(R.id.etContact);
        btnSubmit     = findViewById(R.id.btnSubmit);
        progressBar   = findViewById(R.id.progressBar);
    }

    private void validateAndSubmit() {
        String itemName    = etItemName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String location    = etLocation.getText().toString().trim();
        String contact     = etContact.getText().toString().trim();

        if (TextUtils.isEmpty(itemName)) {
            etItemName.setError(getString(R.string.error_required));
            etItemName.requestFocus(); return;
        }
        if (TextUtils.isEmpty(description)) {
            etDescription.setError(getString(R.string.error_required));
            etDescription.requestFocus(); return;
        }
        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError(getString(R.string.error_required));
            etQuantity.requestFocus(); return;
        }
        if (TextUtils.isEmpty(location)) {
            etLocation.setError(getString(R.string.error_required));
            etLocation.requestFocus(); return;
        }
        if (TextUtils.isEmpty(contact)) {
            etContact.setError(getString(R.string.error_required));
            etContact.requestFocus(); return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            etQuantity.setError(getString(R.string.error_quantity));
            etQuantity.requestFocus(); return;
        }

        submitDonation(new Donation(itemName, description, quantity, location, contact));
    }

    private void submitDonation(Donation donation) {
        setLoading(true);
        Log.d(TAG, "Starting donation submission...");

        // Use a plain HashMap instead of the Donation object to rule out
        // serialization issues with @ServerTimestamp
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("itemName",       donation.getItemName());
        data.put("description",    donation.getDescription());
        data.put("quantity",       donation.getQuantity());
        data.put("pickupLocation", donation.getPickupLocation());
        data.put("contactInfo",    donation.getContactInfo());
        data.put("pickedUp",       false);
        data.put("createdAt",      com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "SUCCESS — Donation ID: " + docRef.getId());
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(
                                SubmitDonationActivity.this,
                                "Donation submitted successfully!",
                                Toast.LENGTH_LONG
                        ).show();
                        finish();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "FAILED — " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(
                                SubmitDonationActivity.this,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });
                });
    }
    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading);
        btnSubmit.setAlpha(loading ? 0.6f : 1f);
    }
}