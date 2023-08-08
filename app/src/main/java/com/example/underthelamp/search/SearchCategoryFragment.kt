package com.example.underthelamp.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentSearchCategoryBinding

class SearchCategoryFragment : Fragment(), View.OnClickListener {

    lateinit var binding : FragmentSearchCategoryBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchCategoryBinding.inflate(inflater, container, false)
        val view = binding.root

        // 클릭한 view에 대한 ID 배열 지정
        val viewId = arrayOf(
            R.id.art_view,
            R.id.public_music_view,
            R.id.music_view,
            R.id.play_view,
            R.id.literature_view,
            R.id.video_view
        )

        // view에 대한 클릭리스너 설정
        for (viewId in viewId) {
            view.findViewById<View>(viewId).setOnClickListener(this)
        }
        return view
    }

    override fun onClick(v: View?) {
        val viewId = v?.id
        // view id  토스트 메시지로 출력
        val viewIdString = resources.getResourceEntryName(viewId!!)
        Toast.makeText(requireContext(), "$viewIdString", Toast.LENGTH_SHORT).show()

        // 특정 view에 대한 동작 지정
        when (viewId) {
            R.id.art_view -> {

            }
            R.id.public_music_view -> {

            }
            R.id.music_view -> {

            }
            R.id.play_view -> {

            }
            R.id.literature_view -> {

            }
            R.id.video_view -> {

            }
        }
    }
}