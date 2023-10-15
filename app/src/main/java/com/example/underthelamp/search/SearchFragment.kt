package com.example.underthelamp.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.underthelamp.MainActivity
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentSearchBinding
import com.example.underthelamp.model.HistoryDTO
import com.example.underthelamp.navigation.GridFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_search.searchForm
import kotlinx.android.synthetic.main.fragment_search.view.searchHistory
import kotlinx.android.synthetic.main.item_history.view.historyText
import com.example.underthelamp.search.SearchFragmentListener
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.item_history.view.deleteHistory

interface SearchFragmentListener {
    fun viewCategory(category: String)
}

class SearchFragment : Fragment(), SearchFragmentListener {

    lateinit var binding : FragmentSearchBinding
    var firestore : FirebaseFirestore? = null
    var loginUid : String? = null
    var searchText : String? = null
    private var categoryIcon: String? = null /** 표시할 아이콘 */

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, searchInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        loginUid = FirebaseAuth.getInstance().currentUser?.uid
        val view = binding.root

        // framelayout 에 category 표시
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(binding.framelayout.id, SearchCategoryFragment(this))
        transaction.commit()

        binding.searchBtn.setOnClickListener { // 검색버튼을 누를 경우
            // 잠시 주석 처리 추후 처리
            searchText = binding.searchInput.text.toString()
            showSearchGridFragment(searchText!!)

            historyUpload()
        }

        // 검색 기록에 대한 adapter와 layoutManager 지정
        view.searchHistory.adapter = HistoryAdapter()

        // 가로로 설정
        view.searchHistory.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)

        return view
    }


    // 카테고리를 누를 경우 카테고리 선택 버튼이 보이도록하는 함수
    override fun viewCategory(category: String){

       when (category) {
            "미술" -> {
                binding.categoryIcon.setImageResource(R.drawable.commu_art_icon)
            }

            "문학" -> {
                binding.categoryIcon.setImageResource(R.drawable.commu_literature)
            }

           "대중음악" -> {
               binding.categoryIcon.setImageResource(R.drawable.commu_publicmusic_icon)
           }

           "음악" -> {
               binding.categoryIcon.setImageResource(R.drawable.commu_music)
           }

           "연극" -> {
               binding.categoryIcon.setImageResource(R.drawable.commu_theater)
           }

           "영상" -> {
               binding.categoryIcon.setImageResource(R.drawable.commu_video)
           }

            else -> binding.categoryIcon.setImageResource(R.drawable.commu_art_icon)
       }

        var categoryName = category


        binding.categoryText.setText(categoryName).toString()

        // 검색창 및 검색기록은 안보이도록
        binding.searchForm.visibility = View.GONE
        binding.searchHistory.visibility = View.GONE

        // 카테고리 표시는 보이도록
        binding.categoryForm.visibility = View.VISIBLE
    }

    private fun historyUpload() {
        var searchInput = binding.searchInput
        var historyDTO = HistoryDTO()

        var timestamp = com.google.firebase.Timestamp.now()
        historyDTO.history = searchInput.text.toString()
        historyDTO.timestamp = timestamp

        FirebaseFirestore.getInstance()
            .collection("history")
            .document(loginUid!!)
            .collection("list")
            .document(timestamp.toString())
            .set(historyDTO)

        searchInput.setText("") // 검색 초기화

    }
    // adapter
    inner class HistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var historyDTOS : ArrayList<HistoryDTO> = arrayListOf()

        init {
            firestore?.collection("history")
                ?.document(loginUid!!)
                ?.collection("list")
                ?.orderBy("timestamp", Query.Direction.DESCENDING)
                ?.addSnapshotListener { searchQuerySnapshot, firebaseFireException ->
                historyDTOS.clear()
                if(searchQuerySnapshot == null) return@addSnapshotListener

                for (snapshot in searchQuerySnapshot!!.documents) {
                    var item = snapshot.toObject(HistoryDTO::class.java)
                    historyDTOS.add(item!!)
                }
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
            return CustomHistoryViewHolder(view)
        }

        inner class CustomHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return historyDTOS.size
        }

        // 검색 기록 표시
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var historyViewHolder = (holder as CustomHistoryViewHolder).itemView

            // 저장된 검색 기록 불러 오기
            historyViewHolder.historyText.text = historyDTOS[position].history.toString()

            // 검색 기록의 x 를 누를 경우
            historyViewHolder.deleteHistory.setOnClickListener { v ->

                FirebaseFirestore.getInstance()
                    .collection("history")
                    .document(loginUid!!)
                    .collection("list")
                    .document(historyDTOS[position].timestamp.toString()) // 여기서 documentId는 HistoryDTO에 추가해야 함
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "검색 기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }

        }

    }
// 검색 버튼을 누를 경우 검색한 내용의 이미지들이 Grid 형태로 표시
    private fun showSearchGridFragment(searchText : String) {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        val fragment = SearchGridFragment()

        var bundle = Bundle()
        bundle.putString("explain", searchText)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.framelayout, fragment)
        fragmentTransaction.commit()
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