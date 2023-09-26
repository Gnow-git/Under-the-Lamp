package com.example.underthelamp.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.underthelamp.databinding.FragmentSettingBinding
import com.example.underthelamp.user.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SettingFragment : Fragment() {

    lateinit var binding : FragmentSettingBinding
    var auth : FirebaseAuth? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        var view = binding.root

        auth = FirebaseAuth.getInstance()

        // logoutBtn 을 눌렀을 경우 로그아웃 처리, LoginActivity로 이동
        binding.logoutBtn.setOnClickListener {
            activity?.finish()
            startActivity(Intent(activity, LoginActivity::class.java))
            auth?.signOut()
        }

        return view
    }
}