package com.example.artisan;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private RecyclerView productRecyclerView, profileRecyclerView;
    private ProductHomeAdapter productAdapter;
    private ProfileAdapter profileAdapter;
    private Button productsButton, profileButton;
    private TextView sortText, priceSort, likesSort, ratingSort, noResultsText;
    private LinearLayout sortOptionsLayout;

    private boolean isSearchingProducts = true;
    private List<Product> productList = new ArrayList<>();
    private List<Profile> profileList = new ArrayList<>();

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        setupRecyclerViews();
        setupListeners();

        updateButtonColors();
        return view;
    }

    private void initializeViews(View view) {
        searchEditText = view.findViewById(R.id.searchEditText);
        productRecyclerView = view.findViewById(R.id.searchRecylerView);
        profileRecyclerView = view.findViewById(R.id.profileRecyclerView);
        productsButton = view.findViewById(R.id.productsButton);
        profileButton = view.findViewById(R.id.profileButton);
        sortText = view.findViewById(R.id.sortTextView);
        sortOptionsLayout = view.findViewById(R.id.sortLinearLayout);
        likesSort = view.findViewById(R.id.likesSort);
        priceSort = view.findViewById(R.id.priceSort);
        ratingSort = view.findViewById(R.id.ratingSort);
        noResultsText = view.findViewById(R.id.noResultsText);
    }

    private void setupRecyclerViews() {
        productAdapter = new ProductHomeAdapter(productList, requireContext());
        productAdapter.setLayoutMode(false);
        productRecyclerView.setAdapter(productAdapter);
        productRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        productRecyclerView.setVisibility(View.GONE);

        profileAdapter = new ProfileAdapter(profileList, requireContext());
        profileRecyclerView.setAdapter(profileAdapter);
        profileRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        profileRecyclerView.setVisibility(View.GONE);
    }

    private void setupListeners() {
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });


        productsButton.setOnClickListener(v -> {
            isSearchingProducts = true;
            priceSort.setVisibility(View.VISIBLE);
            sortText.setText("Sort By");
            updateButtonColors();
            performSearch();
        });

        profileButton.setOnClickListener(v -> {
            isSearchingProducts = false;
            priceSort.setVisibility(View.GONE);
            sortText.setText("Sort By");
            updateButtonColors();
            performSearch();
        });

        sortText.setOnClickListener(v -> {
            sortOptionsLayout.setVisibility(sortOptionsLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });
        priceSort.setOnClickListener(v -> applySort("price"));
        likesSort.setOnClickListener(v -> applySort("likes"));
        ratingSort.setOnClickListener(v -> applySort("rating"));
    }

    private void applySort(String criteria) {
        if(criteria==null||criteria.isEmpty()){
            applySearch();
            return;
        }
        sortText.setText("Sort By " + capitalize(criteria));
        sortOptionsLayout.setVisibility(View.GONE);

        if (isSearchingProducts) {
            productList = mergeSortProducts(productList, criteria);
            productAdapter.setProductList(productList);
            productAdapter.notifyDataSetChanged();

            noResultsText.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            profileList = mergeSortProfiles(profileList, criteria);
            profileAdapter.setProfileList(profileList);
            profileAdapter.notifyDataSetChanged();

            noResultsText.setVisibility(profileList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
    private void applySearch(){
        if (isSearchingProducts) {
            productAdapter.setProductList(productList);
            productAdapter.notifyDataSetChanged();

            noResultsText.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            profileAdapter.setProfileList(profileList);
            profileAdapter.notifyDataSetChanged();

            noResultsText.setVisibility(profileList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            productRecyclerView.setVisibility(View.GONE);
            profileRecyclerView.setVisibility(View.GONE);
            return;
        }

        if (isSearchingProducts) {
            productRecyclerView.setVisibility(View.VISIBLE);
            profileRecyclerView.setVisibility(View.GONE);
            searchProducts(query);
        } else {
            profileRecyclerView.setVisibility(View.VISIBLE);
            productRecyclerView.setVisibility(View.GONE);
            searchProfiles(query);
        }
    }

    private void searchProducts(String query) {
        Log.d("SearchDebug", "Search triggered for product"+query);
        saveRecentSearch(query);
        db.collection("products").get().addOnSuccessListener(snapshot -> {
            productList.clear();
            String lowerQuery = query.toLowerCase();
            List<Task<Void>> tasks = new ArrayList<>();

            for (QueryDocumentSnapshot doc : snapshot) {
                try {
                    String name = doc.getString("title");
                    String description = doc.getString("description");
                    List<String> tags = (List<String>) doc.get("tags");

                    boolean match = (name != null && name.toLowerCase().contains(lowerQuery)) ||
                            (description != null && description.toLowerCase().contains(lowerQuery));
                    for (String tag : tags) {
                        if (tag.toLowerCase().contains(lowerQuery)) {
                            match = true;
                            break;
                        }
                    }

                    if (!match) continue;

                    String sellerID = doc.getString("userId");
                    if (sellerID == null || sellerID.isEmpty()) continue;

                    Task<Void> task = db.collection("users").document(sellerID).get().continueWith(userTask -> {
                        if (!userTask.isSuccessful()) return null;

                        DocumentSnapshot userDoc = userTask.getResult();
                        Product product = new Product(
                                doc.getId(),
                                userDoc.getString("profilePictureUrl"),
                                userDoc.getString("fullname"),
                                userDoc.getDouble("rating"),
                                (List<String>) doc.get("imageUrls"),
                                doc.getString("title"),
                                doc.getLong("likes"),
                                doc.getLong("comments"),
                                doc.getDouble("price"),
                                doc.getString("aspectRatio"),
                                sellerID
                        );
                        productList.add(product);
                        return null;
                    });
                    tasks.add(task);
                } catch (Exception e) {
                    Log.e("SearchFragment", "Error parsing product", e);
                }
            }

            Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                String pass=sortText.getText().toString().replace("Sort By", "").toLowerCase();
                applySort(pass.trim());
            });
        });
    }

    private void searchProfiles(String query) {
        db.collection("users").get().addOnSuccessListener(snapshot -> {
            profileList.clear();
            String lowerQuery = query.toLowerCase();

            for (QueryDocumentSnapshot doc : snapshot) {
                String name = doc.getString("fullname");
                if (name == null || !name.toLowerCase().contains(lowerQuery)) continue;
                if (!Objects.equals(doc.getString("userType"), "Seller")) continue;

                profileList.add(new Profile(
                        doc.getString("profilePictureUrl"),
                        name,
                        doc.getDouble("rating"),
                        doc.getLong("totalLikes"),
                        doc.getId()
                ));
            }

            String pass=sortText.getText().toString().replace("Sort By", "").toLowerCase();
            applySort(pass.trim());
        });
    }

    private void updateButtonColors() {
        Context context = requireContext();
        productsButton.setBackgroundColor(ContextCompat.getColor(context, isSearchingProducts ? R.color.darkred : R.color.grey));
        profileButton.setBackgroundColor(ContextCompat.getColor(context, isSearchingProducts ? R.color.grey : R.color.darkred));
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    private List<Profile> mergeSortProfiles(List<Profile> list, String sortBy) {
        if (list.size() <= 1) return list;
        int mid = list.size() / 2;
        List<Profile> left = mergeSortProfiles(list.subList(0, mid), sortBy);
        List<Profile> right = mergeSortProfiles(list.subList(mid, list.size()), sortBy);
        return mergeLists(left, right, sortBy);
    }

    private List<Product> mergeSortProducts(List<Product> list, String sortBy) {
        if (list.size() <= 1) return list;
        int mid = list.size() / 2;
        List<Product> left = mergeSortProducts(list.subList(0, mid), sortBy);
        List<Product> right = mergeSortProducts(list.subList(mid, list.size()), sortBy);
        return mergeLists(left, right, sortBy);
    }

    private <T> List<T> mergeLists(List<T> left, List<T> right, String sortBy) {
        List<T> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            Double leftVal = getValue(left.get(i), sortBy);
            Double rightVal = getValue(right.get(j), sortBy);
            if (leftVal >= rightVal) {
                result.add(sortBy.equals("price") ? right.get(j++): left.get(i++));
            } else {
                result.add(sortBy.equals("price") ? left.get(i++): right.get(j++));
            }
        }
        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        return result;
    }

    private Double getValue(Object obj, String key) {
        try {
            if (obj instanceof Profile) {
                Profile p = (Profile) obj;
                if ("likes".equals(key)) return (double) p.getTotalLikes();
                if ("rating".equals(key)) return p.getRating();
            } else if (obj instanceof Product) {
                Product p = (Product) obj;
                if ("price".equals(key)) return  p.getProductPrice();
                if ("likes".equals(key)) return (double) p.getProductLikes();
                if ("rating".equals(key)) return p.getSellerRating();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Could not sort", Toast.LENGTH_SHORT).show();
        }
        return 0.0;
    }
    private void saveRecentSearch(String searchedText) {
        if (searchedText == null || searchedText.trim().isEmpty()) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        DocumentReference userDocRef = db.collection("users").document(userId);

        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> recommended = new HashMap<>();
            Map<String, Object> searchedProducts = new HashMap<>();

            if (documentSnapshot.exists()) {
                Object recObj = documentSnapshot.get("recommended");
                if (recObj instanceof Map) {
                    recommended = (Map<String, Object>) recObj;

                    Object searchObj = recommended.get("searchedProducts");
                    if (searchObj instanceof Map) {
                        searchedProducts = (Map<String, Object>) searchObj;
                    }
                }
            }
            List<String> searchList = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                String key = String.valueOf(i);
                if (searchedProducts.containsKey(key)) {
                    searchList.add((String) searchedProducts.get(key));
                }
            }
            searchList.remove(searchedText);
            if (searchList.size() >= 10) {
                searchList.remove(0);
            }
            searchList.add(searchedText);

            Map<String, Object> updatedSearchMap = new HashMap<>();
            for (int i = 0; i < searchList.size(); i++) {
                updatedSearchMap.put(String.valueOf(i), searchList.get(i));
            }

            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("recommended.searchedProducts", updatedSearchMap);

            userDocRef.update(updateMap);

        }).addOnFailureListener(e -> Log.e("RecentSearch", "Fetch failed: " + e.getMessage()));
    }
}
