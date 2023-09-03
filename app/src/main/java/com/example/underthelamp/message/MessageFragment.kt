package com.example.underthelamp.message

import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentMessageBinding
import com.example.underthelamp.model.MessageDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.item_message.view.messageContent
import kotlinx.android.synthetic.main.item_message.view.messageProfile
import kotlinx.android.synthetic.main.item_message.view.messageTime
import kotlinx.android.synthetic.main.item_message.view.messageUserName

class MessageFragment : Fragment() {
    lateinit var binding : FragmentMessageBinding
    var firestore : FirebaseFirestore? = null
    var userId : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        firestore = FirebaseFirestore.getInstance()
        // 현재 로그인한 유저의 id 정보 확인
        userId = FirebaseAuth.getInstance().currentUser?.uid
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        val view = binding.root

        /** chatList에 대한 adapter와 layoutManager 지정 */
        binding.chatList.adapter = ChatAdapter()
        binding.chatList.layoutManager = LinearLayoutManager(activity)

        return view
    }

    inner class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var messageUidList: ArrayList<String> = arrayListOf()   // otherUserId를 저장할 리스트

        /** firestore 초기화 */
        init{
            firestore?.collection("message")
                ?.whereEqualTo("userId", userId)    // 현재 로그인한 사용자가 포함된 메시지 필터링
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    messageUidList.clear()

                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot.documents) {
                        val messageDTO = snapshot.toObject(MessageDTO::class.java)!!

                        // messageDTO의 userId와 현재 로그인한 userId를 비교 후 상대방에 대한 Id 정보를 arrayList로 저장
                        if(messageDTO.userId == userId){
                            messageUidList.add(messageDTO.otherUserId.toString())
                        }else Toast.makeText(activity, "사용자 정보가 다릅니다.", Toast.LENGTH_SHORT).show()
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            return CustomMessageViewHolder(view)
        }

        inner class CustomMessageViewHolder(view: View) : RecyclerView.ViewHolder(view)
        override fun getItemCount(): Int {
            return messageUidList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val messageViewHolder = (holder as CustomMessageViewHolder).itemView

            getUserInfo(messageViewHolder, position)    // 대화 상대에 대한 정보 불러 오기
        }

        /** 상대방의 이름 정보를 가져와 적용 하는 함수 */
        private fun getUserInfo(messageViewHolder: View, position: Int){
            // 대화 하고 있는 사람이 있을 경우
            if (position < messageUidList.size){
                val otherUserId = messageUidList[position]

                // 대화 상대의 프로필 이미지 불러오기
                firestore?.collection("profileImage")
                    ?.document(otherUserId)
                    ?.get()
                    ?.addOnSuccessListener { querySnapshot ->
                        if (querySnapshot != null ){
                            val imageUrl = querySnapshot.getString("image")
                            Glide.with(this@MessageFragment).load(imageUrl).into(messageViewHolder.messageProfile)
                        }
                    }
                    ?.addOnFailureListener{ exception ->
                        Toast.makeText(activity, "프로필을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
                
                // 대화 상대의 이름 불러오기
                firestore?.collection("userinfo")
                    ?.document(otherUserId)
                    ?.collection("userinfo")
                    ?.document("detail")
                    ?.get()
                    ?.addOnSuccessListener { querySnapshot ->
                        if (querySnapshot != null) {
                            // userinfo collection 에서 user_name 필드의 값을 가져 오기
                            val userName = querySnapshot.getString("user_name")

                            // 가져온 user_name 의 값을 textView 에 적용, 채팅한 상대방 이름 표시
                            messageViewHolder.messageUserName.text = userName

                            // 가장 최근에 한 대화 불러 오기
                            getLatestChat(messageViewHolder, position, otherUserId)

                        } else {
                            // userinfo 에서 값이 존재 하지 않는 경우
                            messageViewHolder.messageUserName.text = "사용자 정보 없음"
                        }
                    }?.addOnFailureListener { exception ->
                        // 불러 오기 실패 시
                        messageViewHolder.messageUserName.text = "사용자 정보를 불러 오는데 실패하였습니다."
                    }
                // 대화 하고 있는 상대방이 없는 경우
            } else Toast.makeText(activity, "대화 상대가 없습니다.", Toast.LENGTH_SHORT).show()
        }

        /** 가장 최근에 대화한 내용을 불러 오는 함수 */
        private fun getLatestChat(messageViewHolder: View, position: Int, otherUserId : String){
            if (position < messageUidList.size){
                firestore?.collection("message")
                    ?.whereEqualTo("userId", userId)
                    ?.whereEqualTo("otherUserId", otherUserId)
                    ?.get()
                    ?.addOnSuccessListener { mainCollectionSnapshot ->
                        if (mainCollectionSnapshot != null && !mainCollectionSnapshot.isEmpty){

                            for (documentSnapshot in mainCollectionSnapshot.documents) {
                                val messageSubCollection = documentSnapshot.reference.collection("chat")

                                /** 서브 컬렉션 chat 에 접근, 가장 마지막으로 했던 대화를 보여 주도록 정렬 */
                                messageSubCollection
                                    .orderBy("timestamp", Query.Direction.DESCENDING)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener { chatSnapshot ->

                                        if (!chatSnapshot.isEmpty) {
                                            val lastChatDocument = chatSnapshot.documents[0]
                                            messageViewHolder.messageContent.text = lastChatDocument.getString("text")
                                            timeCalculate(messageViewHolder, lastChatDocument)

                                        } else {
                                            messageViewHolder.messageContent.text = " 대화가 없습니다."
                                        }

                                    }.addOnFailureListener{ exception ->
                                        // 오류 처리
                                        messageViewHolder.messageContent.text = " 대화를 불러오는데 실패하였습니다."
                                    }
                            }
                        }

                    }
                    ?.addOnFailureListener { exception ->
                        // 오류 처리
                        messageViewHolder.messageContent.text = "대화를 불러오는데 실패하였습니다."
                    }
            }
        }

        /** 몇 분 전 과 같은 시간 계산을 해주는 함수 */
        private fun timeCalculate(messageViewHolder: View, lastChatDocument: DocumentSnapshot) {

            // 저장 된 timestamp 값 불러 오기
            val firestoreTimestamp = lastChatDocument.getTimestamp("timestamp")?.toDate()

            // 현재 시간
            val currentTimeMillis = System.currentTimeMillis()

            // 시간 차이 계산(밀리 초)
            val timeDifferenceMillis = currentTimeMillis - firestoreTimestamp!!.time

            // 분 단위로 변환
            val minutes = (timeDifferenceMillis / (1000 * 60)).toInt()

            // 시간 단위로 변환
            val hours = minutes / 60

            // 해당 텍스트로 변환
            val timeText = when {
                minutes < 1 -> "방금 전"
                minutes == 1 -> "1분 전"
                minutes < 60 -> "$minutes 분 전"
                hours == 1 -> "1 시간 전"
                hours < 24 -> "$hours 시간 전"
                else -> "오래 전"
            }

            // 텍스트 설정
            messageViewHolder.messageTime.text = timeText
        }

    }
}