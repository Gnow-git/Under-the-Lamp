package com.example.underthelamp.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

interface SearchFragmentListener {
    fun viewCategory(category: String)
}

class SearchFragment : Fragment(), SearchFragmentListener {

    lateinit var binding : FragmentSearchBinding
    var firestore : FirebaseFirestore? = null
    var loginUid : String? = null
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
            historyUpload()
            // 잠시 주석 처리 추후 처리
            //showSearchGridFragment()
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

        historyDTO.history = searchInput.text.toString()
        historyDTO.timestamp = com.google.firebase.Timestamp.now()

        FirebaseFirestore.getInstance()
            .collection("history")
            .document(loginUid!!)
            .collection("list")
            .document()
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
                ?.orderBy("timestamp")
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

        }

    }
// 검색 버튼을 누를 경우 검색한 내용의 이미지들이 Grid 형태로 표시
    private fun showSearchGridFragment() {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        val fragment = SearchGridFragment()
        fragmentTransaction.replace(R.id.framelayout, fragment)
        fragmentTransaction.commit()
    }

}