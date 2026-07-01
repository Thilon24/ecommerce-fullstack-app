package com.example.ecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerceapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

// PayHere SDK Imports
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class PaymentActivity extends AppCompatActivity {

    double amount = 0.0;
    String userAddress = "";
    Toolbar toolbar;
    TextView subTotal, total;
    Button paymentBtn;

    private static final int PAYHERE_REQUEST = 11001;

    // Firebase variables
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Firebase Initialize
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.payment_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        amount = getIntent().getDoubleExtra("amount", 0.0);


        userAddress = getIntent().getStringExtra("address");
        if (userAddress == null || userAddress.isEmpty()) {
            userAddress = "No Address Provided";
        }

        subTotal = findViewById(R.id.sub_total);
        total = findViewById(R.id.total_amt);
        paymentBtn = findViewById(R.id.pay_btn);

        subTotal.setText(String.format("Rs. %.2f", amount));
        total.setText(String.format("Rs. %.2f", amount));

        paymentBtn.setOnClickListener(v -> {
            if (amount > 0) {
                paymentMethod();
            } else {
                Toast.makeText(this, "Amount cannot be zero!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void paymentMethod() {
        InitRequest req = new InitRequest();
        req.setMerchantId("1226946");
        req.setMerchantSecret("Mjk2MzUzMDk0MDI3MjY2OTQ3NzAxNjIxNDgwMjA2Mjk4NjE0NDczMg==");
        req.setCurrency("LKR");
        req.setAmount(amount);
        req.setOrderId("Order_" + System.currentTimeMillis());
        req.setItemsDescription("E-Commerce Purchase");

        req.getItems().add(new lk.payhere.androidsdk.model.Item("1", "E-Commerce Item", 1, amount));


        req.getCustomer().setFirstName("Shehan");
        req.getCustomer().setLastName("Thilon");
        req.getCustomer().setEmail("thilonproject@gmail.com");
        req.getCustomer().setPhone("+94702586129");


        req.getCustomer().getAddress().setAddress(userAddress);
        req.getCustomer().getAddress().setCity("Gampaha");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

            if (resultCode == Activity.RESULT_OK) {
                if (response != null && response.isSuccess()) {
                    saveOrderToFirestore();
                    clearCart();
                    playSuccessSound();
                    sendSuccessNotification();

                    Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    String errorMsg = (response != null && response.getData() != null) ? response.getData().getMessage() : "Payment Failed";
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                String cancelMsg = (response != null && response.getData() != null) ? response.getData().getMessage() : "Payment Canceled";
                Toast.makeText(this, cancelMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearCart() {
        if (auth.getCurrentUser() != null) {
            firestore.collection("AddToCart")
                    .document(auth.getCurrentUser().getUid())
                    .collection("User")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                doc.getReference().delete();
                            }
                        }
                    });
        }
    }

    private void saveOrderToFirestore() {
        if (auth.getCurrentUser() != null) {
            String currentUserId = auth.getCurrentUser().getUid();

            String directPName = getIntent().getStringExtra("productName");
            String directPImg = getIntent().getStringExtra("productImage");


            if (directPName != null && !directPName.isEmpty()) {
                HashMap<String, Object> orderMap = new HashMap<>();
                orderMap.put("orderId", "ORD_" + System.currentTimeMillis());
                orderMap.put("productName", directPName);
                orderMap.put("productImage", directPImg);
                orderMap.put("totalAmount", amount);
                orderMap.put("paymentStatus", "Paid");
                orderMap.put("orderDate", com.google.firebase.Timestamp.now());


                orderMap.put("deliveryAddress", userAddress);

                firestore.collection("CurrentUser").document(currentUserId)
                        .collection("MyOrders").add(orderMap);

            } else {

                firestore.collection("AddToCart").document(currentUserId)
                        .collection("User").get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                for (DocumentSnapshot doc : task.getResult()) {
                                    HashMap<String, Object> cartOrderMap = new HashMap<>();
                                    cartOrderMap.put("orderId", "ORD_" + System.currentTimeMillis());
                                    cartOrderMap.put("productName", doc.get("productName"));
                                    cartOrderMap.put("productImage", doc.get("productImage"));
                                    cartOrderMap.put("totalAmount", doc.get("totalPrice"));
                                    cartOrderMap.put("paymentStatus", "Paid");
                                    cartOrderMap.put("orderDate", com.google.firebase.Timestamp.now());


                                    cartOrderMap.put("deliveryAddress", userAddress);

                                    firestore.collection("CurrentUser").document(currentUserId)
                                            .collection("MyOrders").add(cartOrderMap);
                                }
                            }
                        });
            }
        }
    }

    private void playSuccessSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.payment_success);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        }
    }

    private void sendSuccessNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("payment_channel", "Payment Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "payment_channel")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Order Placed")
                .setContentText("Your payment of Rs. " + amount + " was successful!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}