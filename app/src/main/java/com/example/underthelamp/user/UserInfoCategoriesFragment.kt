package com.example.underthelamp.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentUserInfoCatergoriesBinding
import com.example.underthelamp.navigation.model.UserCategoryDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserInfoCategoriesFragment : Fragment() {

    private lateinit var binding: FragmentUserInfoCatergoriesBinding
    private lateinit var mAuth: FirebaseAuth
    private val db = Firebase.firestore
    private val userinfo = db.collection("userinfo")
    private lateinit var userCategoryDTO: UserCategoryDTO

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUserInfoCatergoriesBinding.inflate(inflater, container, false)

        // 인증 초기화
        mAuth = Firebase.auth

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var userCategory: String = "카테고리"
        var userGoal : String = "목적"
        var userExperience : String = "경험"

        val categoryViews = listOf(binding.cateArts, binding.catePublicMusic, binding.cateMusic, binding.catePlay, binding.cateLiterature)
        val goalViews = listOf(binding.button, binding.button2, binding.button3, binding.button4, binding.button5, binding.button6, binding.button7)
        val experienceView = listOf(binding.button8, binding.button9, binding.button10, binding.button11, binding.button12,)
        val color_se = ContextCompat.getColor(requireContext(), R.color.selectColor)
        val color_un = ContextCompat.getColor(requireContext(), R.color.defaultColor)

        categoryViews.forEach { view ->
            view.setOnClickListener {
                categoryViews.forEach { categoryViews ->
                    if (categoryViews == view) {
                        // 뷰가 선택될 경우
                        when (categoryViews) {
                            binding.cateArts -> userCategory = "미술"
                            binding.catePublicMusic -> userCategory = "대중음악"
                            binding.cateMusic -> userCategory = "음악"
                            binding.catePlay -> userCategory = "연극"
                            binding.cateLiterature -> userCategory = "문학"
                        }
                    } else {
                        // 선택한 뷰의 나머지
                    }
                }
            }
        }

        goalViews.forEach { view ->
            view.setOnClickListener {
                goalViews.forEach { goalViews ->
                    if (goalViews == view) {
                        goalViews.setTextColor(color_se) // 선택된 뷰의 색상 변경
                        goalViews.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.button_round_se)
                        when (goalViews) {
                            binding.button -> userGoal = "공모전"
                            binding.button2 -> userGoal = "실무활용"
                            binding.button3 -> userGoal = "자기개발"
                            binding.button4 -> userGoal = "면접준비"
                            binding.button5 -> userGoal = "포트폴리오"
                            binding.button6 -> userGoal = "컨텐츠 제작"
                            binding.button7 -> userGoal = "학업과제"
                        }
                    } else {
                        goalViews.setTextColor(color_un)   // 원래대로
                        goalViews.setBackgroundResource(R.drawable.button_round)
                    }
                }
            }
        }

        experienceView.forEach { view ->
            view.setOnClickListener {
                experienceView.forEach { experienceView ->
                    if (experienceView == view) {
                        experienceView.setTextColor(color_se) // 선택된 뷰의 색상 변경
                        experienceView.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.button_round_se)
                        when (experienceView) {
                            binding.button8 -> userExperience = "전무"
                            binding.button9 -> userExperience = "입문자"
                            binding.button10 -> userExperience = "중급자"
                            binding.button11 -> userExperience = "전공자"
                            binding.button12 -> userExperience = "실무경험"
                        }
                    } else {
                        experienceView.setTextColor(color_un)   // 원래대로
                        experienceView.setBackgroundResource(R.drawable.button_round)
                    }
                }
            }
        }
        // 화살표 버튼 클릭 이벤트 처리
        binding.next.setOnClickListener {
            val userDetailFragment = UserDetailFragment()
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentFrame, userDetailFragment)
                addToBackStack(null)
                commit()
            }
            saveUserInfoCategoryDTO(userCategory, userGoal, userExperience)
        }
    }

    private fun saveUserInfoCategoryDTO(userCategory : String, userGoal : String, userExperience : String) {

        userCategoryDTO = UserCategoryDTO(userCategory, userGoal, userExperience)

        // Firestore에 DTO 저장
        userinfo.document(getCurrentUserUid()).collection("userinfo").document("category").set(userCategoryDTO)
            .addOnSuccessListener {
                // 성공적으로 저장된 경우 처리할 로직 작성
            }
            .addOnFailureListener {
                // 저장 실패 시 처리할 로직 작성
            }
    }

    private fun getCurrentUserUid(): String {
        // 현재 사용자의 UID 반환 (Firebase Auth 사용)
        return mAuth.currentUser?.uid ?: ""
    }
}