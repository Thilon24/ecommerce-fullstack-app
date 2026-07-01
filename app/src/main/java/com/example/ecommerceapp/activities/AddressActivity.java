package com.example.ecommerceapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerceapp.R;
import com.example.ecommerceapp.adapters.AddressAdapter;
import com.example.ecommerceapp.models.AddressModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddressActivity extends AppCompatActivity implements AddressAdapter.SelectedAddress {

    Button addAddress;
    RecyclerView recyclerView;
    private List<AddressModel> addressModelList;
    private AddressAdapter addressAdapter;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    Button paymentBtn;
    Toolbar toolbar;
    String mAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        toolbar = findViewById(R.id.address_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.address_recycler);
        paymentBtn = findViewById(R.id.payment_btn);
        addAddress = findViewById(R.id.add_address_btn);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        addressModelList = new ArrayList<>();
        addressAdapter = new AddressAdapter(getApplicationContext(), addressModelList, this);
        recyclerView.setAdapter(addressAdapter);


        firestore.collection("CurrentUser").document(auth.getCurrentUser().getUid())
                .collection("Address").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                AddressModel addressModel = doc.toObject(AddressModel.class);
                                addressModelList.add(addressModel);
                                addressAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

        paymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAddress.isEmpty()) {
                    Toast.makeText(AddressActivity.this, "Please select an address", Toast.LENGTH_SHORT).show();
                    return;
                }


                double finalAmount = getIntent().getDoubleExtra("totalAmount", 0.0);


                Intent intent = new Intent(AddressActivity.this, PaymentActivity.class);
                intent.putExtra("amount", finalAmount);
                intent.putExtra("address", mAddress);
                intent.putExtra("productName", getIntent().getStringExtra("productName"));
                intent.putExtra("productImage", getIntent().getStringExtra("productImage"));
                startActivity(intent);
            }
        });

        addAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddressActivity.this, AddAddressActivity.class));
            }
        });
    }


    private void clearCart() {
        firestore.collection("AddToCart")
                .document(auth.getCurrentUser().getUid())
                .collection("User")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                doc.getReference().delete();
                            }
                        }
                    }
                });
    }

    @Override
    public void setAddress(String address) {
        mAddress = address;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}