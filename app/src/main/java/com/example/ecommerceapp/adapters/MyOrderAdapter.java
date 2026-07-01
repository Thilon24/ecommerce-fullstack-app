package com.example.ecommerceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.activities.OrderHistoryActivity;
import com.example.ecommerceapp.models.MyOrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.ViewHolder> {

    Context context;
    List<MyOrderModel> list;
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    public MyOrderAdapter(Context context, List<MyOrderModel> list) {
        this.context = context;
        this.list = list;

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyOrderModel model = list.get(position);

        holder.orderId.setText("Order ID: " + model.getOrderId());
        holder.itemName.setText("Item: " + model.getProductName());
        holder.amount.setText(String.format("Amount: Rs. %.2f", model.getTotalAmount()));
        holder.status.setText("Status: " + model.getPaymentStatus());

        Glide.with(context)
                .load(model.getProductImage())
                .placeholder(R.drawable.bggg)
                .into(holder.itemImg);


        holder.itemView.setOnClickListener(v -> {
            if (context instanceof OrderHistoryActivity) {
                ((OrderHistoryActivity) context).generatePDF(model);
            }
        });

        if (model.getOrderDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            String date = sdf.format(model.getOrderDate().toDate());
            holder.date.setText("Date: " + date);
        }


        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (model.getDocumentId() != null) {
                    firestore.collection("CurrentUser")
                            .document(auth.getCurrentUser().getUid())
                            .collection("MyOrders")
                            .document(model.getDocumentId()) // Firestore Document ID එකෙන් මකනවා
                            .delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    int currentPosition = holder.getAdapterPosition();
                                    if (currentPosition != RecyclerView.NO_POSITION) {
                                        list.remove(currentPosition);
                                        notifyItemRemoved(currentPosition);
                                        notifyItemRangeChanged(currentPosition, list.size());
                                        Toast.makeText(context, "Order History Deleted Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(context, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(context, "Cannot delete: Document ID is null", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, itemName, amount, date, status;
        ImageView itemImg, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.order_id);
            itemName = itemView.findViewById(R.id.order_item_name);
            itemImg = itemView.findViewById(R.id.order_item_img);
            amount = itemView.findViewById(R.id.order_amount);
            date = itemView.findViewById(R.id.order_date);
            status = itemView.findViewById(R.id.order_status);

            deleteBtn = itemView.findViewById(R.id.order_delete_btn);
        }
    }
}