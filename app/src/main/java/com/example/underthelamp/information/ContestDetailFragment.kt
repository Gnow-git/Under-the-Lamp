package com.example.underthelamp.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentContestDetailBinding
import com.example.underthelamp.model.AlarmDTO
import com.example.underthelamp.model.ContestDTO
import com.example.underthelamp.navigation.util.FcmPush
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.item_contest_comment.view.commentviewitem_textview_comment
import kotlinx.android.synthetic.main.item_contest_comment.view.commentviewitem_textview_profile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 공모전(Contest) 의 상세 보기 기능 및 댓글을 보여 주는 fragment */
/** 현재는 댓글만 보여 주도록 구현 */

class ContestDetailFragment : Fragment() {

    lateinit var binding : FragmentContestDetailBinding
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var contestUid : String? = null
    var destinationUid : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentContestDetailBinding.inflate(inflater, container, false)
        var view = binding.root

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        /** 게시물 상세 페이지 댓글 */
        contestUid = arguments?.getString("contestUid")
        destinationUid = arguments?.getString("destinationUid")

        /** 게시물 상세 내용 불러 오기 */
        if (contestUid != null){
            firestore?.collection("contest")?.document(contestUid!!)
                ?.get()
                ?.addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {
                        val contestDTO = documentSnapshot.toObject(ContestDTO::class.java)

                        /** 게시물 제목 */
                        binding.detailTitle.text = contestDTO?.contestTitle

                        /** 작성 날짜 */
                        val timestamp = contestDTO?.timestamp
                        //val date = timestampToDate(timestamp)
                        //binding.detailDate.text = formatDate(date)

                        /** 작성 된 시간 */
                        //binding.detailTime.text = formatTimeAgo(timestamp)

                        /** 게시물 내용 */
                        binding.detailContest.text = contestDTO?.contestContent?.replace("\\n", "\n")

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
            // contestUid 가 null 일 경우
            val informationFragment = InformationFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_content, informationFragment)
                .addToBackStack(null)
                .commit()
        }

        /** 댓글 Recyclerview 함수 초기화 */
        val comment = view.findViewById<RecyclerView>(R.id.contestCommentRecyclerView)
        comment.adapter = ContestCommentRecyclerViewAdapter()
        comment.layoutManager = LinearLayoutManager(activity)

        binding.commentSendBtn.setOnClickListener {
            var comment = ContestDTO.ContestComment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = binding.commentEditMessage.text.toString()
            //comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("contest").document(contestUid!!).collection("comments").document().set(comment)
            //commentAlarm(destinationUid!!, comment_edit_message.text.toString())
            binding.commentEditMessage.setText("")
        }
        return view
    }

    fun commentAlarm(destinationUid : String, message: String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.kind = 1
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.timestamp = com.google.firebase.Timestamp.now()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        var msg = FirebaseAuth.getInstance().currentUser?.email + " " + getString(R.string.alarm_comment) + " of " + message
        FcmPush.instance.sendMessage(destinationUid, "Under_thee_Lamp", msg)
    }

    inner class ContestCommentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comments : ArrayList<ContestDTO.ContestComment> = arrayListOf()

        init {
            FirebaseFirestore.getInstance()
                .collection("contest")
                .document(contestUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if(querySnapshot == null) return@addSnapshotListener

                    for(snapshot in querySnapshot.documents!!){
                        comments.add(snapshot.toObject(ContestDTO.ContestComment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_contest_comment, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = (holder as CustomViewHolder).itemView

            // 댓글 불러 오기
            view.commentviewitem_textview_comment.text = comments[position].comment
            view.commentviewitem_textview_profile.text = comments[position].userId
        }

    }

    fun timestampToDate(timestamp: Long?): Date {
        return Date(timestamp!!)
    }

    fun formatDate(date: Date): String {
        val format = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        return format.format(date)
    }

    fun formatTime(date: Date): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }

    fun formatTimeAgo(timestamp: Long?): String {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - timestamp!!

        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)

        return when {
            minutes < 60 -> "$minutes 분 전"
            hours < 24 -> "$hours 시간 전"
            else -> formatTime(Date(timestamp))
        }
    }

}