package com.michael.sender;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final int CAMERA_REQUEST_CODE = 102;
    String currentPhotoPath;
    Info userSender;

    // XML Elements
    ImageButton sosBtn;

    // GPS
    GPSLocation gpsReader;
    double lat, lon;
    public LocationManager locationManager;
    Geocoder geocoder;
    List<Address> addressList;
    String address, phoneSend = "0123456789";

    // Permission
    List<String> listPermissions = new ArrayList<>();

    // Google Firebase
    private DatabaseReference databaseReference;

    // Storage
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permissions
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO},
                PackageManager.PERMISSION_GRANTED);

        // XML Elements
        sosBtn = findViewById(R.id.sosBtn);

        // Google Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Storage
        storageReference = FirebaseStorage.getInstance().getReference();

        // GPS location
        gpsReader = new GPSLocation(getApplicationContext());
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // SOS button listener
        sosBtn.setOnClickListener(v -> {
            checkPermission();  // Checks Permissions
            takePicture();      // Takes photo and sends to Firebase Storage
        });
    }

    private void sendHelp() {
        userSender = new Info("OK"); // New Sender
        databaseReference.child(phoneSend).setValue(userSender); // Adds user to the database
        Location location = gpsReader.getLocation(); // Gets location
        String googleLocation; // Stores version of address that can be used with google maps

        if (location != null) { // If a location was taken in
            lat = location.getLatitude(); // Gets latitude
            lon = location.getLatitude(); // Gets Longitude
            googleLocation = "https://www.google.com/maps/@" + lat + "," + lon; // Gets google location link
            geocoder = new Geocoder(MainActivity.this, Locale.getDefault()); // Creates object for getting address of user (Not co-ordinates

            try {
                addressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1); // Finds the address using co-ordinates
            } catch (IOException e) {
                e.printStackTrace();
            }
            address = addressList.get(0).getAddressLine(0); // Puts address into String variable

            // Changes the values of the sender in the realtime database
            databaseReference.child(phoneSend).child("status").setValue("SOS REQUEST");
            databaseReference.child(phoneSend).child("location").setValue(address);
            databaseReference.child(phoneSend).child("timestamp").setValue(ServerValue.TIMESTAMP);

            // stores the the address of the user in an intent so we can use it in the next view
            Intent intent = new Intent(MainActivity.this, WaitingActivity.class);
            intent.putExtra("location", address);
            startActivity(intent);
        }
    }

    // This is for opening the camera and allowing the user to take a photo
    // This also sends the photo to the database
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                File f = new File(currentPhotoPath);
                Log.d("tag", "Absolute Url of Image is " + Uri.fromFile(f));

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

                uploadImageToFirebase(f.getName(),contentUri);
            }
        }
        sendHelp(); // Starts send help method for getting location and alerting database
    }

    // Permissions
    private void checkPermission() {
        int fineLocation = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int camera = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        int audio = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO);
        int readFiles = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeFiles = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(fineLocation != PackageManager.PERMISSION_GRANTED)
            listPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if(coarseLocation != PackageManager.PERMISSION_GRANTED)
            listPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        if(camera != PackageManager.PERMISSION_GRANTED)
            listPermissions.add(Manifest.permission.CAMERA);

        if(audio != PackageManager.PERMISSION_GRANTED)
            listPermissions.add(Manifest.permission.RECORD_AUDIO);

        if(readFiles != PackageManager.PERMISSION_GRANTED)
            listPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        if(writeFiles != PackageManager.PERMISSION_GRANTED)
            listPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(!listPermissions.isEmpty())
            ActivityCompat.requestPermissions(MainActivity.this, listPermissions.toArray(new String[listPermissions.size()]), 1);
    }

    private File createImageFile() throws IOException { // Used for creating the unique file for the image taken by the user
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory*/
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void takePicture() { // Used for allowing the user to take a photo by opening the camera
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("tag", "Error creating file for image"); // Error occurred while creating the File
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.michael.android.fileprovider", photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI); // Sends URI to activity result method
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private void uploadImageToFirebase(String name, Uri contentUri) { // Uploads img file to the database
        final StorageReference image = storageReference.child("images/" + name);
        image.putFile(contentUri).addOnSuccessListener(taskSnapshot -> {
            image.getDownloadUrl().addOnSuccessListener(uri ->
                    Log.d("tag",
                            "onSuccess: Uploaded Image URl is " + uri.toString()));

            Toast.makeText(MainActivity.this,
                    "Image Is Uploaded.", Toast.LENGTH_SHORT).show();

        }).addOnFailureListener(e ->
                Toast.makeText(MainActivity.this,
                        "Upload Failled.", Toast.LENGTH_SHORT).show());
    }
}























