package com.example.underthelamp.message

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentChatBinding
import com.example.underthelamp.model.MessageDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.Date
import kotlinx.android.synthetic.main.item_chat_other.view.chatContent
import kotlinx.android.synthetic.main.item_chat_other.view.chatTime
import kotlinx.android.synthetic.main.item_date_line.view.dateText
import java.text.SimpleDateFormat
import java.util.Locale

class ChatFragment : Fragment() {
    lateinit var binding : FragmentChatBinding
    var firestore : FirebaseFirestore? = null
    var loginUserId : String? = null
    var messageIdList : String? = null
    var currentDateLineSet = false  // 현재 날짜 구분선이 추가되었는지 파악하는 함수

    // 메시지 보낸 사용자가 누군지 파악하기 위한 상수 & 날짜 구분선인지 메시지인지 파악하기 위한 상수
    companion object {
        private const val VIEW_TYPE_LOGIN = 1   // 로그인한 사용자 item 레이아웃 지정 상수
        private const val VIEW_TYPE_OTHER = 2   // 상대방 사용자 item 레이아웃 지정 상수
        private const val VIEW_TYPE_DATELINE = 3    // 날짜 구분선 item 레이아웃 지정 상수
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root

        firestore = FirebaseFirestore.getInstance()

        // 현재 로그인한 사용자 파악
        loginUserId = FirebaseAuth.getInstance().currentUser?.uid

        /** 해당 대화방 id 불러오기 */
        messageIdList = arguments?.getString("messageIdList")

        /** chatRecyclerView에 대한 adapter와 layoutManager 지정 */
        binding.chatRecyclerView.adapter = ChatAdapter()
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(activity)

        /** 메시지 전송 */
        binding.messageSendBtn.setOnClickListener {
            sendMessage()
        }
        
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    inner class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var chats : ArrayList<MessageDTO.Chat> = arrayListOf()

        init {
            FirebaseFirestore.getInstance()
                .collection("message")
                .document(messageIdList!!)
                .collection("chat")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    chats.clear()
                    if(querySnapshot == null) return@addSnapshotListener
                    
                    var lastDate: String? = null // 마지막으로 날짜 구분선을 처리한 날짜(중복 생성 방지)

                    for(snapshot in querySnapshot.documents!!){
                        //chats.add(snapshot.toObject(MessageDTO.Chat::class.java)!!)
                        val chat = snapshot.toObject(MessageDTO.Chat::class.java)
                        if (chat != null) {
                            val timestamp = chat.timestamp?.toDate()
                            val currentDate = formatDate(timestamp)

                            // 메시지의 날짜가 바뀌었을 때 날짜 구분선 추가
                            if (lastDate != currentDate) {  // 날짜 구분선이 생성되어 있지 않다면
                                val dateLineChat = MessageDTO.Chat()
                                dateLineChat.dateLine = true    // 날짜 구분선임을 나타냄
                                dateLineChat.timestamp = chat.timestamp // 날짜 구분선이 생긴 시간
                                chats.add(dateLineChat)
                                lastDate = currentDate
                            }
                            
                            chats.add(chat)
                        }
                    }
                    notifyDataSetChanged()
                }
        }

        // View가 현재 로그인한 사용자가 보낸 메시지인지, 상대방 사용자의 메시지인지, 날짜 구분선인지 판단하는 함수
        override fun getItemViewType(position: Int): Int {
            return if (chats[position].uid == loginUserId){   // 로그인한 사용자 일 경우
                VIEW_TYPE_LOGIN
            } else if (chats[position].dateLine == true ){ // 날짜 구분선인 경우
                VIEW_TYPE_DATELINE
            } else {    // 로그인 사용자가 아닌 경우, 상대방 메시지로 처리
                VIEW_TYPE_OTHER
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            // 뷰 타입에 대해 return
            return when (viewType) {
                VIEW_TYPE_DATELINE -> {
                    val dateView = LayoutInflater.from(parent.context).inflate(R.layout.item_date_line, parent, false)
                    CustomChatViewHolder(dateView)
                }
                VIEW_TYPE_LOGIN -> {
                    val loginView = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_login, parent, false)
                    CustomChatViewHolder(loginView)
                }
                else -> {
                    val otherView = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_other, parent, false)
                    CustomChatViewHolder(otherView)
                }
            }
        }

        inner class CustomChatViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return chats.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var userChatView = (holder as CustomChatViewHolder).itemView

            if (getItemViewType(position) != VIEW_TYPE_DATELINE){
                // 메시지 보낸 시간 불러오기
                timeCalculate(userChatView, chats, position)

                // 메시지 내용 불러오기
                userChatView.chatContent.text = chats[position].text
            } else {
                // 날짜 구분선 불러오기
                val timestamp = chats[position].timestamp?.toDate()
                val formattedDate = timestamp?.let { formatDate(timestamp) }
                userChatView.dateText.text = formattedDate.toString()   // 날짜 구분선에 나타날 날짜 지정
            }
        }

        /** 원하는 날짜 형태로 바꾸는 함수 */
        private fun formatDate(date: java.util.Date?): String {
            val format = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            return format.format(date)
        }

    }

    /** messageInput 에서 입력한 값을 저장하는 함수 */
    private fun sendMessage() {
        val timestamp = com.google.firebase.Timestamp.now()
        val messageDTO = MessageDTO.Chat()

        // 대화 내용이 저장될 위치 지정
        val documentRef = firestore?.collection("message")
            ?.document(messageIdList!!)
            ?.collection("chat")
            ?.document()

        // 대화한 사용자 id 추가
        messageDTO.uid = loginUserId

        // 보낸 메시지 내용 추가
        messageDTO.text = binding.messageInput.text.toString()

        // 메시지 보낸 시간 추가
        messageDTO.timestamp = timestamp

        // 추가된 내용들을 firestore 에 저장
        documentRef?.set(messageDTO)

        // 메시지 보낸 후 EditText 초기화
        binding.messageInput.setText("")

    }

    /** 몇 분 전 과 같은 시간 계산을 해주는 함수 */
    private fun timeCalculate(chatView: View, chats: ArrayList<MessageDTO.Chat>, position: Int) {

        if (position >= 0 && position < chats.size){
            // 저장 된 timestamp 값 불러 오기
            val chatTimestamp = chats[position].timestamp?.toDate()

            // 현재 시간
            val currentTimeMillis = System.currentTimeMillis()

            // 시간 차이 계산(밀리 초)
            val timeDifferenceMillis = currentTimeMillis - chatTimestamp!!.time

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

            // 메시지 텍스트 설정
            chatView.chatTime.text = timeText
        }
    }
}