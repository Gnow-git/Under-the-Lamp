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
import com.example.underthelamp.databinding.FragmentInformationBinding
import com.example.underthelamp.databinding.FragmentMessageBinding
import com.example.underthelamp.model.MessageDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.item_message.messageUserName
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

            // 대화 하고 있는 사람이 있을 경우
            if (position < messageUidList.size){
                val otherUserId = messageUidList[position]

                // 상대방 정보를 가져 오기 위해 firestore 에 접근
                firestore?.collection("userinfo")
                    ?.document(otherUserId)
                    ?.collection("userinfo")
                    ?.document("detail")
                    ?.get()
                    ?.addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot != null) {
                            // userinfo collection 에서 user_name 필드의 값을 가져 오기
                            val userName = documentSnapshot.getString("user_name")

                            // 가져온 user_name 의 값을 textView 에 적용
                            messageViewHolder.messageUserName.text = userName
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

    }
}