package com.example.underthelamp.user

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import com.example.underthelamp.R
import com.example.underthelamp.databinding.ActivityLoginBinding



class LoginActivity : AppCompatActivity() {


    private lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginFragment()

    }

    private fun loginFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = LoginFragment()
        fragmentTransaction.replace(R.id.fragmentFrame, fragment)
        fragmentTransaction.commit()

    }


}