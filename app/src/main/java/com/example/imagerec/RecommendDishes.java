package com.example.imagerec;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class RecommendDishes extends Fragment {

    private String dishName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend_dishes, container, false);

        // Retrieve dish data from arguments
        if (getArguments() != null) {
            dishName = getArguments().getString("dish_name");
        }

        // Set up ImageButton for Back
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            // Apply fragment transaction animation when popping the back stack
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.fragment_container, new HomeFragment()) // Replace with HomeFragment
                    .addToBackStack(null) // Add to back stack so we can go back to RecommendDishes if needed
                    .commit();
        });

        // Update TextView with dish name or data from the database
        TextView textView = view.findViewById(R.id.textView);
        textView.setText("Selected Dish: " + dishName);

        return view;
    }
}
