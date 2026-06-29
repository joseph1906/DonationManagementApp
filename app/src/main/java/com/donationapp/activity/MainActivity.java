package com.donationapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.donationmanagementapp.R;
import com.donationapp.adapter.DonationAdapter;
import com.donationapp.model.Donation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG        = "MainActivity";
    private static final String COLLECTION = "donations";

    private RecyclerView          recyclerView;
    private DonationAdapter       adapter;
    private ProgressBar           progressBar;
    private TextView              tvEmpty;
    private EditText              etSearch;

    private final List<Donation>  allDonations      = new ArrayList<>();
    private final List<Donation>  filteredDonations = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        bindViews();
        setupRecyclerView();
        setupSearch();
        setupFab();
        loadDonations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDonations();
    }

    private void bindViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar  = findViewById(R.id.progressBar);
        tvEmpty      = findViewById(R.id.tvEmpty);
        etSearch     = findViewById(R.id.etSearch);
    }

    private void setupRecyclerView() {
        adapter = new DonationAdapter(
                this,
                filteredDonations,
                this::onMarkPickedUp,
                this::onDonationCardClicked
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterDonations(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fabAddDonation);
        fab.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SubmitDonationActivity.class))
        );
    }

    private void loadDonations() {
        showLoading(true);

        db.collection(COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allDonations.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Donation donation = doc.toObject(Donation.class);
                        donation.setId(doc.getId());
                        allDonations.add(donation);
                    }
                    filterDonations(etSearch.getText().toString().trim());
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading donations", e);
                    Toast.makeText(this, getString(R.string.error_load), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void filterDonations(String query) {
        filteredDonations.clear();
        if (query.isEmpty()) {
            filteredDonations.addAll(allDonations);
        } else {
            String lq = query.toLowerCase();
            for (Donation d : allDonations) {
                boolean nameMatch     = d.getItemName()       != null && d.getItemName().toLowerCase().contains(lq);
                boolean locationMatch = d.getPickupLocation() != null && d.getPickupLocation().toLowerCase().contains(lq);
                if (nameMatch || locationMatch) filteredDonations.add(d);
            }
        }
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filteredDonations.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void onMarkPickedUp(Donation donation, int position) {
        db.collection(COLLECTION)
                .document(donation.getId())
                .update("pickedUp", true)
                .addOnSuccessListener(unused -> {
                    donation.setPickedUp(true);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, R.string.marked_picked_up, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to mark picked up", e);
                    Toast.makeText(this, R.string.error_update, Toast.LENGTH_SHORT).show();
                });
    }

    private void onDonationCardClicked(Donation donation) {
        Intent intent = new Intent(this, DonationDetailActivity.class);
        intent.putExtra(DonationDetailActivity.EXTRA_DONATION_ID, donation.getId());
        startActivity(intent);
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.GONE   : View.VISIBLE);
    }
}