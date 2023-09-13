package com.example.eyalandmorad;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;



public class LoginPage extends AppCompatActivity {

    // ...

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private Button SigninButton;
    private TextView errorTextView;

    private FirebaseAuth firebaseAuth;
    private EventDataBase eventDataBase;
    private FireBase fb;

    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpage);
        eventDataBase = EventDataBase.getInstance(this);
        fb = new FireBase();
        fb.syncFireBaseWithLocalDB(eventDataBase);
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        SigninButton = findViewById(R.id.SignupButton);
        errorTextView = findViewById(R.id.errorTextView);

        // Set click listener for the sign-up button
        SigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the sign-in activity
                Intent intent = new Intent(LoginPage.this, SignInActivity.class);
                startActivity(intent);
            }
        });

        // Set click listener for the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredEmail = usernameEditText.getText().toString();
                String enteredPassword = passwordEditText.getText().toString();

                // Check if the entered username and password are exists and correct
                if (enteredEmail.isEmpty() || enteredPassword.isEmpty()) {
                    Toast.makeText(LoginPage.this, "Please fill all fields!", Toast.LENGTH_LONG).show();
                    return;
                }

                firebaseAuth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                        .addOnCompleteListener(LoginPage.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(LoginPage.this, EventManagementActivity.class);
                                    intent.putExtra("username", enteredEmail);
                                    startActivity(intent);
                                } else {
                                    // Failed to sign in, handle the error and tell the user what the error is.
                                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                    Toast.makeText(LoginPage.this, "Error!", Toast.LENGTH_LONG).show();
                                    switch (errorCode) {
                                        case "login_invalid_email":
                                            errorTextView.setText(getResources().getString(R.string.signup_error_invalid_email));
                                            break;
                                        case "ERROR_WRONG_PASSWORD":
                                        case "ERROR_USER_NOT_FOUND":
                                            errorTextView.setText(getResources().getString(R.string.login_error_msg));
                                            break;
                                        default:
                                            errorTextView.setText(getResources().getString(R.string.signup_error_default));
                                            break;
                                    }
                                    errorTextView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
            }
        });

        // Initialize AuthStateListener
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    // User is signed in, navigate to EventManagementActivity
                    Intent intent = new Intent(LoginPage.this, EventManagementActivity.class);
                    intent.putExtra("username", currentUser.getEmail());
                    startActivity(intent);
                    finish(); // Finish the LoginActivity to prevent the user from going back to it
                } else {
                    // User is signed out, do nothing or show the login UI
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start listening for Auth state changes
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop listening for Auth state changes when the activity is stopped
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    // ...
}

