package com.example.underthelamp.search

import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.init
import com.bumptech.glide.request.RequestOptions
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentSearchRecyclerBinding
import com.example.underthelamp.databinding.FragmentUserInfoCatergoriesBinding
import com.example.underthelamp.navigation.UserFragment
import com.example.underthelamp.model.ArtistDTO
import com.example.underthelamp.model.ContentDTO
import com.example.underthelamp.model.UserCategoryDTO
import com.example.underthelamp.model.UserDetailDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_search.view.*

class SearchRecyclerFragment : Fragment(){

    private lateinit var binding: FragmentSearchRecyclerBinding
    private lateinit var mAuth: FirebaseAuth
    private val db = Firebase.firestore
    private val userinfo = db.collection("userinfo")
    private val profileImage = db.collection("profileImage")
    private lateinit var artistDTO: ArtistDTO

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchRecyclerBinding.inflate(inflater, container, false)

        // 인증 초기화
        mAuth = Firebase.auth

        binding.searchRecyclerview.adapter = SearchRecyclerViewAdapter()
        binding.searchRecyclerview.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }

    inner class SearchRecyclerViewAdapter : RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder>() {

        private var userList: ArrayList<UserDetailDTO> = arrayListOf()
        private var userCategoryList: ArrayList<UserCategoryDTO> = arrayListOf()
        private var contentDTOs : ArrayList<ContentDTO> = arrayListOf()

        init {
            // 현재 저장된 모든 유저의 정보를 보여줌
            userinfo.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    userList.clear()
                    userCategoryList.clear()

                    for (documentSnapshot in querySnapshot.documents) {
                        val uid = documentSnapshot.id

                        val categoryDocRef = userinfo.document(uid).collection("userinfo").document("category")
                        categoryDocRef.get().addOnSuccessListener { categorySnapshot ->
                            val userCategoryDTO = categorySnapshot.toObject(UserCategoryDTO::class.java)
                            if (userCategoryDTO != null) {
                                userCategoryList.add(userCategoryDTO)
                            }
                            notifyDataSetChanged()
                        }

                        val detailDocRef = userinfo.document(uid).collection("userinfo").document("detail")
                        detailDocRef.get().addOnSuccessListener { detailSnapshot ->
                            val userDetailDTO = detailSnapshot.toObject(UserDetailDTO::class.java)
                            if (userDetailDTO != null) {
                                userList.add(userDetailDTO)
                            }
                            notifyDataSetChanged()
                        }
                    }
                }
            }

            profileImage.addSnapshotListener { querySnapshots, firebaseFirestoreException ->
                // Somtimes, This code return null of querySnapshot when it signout
                if(querySnapshots == null) return@addSnapshotListener

                // Get data
                for(snapshot in querySnapshots.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java )!!)
                }
                notifyDataSetChanged()
            }

        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val viewHolder = holder.itemView
            viewHolder.name.text = userList[position].user_name
            viewHolder.category.text = userCategoryList[position].user_category

            // 프로필 이미지 불러오기 작업해야함

        }


        override fun getItemCount(): Int {
            return userList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.name
            val category: TextView = itemView.category
        }

        fun search(searchWord: String) {

            userinfo.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    // ArrayList 비워줌
                    userList.clear()
                    userCategoryList.clear()

                    for (documentSnapshot in querySnapshot.documents) {
                        val uid = documentSnapshot.id

                        val categoryDocRef =
                            userinfo.document(uid).collection("userinfo").document("category")
                        categoryDocRef.get().addOnSuccessListener { categorySnapshot ->
                            val userCategoryDTO = categorySnapshot.toObject(UserCategoryDTO::class.java)
                            if (documentSnapshot.contains(searchWord)) {
                                userCategoryList.add(userCategoryDTO!!)
                            }
                            notifyDataSetChanged()
                        }

                        val detailDocRef =
                            userinfo.document(uid).collection("userinfo").document("detail")
                        detailDocRef.get().addOnSuccessListener { detailSnapshot ->
                            val userDetailDTO = detailSnapshot.toObject(UserDetailDTO::class.java)
                            if (documentSnapshot.contains(searchWord)) {
                                userList.add(userDetailDTO!!)
                            }
                            notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }
}