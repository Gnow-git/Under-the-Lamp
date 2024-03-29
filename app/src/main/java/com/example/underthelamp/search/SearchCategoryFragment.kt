package com.example.underthelamp.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.underthelamp.MainActivity
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentSearchCategoryBinding
import com.google.firebase.firestore.FirebaseFirestore

class SearchCategoryFragment(private val listener: SearchFragmentListener) : Fragment(), View.OnClickListener {

    lateinit var binding: FragmentSearchCategoryBinding
    var firestore: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        firestore = FirebaseFirestore.getInstance()
        binding = FragmentSearchCategoryBinding.inflate(inflater, container, false)
        val view = binding.root

        // 클릭한 view에 대한 ID 배열 지정
        val viewId = arrayOf(
            R.id.artView,
            R.id.publicMusicView,
            R.id.musicView,
            R.id.theaterView,
            R.id.literatureView,
            R.id.videoView
        )

        // view에 대한 클릭리스너 설정
        for (viewId in viewId) {
            view.findViewById<View>(viewId).setOnClickListener(this)
        }
        return view
    }

    /** 각 뷰 클릭 시 해당 뷰의 카테고리와 동일한 유저가 작성한 게시물 출력 */
    override fun onClick(v: View?) {
        val viewId = v?.id

        // 특정 view에 대한 동작 지정
        val categoryMap = mapOf(
            R.id.artView to "미술",
            R.id.publicMusicView to "대중음악",
            R.id.musicView to "음악",
            R.id.theaterView to "연극",
            R.id.literatureView to "문학",
            R.id.videoView to "영상"
        )

        val category = categoryMap[viewId]
        if (category != null) {
            // SearchFragment 의 categoryForm 에 표시하도록 설정
            listener?.viewCategory(category)

            searchFirebase(category)
        }
    }
    /** 카테고리와 동일한 유저의 게시물을 보여주는 함수 */
    private fun searchFirebase(category: String) {

        firestore?.collection("userinfo")?.get()?.addOnSuccessListener { mainCollectionSnapshot ->
            val userUids = ArrayList<String>()

            for (documentSnapshot in mainCollectionSnapshot) {
                val userUid = documentSnapshot.id
                val userSubCollection = documentSnapshot.reference.collection("userinfo")

                /** 서브 컬렉션 을 사용 중이기 때문에 따로 지정, 사용자의 카테고리 정보를 불러옴 */
                userSubCollection.whereEqualTo("user_category", category)
                    .get()
                    .addOnSuccessListener { subcollectionQuerySnapshot ->

                        if (!subcollectionQuerySnapshot.isEmpty) {
                            userUids.add(userUid)
                        }
                        showSearchResult(category, userUids)
                    }.addOnFailureListener { e ->
                        // 오류시
                        Toast.makeText(activity, "정보를 불러 오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
        }?.addOnFailureListener { e ->
            // 오류 발생시
            Toast.makeText(activity, "정보를 불러 오지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSearchResult(category: String, userId: ArrayList<String>) {
        if (userId.isNotEmpty()) {
            val gridFragment = SearchGridFragment.newInstance(category, userId)
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.framelayout, gridFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        } else {
            // 서브컬렉션에 저장된 게시물이 없는 경우
            // Toast 메시지를 넣을 경우 반복문으로 인해 반복 호출, 추후 해결 요망
        }
    }

    // 뒤로가기 버튼 제어
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // MainActivity로 이동하려면 Intent를 사용합니다.
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
}
