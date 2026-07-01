package com.example.ecommerceapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerceapp.R;
import com.example.ecommerceapp.models.MyCartModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;



public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.ViewHolder> {

    Context context;
    List<MyCartModel> list;
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    public MyCartAdapter(Context context, List<MyCartModel> list) {
        this.context = context;
        this.list = list;
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.my_cart_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyCartModel model = list.get(position);

        holder.name.setText(model.getProductName());
        holder.price.setText("Rs. " + model.getProductPrice());
        holder.totalPrice.setText("Total: Rs. " + model.getTotalPrice());
        holder.totalQuantity.setText("Qty: " + model.getTotalQuantity());

        updateTotalAmount();

        holder.deleteBtn.setOnClickListener(v -> {
            if (model.getDocumentId() != null) {
                firestore.collection("AddToCart")
                        .document(auth.getCurrentUser().getUid())
                        .collection("User")
                        .document(model.getDocumentId())
                        .delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                list.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                                notifyItemRangeChanged(holder.getAdapterPosition(), list.size());
                                updateTotalAmount();
                                Toast.makeText(context, "Item Deleted!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void updateTotalAmount() {
        int overAllTotalAmount = 0;
        for (MyCartModel model : list) {
            overAllTotalAmount += model.getTotalPrice();
        }
        Intent intent = new Intent("MyTotalAmount");
        intent.putExtra("totalAmount", overAllTotalAmount);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, date, time, totalQuantity, totalPrice;
        ImageView deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            date = itemView.findViewById(R.id.current_date);
            time = itemView.findViewById(R.id.current_time);
            totalQuantity = itemView.findViewById(R.id.total_quantity);
            totalPrice = itemView.findViewById(R.id.total_price);


            deleteBtn = itemView.findViewById(R.id.delete_item_btn);
        }
    }
}