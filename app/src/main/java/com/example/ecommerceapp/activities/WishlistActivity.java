package com.example.ecommerceapp.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.adapters.WishlistAdapter;
import java.util.ArrayList;

public class WishlistActivity extends AppCompatActivity {

    ListView listView;
    DatabaseHelper myDb;


    ArrayList<String> itemIds, itemNames, itemPrices, itemImageUrls;
    WishlistAdapter adapter;
    TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        Toolbar toolbar = findViewById(R.id.wishlist_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Wishlist");
        }

        myDb = new DatabaseHelper(this);
        listView = findViewById(R.id.wishlist_list);
        emptyText = findViewById(R.id.empty_wishlist_text);


        itemIds = new ArrayList<>();
        itemNames = new ArrayList<>();
        itemPrices = new ArrayList<>();
        itemImageUrls = new ArrayList<>();

        viewData();
    }

    private void viewData() {

        itemIds.clear();
        itemNames.clear();
        itemPrices.clear();
        itemImageUrls.clear();

        Cursor cursor = myDb.getAllData();

        if (cursor.getCount() == 0) {
            emptyText.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

            while (cursor.moveToNext()) {
                // SQLite
                itemIds.add(cursor.getString(0));
                itemNames.add(cursor.getString(1));
                itemPrices.add(cursor.getString(2));
                itemImageUrls.add(cursor.getString(3));
            }


            adapter = new WishlistAdapter(this, itemIds, itemNames, itemPrices, itemImageUrls);
            listView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}