package com.oriya_s.viewmodel;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.oriya_s.model.User;
import com.oriya_s.model.UserProfiles;
import com.oriya_s.repository.BASE.BaseRepository;
import com.oriya_s.repository.UesrsRepository;
import com.oriya_s.viewmodel.BASE.BaseViewModel;

public class UsersViewmodel extends BaseViewModel<User, UserProfiles> {

    private UesrsRepository repository;

    public UsersViewmodel(Application application) {
        super(User.class, UserProfiles.class, application);
    }

    @Override
    protected BaseRepository<User, UserProfiles> createRepository(Application application) {
        repository = new UesrsRepository(application);
        return repository;
    }

    public void login(String email, String password){
        final User user = null;
        repository.getCollection().whereEqualTo("email", email).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                                User user = document.toObject(User.class);
                                if (user.getPassword().equals(password)){
                                    lvEntity.setValue(user);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        lvEntity.setValue(null);
                    }
                });
    }
}