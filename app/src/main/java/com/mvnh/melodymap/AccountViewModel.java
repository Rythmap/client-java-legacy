package com.mvnh.melodymap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AccountViewModel extends ViewModel {
    private static MutableLiveData<String> _accountInfoLiveData = new MutableLiveData<>("");
    public LiveData<String> accountInfoLiveData = _accountInfoLiveData;

    public static void setAccountInfo(String accountInfo) {
        _accountInfoLiveData.setValue(accountInfo);
    }
    public static LiveData<String> getAccountInfo() {
        return _accountInfoLiveData;
    }
}
