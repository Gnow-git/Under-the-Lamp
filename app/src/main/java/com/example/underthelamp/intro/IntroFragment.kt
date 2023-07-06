package com.example.underthelamp.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.underthelamp.R
import kotlinx.android.synthetic.main.fragment_intro.*

class IntroFragment : Fragment() {
    private var image: Int? = null
    private var text : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            image = it.getInt("image", 0)
            text = it.getString("text", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView.setImageResource(image!!)
        textView.text = text
    }

    companion object {
        fun newInstance(image: Int, text: String) =
            IntroFragment().apply {
                arguments = Bundle().apply {
                    putInt("image", image)
                    putString("text", text)
                }
            }
    }
}