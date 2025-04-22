package com.oriya_s.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewmodel extends ViewModel {
    private final MutableLiveData<String> userName = new MutableLiveData<>();

    public LiveData<String> getUserName() {
        return userName;
    }
// Example :)
    public void loadUserName() {
        userName.setValue("Oriya");
    }
}
