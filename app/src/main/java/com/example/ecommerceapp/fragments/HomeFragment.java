package com.example.ecommerceapp.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.activities.ShowAllActivity;
import com.example.ecommerceapp.adapters.CategoryAdapter;
import com.example.ecommerceapp.adapters.NewProductsAdapter;
import com.example.ecommerceapp.adapters.PopularProductsAdapter;
import com.example.ecommerceapp.models.CategoryModel;
import com.example.ecommerceapp.models.NewProductsModel;
import com.example.ecommerceapp.models.PopularProductsModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    TextView catShowAll, popularShowAll, newProductShowAll;
    LinearLayout linearLayout;
    ProgressDialog progressDialog;
    RecyclerView catRecyclerView, newProductRecyclerView, popularRecyclerView;

    CategoryAdapter categoryAdapter;
    List<CategoryModel> categoryModelList;

    NewProductsAdapter newProductsAdapter;
    List<NewProductsModel> newProductsModelList;

    PopularProductsAdapter popularProductsAdapter;
    List<PopularProductsModel> popularProductsModelList;

    SearchView searchView;
    FirebaseFirestore db;

    public HomeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(getActivity());

        searchView = root.findViewById(R.id.search_view);
        catRecyclerView = root.findViewById(R.id.rec_category);
        newProductRecyclerView = root.findViewById(R.id.new_product_rec);
        popularRecyclerView = root.findViewById(R.id.popular_rec);
        catShowAll = root.findViewById(R.id.category_see_all);
        popularShowAll = root.findViewById(R.id.popular_see_all);
        newProductShowAll = root.findViewById(R.id.newProducts_see_all);
        linearLayout = root.findViewById(R.id.home_layout);

        progressDialog.setTitle("Welcome To STHUB");
        progressDialog.setMessage("Please Wait....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        setupImageSlider(root);
        initRecyclerViews();
        loadFirebaseData();
        setupClickListeners();


        String url = "https://raw.githubusercontent.com/ShehanThilon/dummy-api/main/status.txt";

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {

                    Toast.makeText(getActivity(), "Server Message: " + response, Toast.LENGTH_LONG).show();
                },
                error -> android.util.Log.e("VOLLEY_ERROR", error.toString())
        );
        queue.add(stringRequest);

        return root;
    }

    private void filterList(String text) {
        List<PopularProductsModel> filteredList = new ArrayList<>();
        for (PopularProductsModel item : popularProductsModelList) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (!filteredList.isEmpty()) {
            popularProductsAdapter.setFilteredList(filteredList);
        }
    }

    private void initRecyclerViews() {
        // Category
        catRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        categoryModelList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(getContext(), categoryModelList);
        catRecyclerView.setAdapter(categoryAdapter);

        // New Products
        newProductRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        newProductsModelList = new ArrayList<>();
        newProductsAdapter = new NewProductsAdapter(getContext(), newProductsModelList);
        newProductRecyclerView.setAdapter(newProductsAdapter);

        // Popular Products
        popularRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        popularProductsModelList = new ArrayList<>();
        popularProductsAdapter = new PopularProductsAdapter(getContext(), popularProductsModelList);
        popularRecyclerView.setAdapter(popularProductsAdapter);
    }

    private void loadFirebaseData() {

        db.collection("Category").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                categoryModelList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    CategoryModel model = document.toObject(CategoryModel.class);
                    categoryModelList.add(model);
                }
                categoryAdapter.notifyDataSetChanged();
                linearLayout.setVisibility(View.VISIBLE);
                progressDialog.dismiss();
            }
        });


        db.collection("NewProducts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                newProductsModelList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    NewProductsModel model = document.toObject(NewProductsModel.class);
                    newProductsModelList.add(model);
                }
                newProductsAdapter.notifyDataSetChanged();
                if (newProductsModelList.size() > 0) {
                    Toast.makeText(getActivity(), "Products Loaded: " + newProductsModelList.size(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        db.collection("AllProducts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                popularProductsModelList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    PopularProductsModel model = document.toObject(PopularProductsModel.class);
                    popularProductsModelList.add(model);
                }
                popularProductsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setupImageSlider(View root) {
        ImageSlider imageSlider = root.findViewById(R.id.image_slider);
        List<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.banner1, "Discount On Shoes Items", ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.banner2, "Discount On Perfume", ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.banner3, "70% OFF", ScaleTypes.CENTER_CROP));
        imageSlider.setImageList(slideModels);
    }

    private void setupClickListeners() {
        View.OnClickListener seeAllClick = v -> startActivity(new Intent(getContext(), ShowAllActivity.class));
        catShowAll.setOnClickListener(seeAllClick);
        newProductShowAll.setOnClickListener(seeAllClick);
        popularShowAll.setOnClickListener(seeAllClick);
    }
}