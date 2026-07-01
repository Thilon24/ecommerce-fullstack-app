package com.example.ecommerceapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64; // වැදගත්: Base64 සඳහා
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ecommerceapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView profileImg;
    EditText name, email;
    Button updateBtn;

    FirebaseFirestore db;
    FirebaseAuth auth;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase Initialize  NO using Storage
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileImg = findViewById(R.id.profile_img);
        name = findViewById(R.id.profile_name);
        email = findViewById(R.id.profile_email);
        updateBtn = findViewById(R.id.profile_update_btn);

        loadUserData();

        profileImg.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 33);
        });

        updateBtn.setOnClickListener(v -> updateProfile());
    }

    private void loadUserData() {
        if (auth.getCurrentUser() != null) {
            email.setText(auth.getCurrentUser().getEmail());

            db.collection("CurrentUser").document(auth.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String uName = task.getResult().getString("userName");
                            String uImg = task.getResult().getString("profileImage");

                            name.setText(uName);
                            if (uImg != null && !uImg.isEmpty()) {

                                try {
                                    byte[] decodedString = Base64.decode(uImg, Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    profileImg.setImageBitmap(decodedByte);
                                } catch (Exception e) {

                                    Glide.with(this).load(uImg).into(profileImg);
                                }
                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImg.setImageURI(imageUri);
        }
    }



    private void updateProfile() {
        String currentUid = auth.getUid();
        String updatedName = name.getText().toString();

        if (updatedName.isEmpty()) {
            name.setError("Enter Your Name");
            return;
        }

        Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userName", updatedName);

        if (imageUri != null) {

            String base64Image = encodeImage(imageUri);
            if (base64Image != null) {
                userMap.put("profileImage", base64Image);
            }
        }

         db.collection("CurrentUser").document(currentUid)
                .set(userMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private String encodeImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream);
            byte[] bytes = outputStream.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}