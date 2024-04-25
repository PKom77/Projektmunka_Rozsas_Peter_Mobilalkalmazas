package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final String PACKAGE = MainActivity.class.getPackage().toString();
    private static final String CNAME = IndexActivity.class.getName();

    private FirebaseFirestore firestore;
    private CollectionReference items;

    private static final int KEY = 99;
    private String emailOfUser;

    private static final int RECORD_AUDIO_PERMISSION_CODE = 101;
    private SpeechRecognizer speechRecognizer;

    Spinner ownCity;
    EditText UnameET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firestore = FirebaseFirestore.getInstance();
        items = firestore.collection("UserData");
        sharedPreferences = getSharedPreferences(PACKAGE, MODE_PRIVATE);
        emailOfUser = sharedPreferences.getString("email", "email");

        ownCity = findViewById(R.id.varosom);
        UnameET = findViewById(R.id.UnameUser);

        String[] values = getResources().getStringArray(R.array.hungarian_cities);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ownCity.setAdapter(adapter);

        String city = sharedPreferences.getString("City", "Budapest");

        ownCity.setSelection(adapter.getPosition(city));
        userDataGiver(sharedPreferences.getString("email", "Senki"));


        requestRecordAudioPermission();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_CODE);
        } else {
            initSpeechRecognizer();
        }

    }

    public void back(View view) {
        Intent intent = new Intent(this, IndexActivity.class);
        intent.putExtra("KEY", KEY);
        startActivity(intent);
    }

    public void delete(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(CNAME, "A felhasználó fiókja sikeresen törölve.");
                        } else {
                            Log.e(CNAME, "Hiba történt a fiók törlésekor.", task.getException());
                        }
                    }
                });
        items.whereEqualTo("email", emailOfUser)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String documentId = document.getId();
                                items.document(documentId).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(CNAME, "Felhasználó sikeresen törölve: " + documentId);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(CNAME, "Felhasználó törlése sikertelen: " + documentId, e);
                                            }
                                        });
                            }
                        } else {
                            Log.d(CNAME, "Sikertelen lekérdezés: ", task.getException());
                        }
                    }
                });
        Toast.makeText(ProfileActivity.this, "Profil törölve", Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("KEY", KEY);
        startActivity(intent);
    }

    private void userDataGiver(String email) {
        items.whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String felhasznalonev = document.getString("nev");
                        UnameET.setText(felhasznalonev);
                        Log.d(CNAME, "A felhasználónév: " + felhasznalonev);
                    }
                } else {
                    Log.e(CNAME, "Hiba történt az adatbázis lekérdezése során", task.getException());
                }
            }
        });
    }

    public void modify(View view) {
        if (UnameET.getText().toString().equals("")){
            Toast.makeText(ProfileActivity.this, "Nincs megadva felhasználónév!", Toast.LENGTH_SHORT).show();
        }else {
            items.whereEqualTo("email", emailOfUser)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String documentId = document.getId();
                                    // A dokumentum módosítása
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("nev", UnameET.getText().toString());
                                    updates.put("city", ownCity.getSelectedItem().toString());
                                    items.document(documentId).update(updates)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(CNAME, "Felhasználó nevének módosítása sikeres: " + documentId);
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor = sharedPreferences.edit();
                                                    editor.putString("City", ownCity.getSelectedItem().toString());
                                                    editor.apply();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(CNAME, "Felhasználó nevének módosítása sikertelen: " + documentId, e);
                                                }
                                            });
                                }
                            } else {
                                Log.d(CNAME, "Sikertelen lekérdezés: ", task.getException());
                            }
                        }
                    });
            Toast.makeText(ProfileActivity.this, "Sikeres módosítás!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {}

            @Override
            public void onResults(Bundle results) {
                // Hangfelismerés eredményének megkapása
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && matches.size() > 0) {
                    String text = matches.get(0); // Az első találatot használjuk
                    UnameET.setText(text);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }



    private void startSpeechRecognition() {
        if (speechRecognizer != null) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Beszéljen most...");
            speechRecognizer.startListening(intent);
        } else {
            Toast.makeText(this, "A hangfelismerő még nincs inicializálva", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestRecordAudioPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Engedélykérés");
            builder.setMessage("Az alkalmazásnak szüksége van mikrofonhoz való hozzáférésre a hangfelismeréshez.");
            builder.setPositiveButton("Engedély megadása", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Az engedély megadása újra kérés
                    ActivityCompat.requestPermissions(ProfileActivity.this,
                            new String[]{android.Manifest.permission.RECORD_AUDIO},
                            RECORD_AUDIO_PERMISSION_CODE);
                }
            });
            builder.setNegativeButton("Elutasítás", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(ProfileActivity.this, "Kérjük, engedélyezze a mikrofonhoz való hozzáférést a beállításokban", Toast.LENGTH_LONG).show();
                }
            });
            builder.show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Engedély megadva, inicializálja a SpeechRecognizer-t
                initSpeechRecognizer();
            } else {
                // Engedély megtagadva, jelezze a felhasználónak
                Toast.makeText(this, "Mikrofonhoz való hozzáférés megtagadva", Toast.LENGTH_SHORT).show();
            }
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
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        Log.i(CNAME, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(CNAME, "onResume");
    }

    public void hangfelismer(View view) {
        startSpeechRecognition();
    }
}