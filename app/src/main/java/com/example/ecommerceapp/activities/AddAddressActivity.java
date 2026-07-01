package com.example.ecommerceapp.activities;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ecommerceapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddAddressActivity extends AppCompatActivity implements SensorEventListener {

    EditText name, address, city, postalCode, phoneNumber;
    Toolbar toolbar;
    Button addAddressBtn, btnShowMap;

    FirebaseFirestore firestore;
    FirebaseAuth auth;

    // Sensor Variables
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float lastAcceleration;
    private float currentAcceleration;
    private float shake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        // Firebase & UI Initialize
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.add_address_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        name = findViewById(R.id.ad_name);
        address = findViewById(R.id.ad_address);
        city = findViewById(R.id.ad_city);
        phoneNumber = findViewById(R.id.ad_phone);
        postalCode = findViewById(R.id.ad_code);
        addAddressBtn = findViewById(R.id.ad_add_address);
        btnShowMap = findViewById(R.id.btn_show_on_map);

        // Sensor Setup
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        accelerationSetup();

        // --- Google Maps Intent ---
        btnShowMap.setOnClickListener(v -> {
            String userLoc = address.getText().toString() + " " + city.getText().toString();
            if (!userLoc.trim().isEmpty()) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(userLoc));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Please enter address details first!", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Add Address to Firestore ---
        addAddressBtn.setOnClickListener(v -> {
            String userName = name.getText().toString();
            String userCity = city.getText().toString();
            String userAddress = address.getText().toString();
            String userCode = postalCode.getText().toString();
            String userNumber = phoneNumber.getText().toString();

            if (!userName.isEmpty() && !userCity.isEmpty() && !userAddress.isEmpty() && !userCode.isEmpty() && !userNumber.isEmpty()) {

                String final_address = userName + ", " + userAddress + ", " + userCity + ". Post Code: " + userCode + ". Tel: " + userNumber;
                Map<String, Object> map = new HashMap<>();
                map.put("userAddress", final_address);

                firestore.collection("CurrentUser").document(auth.getCurrentUser().getUid())
                        .collection("Address").add(map).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(AddAddressActivity.this, "Address Added Successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(AddAddressActivity.this, AddressActivity.class));
                                finish();
                            }
                        });
            } else {
                Toast.makeText(AddAddressActivity.this, "Kindly Fill All Fields!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Sensor Logic ---
    private void accelerationSetup() {
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        lastAcceleration = currentAcceleration;
        currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = currentAcceleration - lastAcceleration;
        shake = shake * 0.9f + delta;


        if (shake > 12) {
            clearFields();
            Toast.makeText(this, "Fields Cleared by Shake!", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        name.setText("");
        address.setText("");
        city.setText("");
        postalCode.setText("");
        phoneNumber.setText("");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}