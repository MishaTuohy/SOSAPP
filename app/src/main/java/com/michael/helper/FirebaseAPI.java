package com.michael.helper;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseAPI {
    private DatabaseReference databaseReference;
    private List<NotificationsModel> users = new ArrayList<>();

    public FirebaseAPI() {
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    public interface DataStatus {
        void DataIsLoaded(List<NotificationsModel> users, List<String> keys);
    }


    public void getUsers(final DataStatus dataStatus) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                List<String> keys = new ArrayList<>();
                for(DataSnapshot keyNode: snapshot.getChildren()) {
                    keys.add(keyNode.getKey());
                    NotificationsModel user = keyNode.getValue(NotificationsModel.class);
                    users.add(user);
                }
                dataStatus.DataIsLoaded(users, keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}