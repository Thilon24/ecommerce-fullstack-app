package com.example.ecommerceapp.activities;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerceapp.R;
import com.example.ecommerceapp.adapters.MyOrderAdapter;
import com.example.ecommerceapp.models.MyOrderModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderHistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<MyOrderModel> orderList;
    MyOrderAdapter adapter;
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        recyclerView = findViewById(R.id.order_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        adapter = new MyOrderAdapter(this, orderList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadOrders();
    }

    private void loadOrders() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        firestore.collection("CurrentUser").document(userId).collection("MyOrders")
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        orderList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            MyOrderModel model = doc.toObject(MyOrderModel.class);


                            if (model != null) {
                                model.setDocumentId(doc.getId());
                                orderList.add(model);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void generatePDF(MyOrderModel model) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Title
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(30);
        titlePaint.setColor(Color.BLUE);
        canvas.drawText("STHUB INVOICE", 297, 80, titlePaint);

        // Content
        paint.setTextSize(14);
        paint.setColor(Color.BLACK);
        canvas.drawText("Order ID: " + model.getOrderId(), 50, 150, paint);
        canvas.drawText("Item: " + model.getProductName(), 50, 180, paint);
        canvas.drawText("Amount Paid: Rs. " + model.getTotalAmount(), 50, 210, paint);
        canvas.drawText("Status: " + model.getPaymentStatus(), 50, 240, paint);


        if (model.getOrderDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String date = sdf.format(model.getOrderDate().toDate());
            canvas.drawText("Date: " + date, 50, 270, paint); // value of Y (adu krnw)
        }

        canvas.drawText("--------------------------------------------------", 50, 300, paint);
        titlePaint.setTextSize(18);
        titlePaint.setColor(Color.BLACK);
        canvas.drawText("Thank you for shopping with us!", 297, 350, titlePaint);

        pdfDocument.finishPage(page);

        // File Save Logic
        String fileName = "Invoice_" + System.currentTimeMillis() + ".pdf";
        File file = new File(getExternalFilesDir(null), fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF Saved Successfully", Toast.LENGTH_SHORT).show();
            viewPDF(file);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }

    private void viewPDF(File file) {

        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No PDF Viewer found. Please install one.", Toast.LENGTH_SHORT).show();
        }
    }
}