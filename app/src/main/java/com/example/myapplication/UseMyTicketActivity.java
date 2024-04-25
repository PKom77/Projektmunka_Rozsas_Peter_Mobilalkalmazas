package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

public class UseMyTicketActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final int KEY = 99;
    private static final String PACKAGE = MainActivity.class.getPackage().toString();
    private static final String CNAME = MyTicketsActivity.class.getName();
    private FirebaseFirestore firestore;
    private CollectionReference items;
    private CollectionReference itemsUserData;

    private MegvaltottJegyek currentJegy;
    private NotificationHandler notificationHandler;

    TextView TUname;
    TextView TFrom;
    TextView TTo;

    TextView TAt;
    TextView TDb;
    TextView TPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_my_ticket);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        sharedPreferences = getSharedPreferences(PACKAGE, MODE_PRIVATE);
        notificationHandler = new NotificationHandler(this);

        firestore = FirebaseFirestore.getInstance();
        items = firestore.collection("Jegyek");
        itemsUserData = firestore.collection("UserData");

        currentJegy = new MegvaltottJegyek(sharedPreferences.getString("ticketdata_ido", "0"),sharedPreferences.getString("ticketdata_indulo", "Sehol"),sharedPreferences.getString("ticketdata_erkezo", "Sehol"),sharedPreferences.getString("ticketdata_ar", "0"),sharedPreferences.getString("ticketdata_ferohely", "0"),sharedPreferences.getString("email", "email"));

        TUname = findViewById(R.id.tulajdonos);
        TFrom = findViewById(R.id.induloAllomas);
        TTo = findViewById(R.id.erkezoAllomas);
        TDb = findViewById(R.id.dbAllomas);
        TPrice = findViewById(R.id.arAllomas);
        TAt = findViewById(R.id.idoAllomas);

        userDataGiver((sharedPreferences.getString("email", "Senki")));
        TFrom.setText(sharedPreferences.getString("ticketdata_indulo", "Senki"));
        TTo.setText(sharedPreferences.getString("ticketdata_erkezo", "Senki"));
        String d = sharedPreferences.getString("ticketdata_ferohely", "Senki") + " db jegy";
        TDb.setText(d);
        String p = sharedPreferences.getString("ticketdata_ar", "Senki") + " Ft";
        TPrice.setText(p);
        TAt.setText(sharedPreferences.getString("ticketdata_ido", "Senki"));

        int key = getIntent().getIntExtra("KEY", 0);

        if (key != 99) {
            finish();
        }
    }

    private void userDataGiver(String email) {
        itemsUserData.whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String felhasznalonev = document.getString("nev") + " jegye";
                        TUname.setText(felhasznalonev);
                        Log.d(CNAME, "A felhasználónév: " + felhasznalonev);
                    }
                } else {
                    Log.e(CNAME, "Hiba történt az adatbázis lekérdezése során", task.getException());
                }
            }
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent(this, IndexActivity.class);
        intent.putExtra("KEY", KEY);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String sp = sharedPreferences.getString("ticketdata_indulo", "Senki");
        if (sp.equals("Senki")){
            finish();
        }
        Log.i(CNAME, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(UseMyTicketActivity.this, "Jegy beváltási ciklusa befejeződött!", Toast.LENGTH_LONG).show();
        Log.i(CNAME, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(UseMyTicketActivity.this, "Jegy beváltási ciklusa befejeződött!", Toast.LENGTH_LONG).show();
        Log.i(CNAME, "onDestroy");
    }

    @Override
    protected void onPause() {
        Toast.makeText(UseMyTicketActivity.this, "Jegy beváltási ciklusa szüneteltetve!", Toast.LENGTH_LONG).show();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sp = sharedPreferences.getString("ticketdata_indulo", "Senki");
        if (sp.equals("Senki")){
            finish();
        }
        Log.i(CNAME, "onStart");
        Log.i(CNAME, "onResume");
    }

    public void bevalt(View view) {
        items.whereEqualTo("email", currentJegy.getEmail())
                .whereEqualTo("erkezo", currentJegy.getErkezo())
                .whereEqualTo("indulo", currentJegy.getIndulo())
                .whereEqualTo("ido", currentJegy.getIdo())
                .whereEqualTo("ferohely", currentJegy.getFerohely())
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

        notificationHandler.send("Sikeres érvényesítés! Jegyét a buszsofőr elfogadta. Jó utat kívánunk!");
        Toast.makeText(UseMyTicketActivity.this, "Jegy elfogadva, rendszerből törölve, jó utat!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, IndexActivity.class);
        intent.putExtra("KEY", KEY);
        startActivity(intent);
    }

    public void back(View view) {
        Intent intent = new Intent(this, MyTicketsActivity.class);
        intent.putExtra("KEY", KEY);
        startActivity(intent);
    }
}