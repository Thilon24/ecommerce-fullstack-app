package com.example.ecommerceapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.ecommerceapp.MapsActivity;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.fragments.HomeFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    Fragment homeFragment;
    FirebaseAuth auth;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FirebaseFirestore firestore;

    private MediaPlayer mediaPlayer;
    private SensorManager sensorManager;
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private ChargerReceiver chargerReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permission Check
        checkNotificationPermission();

        // Broadcast Receiver Setup
        chargerReceiver = new ChargerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(chargerReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(chargerReceiver, filter);
        }

        // Firebase Initialize
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // UI Setup
        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Navigation Listener
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(MainActivity.this, cartActivity.class));
            } else if (id == R.id.nav_wishlist) {
                startActivity(new Intent(MainActivity.this, WishlistActivity.class));
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }else if(id == R.id.nav_messages) {
                startActivity(new Intent(MainActivity.this, ChatActivity.class));
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(MainActivity.this, OrderHistoryActivity.class));
            } else if (id == R.id.nav_contact) {
                showContactDialog();
            } else if (id == R.id.nav_about) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            } else if (id == R.id.nav_promo) {
                playPromoAudio();
            } else if (id == R.id.nav_logout) {
                logoutUser();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Header Data Setup
        updateHeaderData();

        // Initial Fragment
        homeFragment = new HomeFragment();
        loadFragment(homeFragment);

        // Sensor Initialization
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acceleration = 10f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
    }

    private void updateHeaderData() {
        View headerView = navigationView.getHeaderView(0);
        TextView userEmail = headerView.findViewById(R.id.user_email_txt);
        TextView userName = headerView.findViewById(R.id.user_name_txt);
        CircleImageView userImg = headerView.findViewById(R.id.user_image_view);

        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            userEmail.setText(auth.getCurrentUser().getEmail());

            firestore.collection("CurrentUser").document(uid).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {

                                //usename
                                if (doc.getString("userName") != null) {
                                    userName.setText(doc.getString("userName"));
                                }

                                //Show base 64 image
                                String base64Image = doc.getString("profileImage");
                                if (base64Image != null && !base64Image.isEmpty()) {
                                    try {
                                        // Base64 String Conver to img
                                        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);


                                        userImg.setImageBitmap(decodedByte);
                                    } catch (Exception e) {

                                        userImg.setImageResource(R.drawable.ic_baseline_account_circle);
                                    }
                                }
                            }
                        }
                    });
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void playPromoAudio() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.welcome_sound);
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Toast.makeText(this, "Audio Paused", Toast.LENGTH_SHORT).show();
        } else {
            mediaPlayer.start();
            Toast.makeText(this, "Playing Welcome Audio...", Toast.LENGTH_SHORT).show();
        }
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = currentAcceleration - lastAcceleration;
            acceleration = acceleration * 0.9f + delta;
            if (acceleration > 12) {
                showContactDialog();
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private void showContactDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Contact Support");
        builder.setMessage("Need help? Do you want to call our customer care?");
        builder.setPositiveButton("Call Now", (dialog, which) -> {
            String phoneNumber = "0702586129";
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void logoutUser() {
        auth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        }

        updateHeaderData();
    }

    @Override
    protected void onPause() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorListener);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chargerReceiver != null) {
            unregisterReceiver(chargerReceiver);
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.home_container, fragment);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        if (id == R.id.menu_my_cart) {
            startActivity(new Intent(MainActivity.this, cartActivity.class));
            return true;
        }
        if (id == R.id.menu_logout) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}