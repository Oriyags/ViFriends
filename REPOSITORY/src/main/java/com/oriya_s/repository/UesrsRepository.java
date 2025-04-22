package com.oriya_s.repository;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.oriya_s.model.User;
import com.oriya_s.model.UserProfiles;
import com.oriya_s.repository.BASE.BaseRepository;

public class UesrsRepository extends BaseRepository<User, UserProfiles> {

    public UesrsRepository(Application application) {
        super(User.class, UserProfiles.class, application);
    }

    @Override
    protected Query getQueryForExist(User entity) {
        return getCollection().whereEqualTo("email", entity.getEmail());
    }
}