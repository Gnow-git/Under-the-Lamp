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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentDetailViewBinding
import com.example.underthelamp.model.AlarmDTO
import com.example.underthelamp.model.ContentDTO
import com.example.underthelamp.navigation.CommentActivity
import com.example.underthelamp.navigation.util.FcmPush
import com.example.underthelamp.user.UserFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail_view.view.detailRecyclerView
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment() {

    lateinit var binding : FragmentDetailViewBinding
    var firestore : FirebaseFirestore? = null
    var loginUid : String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDetailViewBinding.inflate(inflater, container, false)
        var view = binding.root

        firestore = FirebaseFirestore.getInstance()
        loginUid = FirebaseAuth.getInstance().currentUser?.uid

        /** detailRecyclerView 에 대한 adapter 와 layoutManager 지정 */
        view.detailRecyclerView.adapter = DetailViewRecyclerViewAdapter()
        view.detailRecyclerView.layoutManager = LinearLayoutManager(activity)
        return view
    }

    /** detailRecyclerView 에 대한 adapter */
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

        /** 유저의 작품을 보여 주는 ViewHolder */
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            var DetailViewHolder = (holder as CustomViewHolder).itemView

            /** 게시물 작성한 유저의 정보 불러오기 */
            getDetailViewUserInfo(DetailViewHolder, position)

            // 게시물 이미지 불러오기
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(DetailViewHolder.detailImage)

            // 좋아요 개수 불러오기
            DetailViewHolder.detailLikeCount.text = "Likes " + contentDTOs!![position].favoriteCount

            /** 좋아요 버튼 누를 경우 */
            DetailViewHolder.likeBtn.setOnClickListener {
                favoriteEvent(loginUid.toString(), position)
            }

            if(contentDTOs!![position].favorites.containsKey(loginUid)){

                // 좋아요를 한 상태인 경우
                DetailViewHolder.likeBtn.setBackgroundResource(R.drawable.like_icon)

            } else{
                // 좋아요를 안한 상태인 경우
                DetailViewHolder.likeBtn.setBackgroundResource(R.drawable.like_icon_default)
            }
            
            // 게시물의 프로필 사진을 누를 경우
            DetailViewHolder.detailProfileImage.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].contentUid)
                bundle.putString("userId",contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
            }

            DetailViewHolder.commentBtn.setOnClickListener { v ->
                var intent = Intent(v.context, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationUid", contentDTOs[position].contentUid)
                startActivity(intent)
            }

            DetailViewHolder.detailMenuBtn.setOnClickListener{
                // 상세 메뉴 버튼을 누를 경우 하단 BottomSheet 나타나게하기
                showModalBottomSheet()
            }
        }

        /** 게시물을 올린 사람의 정보를 불러 오는 함수 */
        private fun getDetailViewUserInfo(DetailViewHolder: View, position: Int){

            // 게시물 작성한 유저의 프로필 이미지 불러오기
            firestore!!.collection("profileImage")
                .document(contentDTOs[position].userId.toString())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot != null) {
                        val imageUrl = querySnapshot.getString("image")
                        Glide.with(DetailViewHolder.context).load(imageUrl).into(DetailViewHolder.detailProfileImage)
                        DetailViewHolder.detailProfileImage.background = DetailViewHolder.resources.getDrawable(R.drawable.radius, null)
                        DetailViewHolder.detailProfileImage.clipToOutline = true
                    } else{
                        // 프로필 이미지가 없는 경우, 기본 이미지로 설정
                        Glide.with(DetailViewHolder.context).load(R.drawable.profile_default).into(DetailViewHolder.detailProfileImage)
                        DetailViewHolder.detailProfileImage.background = DetailViewHolder.resources.getDrawable(R.drawable.radius, null)
                        DetailViewHolder.detailProfileImage.clipToOutline = true
                    }


                } // 프로필 이미지 불러오지 못했을 경우
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "프로필 이미지 불러오기 실패: ", exception)
                }


            // 게시물을 올린 사용자의 이름 불러오기
            firestore!!.collection("userinfo")
                .document(contentDTOs[position].userId.toString()).collection("userinfo").document("detail")
                .get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("user_name")
                        DetailViewHolder.detailUserName.text = userName
                    } else {
                        DetailViewHolder.detailUserName.text = "이름을 불러올 수 없습니다."
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "데이터 가져오기 실패: ", exception)
                    DetailViewHolder.detailUserName.text  = "데이터를 가져오는 중에 오류가 발생하였습니다."
                }
        }

        fun favoriteEvent(loginUid : String , position : Int){
            val contentDTO = contentDTOs?.get(position)

            if (contentDTO?.contentUid != null) {
                val contentRef = firestore?.collection("images")?.document(contentDTO.contentUid!!)

                firestore?.runTransaction { transaction ->
                    val contentDoc = transaction.get(contentRef!!)
                    
                    if (contentDoc.exists()) {
                        val updateContentDTO = contentDoc.toObject(ContentDTO::class.java)
                        
                        if (updateContentDTO != null) {
                            if (updateContentDTO.favorites.containsKey(loginUid)) {
                                // 하트 버튼이 눌려져 있는 경우
                                updateContentDTO.favoriteCount = updateContentDTO.favoriteCount - 1
                                updateContentDTO.favorites.remove(loginUid)
                            } else {
                                // 하트 버튼이 안눌려져 있는 경우
                                updateContentDTO.favoriteCount = updateContentDTO.favoriteCount + 1
                                updateContentDTO.favorites[loginUid] = true
                                favoriteAlarm(contentDTO.contentUid!!)
                            }

                            // 변경된 내용을 Firestore 에 적용
                            transaction.set(contentRef, updateContentDTO)
                        }
                    }
                }
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