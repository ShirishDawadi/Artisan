package com.example.artisan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddAddress extends AppCompatActivity {
    private String productId;
    private String variationName;
    private int quantity;
    private TextView editTextFullName;
    private EditText  editTextMobile, editTextWard, editTextTole, editTextCity, editTextLandmark, editTextInstructions;
    private Spinner spinnerProvince, spinnerDistrict;
    private CheckBox checkBoxDefaultAddress;
    private Button buttonSaveAddress;
    private String selectedProvince = "";
    private String selectedDistrict = "";
    private FirebaseFirestore db;
    private String userId;
    private String userName;
    private Map<String, List<String>> provinceDistricts = new HashMap<>();
    private List<String> provinces = new ArrayList<>();
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_address);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        productId = intent.getStringExtra("productId");
        variationName = intent.getStringExtra("variationName");
        quantity = intent.getIntExtra("quantity", 0);

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextMobile = findViewById(R.id.editTextMobile);
        editTextWard = findViewById(R.id.editTextWard);
        editTextTole = findViewById(R.id.editTextTole);
        editTextCity = findViewById(R.id.editTextCity);
        editTextLandmark = findViewById(R.id.editTextLandmark);
        editTextInstructions = findViewById(R.id.editTextInstructions);
        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        checkBoxDefaultAddress = findViewById(R.id.checkBoxDefaultAddress);
        buttonSaveAddress = findViewById(R.id.buttonSaveAddress);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        populateProvinceDistrictData();
        populateProvinceSpinner();

        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProvince = provinces.get(position);
                populateDistrictSpinner(selectedProvince);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDistrict = (String) parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        loadUserDataAndAddress();
        buttonSaveAddress.setOnClickListener(v -> saveAddressToFirestore());

        backButton=findViewById(R.id.backButton);
        backButton.setOnClickListener(v->finish());
    }

    private void populateProvinceDistrictData() {
        provinces.add("Province No. 1");
        provinces.add("Province No. 2");
        provinces.add("Bagmati Province");
        provinces.add("Gandaki Province");
        provinces.add("Lumbini Province");
        provinces.add("Karnali Province");
        provinces.add("Sudurpashchim Province");

        provinceDistricts.put("Province No. 1", new ArrayList<>(List.of(
                "Bhojpur", "Dhankuta", "Ilam", "Jhapa", "Khotang", "Morang",
                "Okhaldhunga", "Panchthar", "Sankhuwasabha", "Solukhumbu",
                "Sunsari", "Taplejung", "Terhathum", "Udayapur"
        )));
        provinceDistricts.put("Province No. 2", new ArrayList<>(List.of(
                "Bara", "Dhanusha", "Mahottari", "Parsa", "Rautahat",
                "Saptari", "Sarlahi", "Siraha"
        )));
        provinceDistricts.put("Bagmati Province", new ArrayList<>(List.of(
                "Bhaktapur", "Chitwan", "Dhading", "Dolakha", "Kathmandu",
                "Kavrepalanchok", "Lalitpur", "Makwanpur", "Nuwakot",
                "Ramechhap", "Rasuwa", "Sindhuli", "Sindhupalchok"
        )));
        provinceDistricts.put("Gandaki Province", new ArrayList<>(List.of(
                "Baglung", "Gorkha", "Kaski", "Lamjung", "Manang",
                "Mustang", "Myagdi", "Nawalparasi (Bardaghat Susta East)",
                "Parbat", "Syangja", "Tanahun"
        )));
        provinceDistricts.put("Lumbini Province", new ArrayList<>(List.of(
                "Arghakhanchi", "Banke", "Bardiya", "Dang",
                "Eastern Rukum", "Kapilvastu", "Palpa", "Pyuthan",
                "Rolpa", "Rupandehi", "Nawalparasi (Bardaghat Susta West)"
        )));
        provinceDistricts.put("Karnali Province", new ArrayList<>(List.of(
                "Dailekh", "Dolpa", "Humla", "Jajarkot", "Jumla",
                "Kalikot", "Mugu", "Salyan", "Surkhet", "Western Rukum"
        )));
        provinceDistricts.put("Sudurpashchim Province", new ArrayList<>(List.of(
                "Achham", "Baitadi", "Bajhang", "Bajura", "Dadeldhura",
                "Darchula", "Doti", "Kailali", "Kanchanpur"
        )));
    }
    private void populateProvinceSpinner() {
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                provinces
        );
        provinceAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerProvince.setAdapter(provinceAdapter);

        int bagmatiIndex = provinces.indexOf("Bagmati Province");
        if (bagmatiIndex >= 0) {
            spinnerProvince.setSelection(bagmatiIndex);
            selectedProvince = "Bagmati Province";
            populateDistrictSpinner(selectedProvince);
        }
    }

    private void populateDistrictSpinner(String province) {

        List<String> districts = provinceDistricts.get(province);
        if (districts != null) {
            ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, districts);
            districtAdapter.setDropDownViewResource(R.layout.spinner_item);
            spinnerDistrict.setAdapter(districtAdapter);
            selectedDistrict = districts.get(0);
        } else {
            spinnerDistrict.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, new ArrayList<>()));
            selectedDistrict = "";
        }
    }

    private void loadUserDataAndAddress() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        userName = documentSnapshot.getString("fullname");
                        String userMobile = documentSnapshot.getString("phone");
                        if (userName != null) {
                            editTextFullName.setText(userName);
                        }
                        if (userMobile != null) {
                            editTextMobile.setText(userMobile);
                        }
                        Map<String, Object> defaultAddress = (Map<String, Object>) documentSnapshot.get("defaultAddress");

                        if (defaultAddress != null) {
                            String mobile = (String) defaultAddress.get("mobile");
                            String ward = (String) defaultAddress.get("ward");
                            String tole = (String) defaultAddress.get("tole");
                            String city = (String) defaultAddress.get("city");
                            String landmark = (String) defaultAddress.get("landmark");
                            String instructions = (String) defaultAddress.get("instructions");
                            String district = (String) defaultAddress.get("district");
                            String province = (String) defaultAddress.get("province");

                            editTextMobile.setText(mobile);
                            editTextWard.setText(ward);
                            editTextTole.setText(tole);
                            editTextCity.setText(city);
                            editTextLandmark.setText(landmark);
                            editTextInstructions.setText(instructions);

                            selectedDistrict = district;
                            selectedProvince = province;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
    }
    private void saveAddressToFirestore() {
        String buyerName = editTextFullName.getText().toString().trim();
        String mobile = editTextMobile.getText().toString().trim();
        String ward = editTextWard.getText().toString().trim();
        String tole = editTextTole.getText().toString().trim();
        String city = editTextCity.getText().toString().trim();
        String landmark = editTextLandmark.getText().toString().trim();
        String instructions = editTextInstructions.getText().toString().trim();
        boolean isDefault = checkBoxDefaultAddress.isChecked();

        if (mobile.isEmpty() || city.isEmpty() || selectedProvince.isEmpty() || selectedDistrict.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> address = new HashMap<>();
        address.put("mobile", mobile);
        address.put("ward", ward);
        address.put("tole", tole);
        address.put("city", city);
        address.put("district", selectedDistrict);
        address.put("province", selectedProvince);
        address.put("landmark", landmark);
        address.put("instructions", instructions);

        Intent intent = new Intent(AddAddress.this, PaymentOption.class);
        intent.putExtra("productId", productId);
        intent.putExtra("variationName", variationName);
        intent.putExtra("quantity", quantity);
        intent.putExtra("buyerName",buyerName);
        intent.putExtra("shippingAddress", (java.io.Serializable) address);

        db.collection("users").document(userId)
                .update("shippingAddress", address)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Address saved successfully.", Toast.LENGTH_SHORT).show();

                    if (isDefault) {
                        db.collection("users").document(userId)
                                .update("defaultAddress", address)
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(this, "Default address updated.", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                    finish();

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update default address.", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                    finish();
                                });
                    } else {
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save address.", Toast.LENGTH_SHORT).show();
                });
    }
}