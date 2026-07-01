package com.example.ecommerceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.activities.DatabaseHelper;
import java.util.ArrayList;

public class WishlistAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> ids, names, prices, imageUrls;
    LayoutInflater inflater;
    DatabaseHelper dbHelper;

    public WishlistAdapter(Context context, ArrayList<String> ids, ArrayList<String> names, ArrayList<String> prices, ArrayList<String> imageUrls) {
        this.context = context;
        this.ids = ids;
        this.names = names;
        this.prices = prices;
        this.imageUrls = imageUrls;
        this.dbHelper = new DatabaseHelper(context);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return names.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.wishlist_item_layout, parent, false);
        }

        ImageView img = convertView.findViewById(R.id.wishlist_item_image);
        TextView name = convertView.findViewById(R.id.wishlist_item_name);
        TextView price = convertView.findViewById(R.id.wishlist_item_price);
        ImageView deleteBtn = convertView.findViewById(R.id.wishlist_delete_btn);

        name.setText(names.get(position));
        price.setText(prices.get(position));

        Glide.with(context).load(imageUrls.get(position)).into(img);


        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String currentId = ids.get(position);
                Integer deletedRows = dbHelper.deleteData(currentId);

                if (deletedRows > 0) {

                    ids.remove(position);
                    names.remove(position);
                    prices.remove(position);
                    imageUrls.remove(position);


                    notifyDataSetChanged();
                    Toast.makeText(context, "Removed from Wishlist", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error deleting item", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return convertView;
    }
}