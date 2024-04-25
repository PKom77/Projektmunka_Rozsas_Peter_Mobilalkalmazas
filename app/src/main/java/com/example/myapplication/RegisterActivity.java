package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class RegisterActivity extends AppCompatActivity {

    private static final String CNAME = RegisterActivity.class.getName();
    private static final String PACKAGE = MainActivity.class.getPackage().toString();
    private static final int KEY = 99;

    private FirebaseFirestore firestore;
    private CollectionReference items;

    private SharedPreferences sharedPreferences;
    private FirebaseAuth auth;

    EditText ETUname;
    EditText ETEmail;
    Spinner SCity;
    EditText ETPwd;
    EditText ETPwdAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ETUname = findViewById(R.id.ETUname);
        ETEmail = findViewById(R.id.ETEmail);
        SCity = findViewById(R.id.varosom);
        ETPwd = findViewById(R.id.ETPasswd);
        ETPwdAgain = findViewById(R.id.ETPasswdAgain);

        firestore = FirebaseFirestore.getInstance();
        items = firestore.collection("UserData");

        String[] items = getResources().getStringArray(R.array.hungarian_cities);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SCity.setAdapter(adapter);

        sharedPreferences = getSharedPreferences(PACKAGE, MODE_PRIVATE);

        String selectedCity = sharedPreferences.getString("City", "");
        if (!selectedCity.isEmpty()) {
            int position = adapter.getPosition(selectedCity);
            SCity.setSelection(position);
        }

        auth = FirebaseAuth.getInstance();

        int key = getIntent().getIntExtra("KEY", 0);
        Log.i(CNAME, "onCreate");

        if (key != 99) {
            finish();
        }
    }

    public void register(View view) {
        String uname = ETUname.getText().toString();
        String email = ETEmail.getText().toString();
        String city = SCity.getSelectedItem().toString();
        String pwd = ETPwd.getText().toString();
        String pwdagain = ETPwdAgain.getText().toString();

        if (!pwd.equals(pwdagain) || uname.equals("") || email.equals("") || pwd.equals("")){
            Log.e(CNAME, "Nincs minden adat megfelelően kitöltve!");
            Toast.makeText(RegisterActivity.this, "Nincs minden adat megfelelően kitöltve!", Toast.LENGTH_LONG).show();
        } else {
            // Ellenőrizzük, hogy van-e már ilyen email az adatbázisban
            items.whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Az email cím már létezik az adatbázisban, ezért ne lehessen regisztrálni
                            Log.e(CNAME, "Az email cím már foglalt!");
                            Toast.makeText(RegisterActivity.this, "Az email cím már foglalt!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Az email cím még nem létezik az adatbázisban, tehát regisztrálhat
                            auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        Log.d(CNAME, "User created successfully");
                                        enter();
                                    } else {
                                        Log.d(CNAME, "User was't created successfully:", task.getException());
                                        Toast.makeText(RegisterActivity.this, "A jelszónak legalább 6 karakter hosszúnak kell lennie!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            UserDataStore ud = new UserDataStore(uname, email, city);
                            items.add(ud)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d(CNAME, "Sikeresen hozzáadva: " + documentReference.getId());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(CNAME, "Hiba történt a hozzáadás során", e);
                                        }
                                    });
                        }
                    } else {
                        Log.e(CNAME, "Hiba történt az adatbázis lekérdezése során", task.getException());
                        Toast.makeText(RegisterActivity.this, "Hiba történt az adatbázis lekérdezése során", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void back(View view) {
        finish();
    }

    private void enter(){
        Intent intent = new Intent(this, IndexActivity.class);
        intent.putExtra("KEY", KEY);
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
        editor.putString("City", SCity.getSelectedItem().toString());
        editor.apply();
        editor = sharedPreferences.edit();
        editor.putString("email", ETEmail.getText().toString());
        editor.apply();
        Log.i(CNAME, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(CNAME, "onResume");
    }
}