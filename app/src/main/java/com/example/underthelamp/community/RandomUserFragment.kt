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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import kotlinx.android.synthetic.main.activity_main.messageBtn
import kotlinx.android.synthetic.main.fragment_random_user.randomUserFrame
import kotlinx.android.synthetic.main.fragment_user_detail.view.back
import kotlinx.android.synthetic.main.modal_bottom_sheet_community.artLayout
import kotlinx.android.synthetic.main.modal_bottom_sheet_community.literatureLayout
import kotlinx.android.synthetic.main.modal_bottom_sheet_community.musicLayout
import kotlinx.android.synthetic.main.modal_bottom_sheet_community.publicMusicLayout
import kotlinx.android.synthetic.main.modal_bottom_sheet_community.theaterLayout
import kotlinx.android.synthetic.main.modal_bottom_sheet_community.videoLayout
import java.lang.IllegalArgumentException

class RandomUserFragment: Fragment() {

    lateinit var binding : FragmentRandomUserBinding
    var firestore : FirebaseFirestore? = null
    lateinit var parentActivity: MainActivity   // Activity 지정
    val selectCategory = mutableMapOf<Int, Boolean>()    // 카테고리 선택 여부 판단
    private val categoryFilter = ArrayList<String>()    // 선택한 카테고리가 저장될 리스트

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

        firestore?.collection("userinfo")
            ?.get()
            ?.addOnSuccessListener { mainCollectionSnapshot ->

                for (documentSnapshot in mainCollectionSnapshot) {

                    val subCollection = documentSnapshot.reference.collection("userinfo")

                    if (categoryFilter.isEmpty()){

                        firestore?.collection("userinfo")
                            ?.get()
                            ?.addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    val randomIndex = (0 until querySnapshot.size()).random()
                                    val document = querySnapshot.documents[randomIndex]

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

                    }else{
                        subCollection
                            .whereIn("user_category", categoryFilter)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    val randomIndex = (0 until querySnapshot.size()).random()
                                    val selectedUid = documentSnapshot.getString("uid")

                                    if (selectedUid != null) {
                                        firestore?.collection("userinfo")
                                            ?.document(selectedUid)
                                            ?.collection("userinfo")
                                            ?.document("detail")
                                            ?.get()
                                            ?.addOnSuccessListener { detailDocumentSnapshot ->
                                                val username = detailDocumentSnapshot.getString("user_name")
                                                userNameTextView.text = username

                                                loadRandomUserImage(selectedUid)
                                            }
                                    }

                                } else
                                    Toast.makeText(activity, "저장된 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                            ?.addOnFailureListener { exception ->
                                // 예외 시
                                Toast.makeText(activity, "유저 정보를 불러오는데 실패했습니다",Toast.LENGTH_SHORT).show()
                            }
                    }
                }
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
                        }
                    } else {
                        // 사용자가 올린 게시물이 없을 경우 기본 이미지로 설정
                        //binding.userPostImage.setImageResource(R.drawable.random_user_default)

                        parentActivity.randomUserImage("emptyImage") // 불러온 imageUrl 전달
                    }
                }
        }
    }

    /** 카테고리 필터 선택 BottomSheet 를 나오게 하는 함수 */
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

        // 각 View 에 대한 매핑 [Layout(View), Icon(ImageView), Text(TextView)]
        val categoryIdToResourceMap = mapOf(
            R.id.artLayout to Triple(R.id.artLayout, R.id.artIcon, R.id.artText),
            R.id.publicMusicLayout to Triple(R.id.publicMusicLayout, R.id.publicMusicIcon, R.id.publicMusicText),
            R.id.musicLayout to Triple(R.id.musicLayout, R.id.musicIcon, R.id.musicText),
            R.id.theaterLayout to Triple(R.id.theaterLayout, R.id.theaterIcon, R.id.theaterText),
            R.id.literatureLayout to Triple(R.id.literatureLayout, R.id.literatureIcon, R.id.literatureText),
            R.id.videoLayout to Triple(R.id.videoLayout, R.id.videoIcon, R.id.videoText),
            )

        // 선택된 View 에 대한 id array 지정
        val categoryIds = arrayOf(
            R.id.artLayout, R.id.publicMusicLayout, R.id.musicLayout,
            R.id.theaterLayout, R.id.literatureLayout, R.id.videoLayout
        )

        // categoryIds 배열 순회
        for (categoryId in categoryIds) {
            val categoryLayout = dialog.findViewById<View>(categoryId)

            // 누른 카테고리 뷰를 판단하여 색상 변경 및 이벤트 처리
            categoryLayout.setOnClickListener { view ->

                val resourceIds = categoryIdToResourceMap[categoryId]

                if (resourceIds != null) {
                    val (layoutId, iconId, textId) = resourceIds
                    val layout = dialog.findViewById<View>(layoutId)
                    val icon = dialog.findViewById<ImageView>(iconId)
                    val text = dialog.findViewById<TextView>(textId)

                    // 카테고리가 선택된 상태인지 확인
                    val isSelect = selectCategory[categoryId] ?: false

                    if (isSelect) {
                        // 선택된 상태라면, 원래 색상으로 설정
                        restoreCategoryColor(layout, icon, text)
                        selectCategory[categoryId] = false

                        // 필터링 해제, textView 의 text 불러와서 리스트에 저장
                        categoryFilter.remove(text.text.toString())
                    } else {
                        // 선택 안된 상태라면, 지정한 색상으로 설정
                        changeCategoryColor(layout, icon, text)
                        selectCategory[categoryId] = true
                        
                        // 필터링 적용, textView 의 text 불러와서 리스트에 저장 
                        categoryFilter.add(text.text.toString())
                    }
                }
            }
        }

        // Modal BottomSheet 표시
        dialog.show()
    }

    /** 지정한 색상으로 바꾸는 함수 */
    private fun changeCategoryColor(layout: View, icon : ImageView, text: TextView) {

        layout.setBackgroundResource(R.drawable.community_round_select)
        icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
        text.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

    }

    /** 원래 색상으로 바꾸는 함수 */
    private fun restoreCategoryColor(layout: View, icon : ImageView, text: TextView) {

        layout.setBackgroundResource(R.drawable.community_round)
        icon.clearColorFilter()
        text.setTextColor(ContextCompat.getColor(requireContext(), R.color.defaultColor))

    }
}