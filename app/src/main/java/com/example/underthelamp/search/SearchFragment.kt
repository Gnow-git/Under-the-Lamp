package com.example.underthelamp.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentSearchBinding
import com.example.underthelamp.navigation.GridFragment

class SearchFragment : Fragment() {

    lateinit var binding : FragmentSearchBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, searchInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root

        // framelayout 에 category 표시
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(binding.framelayout.id, SearchCategoryFragment())
        transaction.commit()

        binding.searchBtn.setOnClickListener { // 검색버튼을 누를 경우
            showSearchGridFragment()
        }

        return view
    }
// 검색 버튼을 누를 경우 검색한 내용의 이미지들이 Grid 형태로 표시
    private fun showSearchGridFragment() {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        val fragment = SearchGridFragment()
        fragmentTransaction.replace(R.id.framelayout, fragment)
        fragmentTransaction.commit()
    }
}