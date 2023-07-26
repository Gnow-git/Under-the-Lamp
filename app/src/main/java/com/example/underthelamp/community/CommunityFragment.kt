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
import com.example.underthelamp.databinding.FragmentCommunityBinding
import com.example.underthelamp.navigation.model.CommunityDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_community.view.community_recyclerview
import kotlinx.android.synthetic.main.fragment_detail.view.detailviewfragment_recyclerview
import kotlinx.android.synthetic.main.item_community.view.communityItem_image
import kotlinx.android.synthetic.main.item_community.view.community_text
import kotlinx.android.synthetic.main.item_community.view.community_title
import kotlinx.android.synthetic.main.item_detail.view.detailviewitem_profile_textview

class CommunityFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    var uid : String? = null

    lateinit var binding : FragmentCommunityBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_community, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.community_recyclerview.adapter = CommunityAdapter()
        view.community_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class CommunityAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var communityDTOs : ArrayList<CommunityDTO> = arrayListOf() // DTO 지정
        var communityUidList : ArrayList<String> = arrayListOf()
        init{
            firestore?.collection("community")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                communityDTOs.clear()
                communityUidList.clear()
                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(CommunityDTO::class.java)
                    communityDTOs.add(item!!)
                    communityUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_community,parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return communityDTOs.size
        }

        /**
         * 유저의 게시글을 불러와 보여주는 ViewHolder
         */
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            var viewHolder = (holder as CustomViewHolder).itemView

            // 작성한 게시물의 제목 불러오기
            viewHolder.community_title.text = communityDTOs!![position].community_title

            // 작성한 게시물의 내용 불러오기
            viewHolder.community_text.text = communityDTOs!![position].community_content
            // Image 불러오기
            Glide.with(holder.itemView.context).load(communityDTOs!![position].imageUrl).into(viewHolder.communityItem_image)

        }
    }
}