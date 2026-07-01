package com.example.ecommerceapp;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.ecommerceapp.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap mMap = googleMap;



        LatLng storeLocation = new LatLng(7.2422, 80.1246);


        mMap.addMarker(new MarkerOptions()
                .position(storeLocation)
                .title("ST HUb STORE")
                .snippet("Open in 9.00AM To 5.00PM"));


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLocation, 15f));


        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}