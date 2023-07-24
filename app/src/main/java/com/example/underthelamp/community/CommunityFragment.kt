package com.example.underthelamp.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.underthelamp.databinding.FragmentCommunityBinding

class CommunityFragment : Fragment() {

    lateinit var binding : FragmentCommunityBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }
}