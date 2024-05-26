package com.dicoding.sub1.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.sub1.preference.UserPreferences
import kotlinx.coroutines.launch

class LoginViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    fun getLoginSession(): LiveData<Boolean> {
        return userPreferences.getLoginSession().asLiveData()
    }

    fun saveLoginSession(loginSession: Boolean) {
        viewModelScope.launch {
            userPreferences.saveLoginSession(loginSession)
        }
    }

    fun getToken(): LiveData<String> {
        return userPreferences.getToken().asLiveData()
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            userPreferences.saveToken(token)
        }
    }

    fun saveName(name: String) {
        viewModelScope.launch {
            userPreferences.saveName(name)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.logout()
        }
    }
}
