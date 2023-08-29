package com.example.underthelamp.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.underthelamp.databinding.FragmentInformationBinding
import com.example.underthelamp.databinding.FragmentMessageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessageFragment : Fragment() {
    lateinit var binding : FragmentMessageBinding
    var firestore : FirebaseFirestore? = null
    var userId : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        firestore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }
}