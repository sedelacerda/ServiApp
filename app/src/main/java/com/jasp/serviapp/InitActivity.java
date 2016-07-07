package com.jasp.serviapp;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class InitActivity extends AppCompatActivity {

    public static DatabaseReference myFirebaseRef;
    public static FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_init);

        myFirebaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();


        /** Iniciamos el autenticador de Firebase */
        mAuth = FirebaseAuth.getInstance();

        /* Si el usuario ya se ha registrado en el telefono, entonces se inicia sesion automaticamente */
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    String loginEmail = user.getEmail();
                    String phoneNumber = loginEmail.substring(0, loginEmail.indexOf('@'));
                    //Toast.makeText(InitActivity.this, phoneNumber, Toast.LENGTH_LONG).show();

                    ValueEventListener userInfoListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            LoginActivity.user = dataSnapshot.getValue(User.class);
                            startActivity(new Intent(InitActivity.this, NavigationActivity.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
                    myFirebaseRef.child("users").child(phoneNumber).addValueEventListener(userInfoListener);

                } else {
                    startActivity(new Intent(InitActivity.this, LoginActivity.class));
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }
}
