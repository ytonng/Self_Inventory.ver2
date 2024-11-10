package com.example.imagerec;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Set up the image slider
        ImageSlider imageSlider = view.findViewById(R.id.imageSlider);
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.slideimg1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slideimg2, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slideimg3, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slideimg4, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slideimg5, ScaleTypes.FIT));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

        // Set up ImageButtons with click listeners to open RecommendDishes fragment with different dish data
        view.findViewById(R.id.btn1).setOnClickListener(v -> openRecommendDishesFragment("Dish 1"));
        view.findViewById(R.id.btn2).setOnClickListener(v -> openRecommendDishesFragment("Dish 2"));
        view.findViewById(R.id.btn3).setOnClickListener(v -> openRecommendDishesFragment("Dish 3"));
        view.findViewById(R.id.btn4).setOnClickListener(v -> openRecommendDishesFragment("Dish 4"));

        return view;
    }

    private void openRecommendDishesFragment(String dishName) {
        // Create new instance of RecommendDishes fragment
        RecommendDishes recommendDishesFragment = new RecommendDishes();

        // Create a bundle to hold the dish data
        Bundle bundle = new Bundle();
        bundle.putString("dish_name", dishName); // Pass the dish name or ID
        recommendDishesFragment.setArguments(bundle); // Set arguments to fragment

        // Replace current fragment with RecommendDishes fragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, recommendDishesFragment);
        transaction.addToBackStack(null); // Adds to back stack so user can navigate back
        transaction.commit();
    }
}
