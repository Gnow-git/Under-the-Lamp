package com.example.underthelamp.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentCommunityDetailBinding
import com.example.underthelamp.navigation.model.AlarmDTO
import com.example.underthelamp.navigation.model.CommunityDTO
import com.example.underthelamp.navigation.model.ContentDTO
import com.example.underthelamp.navigation.util.FcmPush
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_comment.comment_edit_message
import kotlinx.android.synthetic.main.fragment_community_detail.view.detail_date
import kotlinx.android.synthetic.main.fragment_community_detail.view.detail_title
import kotlinx.android.synthetic.main.fragment_community_detail.view.detail_user
import kotlinx.android.synthetic.main.item_community_comment.view.commentviewitem_textview_comment
import kotlinx.android.synthetic.main.item_community_comment.view.commentviewitem_textview_profile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Community 의 상세 보기 기능 및 댓글을 보여 주는 fragment */
/** 현재는 댓글만 보여 주도록 구현 */

class CommunityDetailFragment : Fragment() {

    lateinit var binding : FragmentCommunityDetailBinding
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var communityUid : String? = null
    var destinationUid : String? = null
    private var communityDTOs: List<CommunityDTO>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCommunityDetailBinding.inflate(inflater, container, false)
        var view = binding.root

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        /** 게시물 상세 페이지 댓글 */
        communityUid = arguments?.getString("communityUid")
        destinationUid = arguments?.getString("destinationUid")

        /** 게시물 상세 내용 불러 오기 */
        if (communityUid != null){
            firestore?.collection("community")?.document(communityUid!!)
                ?.get()
                ?.addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {
                        val communityDTO = documentSnapshot.toObject(CommunityDTO::class.java)
                        /** 게시물 제목 */
                        binding.detailTitle.text = communityDTO?.community_title

                        /** 작성자 */
                        binding.detailUser.text = communityDTO?.userId

                        /** 작성 날짜 */
                        //binding.detailDate.text = communityDTO?.timestamp

                        /** 작성 시간 */
                        //binding.detailTime.text = communityDTO?.timestamp

                        /** 게시물 내용 */
                        binding.detailCommunity.text = communityDTO?.community_content

                        /** 게시물 사진 */
                        var imageUrl = documentSnapshot.getString("imageUrl")
                        Glide.with(this).load(imageUrl).into(binding.detailImage) // binding을 이용하여 ImageView를 가져옴
                    }
                }
                ?.addOnFailureListener { exception ->
                    // 불러 오기 실패 시
                    // 나중에 경고 창 구현 하기
                }
        } else {
            // communityUid 가 null 일 경우
            val communityFragment = CommunityFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_content, communityFragment)
                .addToBackStack(null)
                .commit()
        }

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
                    if(querySnapshot == null) return@addSnapshotListener

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
            var view = (holder as CustomViewHolder).itemView
           // val timestamp = communityDTOs!![position].timestamp // 날짜 출력을 위한 상수 설정
            //val formattedDate = formatDate(timestamp)   // 날짜 변환

            // 댓글 불러 오기
            view.commentviewitem_textview_comment.text = comments[position].comment
            view.commentviewitem_textview_profile.text = comments[position].userId
        }

    }

    /** 날짜 변경을 위한 함수 */
    private fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return "0000.00.00"  // 값이 없을 경우

        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        return dateFormat.format(date)
    }

}