package com.example.underthelamp.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentCommunityDetailBinding
import com.example.underthelamp.navigation.model.AlarmDTO
import com.example.underthelamp.navigation.model.CommunityDTO
import com.example.underthelamp.navigation.model.ContentDTO
import com.example.underthelamp.navigation.util.FcmPush
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_comment.comment_edit_message
import kotlinx.android.synthetic.main.item_community_comment.view.commentviewitem_textview_comment
import kotlinx.android.synthetic.main.item_community_comment.view.commentviewitem_textview_profile

/** Community 의 상세 보기 기능 및 댓글을 보여 주는 fragment */
/** 현재는 댓글만 보여 주도록 구현 */

class CommunityDetailFragment : Fragment() {

    lateinit var binding : FragmentCommunityDetailBinding
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var communityUid : String? = null
    var destinationUid : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCommunityDetailBinding.inflate(inflater, container, false)
        var view = binding.root

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        // 게시물 상세 페이지 댓글
        communityUid = arguments?.getString("communityUid")
        destinationUid = arguments?.getString("destinationUid")

        /** 댓글 Recyclerview 함수 초기화 */
        val comment = view.findViewById<RecyclerView>(R.id.community_comment_recyclerview)
        comment.adapter = CommunityCommentRecyclerViewAdapter()
        comment.layoutManager = LinearLayoutManager(activity)

        binding.commentBtnSend.setOnClickListener {
            var comment = CommunityDTO.CommunityComment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_edit_message.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("community").document(communityUid!!).collection("comments").document().set(comment)
            //commentAlarm(destinationUid!!, comment_edit_message.text.toString())
            comment_edit_message.setText("")
        }
        return view
    }

    fun commentAlarm(destinationUid : String, message: String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.kind = 1
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.timestamp = System.currentTimeMillis()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        var msg = FirebaseAuth.getInstance().currentUser?.email + " " + getString(R.string.alarm_comment) + " of " + message
        FcmPush.instance.sendMessage(destinationUid, "Under_thee_Lamp", msg)
    }

    inner class CommunityCommentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comments : ArrayList<CommunityDTO.CommunityComment> = arrayListOf()

        init {
            FirebaseFirestore.getInstance()
                .collection("community")
                .document(communityUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if(querySnapshot == null)return@addSnapshotListener

                    for(snapshot in querySnapshot.documents!!){
                        comments.add(snapshot.toObject(CommunityDTO.CommunityComment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_community_comment, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            view.commentviewitem_textview_comment.text = comments[position].comment
            view.commentviewitem_textview_profile.text = comments[position].userId
        }

    }

}