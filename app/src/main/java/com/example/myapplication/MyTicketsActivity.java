package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class MyTicketsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final int KEY = 99;
    private static final String PACKAGE = MainActivity.class.getPackage().toString();
    private static final String CNAME = MyTicketsActivity.class.getName();
    private FirebaseFirestore firestore;
    private CollectionReference items;
    private CollectionReference itemsMenetend;
    private String emailOfUser;

    TextView titleOfCityTV;

    private ArrayList<MegvaltottJegyek> dataJegyeim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);
        sharedPreferences = getSharedPreferences(PACKAGE, MODE_PRIVATE);

        titleOfCityTV = findViewById(R.id.titleOfCity);

        int key = getIntent().getIntExtra("KEY", 0);
        Log.i(CNAME, "onCreate");

        if (key != 99) {
            finish();
        }

        firestore = FirebaseFirestore.getInstance();
        items = firestore.collection("Jegyek");
        itemsMenetend = firestore.collection("Menetrend");
        dataJegyeim = new ArrayList<>();
        emailOfUser = sharedPreferences.getString("email", "email");
        searchAndPopulateTable();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void searchAndPopulateTable() {
        dataJegyeim.clear();
        items.whereEqualTo("email", emailOfUser)
                .orderBy("indulo", Query.Direction.ASCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> elemek = new ArrayList<>();
                            elemek.add("");
                            elemek.add("Indul");
                            elemek.add("Érkezik");
                            elemek.add("db");

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MegvaltottJegyek current = new MegvaltottJegyek(document.getData().get("ido").toString(), document.getData().get("indulo").toString(), document.getData().get("erkezo").toString(), document.getData().get("ar").toString(),document.getData().get("ferohely").toString(),emailOfUser);
                                dataJegyeim.add(current);
                                elemek.add(current.getIdo());
                                elemek.add(current.getIndulo());
                                elemek.add(current.getErkezo());
                                elemek.add(current.getFerohely());
                            }

                            populateTable(elemek);
                        } else {
                            Log.d(CNAME, "Sikertelen lekérdezés: ", task.getException());
                        }
                    }
                });
    }

    private void populateTable(List<String> elemek) {
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        int h = 0;
        for (int i = 0; i < dataJegyeim.size()+1; i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            for (int j = 0; j < 4; j++) {
                TextView textView = new TextView(this);
                textView.setText(elemek.get(h));
                textView.setPadding(4, 4, 4, 4);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                textView.setGravity(Gravity.CENTER);
                tableRow.addView(textView);
                h++;
            }

            if (i != 0) {

                final int index = i;

                Button button = new Button(this);
                button.setText("Bevált");
                button.setPadding(4, 4, 4, 4);
                button.setGravity(Gravity.CENTER);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        useTicket(dataJegyeim.get(index-1));
                        // Gombra kattintáskor végrehajtandó műveletek
                        Toast.makeText(MyTicketsActivity.this, "Beváltás elindítva", Toast.LENGTH_SHORT).show();
                    }
                });
                tableRow.addView(button);

                Button button2 = new Button(this);
                button2.setText("Töröl");
                button2.setPadding(4, 4, 4, 4);
                button2.setGravity(Gravity.CENTER);
                button2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteTicket(dataJegyeim.get(index-1));
                        // Gombra kattintáskor végrehajtandó műveletek
                        Toast.makeText(MyTicketsActivity.this, "Törlés sikeres", Toast.LENGTH_SHORT).show();
                    }
                });
                tableRow.addView(button2);
            }
            tableLayout.addView(tableRow);
        }
    }

    private void useTicket(MegvaltottJegyek jegy){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ticketdata_ar", jegy.getAr());
        editor.putString("ticketdata_erkezo", jegy.getErkezo());
        editor.putString("ticketdata_indulo", jegy.getIndulo());
        editor.putString("ticketdata_ferohely", jegy.getFerohely());
        editor.putString("ticketdata_ido", jegy.getIdo());
        editor.apply();
        Intent intent = new Intent(this, UseMyTicketActivity.class);
        intent.putExtra("KEY", KEY);
        startActivity(intent);
    }

    private void deleteTicket(MegvaltottJegyek jegy){
        items.whereEqualTo("email", emailOfUser)
                .whereEqualTo("erkezo", jegy.getErkezo())
                .whereEqualTo("indulo", jegy.getIndulo())
                .whereEqualTo("ido", jegy.getIdo())
                .whereEqualTo("ferohely", jegy.getFerohely())
                .orderBy("indulo", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            WriteBatch batch = firestore.batch();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference docRef = document.getReference();
                                batch.delete(docRef);
                            }

                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(CNAME, "Dokumentumok sikeresen törölve");
                                    } else {
                                        Log.w(CNAME, "Hiba történt a dokumentumok törlése közben", task.getException());
                                    }
                                }
                            });
                        } else {
                            Log.d(CNAME, "Sikertelen lekérdezés: ", task.getException());
                        }
                    }
                });

        itemsMenetend.whereEqualTo("indulo", jegy.getIndulo())
                .whereEqualTo("erkezo", jegy.getErkezo())
                .whereEqualTo("ar", String.valueOf(Integer.parseInt(jegy.getAr())/Integer.parseInt(jegy.getFerohely())))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Dokumentum frissítése
                                String ujferohely = String.valueOf(Integer.parseInt(String.valueOf(document.getData().get("ferohely"))) + Integer.parseInt(jegy.getFerohely()));

                                itemsMenetend.document(document.getId()).update("ferohely", ujferohely);
                            }
                        } else {
                            Log.d(CNAME, "Hiba történt a lekérdezés során", task.getException());
                        }
                    }
                });

        Intent intent = new Intent(this, IndexActivity.class);
        intent.putExtra("KEY", KEY);
        startActivity(intent);
    }

    public void backpage(View view) {
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
        searchAndPopulateTable();
        Log.i(CNAME, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(CNAME, "onDestroy");
    }

    @Override
    protected void onPause() {
        searchAndPopulateTable();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Animation slideInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation_one);
        titleOfCityTV.startAnimation(slideInAnimation);
        Log.i(CNAME, "onResume");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, IndexActivity.class);
        intent.putExtra("KEY", KEY);
        startActivity(intent);
    }
}