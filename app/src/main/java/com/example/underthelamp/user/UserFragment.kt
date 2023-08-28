package com.example.underthelamp.user

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.underthelamp.MainActivity
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentUserBinding
import com.example.underthelamp.model.AlarmDTO
import com.example.underthelamp.model.ContentDTO
import com.example.underthelamp.model.FollowDTO
import com.example.underthelamp.navigation.util.FcmPush
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment() {

    lateinit var binding : FragmentUserBinding
    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var userId : String? = null
    var auth : FirebaseAuth? = null
    var currentUserUid : String? = null // 상대방의 계정
    companion object {
        var PICK_PROFILE_FROM_ALBUM = 10
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUserBinding.inflate(inflater, container, false)
        userId = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid

        if(userId == currentUserUid){
            // MyPage
            binding.accountBtnFollowSignout.text = getString(R.string.signout)

            binding.accountBtnFollowSignout.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }
        } else {
            // OtherUserPage
            binding.accountBtnFollowSignout.text = "Follow"
            var mainActivity = (activity as MainActivity)

            firestore!!.collection("userinfo")
                .document(userId.toString()).collection("userinfo").document("detail")
                .get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("user_name")
                        binding.userName.text = userName
                    } else {
                        binding.userName.text = "이름을 불러올 수 없습니다."
                    }
                }
                .addOnFailureListener { exception ->
                     Log.d(TAG, "데이터 가져오기 실패: ", exception)
                    binding.userName.text  = "데이터를 가져오는 중에 오류가 발생하였습니다."
                }

            binding.accountBtnFollowSignout.setOnClickListener{
                requestFollow()
            }
        }

        binding.accountRecyclerview.adapter = UserFragmentRecyclerViewAdapter()
        binding.accountRecyclerview.layoutManager = GridLayoutManager(requireActivity(), 3)
 
        // 사용자의 profile 이미지 설정
        binding.userProfileImage.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }

        getProfileImage()
        getFollowerAndFollowing()

        return binding.root
    }

    fun getFollowerAndFollowing() {
        val context = requireContext()  // Fragment를 context에 연결
        firestore?.collection("users")?.document(userId!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO?.followingCount != null){
                binding.followerCount.text = followDTO.followingCount.toString()
            }
            if(followDTO?.followerCount != null){
                binding.followerCount.text = followDTO.followerCount.toString()

                // 팔로우하고 있는 경우 버튼 취소로
                if (followDTO?.followers?.containsKey(currentUserUid!!) == true){
                    binding.accountBtnFollowSignout.text = context.getString(R.string.follow_cancel)
                    if(isAdded()){  // UserFragment가 Activity에 연결되어 있는지 확인
                        binding.accountBtnFollowSignout.background.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
                    }
                } else {    // 팔로우 안하고 있는 경우 버튼 팔로우로
                    if(userId != currentUserUid){
                        binding.accountBtnFollowSignout.text = context.getString(R.string.follow)
                        binding.accountBtnFollowSignout.background.colorFilter = null
                    }
                }

            }
        }
    }
    fun requestFollow() {
        // Save data to my account
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                followDTO = FollowDTO() // 모델 생성
                followDTO!!.followingCount = 1
                followDTO!!.followers[userId!!] = true // 중복 방지를 위해 uid

                transaction.set(tsDocFollowing, followDTO)  // DB에 데이터 등록
                return@runTransaction
            }

            if (followDTO.followings.containsKey(userId)) {  // 팔로우한 경우
                // following 취소
                followDTO?.followingCount = followDTO?.followingCount!! - 1
                followDTO?.followings?.remove(userId)
            } else {    // 팔로우 안한 경우
                // following 실행
                followDTO?.followingCount = followDTO?.followingCount!! + 1
                followDTO?.followings?.set(userId!!, true)
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }
        // 내가 팔로우할 상대방 계정에 접근
        var tsDocFollower = firestore?.collection("users")?.document(userId!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower!!)
                .toObject(FollowDTO::class.java)    // followDTO 값을 읽어옴
            if (followDTO == null) {  // 불러올 값이 없다면
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true // 상대방 계정에 나의 uid 등록
                followerAlarm(userId!!)    // 최초로 팔로우 할때 이벤트
                transaction.set(tsDocFollower, followDTO!!)    // DB에 저장
                return@runTransaction
            }

            if (followDTO!!.followers.containsKey(currentUserUid)) {
                // 상대방 계정에 팔로우를 했을 경우
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid)    // 나의 uid 삭제
            } else {
                // 상대방 계정에 팔로우를 안했을 경우 팔로워 증가
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true // 나의 uid 추가
                followerAlarm(userId!!)
            }
            transaction.set(tsDocFollower, followDTO!!) // DB에 저장
            return@runTransaction
        }
    }
    fun followerAlarm(destinationUid : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser?.email
        alarmDTO.uid = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        // follow push alarm
        var message = auth?.currentUser?.email + getString(R.string.alarm_follow)
        FcmPush.instance.sendMessage(destinationUid, "Under_the_Lamp", message)
    }
    fun getProfileImage(){  // ProfileImage 받아옴
        firestore?.collection("profileImage")?.document(userId!!)?.addSnapshotListener{ documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            if(documentSnapshot.data != null){
                // null이 아닐 경우 이미지 주소를 받아옴
                var url = documentSnapshot?.data!!["image"]
                Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop()).into(binding.userProfileImage!!)
            }
        }
    }
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init {
            firestore?.collection("images")?.whereEqualTo("userId",userId)?.addSnapshotListener { querySnapshots, firebaseFirestoreException ->
                if(querySnapshots == null) return@addSnapshotListener

                for(snapshot in querySnapshots.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java )!!)
                }
                fragmentView?.postCount?.text = contentDTOs.size.toString()
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            var imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(105, 105)
            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(
                RequestOptions().centerCrop()).into(imageview)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}
