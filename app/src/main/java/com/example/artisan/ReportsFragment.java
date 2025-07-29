package com.example.artisan;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ReportsFragment extends Fragment {
    private TabLayout reportsTabLayout;
    private RecyclerView reportsRecycler;
    private ReportAdapter adapter;
    private FirebaseFirestore db;
    private ArrayList<Report> reportList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        reportsTabLayout=view.findViewById(R.id.reportsTabLayout);
        reportsRecycler=view.findViewById(R.id.reportsRecyclerView);
        reportsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReportAdapter(getContext(), reportList);
        reportsRecycler.setAdapter(adapter);

        reportsTabLayout.addTab(reportsTabLayout.newTab().setText("Profiles"));
        reportsTabLayout.addTab(reportsTabLayout.newTab().setText("Products"));

        db=FirebaseFirestore.getInstance();
        reportsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String type = tab.getText().toString().toLowerCase();
                fetchReports(type.equals("profiles") ? "profile" : "product");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        fetchReports("profile");

        return view;
    }
    private void fetchReports(String type) {
        db.collection("reports")
                .whereEqualTo("reportType", type)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reportList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        String reason = doc.getString("reason");
                        String reportedBy = doc.getString("reportedBy");
                        String reprt= type.equals("profile")?"reportedProfileId":"reportedProductId";
                        String reportedId = doc.getString(reprt);
                        Report report = new Report(reason, type, reportedId, reportedBy);
                        reportList.add(report);
                    }
                    adapter.updateList(reportList);
                });
    }
}