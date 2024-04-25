package com.example.plantmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.plantmanager.PeriodicCheckWorker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlantsFragment extends Fragment {

    private SearchView searchViewPlants;
    private ListView listViewSearchResults;
    private ListView listViewUserPlants;
    private ArrayAdapter<String> searchResultsAdapter;
    private ArrayAdapter<String> userPlantsAdapter;
    private ArrayList<String> plantList;
    private ArrayList<String> userPlantList;
    private Map<String, Boolean> userPlantWateredMap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userPlantList = new ArrayList<>();
        userPlantWateredMap = new HashMap<>();

        plantList = readPlantListFromFile();

        retrieveUserPlantList();

        schedulePeriodicCheck();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plants, container, false);

        searchViewPlants = view.findViewById(R.id.searchViewPlants);
        listViewSearchResults = view.findViewById(R.id.listViewSearchResults);
        listViewUserPlants = view.findViewById(R.id.listViewUserPlants);


        searchResultsAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
        userPlantsAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_multiple_choice, userPlantList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View listItemView = convertView;
                if (listItemView == null) {
                    listItemView = LayoutInflater.from(getContext()).inflate(R.layout.custom_list_item, parent, false);
                }

                String currentPlant = getItem(position);

                Button deleteButton = listItemView.findViewById(R.id.deleteButton);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteUserPlant(currentPlant);
                    }
                });


                CheckBox checkBox = listItemView.findViewById(R.id.checkbox);
                checkBox.setText(currentPlant);

                checkBox.setChecked(userPlantWateredMap.getOrDefault(currentPlant, false));

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // Update watering status map
                        userPlantWateredMap.put(currentPlant, isChecked);
                        // Save the updated watering status map to Firebase
                        saveUserPlantWateredStatus();
                    }
                });

                listItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteUserPlant(currentPlant);
                    }
                });

                return listItemView;
            }
        };

        listViewSearchResults.setAdapter(searchResultsAdapter);
        listViewUserPlants.setAdapter(userPlantsAdapter);

        searchViewPlants.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search action here (if needed)
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the original plant list as the user types
                ArrayList<String> filteredPlants = new ArrayList<>();
                for (String plant : plantList) {
                    if (plant.toLowerCase().contains(newText.toLowerCase())) {
                        filteredPlants.add(plant);
                    }
                }
                searchResultsAdapter.clear();
                searchResultsAdapter.addAll(filteredPlants);
                searchResultsAdapter.notifyDataSetChanged();
                return true;
            }
        });

        listViewSearchResults.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedPlant = searchResultsAdapter.getItem(position);
            addUserPlant(selectedPlant);
        });

        searchViewPlants.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchResultsAdapter.clear();
                return false;
            }
        });

        return view;
    }

    private ArrayList<String> readPlantListFromFile() {
        ArrayList<String> plantList = new ArrayList<>();
        try {
            InputStream inputStream = requireActivity().getAssets().open("plant_list.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                plantList.add(line.trim());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return plantList;
    }

    private void addUserPlant(String plant) {
        userPlantList.add(plant);
        userPlantsAdapter.notifyDataSetChanged();
        userPlantWateredMap.put(plant, false);
        saveUserPlantList();
        saveUserPlantWateredStatus();
        Toast.makeText(requireContext(), "Added " + plant + " to your list", Toast.LENGTH_SHORT).show();
    }

    private void deleteUserPlant(String plant) {
        userPlantList.remove(plant);
        userPlantsAdapter.notifyDataSetChanged();
        userPlantWateredMap.remove(plant);
        saveUserPlantList();
        saveUserPlantWateredStatus();
        Toast.makeText(requireContext(), "Deleted " + plant + " from your list", Toast.LENGTH_SHORT).show();
    }

    private void saveUserPlantList() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userPlantsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("plants");
            userPlantsRef.setValue(userPlantList);
        }
    }

    private void saveUserPlantWateredStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userPlantsWateredRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("plantsWatered");
            userPlantsWateredRef.setValue(userPlantWateredMap);
        }
    }

    private void retrieveUserPlantList() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userPlantsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("plants");
            userPlantsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userPlantList.clear();
                    for (DataSnapshot plantSnapshot : dataSnapshot.getChildren()) {
                        String plant = plantSnapshot.getValue(String.class);
                        if (plant != null) {
                            userPlantList.add(plant);
                        }
                    }
                    userPlantsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });

            DatabaseReference userPlantsWateredRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("plantsWatered");
            userPlantsWateredRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userPlantWateredMap.clear();
                    for (DataSnapshot plantSnapshot : dataSnapshot.getChildren()) {
                        String plantName = plantSnapshot.getKey();
                        boolean watered = plantSnapshot.getValue(Boolean.class);
                        if (plantName != null) {
                            userPlantWateredMap.put(plantName, watered);
                        }
                    }
                    userPlantsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }

    private void schedulePeriodicCheck() {
        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(PeriodicCheckWorker.class, 7, TimeUnit.DAYS)
                        .build();

        WorkManager.getInstance(requireContext()).enqueue(periodicWorkRequest);
    }
}
