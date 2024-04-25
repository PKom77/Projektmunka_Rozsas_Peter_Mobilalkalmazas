package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
    private static final String CNAME = MainActivity.class.getName();
    private static final String PACKAGE = MainActivity.class.getPackage().toString();
    private static final int KEY = 99;

    private SharedPreferences sharedPreferences;

    private FirebaseAuth auth;

    private FirebaseFirestore firestore;
    private CollectionReference items;

    EditText unameET;
    EditText pwdET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        firestore = FirebaseFirestore.getInstance();
        items = firestore.collection("UserData");

        setContentView(R.layout.activity_main);
        unameET = findViewById(R.id.ETUname);
        pwdET = findViewById(R.id.ETPasswd);
        sharedPreferences = getSharedPreferences(PACKAGE, MODE_PRIVATE);
        Log.i(CNAME, "onCreate");

        int key = getIntent().getIntExtra("KEY",99);
        Log.i(CNAME, "onCreate");

        if (key != 99){
            finish();
        }
    }

    public void login(View view) {
        String uname = unameET.getText().toString();
        String pwd = pwdET.getText().toString();

        if (uname.equals("") || pwd.equals("")){
            Toast.makeText(MainActivity.this, "Hibás felhasználó név vagy jelszó!", Toast.LENGTH_LONG).show();
        }else{
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", unameET.getText().toString());
            editor.apply();
            Log.i(CNAME, "onPause");
            userDataGiver(unameET.getText().toString());

            auth.signInWithEmailAndPassword(uname, pwd).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Log.d(CNAME, "User logged in successfully");
                        enter();
                    }else {
                        Log.d(CNAME, "User was't logged in successfully:", task.getException());
                        Toast.makeText(MainActivity.this, "Hibás adatok!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void userDataGiver(String email) {
        items.whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String varosa = document.getString("city");
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("City", varosa);
                        editor.apply();
                    }
                } else {
                    Log.e(CNAME, "Hiba történt az adatbázis lekérdezése során", task.getException());
                }
            }
        });
    }

    private void enter(){
        Intent intent = new Intent(this, IndexActivity.class);
        intent.putExtra("KEY", KEY);
        startActivity(intent);
    }

    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("KEY",KEY);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(CNAME, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(CNAME, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(CNAME, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email",unameET.getText().toString());
        editor.apply();

        Log.i(CNAME, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(CNAME, "onResume");
    }

}