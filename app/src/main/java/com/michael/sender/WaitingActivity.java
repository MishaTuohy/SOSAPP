package com.michael.sender;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class WaitingActivity extends AppCompatActivity {

    // XML
    TextView responderTxt, recordingView;
    Button cancelBtn;
    ImageView recordIcon, recordIconBg;

    // Hard coded number
    String phoneSend = "0123456789";

    // For timed events
    Handler handler = new Handler();

    // AUDIO
    private static final String LOG_TAG = "AudioLog";
    private MediaRecorder recorder = null;
    private static String fileName = null;

    // Google Firebase
    private DatabaseReference databaseReference;

    // Storage
    StorageReference storageReference;

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        // Responder location
        Intent data = getIntent();
        String location = data.getStringExtra("location");

        // AUDIO
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/recordedAudio.3gp";

        // Storage
        storageReference = FirebaseStorage.getInstance().getReference();

        responderTxt = findViewById(R.id.responderView);
        cancelBtn = findViewById(R.id.cancelBtn);
        recordIcon = findViewById(R.id.recordIcon);
        recordIconBg = findViewById(R.id.recordIconBg);
        recordingView = findViewById(R.id.recordingView);
        recordIcon.performClick();

        // Google Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        recordIcon.setOnClickListener(v -> {
            startRecording();
            recordingView.setText("Recording...");

            handler.postDelayed(() -> {
                stopRecording();
                uploadAudio();
                recordingView.setText("");
            },5 * 1000);
        });

        cancelBtn.setOnClickListener(v -> {
            databaseReference.child(phoneSend).child("status").setValue("OK");
            databaseReference.child(phoneSend).child("location").setValue("");
            databaseReference.child(phoneSend).child("responder").removeValue();
            databaseReference.child(phoneSend).child("timestamp").removeValue();
            Intent intent = new Intent(WaitingActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Checks to see if request has been accepted by responder
        new FirebaseAPI().getUsers((info, keys) -> { //
            for(int i = 0; i < info.size(); i++) {
                if(info.get(i).getStatus().equals("SOS REQUEST ACCEPTED" ) && info.get(i).getLocation().equals(location)) {
                    responderTxt.setText("Responder " + info.get(i).getResponder() + " is on the way!");
                }
            }
        });
    }

    // Uploads audio file to Firebase Storage
    private void uploadAudio() {
        StorageReference filepath = storageReference.child("audio").child("new_audio.3gp");
        Uri uri = Uri.fromFile(new File(fileName));
        filepath.putFile(uri).addOnSuccessListener(taskSnapshot -> {
        });
    }

    // Makes the cancel button non clickable after 12 seconds
    @Override
    protected void onStart() {
        super.onStart();
        handler.postDelayed(() -> cancelBtn.setEnabled(false), 12 * 1000);
    }

    // Records Audio
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        recorder.start();
    }
    
    // Stops Recording Audio
    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }
}