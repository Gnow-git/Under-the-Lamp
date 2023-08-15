package com.example.underthelamp.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.underthelamp.R
import com.google.firebase.firestore.FirebaseFirestore

class RandomUserFragment: Fragment() {

    var firestore : FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_random_user, container, false)
        firestore = FirebaseFirestore.getInstance()

        return view
    }
}