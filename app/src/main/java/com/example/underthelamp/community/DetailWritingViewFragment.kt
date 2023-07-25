package com.example.underthelamp.community

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.underthelamp.R
import com.example.underthelamp.navigation.model.ContentDTO
import com.example.underthelamp.navigation.model.WritingDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.detailviewfragment_recyclerview
import kotlinx.android.synthetic.main.item_detail.view.detailviewitem_imageview_content
import kotlinx.android.synthetic.main.item_detail.view.detailviewitem_profile_textview

/**
 * 커뮤니티의 글들을 보여주는 Fragment
  */
class DetailWritingViewFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    var uid : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragement_detailwriting, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragment_recyclerview.adapter = DetailWritingViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class DetailWritingViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var writingDTOs : ArrayList<WritingDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init{
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                writingDTOs.clear()
                contentUidList.clear()
                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(WritingDTO::class.java)
                    writingDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail,parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return writingDTOs.size
        }

        /**
         * 유저의 게시글을 보여주는 ViewHolder
         */
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            var viewHolder = (holder as CustomViewHolder).itemView

            /**
             * 게시글을 작성한 UserId 불러오기
             */
        firestore!!.collection("userinfo")
            .document(writingDTOs!![position].uid.toString()).collection("userinfo").document("detail")
            .get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("user_name")
                    viewHolder.detailviewitem_profile_textview.text = userName
                } else {
                    viewHolder.detailviewitem_profile_textview.text = "이름을 불러올 수 없습니다."
                }
            }.addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "데이터 가져오기 실패:", exception)
                viewHolder.detailviewitem_profile_textview.text = "데이터를 가져오는 중에 오류가 발생하였습니다."
            }

        }
    }
}