package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final int KEY = 99;
    private static final String PACKAGE = MainActivity.class.getPackage().toString();
    private static final String CNAME = SearchResultsActivity.class.getName();

    private FirebaseFirestore firestore;
    private CollectionReference items;
    private CollectionReference itemsShop;
    private ArrayList<Jarat> dataJarat;
    private String honnanText;
    private String hovaText;
    private int jegyekSzama;
    private String emailOfUser;
    private NotificationHandler notificationHandler;

    TextView titleOfCityTV;

    private static final String CHANNEL_ID = "com.example.myapplication.channel.notification";
    private static final String CHANNEL_NAME = "Notification Channel";
    private static final String CHANNEL_DESCRIPTION = "Notification channel here";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        firestore = FirebaseFirestore.getInstance();
        items = firestore.collection("Menetrend");
        itemsShop = firestore.collection("Jegyek");
        dataJarat = new ArrayList<>();

        notificationHandler = new NotificationHandler(this);

        sharedPreferences = getSharedPreferences(PACKAGE, MODE_PRIVATE);
        honnanText = sharedPreferences.getString("from", "Budapest");
        hovaText = sharedPreferences.getString("to", "Szeged");
        String stringValue = sharedPreferences.getString("tickets", "1");
        jegyekSzama = Integer.parseInt(stringValue);
        emailOfUser = sharedPreferences.getString("email", "Szeged");

        titleOfCityTV = findViewById(R.id.titleOfCity);

        int key = getIntent().getIntExtra("KEY", 0);
        Log.i(CNAME, "onCreate");

        if (key != 99) {
            finish();
        }

        searchAndPopulateTable();
    }

    private void searchAndPopulateTable() {
        dataJarat.clear();
        Log.d(CNAME, "A " + honnanText + " B " + hovaText);
        items.whereEqualTo("indulo", honnanText)
                .whereEqualTo("erkezo", hovaText)
                .orderBy("ar", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> elemek = new ArrayList<>();
                            elemek.add("Időpont");
                            elemek.add("Ár");
                            elemek.add("Indul");
                            elemek.add("Érkezik");
                            elemek.add("Férőhely");

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Jarat current = new Jarat(document.getData().get("ido").toString(), document.getData().get("indulo").toString(), document.getData().get("erkezo").toString(), document.getData().get("ar").toString(),document.getData().get("ferohely").toString());
                                int ar = Integer.parseInt(current.getAr())*jegyekSzama;
                                current.setAr(String.valueOf(ar));
                                dataJarat.add(current);
                                elemek.add(current.getIdo());
                                String ara = String.valueOf(ar) + " Ft";
                                elemek.add(ara);
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
        for (int i = 0; i < dataJarat.size()+1; i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            for (int j = 0; j < 5; j++) {
                TextView textView = new TextView(this);
                textView.setText(elemek.get(h));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                textView.setPadding(8, 8, 8, 8);
                textView.setGravity(Gravity.CENTER);
                tableRow.addView(textView);
                h++;
            }

            if (i != 0) {

                final int index = i;

                Button button = new Button(this);
                button.setText("Kell!");
                button.setPadding(8, 8, 8, 8);
                button.setGravity(Gravity.CENTER);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buy(dataJarat.get(index-1));
                        // Gombra kattintáskor végrehajtandó műveletek
                        Toast.makeText(SearchResultsActivity.this, "Jegyvásárlás megindítva", Toast.LENGTH_SHORT).show();
                    }
                });
                tableRow.addView(button);
            }
            tableLayout.addView(tableRow);
        }
    }

    public void backpage(View view) {
        Intent intent = new Intent(this, IndexActivity.class);
        intent.putExtra("KEY", KEY);
        startActivity(intent);
    }

    public void buy(Jarat jarat){

        MegvaltottJegyek jegy = new MegvaltottJegyek(jarat.getIdo(), jarat.getIndulo(), jarat.getErkezo(), jarat.getAr(),String.valueOf(jegyekSzama),emailOfUser);
        if(jegyekSzama <= Integer.parseInt(jarat.getFerohely())) {
            itemsShop.add(jegy)
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

            String ar = String.valueOf(Integer.parseInt(jegy.getAr()) / jegyekSzama);
            String ujferohely = String.valueOf(Integer.parseInt(jarat.getFerohely()) - jegyekSzama);
            items.whereEqualTo("indulo", honnanText)
                    .whereEqualTo("erkezo", hovaText)
                    .whereEqualTo("ar", ar)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Dokumentum frissítése
                                    items.document(document.getId()).update("ferohely", ujferohely);
                                }
                            } else {
                                Log.d(CNAME, "Hiba történt a lekérdezés során", task.getException());
                            }
                        }
                    });
            notificationHandler.send("Sikeres jegyvásárlás! A jegyét a jegyeim fül alatt tudja érvényesíteni.");
            Intent intent = new Intent(this, MyTicketsActivity.class);
            intent.putExtra("KEY", KEY);
            startActivity(intent);
        }else{
            Toast.makeText(SearchResultsActivity.this, "Nincs elég hely a buszon!", Toast.LENGTH_SHORT).show();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        Animation slideInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation_one);
        titleOfCityTV.startAnimation(slideInAnimation);
        Log.i(CNAME, "onResume");
    }

}
