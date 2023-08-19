package com.example.underthelamp.community

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import com.example.underthelamp.databinding.FragmentRandomUserBinding
import com.google.firebase.firestore.FirebaseFirestore
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.underthelamp.R
import kotlinx.android.synthetic.main.fragment_random_user.randomUserFrame
import kotlinx.android.synthetic.main.fragment_user_detail.view.back

class RandomUserFragment: Fragment() {

    lateinit var binding : FragmentRandomUserBinding
    var firestore : FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        firestore = FirebaseFirestore.getInstance()
        binding = FragmentRandomUserBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var userPostImage = binding.userPostImage
        var userProfileImage = binding.userProfileImage

        /** ImageView 의 round 적용이 안되어 적용 하기 위한 코드 */
        userPostImage.background = resources.getDrawable(R.drawable.layout_round, null)
        userPostImage.clipToOutline = true

        userProfileImage.background = resources.getDrawable(R.drawable.radius, null)
        userProfileImage.clipToOutline = true

        loadRandomUser()
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

                    /** email 필드 에서 도메인 주소를 제외한 local-parts만 출력 */
                    val email = document.getString("email") ?: ""
                    val username = email.substringBefore('@')
                    userNameTextView.text = username

                    /** 랜덤으로 불러온 유저의 이미지를 불러 오기 위해 uid 저장 */
                    val uid = document.getString("uid")
                    loadRandomUserImage(uid)

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
                ?.whereEqualTo("uid", uid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot == null) return@addSnapshotListener

                    if (!querySnapshot.isEmpty) {
                        // 여러 개의 문서 중에서 첫 번째 문서를 가져옴
                        val document = querySnapshot.documents[0]
                        val imageUrl = document.getString("imageUrl")
                        if (imageUrl != null) {
                            Glide.with(this).load(imageUrl).into(binding.userPostImage)

                            val randomUserFrame = binding.randomUserFrame

                            Glide.with(this).asBitmap().load(imageUrl).into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    Palette.from(resource).generate { palette ->
                                        val dominantColor =
                                            palette?.getDominantColor(Color.WHITE) ?: Color.WHITE
                                        val shadowColors = arrayOf(
                                            ColorUtils.setAlphaComponent(dominantColor, 10),
                                            ColorUtils.setAlphaComponent(dominantColor, 9),
                                            ColorUtils.setAlphaComponent(dominantColor, 8),
                                            ColorUtils.setAlphaComponent(dominantColor, 7),
                                            ColorUtils.setAlphaComponent(dominantColor, 6),
                                            ColorUtils.setAlphaComponent(dominantColor, 5),
                                            ColorUtils.setAlphaComponent(dominantColor, 4),
                                            ColorUtils.setAlphaComponent(dominantColor, 3),
                                            ColorUtils.setAlphaComponent(dominantColor, 2),
                                            ColorUtils.setAlphaComponent(dominantColor, 1)
                                        )
                                        // 그림자의 색상을 변경
                                        val layerDrawable =
                                            randomUserFrame.background as? LayerDrawable
                                        if (layerDrawable != null && layerDrawable.numberOfLayers >= 10) {
                                            for (i in 0 until 10) {
                                                val shapeDrawable =
                                                    layerDrawable.getDrawable(i) as? GradientDrawable
                                                shapeDrawable?.setColor(shadowColors[i])
                                            }
                                        }
                                    }
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    // 작업 미수행
                                }
                            })
                        }
                    } else {
                        Toast.makeText(activity, "사용자가 올린 게시물이 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}