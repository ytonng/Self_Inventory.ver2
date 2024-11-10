package com.example.imagerec;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.imagerec.ml.Model;

public class RefrigeratorFragment extends Fragment {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fab;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY_PICK = 2;
    private static final int IMAGE_SIZE = 224;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_refrigerator, container, false);

        bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        fab = view.findViewById(R.id.fab);

        // Set up the BottomNavigationView listener as before
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.btm_nav_up) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.refrigerator_fragment_container, new UpFridge())
                        .commit();
            } else if (item.getItemId() == R.id.btm_nav_down) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.refrigerator_fragment_container, new LowFridge())
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.refrigerator_fragment_container, new UpFridge())
                    .commit();
        }

        // Set up FloatingActionButton click listener to show BottomSheetDialog
        fab.setOnClickListener(v -> showBottomSheetDialog());

        return view;
    }

    private void showBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());

        // Inflate the bottom sheet layout
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottomsheetlayout, null);

        // Apply transparent background to the bottom sheet content
        bottomSheetView.setBackgroundResource(R.drawable.dialogbg); // Background with rounded corners

        // Set transparent background for the dialog window itself
        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        bottomSheetDialog.setContentView(bottomSheetView);

        // Initialize the views within the bottom sheet
        LinearLayout addManually = bottomSheetView.findViewById(R.id.addmanually);
        LinearLayout freshFoodButton = bottomSheetView.findViewById(R.id.addfood);  // Change to LinearLayout for clickable views

        // Handle Add Manually button click
        addManually.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();

            // Navigate to ItemDetailFragment
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ItemDetailFragment())  // Ensure fragment_container is correct in your activity layout
                    .addToBackStack(null)  // Optional: if you want to add the transaction to backstack for navigation
                    .commit();

            Toast.makeText(requireContext(), "Add manually is clicked", Toast.LENGTH_SHORT).show();
        });

        // Handle Fresh Food button click
        freshFoodButton.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            // Show the dialog for choosing the image source (Camera or Gallery)
            showImageSourceDialog();
            Toast.makeText(requireContext(), "Fresh food button clicked", Toast.LENGTH_SHORT).show();
        });

        // Show the BottomSheetDialog
        bottomSheetDialog.show();
    }

    // Show the dialog for selecting image source (Camera or Gallery)
    // Show the dialog for selecting image source (Camera or Gallery)
    private void showImageSourceDialog() {
        // Create an AlertDialog to show options for camera or gallery
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose Image Source");

        // Set up the options
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Album", "Cancel"}, (dialog, which) -> {
            if (which == 0) {
                // Camera option selected
                Toast.makeText(requireContext(), "Camera option selected", Toast.LENGTH_SHORT).show();
                // Implement camera capture functionality (using an Intent)
                openCamera();
            } else if (which == 1) {
                // Gallery option selected
                Toast.makeText(requireContext(), "Gallery option selected", Toast.LENGTH_SHORT).show();
                // Implement gallery selection functionality (using an Intent)
                openGallery();
            } else if (which == 2) {
                // Cancel option selected
                dialog.dismiss();
                Toast.makeText(requireContext(), "Operation cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the dialog
        builder.show();
    }
    private void handleImage(Uri imageUri) {
        try {
            Bitmap image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            image = Bitmap.createScaledBitmap(image, IMAGE_SIZE, IMAGE_SIZE, false);

            // Pass image to ItemDetailFragment for classification
            classifyAndNavigate(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Classify the image using TensorFlow Lite model
    private void classifyAndNavigate(Bitmap image) {
        try {
            Model model = Model.newInstance(requireContext());

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, IMAGE_SIZE, IMAGE_SIZE, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            int pixel = 0;
            for (int i = 0; i < IMAGE_SIZE; i++) {
                for (int j = 0; j < IMAGE_SIZE; j++) {
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] confidences = outputFeature0.getFloatArray();

            // Find class with highest confidence
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {"Bean", "BitterGround", "BottleGourd", "Brinjal", "Broccoli", "Cabbage", "Capsium", "Carrot", "Cauliflower", "Cucumber", "Papaya", "Potato", "Pumpkin", "Radish", "Tomato"};
            String itemName = classes[maxPos];

            // Now navigate to ItemDetailFragment, passing the item name and image
            ItemDetailFragment itemDetailFragment = new ItemDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("item_name", itemName);
            bundle.putParcelable("item_image", image);
            itemDetailFragment.setArguments(bundle);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, itemDetailFragment)
                    .addToBackStack(null)
                    .commit();

            model.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Open the camera for photo capture
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // Open the gallery to pick an image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_GALLERY_PICK) {
                Uri selectedImageUri = data.getData();
                handleImage(selectedImageUri); // Handle the image
            }
        }
    }
}