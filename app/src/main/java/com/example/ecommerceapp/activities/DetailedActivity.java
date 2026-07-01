package com.example.ecommerceapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.adapters.ReviewAdapter;
import com.example.ecommerceapp.models.NewProductsModel;
import com.example.ecommerceapp.models.PopularProductsModel;
import com.example.ecommerceapp.models.ReviewModel;
import com.example.ecommerceapp.models.ShowAllModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DetailedActivity extends AppCompatActivity {

    ImageView detailedImg;
    TextView name, description, price, quantity;
    RatingBar rating;
    Button addToCart, buyNow, submitReviewBtn, selectImgBtn;
    ImageView addItems, removeItems, reviewImgPreview;

    // --- WISHLIST VARIABLES ---
    ImageButton wishlistBtn;
    DatabaseHelper myDb;
    Animation bounceAnim;

    EditText reviewEditText;
    RatingBar inputRatingBar;

    Toolbar toolbar;
    int totalQuantity = 1;
    int totalPrice = 0;

    private Uri imageUri = null;
    private static final int PICK_IMAGE_REQUEST = 1;

    NewProductsModel newProductsModel = null;
    PopularProductsModel popularProductsModel = null;
    ShowAllModel showAllModel = null;

    FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize SQLite & Animation
        myDb = new DatabaseHelper(this);
        bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

        detailedImg = findViewById(R.id.detailed_img);
        quantity = findViewById(R.id.quantity);
        name = findViewById(R.id.detailed_name);
        rating = findViewById(R.id.my_rating);
        description = findViewById(R.id.detailed_desc);
        price = findViewById(R.id.detailed_price);
        addToCart = findViewById(R.id.add_to_cart);
        buyNow = findViewById(R.id.buy_now);
        addItems = findViewById(R.id.add_item);
        removeItems = findViewById(R.id.remove_item);
        submitReviewBtn = findViewById(R.id.submit_review_btn);
        inputRatingBar = findViewById(R.id.input_rating);
        reviewEditText = findViewById(R.id.review_edit_text);

        wishlistBtn = findViewById(R.id.btn_wishlist);
        selectImgBtn = findViewById(R.id.select_img_btn);
        reviewImgPreview = findViewById(R.id.review_img_preview);

        RecyclerView reviewRecycler = findViewById(R.id.review_recycler);
        reviewRecycler.setLayoutManager(new LinearLayoutManager(this));
        List<ReviewModel> reviewModelList = new ArrayList<>();
        ReviewAdapter reviewAdapter = new ReviewAdapter(reviewModelList);
        reviewRecycler.setAdapter(reviewAdapter);


        final Object obj = getIntent().getSerializableExtra("detailed");
        if (obj instanceof NewProductsModel) {
            newProductsModel = (NewProductsModel) obj;
        } else if (obj instanceof PopularProductsModel) {
            popularProductsModel = (PopularProductsModel) obj;
        } else if (obj instanceof ShowAllModel) {
            showAllModel = (ShowAllModel) obj;
        }


        setDataToUI();

        wishlistBtn.setOnClickListener(v -> {
            wishlistBtn.startAnimation(bounceAnim);
            String pName = name.getText().toString();
            String pPrice = price.getText().toString();
            String pImageUrl = getProductImageUrl();

            boolean isInserted = myDb.insertData(pName, pPrice, pImageUrl);
            if (isInserted) {
                wishlistBtn.setImageResource(R.drawable.favorite_24px);

                Toast.makeText(DetailedActivity.this, "Added To Wish List❤️", Toast.LENGTH_SHORT).show();
            }
        });

        firestore.collection("Reviews")
                .whereEqualTo("productName", name.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            ReviewModel model = doc.toObject(ReviewModel.class);
                            reviewModelList.add(model);
                        }
                        reviewAdapter.notifyDataSetChanged();
                    }
                });

        toolbar = findViewById(R.id.detailed_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        selectImgBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        submitReviewBtn.setOnClickListener(v -> uploadImageAndSubmitReview());
        addToCart.setOnClickListener(v -> addToCart());

        // Quantity
        addItems.setOnClickListener(v -> {
            if (totalQuantity < 10) {
                totalQuantity++;
                quantity.setText(String.valueOf(totalQuantity));
                updatePrices();
            }
        });


        removeItems.setOnClickListener(v -> {
            if (totalQuantity > 1) {
                totalQuantity--;
                quantity.setText(String.valueOf(totalQuantity));
                updatePrices();
            }
        });


        buyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailedActivity.this, AddressActivity.class);


                intent.putExtra("totalAmount", (double) totalPrice);

                intent.putExtra("item", (java.io.Serializable) obj);
                intent.putExtra("productName", name.getText().toString());
                intent.putExtra("productImage", getProductImageUrl());

                startActivity(intent);
            }
        });
    }

    private String getProductImageUrl() {
        if (newProductsModel != null) return newProductsModel.getImg_url();
        if (popularProductsModel != null) return popularProductsModel.getImg_url();
        if (showAllModel != null) return showAllModel.getImg_url();
        return "";
    }

    private void setDataToUI() {
        int basePrice = 0;
        if (newProductsModel != null) {
            Glide.with(getApplicationContext()).load(newProductsModel.getImg_url()).into(detailedImg);
            name.setText(newProductsModel.getName());
            description.setText(newProductsModel.getDescription());
            basePrice = newProductsModel.getPrice();
            if (newProductsModel.getRating() != null) rating.setRating(Float.parseFloat(newProductsModel.getRating()));
        } else if (popularProductsModel != null) {
            Glide.with(getApplicationContext()).load(popularProductsModel.getImg_url()).into(detailedImg);
            name.setText(popularProductsModel.getName());
            description.setText(popularProductsModel.getDescription());
            basePrice = popularProductsModel.getPrice();
            if (popularProductsModel.getRating() != null) rating.setRating(Float.parseFloat(popularProductsModel.getRating()));
        } else if (showAllModel != null) {
            Glide.with(getApplicationContext()).load(showAllModel.getImg_url()).into(detailedImg);
            name.setText(showAllModel.getName());
            description.setText(showAllModel.getDescription());
            basePrice = showAllModel.getPrice();
            if (showAllModel.getRating() != null) rating.setRating(Float.parseFloat(showAllModel.getRating()));
        }

        totalPrice = basePrice * totalQuantity;
        price.setText("RS. " + totalPrice);
    }

    private void updatePrices() {
        int basePrice = 0;
        if (newProductsModel != null) basePrice = newProductsModel.getPrice();
        else if (popularProductsModel != null) basePrice = popularProductsModel.getPrice();
        else if (showAllModel != null) basePrice = showAllModel.getPrice();

        totalPrice = basePrice * totalQuantity;
        price.setText("RS. " + totalPrice);
    }

    private void addToCart() {
        Animation popIn = AnimationUtils.loadAnimation(this, R.anim.pop_in);
        addToCart.startAnimation(popIn);

        android.media.MediaPlayer mediaPlayer = android.media.MediaPlayer.create(DetailedActivity.this, R.raw.beep);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(android.media.MediaPlayer::release);

        String saveCurrentTime, saveCurrentDate;
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        final HashMap<String, Object> cartMap = new HashMap<>();
        cartMap.put("productName", name.getText().toString());
        cartMap.put("productPrice", String.valueOf(totalPrice));
        cartMap.put("currentTime", saveCurrentTime);
        cartMap.put("currentDate", saveCurrentDate);
        cartMap.put("totalQuantity", quantity.getText().toString());
        cartMap.put("totalPrice", totalPrice);
        cartMap.put("productImage", getProductImageUrl());
        firestore.collection("AddToCart").document(auth.getCurrentUser().getUid())
                .collection("User").add(cartMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(DetailedActivity.this, "Added To Cart Success! 🛒", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            reviewImgPreview.setVisibility(View.VISIBLE);
            reviewImgPreview.setImageURI(imageUri);
        }
    }

    private void uploadImageAndSubmitReview() {
        String reviewText = reviewEditText.getText().toString().trim();
        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Please write something!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageUri != null) {
            StorageReference ref = storage.getReference().child("review_images/" + UUID.randomUUID().toString());
            ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveReviewToFirestore(uri.toString());
                });
            });
        } else {
            saveReviewToFirestore("");
        }
    }

    private void saveReviewToFirestore(String imageUrl) {
        HashMap<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("userName", auth.getCurrentUser().getEmail());
        reviewMap.put("productName", name.getText().toString());
        reviewMap.put("rating", inputRatingBar.getRating());
        reviewMap.put("review", reviewEditText.getText().toString());
        reviewMap.put("reviewImg", imageUrl);
        reviewMap.put("timestamp", FieldValue.serverTimestamp());

        firestore.collection("Reviews").add(reviewMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DetailedActivity.this, "Review Submitted!", Toast.LENGTH_SHORT).show();
                reviewEditText.setText("");
                inputRatingBar.setRating(0);
                reviewImgPreview.setVisibility(View.GONE);
                imageUri = null;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}