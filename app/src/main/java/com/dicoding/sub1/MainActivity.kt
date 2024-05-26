package com.dicoding.sub1

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dicoding.sub1.api.LoginDataAccount
import com.dicoding.sub1.databinding.ActivityMainBinding
import com.dicoding.sub1.preference.UserPreferences
import com.dicoding.sub1.ui.HomeActivity
import com.dicoding.sub1.ui.RegisterActivity
import com.dicoding.sub1.viewmodel.LoginViewModel
import com.dicoding.sub1.viewmodel.MainViewModel
import com.dicoding.sub1.viewmodel.ViewModelFactory


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleLogin()
        handleRegister()
        handleUserPreferences()
        playAnimation()
        mainViewModel.isLoadingLogin.observe(this) { isLoading ->
            displayLoading(isLoading)
        }
    }

    private fun playAnimation(){
        ObjectAnimator.ofFloat(binding.iconLogin, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

    }

    private fun handleUserPreferences() {
        val preferences = UserPreferences.getInstance(dataStore)
        val loginViewModel = ViewModelProvider(this, ViewModelFactory(preferences))[LoginViewModel::class.java]

        loginViewModel.getLoginSession().observe(this) { sessionExists ->
            if (sessionExists) {
                redirectToHome()
            }
        }
        mainViewModel.messageLogin.observe(this) { message ->
            handleLoginResponse(message, loginViewModel)
        }
    }

    private fun handleLoginResponse(message: String, loginViewModel: LoginViewModel) {
        val isError = mainViewModel.isErrorLogin
        if (!isError) {
            showToast(message)
            val user = mainViewModel.userLogin.value
            loginViewModel.saveLoginSession(true)
            loginViewModel.saveToken(user?.loginResult!!.token)
            loginViewModel.saveName(user.loginResult.name)
        } else {
            showToast(message)
        }
    }

    private fun redirectToHome() {
        val intent = Intent(this@MainActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun handleLogin() {
        binding.btnLogin.setOnClickListener {
            binding.CVEmail.clearFocus()
            binding.CVPasswordLogin.clearFocus()

            if (isDataValid()) {
                val requestLogin = LoginDataAccount(
                    binding.CVEmail.text.toString().trim(),
                    binding.CVPasswordLogin.text.toString().trim()
                )
                mainViewModel.getResponseLogin(requestLogin)
            } else {
                if (!binding.CVEmail.isEmailValid) binding.CVEmail.error = "Invalid Email"
                if (!binding.CVPasswordLogin.isPasswordValid) binding.CVPasswordLogin.error =
                    "Invalid Password"
            }
        }
        binding.showPassword.setOnCheckedChangeListener { _, isChecked ->
            binding.CVPasswordLogin.transformationMethod = if (isChecked) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }
            binding.CVPasswordLogin.text?.let { binding.CVPasswordLogin.setSelection(it.length) }
        }
    }

    private fun handleRegister() {
        binding.btnRegister.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun isDataValid(): Boolean {
        return binding.CVEmail.isEmailValid && binding.CVPasswordLogin.isPasswordValid
    }

    private fun displayLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.text = if (isLoading) "" else getString(R.string.btnLogin)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
