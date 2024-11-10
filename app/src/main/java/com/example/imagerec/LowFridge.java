package com.example.imagerec;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LowFridge extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_low_fridge, container, false);

        // Set OnClickListener for each ImageButton
        view.findViewById(R.id.item1_button).setOnClickListener(v -> openDetailFragment("Item 1"));
        view.findViewById(R.id.item2_button).setOnClickListener(v -> openDetailFragment("Item 2"));
        view.findViewById(R.id.item3_button).setOnClickListener(v -> openDetailFragment("Item 3"));
        view.findViewById(R.id.item4_button).setOnClickListener(v -> openDetailFragment("Item 4"));

        return view;
    }

    private void openDetailFragment(String itemName) {
        // Create a new instance of LowFridgeDetail
        LowFridgeDetail detailFragment = new LowFridgeDetail();

        // Pass the item name to LowFridgeDetail
        Bundle bundle = new Bundle();
        bundle.putString("item_name", itemName); // Pass the item name or description
        detailFragment.setArguments(bundle);

        // Replace the current fragment with LowFridgeDetail and add LowFridge to the back stack
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment) // Replace with your container ID
                .addToBackStack(null) // Add to back stack so you can navigate back
                .commit();
    }
}
