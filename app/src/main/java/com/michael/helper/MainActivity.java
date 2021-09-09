package com.michael.helper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // XML Elements
    Button acceptRequestBtn, logoutBtn;
    TextView locationView, statusView, userName;

    // Database Reference
    private DatabaseReference databaseReference;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    // Demo help user
    int check = 0;

    // Key of the NotificationsModel object aka phone number
    String senderKey;
    String responderName;
    String responderEmergencyContact;
    String userEmail;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XML Elements
        acceptRequestBtn = findViewById(R.id.notifyBtn);
        logoutBtn = findViewById(R.id.logout);
        userName = findViewById(R.id.userName);
        locationView = findViewById(R.id.location);
        statusView = findViewById(R.id.status);

        // Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Sets it to false until information received
        acceptRequestBtn.setEnabled(false);

        // Button listeners
        acceptRequestBtn.setOnClickListener(v -> {
            databaseReference.child(senderKey).child("status").setValue("SOS REQUEST ACCEPTED");
            databaseReference.child(senderKey).child("responder").setValue(responderName);
            handler.postDelayed(() -> acceptRequestBtn.setText("Confirm"), 12 * 1000);
        });

        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        new FirebaseAPI().getUsers((users, keys) -> {

            // CHECKS IF INFORMATION OF SENDER AND RECEIVER ARE RELATED TO EACH OTHER AND ACTS ACCORDINGLY
            for(int i = 0; i < users.size(); i++) {
                senderKey = keys.get(i);
                if((users.get(i).getStatus().equals("SOS REQUEST") && senderKey.equals(responderEmergencyContact)) || (users.get(i).getStatus().equals("SOS REQUEST ACCEPTED") && senderKey.equals(responderEmergencyContact))) {
                    int iteration = i;

                    firebaseFirestore.collection("users").whereEqualTo("phone", senderKey).get().addOnSuccessListener(queryDocumentSnapshotsSender -> {
                        firebaseFirestore.collection("responders").whereEqualTo("emergencyContactFor", senderKey).get().addOnSuccessListener(queryDocumentSnapshotsReceiver -> {
                            List<DocumentSnapshot> documentSnapshotListSender = queryDocumentSnapshotsSender.getDocuments();

                            for(DocumentSnapshot snapshot: documentSnapshotListSender) {
                                if(snapshot.getData().get("name") != null) {
                                    userName.setText(Objects.requireNonNull(Objects.requireNonNull(snapshot.getData()).get("name")).toString());
                                    locationView.setText(users.get(iteration).getLocation());
                                    statusView.setText(users.get(iteration).getStatus());
                                    acceptRequestBtn.setEnabled(true);
                                    check = 1;
                                } else {
                                    databaseReference.child(senderKey).removeValue();
                                }
                            }

                        }).addOnFailureListener(e -> {
                            Log.d("tag", "Error receiving data from database - responder query");
                        });

                    }).addOnFailureListener(e -> {
                        Log.d("tag", "Error receiving data from database - user/sender query");
                    });
                }

                // RESETS REALTIME DATABASE INFORMATION OF SENDER
                if(!users.get(i).getStatus().equals("SOS REQUEST") && !users.get(i).getStatus().equals("SOS REQUEST ACCEPTED") && responderEmergencyContact != senderKey) {
                    userName.setText("");
                    locationView.setText("");
                    statusView.setText("");
                    acceptRequestBtn.setEnabled(false);

                    if(check == 1) {
                        Toast.makeText(this, "SOS REQUEST CANCELLED", Toast.LENGTH_SHORT).show();
                        check = 0;
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {  // TAKES INFORMATION FROM LOGIN SCREEN TO REMEMBER WHO IS LOGGED IN
                                // AND TO READ IN THE RELEVANT INFORMATION FROM THE DATABASE
        super.onStart();
        Intent data = getIntent();
        userEmail = data.getStringExtra("email");
        firebaseFirestore.collection("responders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot snapshot : snapshotList) {
                        if(userEmail.equals(snapshot.getData().get("email").toString())) {
                            userName.setText(snapshot.getData().get("name").toString());
                            responderName = snapshot.getData().get("name").toString();
                            responderEmergencyContact = snapshot.getData().get("emergencyContactFor").toString();
                        } else {

                        }
                    }
                }).addOnFailureListener(e -> {
            Log.d("tag", "Error on querying database - responder");
        });
    }
}