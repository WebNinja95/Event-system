package com.example.eyalandmorad;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button signInButton, loginButton;
    private TextView errorTextView;
    private FirebaseAuth firebaseAuth;
    private EventDataBase eventDataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        eventDataBase = EventDataBase.getInstance(this);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.SignupButton);
        errorTextView = findViewById(R.id.errorTextView);
        loginButton = findViewById(R.id.loginButton);

        //handle back to log in page button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, LoginPage.class);
                startActivity(intent);
            }
        });

        //handle sign up button
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty()){
                    Toast.makeText(SignInActivity.this, "Please fill all fields!",Toast.LENGTH_LONG).show();
                    return;
                }

                firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    addUserToFireBase(email);
                                    Toast.makeText(SignInActivity.this, "User added successfully\nLog in please.",Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(SignInActivity.this, LoginPage.class);
                                    startActivity(intent);
                                } else {
                                    // Failed to add user to Firebase, handle the error and tell the user what the error.
                                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                    Toast.makeText(SignInActivity.this, "Error!",Toast.LENGTH_LONG).show();
                                    switch (errorCode) {
                                        case "ERROR_INVALID_EMAIL":
                                            errorTextView.setText(getResources().getString(R.string.signup_error_invalid_email));
                                            break;
                                        case "ERROR_WEAK_PASSWORD":
                                            errorTextView.setText(getResources().getString(R.string.signup_error_invalid_password));
                                            break;
                                        case "ERROR_EMAIL_ALREADY_IN_USE":
                                            errorTextView.setText(getResources().getString(R.string.signup_error_email_exists));
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
    }
    public void addUserToFireBase(String userEmail){
        Map<String, Object> users = new HashMap< String, Object >();
        users.put("Email", userEmail);
        users.put("Score", 0);
        users.put("id", "");

        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
        firebaseDB.collection("User")
                .add(users)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        eventDataBase.openDB();
                        eventDataBase.addUser(userEmail, 0, documentReference.getId());
                        eventDataBase.closeDB();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }
}