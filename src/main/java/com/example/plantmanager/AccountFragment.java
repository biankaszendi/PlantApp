package com.example.plantmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountFragment extends Fragment {

    public AccountFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        TextView welcomeTextView = view.findViewById(R.id.welcomeTextView);

        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();

            if (displayName != null && !displayName.isEmpty()) {
                welcomeTextView.setText("Hello, " + displayName + "!");
            } else {
                welcomeTextView.setText("Hello!");
            }

            loadProfilePicture(currentUser.getPhotoUrl(), view);
        } else {
            welcomeTextView.setText("Hello!");
        }

        Button logoutButton = view.findViewById(R.id.logoutbutton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        Button editProfileButton = view.findViewById(R.id.editprofilebutton);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), EditProfileActivity.class));
            }
        });

        return view;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getActivity(), Login.class));
        getActivity().finish();
    }

    private void loadProfilePicture(Uri photoUrl, View view) {
        ImageView profileImageView = view.findViewById(R.id.profileImageView);
        if (photoUrl != null) {
            Glide.with(this).load(photoUrl).into(profileImageView);
        }
}
}
