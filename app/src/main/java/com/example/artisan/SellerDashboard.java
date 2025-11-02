package com.example.artisan;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class SellerDashboard extends AppCompatActivity {
    private TextView totalOrder,totalRevenue,averageOrderValue,totalProductSold;
    private LineChart salesLineChart;
    private TabLayout timeFrameTabLayout,sortFrameTabLayout;
    private TextView ordered,outForDelivery,cancelled,received,refunded,pendingRefund;
    private RecyclerView topProductRecycler;
    private List<Product> allProducts = new ArrayList<>();
    private TopProductAdapter topProductAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String sellerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        db=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();
        sellerId = auth.getCurrentUser().getUid();

        initializeView();

        salesSummary();
        orderBreakdown();

        //Top Products
        loadProducts();

        sortFrameTabLayout.addTab(sortFrameTabLayout.newTab().setText("Most Revenue"));
        sortFrameTabLayout.addTab(sortFrameTabLayout.newTab().setText("Most Orders"));
        sortFrameTabLayout.addTab(sortFrameTabLayout.newTab().setText("Most Likes"));

        topProductRecycler.setLayoutManager(new LinearLayoutManager(this));
        topProductAdapter = new TopProductAdapter(allProducts,this);
        topProductRecycler.setAdapter(topProductAdapter);

        sortFrameTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    Collections.sort(allProducts, (p1, p2) -> Double.compare(p2.getRevenue(), p1.getRevenue()));
                } else if (position == 1) {
                    Collections.sort(allProducts, (p1, p2) -> Long.compare(p2.getOrderCount(), p1.getOrderCount()));
                } else if (position == 2) {
                    Collections.sort(allProducts, (p1, p2) -> Long.compare(p2.getProductLikes(), p1.getProductLikes()));
                }
                topProductAdapter.setSelectedTabPosition(position);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });


        timeFrameTabLayout.addTab(timeFrameTabLayout.newTab().setText("Monthly"));
        timeFrameTabLayout.addTab(timeFrameTabLayout.newTab().setText("Yearly"));

        loadMonthlySalesData();

        timeFrameTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    loadMonthlySalesData();
                } else {
                    loadYearlySalesData();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    private void initializeView(){
        totalOrder=findViewById(R.id.totalOrder);
        totalRevenue= findViewById(R.id.totalRevenue);
        averageOrderValue=findViewById(R.id.averageOrderValue);
        totalProductSold=findViewById(R.id.totalProductSold);

        salesLineChart = findViewById(R.id.salesLineChart);
        timeFrameTabLayout = findViewById(R.id.timeFrameTabLayout);

        ordered=findViewById(R.id.ordered);
        outForDelivery=findViewById(R.id.outForDelivery);
        received=findViewById(R.id.received);
        cancelled=findViewById(R.id.cancelled);
        refunded=findViewById(R.id.refunded);
        pendingRefund= findViewById(R.id.pendingRefund);

        sortFrameTabLayout=findViewById(R.id.sortFrameTabLayout);
        topProductRecycler=findViewById(R.id.topProductRecycler);
    }
    private void salesSummary(){
        db.collection("orders").whereEqualTo("sellerId",sellerId)
                .whereEqualTo("status","Received")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    int ttlOrder=queryDocumentSnapshots.size();
                    double ttlRevenue = 0, avgOrderValue;
                    int ttlProductSold = 0;
                    for(DocumentSnapshot doc:queryDocumentSnapshots){
                        Double orderTotal= doc.getDouble("totalAmount");
                        Long orderQuantity = doc.getLong("quantity");
                        ttlRevenue+=orderTotal;
                        ttlProductSold+=orderQuantity;
                    }
                    avgOrderValue=ttlRevenue/ttlOrder;
                    avgOrderValue = Math.round(avgOrderValue * 100.0) / 100.0;

                    totalOrder.setText(String.valueOf(ttlOrder));
                    totalRevenue.setText("Rs."+ttlRevenue);
                    averageOrderValue.setText("Rs."+avgOrderValue);
                    totalProductSold.setText(String.valueOf(ttlProductSold));
                });
    }
    private void orderBreakdown(){
        getOrderStatusCount("Ordered", ordered);
        getOrderStatusCount("Out for Delivery", outForDelivery);
        getOrderStatusCount("Cancelled", cancelled);
        getOrderStatusCount("Received", received);
        getOrderStatusCount("Refunded", refunded);
        getOrderStatusCount("Pending refund", pendingRefund);
    }
    private void getOrderStatusCount(String status, TextView statusView) {
        db.collection("orders").whereEqualTo("sellerId",sellerId)
                .whereEqualTo("status", status)
                .get().addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    statusView.setText(String.valueOf(count));
                });
    }
    private void loadProducts(){
        db.collection("products").whereEqualTo("userId",sellerId)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for(DocumentSnapshot doc:queryDocumentSnapshots){
                        String productId = doc.getId();
                        List<String> imageUrls= (List<String>) doc.get("imageUrls");
                        String imageUrl= imageUrls.get(0);
                        String name= doc.getString("title");
                        Long likes= doc.getLong("likes");
                        Long orderCount= doc.getLong("orderCount");
                        Double revenue= doc.getDouble("totalRevenue");

                        allProducts.add(new Product(productId, imageUrl, name, likes, orderCount, revenue == null ? 0 : revenue));
                    }
                    Collections.sort(allProducts, (p1, p2) -> Double.compare(p2.getRevenue(), p1.getRevenue()));
                    topProductAdapter.notifyDataSetChanged();
                }).addOnFailureListener(v->{

                });
    }
    private void loadMonthlySalesData() {
        Calendar startCal = Calendar.getInstance();
        startCal.add(Calendar.MONTH, -11);
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        long startTime = startCal.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        Map<Integer, Float> salesMap = new LinkedHashMap<>();
        Calendar temp = (Calendar) startCal.clone();

        for (int i = 0; i < 12; i++) {
            int key = temp.get(Calendar.YEAR) * 100 + temp.get(Calendar.MONTH);
            salesMap.put(key, 0f);
            temp.add(Calendar.MONTH, 1);
        }

        db.collection("orders")
                .whereEqualTo("sellerId", sellerId)
                .whereEqualTo("status", "Received")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    for (DocumentSnapshot doc : querySnapshots) {
                        Timestamp timestamp = doc.getTimestamp("receivedTimestamp");
                        Double amount = doc.getDouble("totalAmount");

                        if (timestamp != null && amount != null) {
                            long time = timestamp.toDate().getTime();
                            if (time >= startTime && time <= endTime) {
                                Calendar orderCal = Calendar.getInstance();
                                orderCal.setTimeInMillis(time);
                                int key = orderCal.get(Calendar.YEAR) * 100 + orderCal.get(Calendar.MONTH);

                                if (salesMap.containsKey(key)) {
                                    float current = salesMap.get(key);
                                    salesMap.put(key, current + amount.floatValue());
                                }
                            }
                        }
                    }
                    List<Entry> entries = new ArrayList<>();
                    int index = 0;
                    for (Float value : salesMap.values()) {
                        entries.add(new Entry(index++, value));
                    }
                    updateLineChart(entries, "Monthly Sales");
                });
    }
    private void loadYearlySalesData() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Map<Integer, Float> salesMap = new HashMap<>();
        for (int i = currentYear - 4; i <= currentYear; i++) {
            salesMap.put(i, 0f);
        }

        db.collection("orders")
                .whereEqualTo("sellerId", sellerId)
                .whereEqualTo("status", "Received")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    for (DocumentSnapshot doc : querySnapshots) {
                        Timestamp timestamp = doc.getTimestamp("receivedTimestamp");
                        Double amount = doc.getDouble("totalAmount");
                        if (timestamp != null && amount != null) {
                            Calendar orderCal = Calendar.getInstance();
                            orderCal.setTime(timestamp.toDate());
                            int year = orderCal.get(Calendar.YEAR);
                            if (salesMap.containsKey(year)) {
                                salesMap.put(year, salesMap.get(year) + amount.floatValue());
                            }
                        }
                    }

                    List<Entry> entries = new ArrayList<>();
                    for (int y = currentYear - 4; y <= currentYear; y++) {
                        entries.add(new Entry(y, salesMap.get(y)));
                    }
                    updateLineChart(entries, "Yearly Sales");
                });
    }
    private void updateLineChart(List<Entry> entries, String label) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(Color.RED);
        dataSet.setCircleColor(Color.RED);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);

        LineData lineData = new LineData(dataSet);
        salesLineChart.setData(lineData);

        salesLineChart.getDescription().setEnabled(false);
        salesLineChart.getLegend().setTextColor(Color.WHITE);

        salesLineChart.animateX(1000, Easing.EaseInOutQuart);

        YAxis leftAxis = salesLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return "Rs. " + (int) value;
            }
        });

        salesLineChart.getAxisRight().setEnabled(false);

        XAxis xAxis = salesLineChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setYOffset(5f);

        if (label.equals("Yearly Sales")) {
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value);
                }
            });
            xAxis.setLabelCount(entries.size(), true);
        } else {
            List<String> monthLabels = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -11);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.getDefault());
            for (int i = 0; i < 12; i++) {
                monthLabels.add(sdf.format(cal.getTime()));
                cal.add(Calendar.MONTH, 1);
            }
            xAxis.setValueFormatter(new IndexAxisValueFormatter(monthLabels));
            xAxis.setLabelCount(monthLabels.size(), true);
        }
        salesLineChart.setExtraBottomOffset(12f);
        salesLineChart.invalidate();
    }
}
