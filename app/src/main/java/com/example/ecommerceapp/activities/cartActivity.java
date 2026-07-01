package com.example.ecommerceapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerceapp.R;
import com.example.ecommerceapp.adapters.MyCartAdapter;
import com.example.ecommerceapp.models.MyCartModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class cartActivity extends AppCompatActivity {


    int overAllTotalAmount = 0;
    TextView overAllAmount;
    Toolbar toolbar;
    RecyclerView recyclerView;
    List<MyCartModel> cartModelList;
    MyCartAdapter cartAdapter;
    Button buyNowBtn;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.my_cart_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        overAllAmount = findViewById(R.id.textView4);
        recyclerView = findViewById(R.id.cart_rec);
        buyNowBtn = findViewById(R.id.buy_now);

        // Broadcast Registration
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver, new IntentFilter("MyTotalAmount"));

        // RecyclerView Setup
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartModelList = new ArrayList<>();
        cartAdapter = new MyCartAdapter(this, cartModelList);
        recyclerView.setAdapter(cartAdapter);


        firestore.collection("AddToCart").document(auth.getCurrentUser().getUid())
                .collection("User").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                MyCartModel myCartModel = doc.toObject(MyCartModel.class);
                                if (myCartModel != null) {
                                    myCartModel.setDocumentId(doc.getId());
                                }
                                cartModelList.add(myCartModel);
                                cartAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

        // Buy Now Button Click Logic
        buyNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (overAllTotalAmount > 0) {
                    Intent intent = new Intent(cartActivity.this, AddressActivity.class);

                    intent.putExtra("totalAmount", (double) overAllTotalAmount);
                    startActivity(intent);
                } else {
                    Toast.makeText(cartActivity.this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            overAllTotalAmount = intent.getIntExtra("totalAmount", 0);
            overAllAmount.setText("Total Amount : Rs. " + overAllTotalAmount);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }
}