package com.example.clockwork_2.ui.classes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ClassesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ClassesViewModel() {
    }

    public LiveData<String> getText() {
        return mText;
    }
}