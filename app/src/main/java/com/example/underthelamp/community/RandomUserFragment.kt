package com.example.underthelamp.community

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import com.example.underthelamp.databinding.FragmentRandomUserBinding
import com.google.firebase.firestore.FirebaseFirestore
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.underthelamp.MainActivity
import com.example.underthelamp.R
import kotlinx.android.synthetic.main.fragment_random_user.randomUserFrame
import kotlinx.android.synthetic.main.fragment_user_detail.view.back
import java.lang.IllegalArgumentException

class RandomUserFragment: Fragment() {

    lateinit var binding : FragmentRandomUserBinding
    var firestore : FirebaseFirestore? = null
    lateinit var parentActivity: MainActivity   // Activity 지정

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            parentActivity = context    // Activity 초기화
        } else {
            throw IllegalArgumentException("상위 액티비티가 필요합니다.")
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        firestore = FirebaseFirestore.getInstance()
        binding = FragmentRandomUserBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //var userPostImage = binding.userPostImage
        var userProfileImage = binding.userProfileImage

//        /** ImageView 의 round 적용이 안되어 적용 하기 위한 코드 */
//        userPostImage.background = resources.getDrawable(R.drawable.layout_round, null)
//        userPostImage.clipToOutline = true

        userProfileImage.background = resources.getDrawable(R.drawable.radius, null)
        userProfileImage.clipToOutline = true

        loadRandomUser()

        /** 주사위 버튼을 눌렀을 경우 */
        binding.randomDice.setOnClickListener {
            loadRandomUser()
        }

        /** 카테고리 선택 버튼을 눌렀을 경우 */
        binding.selectCategory.setOnClickListener {
            showModalBottomSheet()
        }
    }

    /** 유저의 정보를 랜덤 으로 보여 주는 함수 */
    private fun loadRandomUser() {
        val userNameTextView = binding.userName

        // 현재는 테스트 용으로 email 정보를 불러 오도록 임시 작성, 추후 DB 수정 후 name 으로 불러 오도록 수정 예정
        firestore?.collection("userinfo")
            ?.get()
            ?.addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val randomIndex = (0 until querySnapshot.size()).random()
                    val document = querySnapshot.documents[randomIndex]

                    /** email 필드 에서 도메인 주소를 제외한 local-parts만 출력 현재는 미사용 */
//                    val email = document.getString("email") ?: ""
//                    val emailLocal = email.substringBefore('@')

                    val uid = document.getString("uid")
                    if (uid != null) {
                        firestore?.collection("userinfo")
                            ?.document(uid)
                            ?.collection("userinfo")
                            ?.document("detail")
                            ?.get()
                            ?.addOnSuccessListener { detailDocumentSnapshot ->

                                /** 유저의 이름을 출력 데이터가 없을 경우 기본 값 사용*/
                                val username = detailDocumentSnapshot
                                    .getString("user_name") ?: "Unkown"
                                userNameTextView.text = username

                                /** 랜덤으로 불러온 유저의 이미지를 불러 오기 위한 함수 */
                                loadRandomUserImage(uid)
                            }
                    }
                }
            }
            ?.addOnFailureListener { exception ->
                // 예외 시
                Toast.makeText(activity, "유저 정보를 불러오는데 실패했습니다",Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadRandomUserImage(uid: String?) {

        if (uid != null) {
            firestore?.collection("images")
                ?.whereEqualTo("userId", uid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot == null) return@addSnapshotListener

                    if (!querySnapshot.isEmpty) {
                        // 여러 개의 문서 중에서 첫 번째 문서를 가져옴
                        val document = querySnapshot.documents[0]
                        val imageUrl = document.getString("imageUrl")
                        if (imageUrl != null) {
                            parentActivity.randomUserImage(imageUrl = imageUrl) // 불러온 imageUrl 전달
                            //Glide.with(this).load(imageUrl).into(binding.userPostImage)

//                            val randomUserFrame = binding.randomUserFrame

//                            Glide.with(this).asBitmap().load(imageUrl).into(object : CustomTarget<Bitmap>() {
//                                override fun onResourceReady(
//                                    resource: Bitmap,
//                                    transition: Transition<in Bitmap>?
//                                ) {
//                                    Palette.from(resource).generate { palette ->
//                                        val dominantColor =
//                                            palette?.getDominantColor(Color.WHITE) ?: Color.WHITE
//                                        val shadowColors = arrayOf(
//                                            ColorUtils.setAlphaComponent(dominantColor, 10),
//                                            ColorUtils.setAlphaComponent(dominantColor, 9),
//                                            ColorUtils.setAlphaComponent(dominantColor, 8),
//                                            ColorUtils.setAlphaComponent(dominantColor, 7),
//                                            ColorUtils.setAlphaComponent(dominantColor, 6),
//                                            ColorUtils.setAlphaComponent(dominantColor, 5),
//                                            ColorUtils.setAlphaComponent(dominantColor, 4),
//                                            ColorUtils.setAlphaComponent(dominantColor, 3),
//                                            ColorUtils.setAlphaComponent(dominantColor, 2),
//                                            ColorUtils.setAlphaComponent(dominantColor, 1)
//                                        )
//                                        // 그림자의 색상을 변경
//                                        val layerDrawable =
//                                            randomUserFrame.background as? LayerDrawable
//                                        if (layerDrawable != null && layerDrawable.numberOfLayers >= 10) {
//                                            for (i in 0 until 10) {
//                                                val shapeDrawable =
//                                                    layerDrawable.getDrawable(i) as? GradientDrawable
//                                                shapeDrawable?.setColor(shadowColors[i])
//                                            }
//                                        }
//                                    }
//                                }
//
//                                override fun onLoadCleared(placeholder: Drawable?) {
//                                    // 작업 미수행
//                                }
//                            })
                        }
                    } else {
                        // 사용자가 올린 게시물이 없을 경우 기본 이미지로 설정
                        //binding.userPostImage.setImageResource(R.drawable.random_user_default)

                        parentActivity.randomUserImage("emptyImage") // 불러온 imageUrl 전달
                    }
                }
        }
    }

    // 하단 BottomSheet 나오게하는 함수
    private fun showModalBottomSheet() {
        val dialog: Dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_bottom_sheet_community)

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