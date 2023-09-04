package com.example.underthelamp.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatFragment : Fragment() {
    lateinit var binding : FragmentChatBinding
    var firestore : FirebaseFirestore? = null
    var userId : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root

        firestore = FirebaseFirestore.getInstance()

        // 현재 로그인한 사용자 파악
        userId = FirebaseAuth.getInstance().currentUser?.uid

        /** chatRecyclerView에 대한 adapter와 layoutManager 지정 */
        binding.chatRecyclerView.adapter = ChatAdapter()
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(activity)

        return view
    }

    inner class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var chatList: ArrayList<String> = arrayListOf()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_list, parent)
            return CustomChatViewHolder(view)
        }

        inner class CustomChatViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return chatList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        }

//        init{
//            firestore?.collection("message")
//                ?.whereEqualTo("userId", userId)
//                ?.
//        }
    }
}