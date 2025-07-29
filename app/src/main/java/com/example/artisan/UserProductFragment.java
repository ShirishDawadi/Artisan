package com.example.artisan;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProductFragment extends Fragment {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private UserReportAdapter adapter;
    private List<ReportItem> reportList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_product, container, false);

        tabLayout = view.findViewById(R.id.userProductTabLayout);
        recyclerView = view.findViewById(R.id.userProductRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserReportAdapter(getContext(), reportList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        tabLayout.addTab(tabLayout.newTab().setText("Profiles"));
        tabLayout.addTab(tabLayout.newTab().setText("Products"));

        loadReportsAndShow(true);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadReportsAndShow(true);
                } else {
                    loadReportsAndShow(false);
                }
            }
            public void onTabUnselected(TabLayout.Tab tab) {}
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        return view;
    }

    private void loadReportsAndShow(boolean isProfiles) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<ReportItem> items = new ArrayList<>();

        if (isProfiles) {
            db.collection("users")
                    .get()
                    .addOnSuccessListener(userSnapshot -> {
                        if (userSnapshot.isEmpty()) {
                            recyclerView.setAdapter(new UserReportAdapter(requireContext(), items));
                            return;
                        }

                        List<String> userIds = new ArrayList<>();
                        Map<String, String> userIdToName = new HashMap<>();
                        for (var doc : userSnapshot.getDocuments()) {
                            String userId = doc.getId();
                            String name = doc.getString("fullname");
                            userIds.add(userId);
                            userIdToName.put(userId, name != null ? name : "Unknown");
                        }

                        if (userIds.isEmpty()) {
                            recyclerView.setAdapter(new UserReportAdapter(requireContext(), items));
                            return;
                        }

                        List<List<String>> partitions = partitionList(userIds, 10);

                        Map<String, Integer> reportCounts = new HashMap<>();

                        final int[] partitionsProcessed = {0};
                        final int totalPartitions = partitions.size();

                        for (List<String> part : partitions) {
                            db.collection("reports")
                                    .whereIn("reportedProfileId", part)
                                    .get()
                                    .addOnSuccessListener(reportSnapshot -> {
                                        for (var repDoc : reportSnapshot.getDocuments()) {
                                            String repId = repDoc.getString("reportedProfileId");
                                            if (repId != null) {
                                                reportCounts.put(repId, reportCounts.getOrDefault(repId, 0) + 1);
                                            }
                                        }

                                        partitionsProcessed[0]++;
                                        if (partitionsProcessed[0] == totalPartitions) {
                                            for (String id : userIds) {
                                                int count = reportCounts.getOrDefault(id, 0);
                                                items.add(new ReportItem(id, userIdToName.get(id), count, true));
                                            }
                                            items.sort((a, b) -> b.getCount() - a.getCount());
                                            recyclerView.setAdapter(new UserReportAdapter(requireContext(), items));
                                        }
                                    });
                        }
                    });
        } else {
            db.collection("products")
                    .get()
                    .addOnSuccessListener(productSnapshot -> {
                        if (productSnapshot.isEmpty()) {
                            recyclerView.setAdapter(new UserReportAdapter(requireContext(), items));
                            return;
                        }

                        List<String> productIds = new ArrayList<>();
                        Map<String, String> productIdToTitle = new HashMap<>();
                        for (var doc : productSnapshot.getDocuments()) {
                            String productId = doc.getId();
                            String title = doc.getString("title");
                            productIds.add(productId);
                            productIdToTitle.put(productId, title != null ? title : "Unknown");
                        }

                        if (productIds.isEmpty()) {
                            recyclerView.setAdapter(new UserReportAdapter(requireContext(), items));
                            return;
                        }

                        List<List<String>> partitions = partitionList(productIds, 10);

                        Map<String, Integer> reportCounts = new HashMap<>();
                        final int[] partitionsProcessed = {0};
                        final int totalPartitions = partitions.size();

                        for (List<String> part : partitions) {
                            db.collection("reports")
                                    .whereIn("reportedProductId", part)
                                    .get()
                                    .addOnSuccessListener(reportSnapshot -> {
                                        for (var repDoc : reportSnapshot.getDocuments()) {
                                            String repId = repDoc.getString("reportedProductId");
                                            if (repId != null) {
                                                reportCounts.put(repId, reportCounts.getOrDefault(repId, 0) + 1);
                                            }
                                        }

                                        partitionsProcessed[0]++;
                                        if (partitionsProcessed[0] == totalPartitions) {
                                            for (String id : productIds) {
                                                int count = reportCounts.getOrDefault(id, 0);
                                                items.add(new ReportItem(id, productIdToTitle.get(id), count, false));
                                            }
                                            items.sort((a, b) -> b.getCount() - a.getCount());
                                            recyclerView.setAdapter(new UserReportAdapter(requireContext(), items));
                                        }
                                    });
                        }
                    });
        }
    }

    private List<List<String>> partitionList(List<String> list, int n) {
        List<List<String>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += n) {
            partitions.add(list.subList(i, Math.min(i + n, list.size())));
        }
        return partitions;
    }
}
