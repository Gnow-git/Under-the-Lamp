package com.example.underthelamp.home

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.underthelamp.R
import com.example.underthelamp.model.AlarmDTO
import com.example.underthelamp.model.ContentDTO
import com.example.underthelamp.navigation.CommentActivity
import com.example.underthelamp.navigation.util.FcmPush
import com.example.underthelamp.user.UserFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.fragment_user_detail.view.back
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()
        init {

            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                if(querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
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
            return contentDTOs.size
        }

        /**
         * 유저의 작품을 보여주는 ViewHolder
         */
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            var viewholder = (holder as CustomViewHolder).itemView

            // UserId

            firestore!!.collection("userinfo")
                .document(contentDTOs!![position].userId.toString()).collection("userinfo").document("detail")
                .get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("user_name")
                        // viewholder.detailviewitem_profile_textview.text = contentDTOs!![position].userId // 이메일
                        viewholder.detailUserName.text = userName
                    } else {
                        viewholder.detailUserName.text = "이름을 불러올 수 없습니다."
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "데이터 가져오기 실패: ", exception)
                    viewholder.detailUserName.text  = "데이터를 가져오는 중에 오류가 발생하였습니다."
                }


            // Image
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.detailImage)

            // 댓글단 유저 이름 fragment로 추가해야함

            // 작성한 내용
            // viewholder.comment_user.text = contentDTOs!![position].explain

            // likes
            viewholder.detailLikeCount.text = "Likes " + contentDTOs!![position].favoriteCount

            // ProfileImage -> 나중에 프로필 이미지 불러오는 것으로 수정해야함
            //Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.detailviewitem_profile_image)
            viewholder.detailProfileImage.background = viewholder.resources.getDrawable(R.drawable.radius, null)
            viewholder.detailProfileImage.clipToOutline = true


            // This code is when the button is clicked
            viewholder.likeBtn.setOnClickListener {
                favoriteEvent(position)
            }

            // This code is when the page is loaded
            if(contentDTOs!![position].favorites.containsKey(uid)){
                // 좋아요를 누른 경우
                val tintColor = ContextCompat.getColorStateList(requireContext(), R.color.selectColor)
                viewholder.likeBtn.backgroundTintList = tintColor
            } else{
                // 좋아요를 안 누른 경우
                viewholder.likeBtn.setBackgroundResource(R.drawable.like_icon)
            }
            // This code is when the profile image is clicked
            viewholder.detailProfileImage.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].contentUid)
                bundle.putString("userId",contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
            }
            viewholder.commentBtn.setOnClickListener { v ->
                var intent = Intent(v.context, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationUid", contentDTOs[position].contentUid)
                startActivity(intent)
            }

            viewholder.detailMenuBtn.setOnClickListener{
                // 상세 메뉴 버튼을 누를 경우 하단 BottomSheet 나타나게하기
                showModalBottomSheet()
            }
        }

        fun favoriteEvent(position : Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)){
                    // When the button is clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! - 1
                    contentDTO?.favorites?.remove(uid)
                } else {
                    // When the button is not clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! + 1
                    contentDTO?.favorites?.set(uid!!, true)
                    favoriteAlarm(contentDTOs[position].contentUid!!)
                }
                transaction.set(tsDoc, contentDTO)
            }
        }

        fun favoriteAlarm(destinationUid : String){
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid    // 상대방 uid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

            // favorite push alarm
            var message = FirebaseAuth.getInstance()?.currentUser?.email + getString(R.string.alarm_favorite)
            FcmPush.instance.sendMessage(destinationUid, "Under_the_Lamp", message)
        }
        
        // 하단 BottomSheet 나오게하는 함수
        private fun showModalBottomSheet() {
            val dialog: Dialog = Dialog(requireContext())
            dialog.requestWindowFeature(FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.modal_bottom_sheet)

            //val dismissButton: Button = dialog.findViewById(R.id.followBtn)

//            dismissButton.setOnClickListener {
//                dialog.dismiss()
//            }
//
            // Modal BottomSheet 크기
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            
            // Modal BottomSheet 의 background를 제외한 부분은 투명
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
            dialog.window?.setGravity(Gravity.BOTTOM)

            // Modal BottomSheet 표시
            dialog.show()
        }

    }

}