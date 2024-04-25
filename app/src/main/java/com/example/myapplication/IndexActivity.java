package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class IndexActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final String PACKAGE = MainActivity.class.getPackage().toString();
    private static final String CNAME = IndexActivity.class.getName();

    private static final int KEY = 99;

    private FirebaseUser user;

    private Spinner spinnerHonnan;
    private Spinner spinnerHova;
    private EditText jegyekEt;

    private FirebaseFirestore firestore;
    private CollectionReference items;
    private ArrayList<Jarat> dataJarat;

    TextView greetTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        greetTv = findViewById(R.id.textView4);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            finish();
        }

        spinnerHonnan = findViewById(R.id.honnan);
        spinnerHova = findViewById(R.id.hova);
        jegyekEt = findViewById(R.id.editTextNumber);

        firestore = FirebaseFirestore.getInstance();
        items = firestore.collection("Menetrend");
        dataJarat = new ArrayList<>();
        download();

        String[] items = getResources().getStringArray(R.array.hungarian_cities);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHonnan.setAdapter(adapter);
        spinnerHova.setAdapter(adapter);

        sharedPreferences = getSharedPreferences(PACKAGE, MODE_PRIVATE);
        String city = sharedPreferences.getString("City", "Budapest");

        spinnerHonnan.setSelection(adapter.getPosition(city));

        int key = getIntent().getIntExtra("KEY", 0);
        Log.i(CNAME, "onCreate");

        if (key != 99) {
            finish();
        }
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
        editor.putString("from", spinnerHonnan.getSelectedItem().toString());
        editor.apply();
        editor = sharedPreferences.edit();
        editor.putString("to", spinnerHova.getSelectedItem().toString());
        editor.apply();
        editor = sharedPreferences.edit();
        editor.putString("tickets", jegyekEt.getText().toString());
        editor.apply();
        Log.i(CNAME, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Animation slideInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation_two);
        greetTv.startAnimation(slideInAnimation);
        Log.i(CNAME, "onResume");
    }

    public void search(View view) {
        if (jegyekEt.getText().toString().equals("")){
            Toast.makeText(IndexActivity.this, "Nincs megadva a jegyek száma!", Toast.LENGTH_SHORT).show();
        }else {
            if (!spinnerHonnan.getSelectedItem().toString().equals(spinnerHova.getSelectedItem().toString())) {
                Intent intent = new Intent(this, SearchResultsActivity.class);
                intent.putExtra("KEY", KEY);
                startActivity(intent);
            } else {
                Toast.makeText(IndexActivity.this, "A kiindulási és az érkezési pont egyeznek!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void logout(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("KEY", KEY);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        startActivity(intent);
    }

    public void profile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("KEY", KEY);
        startActivity(intent);
    }

    public void tickets(View view) {
        Intent intent = new Intent(this, MyTicketsActivity.class);
        intent.putExtra("KEY", KEY);
        startActivity(intent);
    }

    private void download(){
        items.orderBy("ar").limit(4).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                Jarat current = document.toObject(Jarat.class);
                dataJarat.add(current);
            }

            if (dataJarat.size() == 0) {
                pushData();
                download();
            }
        });
    }

    private void pushData(){
        Jarat j = new Jarat();
        dataJarat = j.general();

        for (int i = 0; i < dataJarat.size(); i++) {
            items.add(dataJarat.get(i))
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
    }
}