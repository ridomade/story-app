package com.dicoding.sub1.ui

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.sub1.R
import com.dicoding.sub1.api.LoginDataAccount
import com.dicoding.sub1.api.RegisterDataAccount
import com.dicoding.sub1.dataStore
import com.dicoding.sub1.databinding.ActivityRegisterBinding
import com.dicoding.sub1.preference.UserPreferences
import com.dicoding.sub1.viewmodel.LoginViewModel
import com.dicoding.sub1.viewmodel.MainViewModel
import com.dicoding.sub1.viewmodel.ViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Create Account"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupObservers()
        handleShowPassword()
        handleRegister()
        handleLogin()
    }

    private fun setupObservers() {
        val pref = UserPreferences.getInstance(dataStore)
        val userLoginViewModel =
            ViewModelProvider(this, ViewModelFactory(pref))[LoginViewModel::class.java]
        userLoginViewModel.getLoginSession().observe(this) { sessionTrue ->
            if (sessionTrue) {
                val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        mainViewModel.messageRegist.observe(this) { messageRegist ->
            handleRegisterResponse(
                mainViewModel.isErrorRegist,
                messageRegist
            )
        }
        mainViewModel.isLoadingRegist.observe(this) {
            displayLoading(it)
        }
        mainViewModel.messageLogin.observe(this) { messageLogin ->
            handleLoginResponse(
                mainViewModel.isErrorLogin,
                messageLogin,
                userLoginViewModel
            )
        }
        mainViewModel.isLoadingLogin.observe(this) {
            displayLoading(it)
        }
    }

    private fun handleLoginResponse(
        isError: Boolean,
        message: String,
        userLoginViewModel: LoginViewModel
    ) {
        if (!isError) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            val user = mainViewModel.userLogin.value
            userLoginViewModel.saveLoginSession(true)
            userLoginViewModel.saveToken(user?.loginResult!!.token)
            userLoginViewModel.saveName(user.loginResult.name)
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleRegisterResponse(isError: Boolean, message: String) {
        if (isError) {
            if (message == "1") {
                binding.cvEmail.setErrorMessage(
                    getString(R.string.email_already_registered),
                    binding.cvEmail.text.toString()
                )
                Toast.makeText(this, getString(R.string.alreadyRegistered), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            val userLogin = LoginDataAccount(
                binding.cvEmail.text.toString(),
                binding.cvPassword.text.toString()
            )
            mainViewModel.getResponseLogin(userLogin)
        }
    }


    private fun handleShowPassword() {
        binding.showPassword.setOnCheckedChangeListener { _, isChecked ->
            val transformationMethod =
                if (isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            binding.cvPassword.transformationMethod = transformationMethod
            binding.cvConfirmPassword.transformationMethod = transformationMethod
        }

    }

    private fun handleRegister() {
        binding.btnRegistAccount.setOnClickListener {
            binding.cvName.clearFocus()
            binding.cvEmail.clearFocus()
            binding.cvPassword.clearFocus()
            binding.cvConfirmPassword.clearFocus()

            if (binding.cvName.isNameValid && binding.cvEmail.isEmailValid && binding.cvPassword.isPasswordValid && binding.cvConfirmPassword.isPasswordValid) {
                val dataRegisterAccount = RegisterDataAccount(
                    name = binding.cvName.text.toString().trim(),
                    email = binding.cvEmail.text.toString().trim(),
                    password = binding.cvPassword.text.toString().trim()
                )

                mainViewModel.getResponseRegister(dataRegisterAccount)
            } else {
                if (!binding.cvName.isNameValid) binding.cvName.error =
                    getString(R.string.emptyName)
                if (!binding.cvEmail.isEmailValid) binding.cvEmail.error =
                    getString(R.string.invalidEmailFormat)
                if (!binding.cvPassword.isPasswordValid) binding.cvPassword.error =
                    getString(R.string.mustMoreThanEightCharacter)
                if (!binding.cvConfirmPassword.isPasswordValid) binding.cvConfirmPassword.error =
                    getString(R.string.passwordCannotbeEmpty)
                Toast.makeText(this, getString(R.string.enterDataCorectly), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun handleLogin() {
        binding.btnLogin.setOnClickListener {
            finish()
        }
    }

    private fun displayLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        finish()
        return super.onSupportNavigateUp()
    }
}
