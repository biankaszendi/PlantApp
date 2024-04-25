package com.example.plantmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Register extends AppCompatActivity implements OnClickListener {

    EditText mEmail, mPassword, mFullname;
    Button mRegisterbutton;
    TextView mLoginbutton;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mFullname = findViewById(R.id.fullname);
        mRegisterbutton = findViewById(R.id.loginbutton);
        mLoginbutton = findViewById(R.id.registertext);

        mRegisterbutton.setOnClickListener(this);

        mLoginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

        fAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser != null) {
            reload2();
        }
    }

    private void reload2() {
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.loginbutton) {
            registerUser();
        }
    }

    private void registerUser() {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            mEmail.setError("E-mail is required!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Password is required!");
            return;
        }

        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = fAuth.getCurrentUser();
                            Toast.makeText(Register.this, "Registered successfully!.",
                                    Toast.LENGTH_SHORT).show();
                            reload();
                        } else {
                            // If sign in fails, display a message to the user.
                            // You can handle specific error cases here if needed.
                            // For simplicity, I'm just showing a toast.
                            Toast.makeText(Register.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void reload() {
        Intent intent = new Intent(Register.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
