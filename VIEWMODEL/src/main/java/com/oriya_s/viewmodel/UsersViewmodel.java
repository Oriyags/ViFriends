package com.oriya_s.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.oriya_s.model.User;
import com.oriya_s.model.UserProfiles;
import com.oriya_s.repository.BASE.BaseRepository;
import com.oriya_s.repository.UesrsRepository;
import com.oriya_s.viewmodel.BASE.BaseViewModel;

public class UsersViewmodel extends BaseViewModel<User, UserProfiles> {

    private final UesrsRepository repository;
    private final MutableLiveData<Boolean> registrationResult = new MutableLiveData<>();

    public UsersViewmodel(Application application) {
        super(User.class, UserProfiles.class, application);
        repository = (UesrsRepository) createRepository(application);
    }

    @Override
    protected BaseRepository<User, UserProfiles> createRepository(Application application) {
        return new UesrsRepository(application);
    }

    public LiveData<Boolean> getRegistrationResult() {
        return registrationResult;
    }

    @Override
    public void save(User user) {
        repository.saveUser(user, registrationResult::postValue);
    }
}