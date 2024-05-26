package com.dicoding.sub1.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.sub1.api.APIConfig
import com.dicoding.sub1.api.LoginDataAccount
import com.dicoding.sub1.api.RegisterDataAccount
import com.dicoding.sub1.api.ResponseDetail
import com.dicoding.sub1.api.ResponseLogin
import retrofit2.Call
import retrofit2.Response

class MainViewModel : ViewModel() {
    private val _isLoadingLogin = MutableLiveData<Boolean>()
    val isLoadingLogin: LiveData<Boolean> = _isLoadingLogin
    var isErrorLogin: Boolean = false
    private val _messageLogin = MutableLiveData<String>()
    val messageLogin: LiveData<String> = _messageLogin
    private val _userLogin = MutableLiveData<ResponseLogin>()
    val userLogin: LiveData<ResponseLogin> = _userLogin

    var isErrorRegist: Boolean = false
    private val _isLoadingRegist = MutableLiveData<Boolean>()
    val isLoadingRegist: LiveData<Boolean> = _isLoadingRegist
    private val _messageRegist = MutableLiveData<String>()
    val messageRegist: LiveData<String> = _messageRegist

    fun getResponseLogin(loginDataAccount: LoginDataAccount) {
        _isLoadingLogin.value = true
        val api = APIConfig.getApiService().loginUser(loginDataAccount)
        api.enqueue(object : retrofit2.Callback<ResponseLogin> {
            override fun onResponse(call: Call<ResponseLogin>, response: Response<ResponseLogin>) {
                _isLoadingLogin.value = false
                val responseBody = response.body()

                if (response.isSuccessful) {
                    isErrorLogin = false
                    _userLogin.value = responseBody!!
                    _messageLogin.value = "Hello ${_userLogin.value!!.loginResult.name}!"
                } else {
                    isErrorLogin = true
                    when (response.code()) {
                        401 -> _messageLogin.value =
                            "Incorrect email or password, please try again."
                        408 -> _messageLogin.value =
                            "Your internet connection is slow, please try again."
                        else -> _messageLogin.value = "Error message: " + response.message()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseLogin>, t: Throwable) {
                isErrorLogin = true
                _isLoadingLogin.value = false
                _messageLogin.value = "Error message: " + t.message.toString()
            }
        })
    }

    fun getResponseRegister(registDataUser: RegisterDataAccount) {
        _isLoadingRegist.value = true
        val api = APIConfig.getApiService().registUser(registDataUser)
        api.enqueue(object : retrofit2.Callback<ResponseDetail> {
            override fun onResponse(call: Call<ResponseDetail>, response: Response<ResponseDetail>) {
                _isLoadingRegist.value = false
                if (response.isSuccessful) {
                    isErrorRegist = false
                    _messageRegist.value = "Congratulations, your account has been created successfully."
                } else {
                    isErrorRegist = true
                    when (response.code()) {
                        400 -> _messageRegist.value =
                            "1"
                        408 -> _messageRegist.value =
                            "Your internet connection is slow, please try again."
                        else -> _messageRegist.value = "Error message: " + response.message()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseDetail>, t: Throwable) {
                isErrorRegist = true
                _isLoadingRegist.value = false
                _messageRegist.value = "Error message: " + t.message.toString()
            }
        })
    }


}
