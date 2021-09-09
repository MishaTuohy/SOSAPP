package com.michael.sender;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseAPI { // (3) MICHAEL ALEXEY TUOHY
    private DatabaseReference databaseReference;
    private List<Info> info = new ArrayList<>();

    public FirebaseAPI() {
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    public interface DataStatus {
        void DataIsLoaded(List<Info> info, List<String> keys);
    }


    public void getUsers(final DataStatus dataStatus) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                info.clear();
                List<String> keys = new ArrayList<>();
                for(DataSnapshot keyNode: snapshot.getChildren()) {
                    keys.add(keyNode.getKey());
                    Info infoNode = keyNode.getValue(Info.class);
                    info.add(infoNode);
                }
                dataStatus.DataIsLoaded(info, keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
