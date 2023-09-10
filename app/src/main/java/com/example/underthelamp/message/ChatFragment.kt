package com.example.underthelamp.message

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.underthelamp.MainActivity
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentChatBinding
import com.example.underthelamp.model.MessageDTO
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_chat.chatRecyclerView
import kotlinx.android.synthetic.main.item_chat_other.view.chatContent
import kotlinx.android.synthetic.main.item_chat_other.view.chatTime
import kotlinx.android.synthetic.main.item_date_line.view.dateText
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.Locale

class ChatFragment : Fragment() {
    lateinit var binding : FragmentChatBinding
    var firestore : FirebaseFirestore? = null
    var loginUserId : String? = null    // 로그인한 사용자에 대한 id
    var otherUserId : String? = null    // 상대방에 대한 id
    var messageIdList : String? = null  // 대화방 id
    lateinit var chatAdapter: ChatAdapter // ChatAdapter 인스턴스 지정
    lateinit var parentActivity: MainActivity

    // 메시지 보낸 사용자가 누군지 파악하기 위한 상수 & 날짜 구분선인지 메시지인지 파악하기 위한 상수
    companion object {
        private const val VIEW_TYPE_LOGIN = 1   // 로그인한 사용자 item 레이아웃 지정 상수
        private const val VIEW_TYPE_OTHER = 2   // 상대방 사용자 item 레이아웃 지정 상수
        private const val VIEW_TYPE_DATELINE = 3    // 날짜 구분선 item 레이아웃 지정 상수
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            parentActivity = context
        } else {
            throw IllegalArgumentException("상위 액티비티가 필요합니다.")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root

        firestore = FirebaseFirestore.getInstance()

        // 현재 로그인한 사용자 파악
        loginUserId = FirebaseAuth.getInstance().currentUser?.uid

        /** 해당 대화방 id 불러오기 */
        messageIdList = arguments?.getString("messageIdList")

        /** 상대방 사용자에 대한 정보 불러오기 */
        otherUserId = arguments?.getString("otherUserIdList")
        /** 불러온 정보 적용 */
        getUserInfo()

        /** chatRecyclerView에 대한 adapter와 layoutManager 지정 */
        chatAdapter = ChatAdapter()
        binding.chatRecyclerView.adapter = chatAdapter
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(activity)

        /** 메시지 전송 */
        binding.messageSendBtn.setOnClickListener {
            sendMessage()
        }

        // bottomNavigation 을 숨긴 후 키보드가 올라오도록 설정 (키보드 이벤트와 click 이벤트 충돌 방지) -> 딜레이로 인해 현재는 취소
//        binding.messageInput.setOnTouchListener { _, _ ->
//            parentActivity.hideNavigation(visible = false)
//            return@setOnTouchListener false
//        }

        // editText가 아닌 다른 화면을 눌렀을 경우
        binding.root.setOnTouchListener { _, _ ->
            //parentActivity.hideNavigation(visible = true)
            hideKeyboard()  // 키보드를 숨기는 함수
            true
        }

        // item 이 나타나는 recyclerView 에 대한 TouchEvent 감지
        binding.chatRecyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                //parentActivity.hideNavigation(visible = true)
                hideKeyboard()
                return false    // 이벤트가 처리 되었는지 여부 나타냄
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
        
        return view
    }

    /** 상대방의 이름 정보를 가져와 적용 하는 함수 */
    private fun getUserInfo(){
        // 대화 하고 있는 사람이 있을 경우
        if (otherUserId != null && otherUserId!!.isNotEmpty()){

            // 대화 상대의 프로필 이미지 불러오기
            firestore?.collection("profileImage")
                ?.document(otherUserId!!)
                ?.get()
                ?.addOnSuccessListener { querySnapshot ->
                    if (querySnapshot != null ){
                        val imageUrl = querySnapshot.getString("image")
                        Glide.with(this@ChatFragment).load(imageUrl).into(binding.otherUserProfile)
                    }
                }
                ?.addOnFailureListener{ exception ->
                    Toast.makeText(activity, "상대방의 프로필을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }

            // 대화 상대의 이름 불러오기
            firestore?.collection("userinfo")
                ?.document(otherUserId!!)
                ?.collection("userinfo")
                ?.document("detail")
                ?.get()
                ?.addOnSuccessListener { querySnapshot ->
                    if (querySnapshot != null) {
                        // userinfo collection 에서 user_name 필드의 값을 가져 오기
                        val userName = querySnapshot.getString("user_name")

                        // 가져온 user_name 의 값을 textView 에 적용, 채팅한 상대방 이름 표시
                        binding.otherUserName.text = userName

                    } else {
                        // userinfo 에서 값이 존재 하지 않는 경우
                        binding.otherUserName.text = "사용자 정보 없음"
                    }
                }?.addOnFailureListener { exception ->
                    // 불러 오기 실패 시
                    binding.otherUserName.text = "사용자 정보를 불러 오는데 실패하였습니다."
                }
            // 대화 하고 있는 상대방이 없는 경우
        } else Toast.makeText(activity, "대화 상대가 없습니다.", Toast.LENGTH_SHORT).show()
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

                            scrollRecyclerView(chatAdapter.itemCount)
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

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
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
        val messageText = binding.messageInput.text.toString().trim() // 입력 값 앞뒤 공백 제거
        val timestamp = com.google.firebase.Timestamp.now()
        val messageDTO = MessageDTO.Chat()

        // 대화 내용이 저장될 위치 지정
        val documentRef = firestore?.collection("message")
            ?.document(messageIdList!!)
            ?.collection("chat")
            ?.document()

        if (messageText.isEmpty()) {
            // 입력을 안한 상태
            Toast.makeText(activity, "메시지를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }


        // 대화한 사용자 id 추가
        messageDTO.uid = loginUserId

        // 보낸 메시지 내용 추가
        messageDTO.text = messageText

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

    /** 키보드를 숨기는 함수 */
    private fun hideKeyboard() {
        if (activity != null && requireActivity().currentFocus != null) {
            val inputManager: InputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    fun scrollRecyclerView(itemCount : Int ) {
        chatRecyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            chatRecyclerView.scrollToPosition(itemCount)
        }
    }
}