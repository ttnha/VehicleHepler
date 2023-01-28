package com.example.vehiclehelper.helper.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FireBaseInit {
    private static FireBaseInit INSTANCE;
    private static DatabaseReference reference;

    private FireBaseInit() {
        reference = FirebaseDatabase.getInstance().getReference();
    }

    public static FireBaseInit getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FireBaseInit();
        }
        return INSTANCE;
    }

    public DatabaseReference getReference() {
        return reference;
    }
}
