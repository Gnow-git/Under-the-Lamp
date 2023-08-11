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

        val categoryViews = listOf(binding.categoriesArt, binding.categoriesPublicMusic, binding.categoriesMusic, binding.categoriesTheater, binding.categoriesLiterature)
        val goalViews = listOf(binding.contestBtn, binding.practicalUseBtn, binding.selfDevelopmentBtn, binding.interviewBtn, binding.portfolioBtn, binding.contentBtn, binding.homeworkBtn)
        val experienceView = listOf(binding.noneBtn, binding.introductionBtn, binding.intermediateBtn, binding.majorBtn, binding.practicalBtn,)
        val select = ContextCompat.getColor(requireContext(), R.color.selectColor)
        val unselect = ContextCompat.getColor(requireContext(), R.color.defaultColor)

        /** 카테고리 선택 */
        categoryViews.forEach { view ->
            view.setOnClickListener {
                categoryViews.forEach { categoryViews ->
                    if (categoryViews == view) {
                        // 뷰가 선택될 경우
                        when (categoryViews) {
                            binding.categoriesArt -> userCategory = "미술"
                            binding.categoriesPublicMusic -> userCategory = "대중음악"
                            binding.categoriesMusic -> userCategory = "음악"
                            binding.categoriesTheater -> userCategory = "연극"
                            binding.categoriesLiterature -> userCategory = "문학"
                        }
                    } else {
                        // 선택한 뷰의 나머지
                    }
                }
            }
        }

        /** 목적 선택 */
        goalViews.forEach { view ->
            view.setOnClickListener {
                goalViews.forEach { goalViews ->
                    if (goalViews == view) {
                        goalViews.setTextColor(select) // 선택된 뷰의 색상 변경
                        goalViews.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.button_round_se)
                        when (goalViews) {
                            binding.contestBtn -> userGoal = "공모전"
                            binding.practicalUseBtn -> userGoal = "실무활용"
                            binding.selfDevelopmentBtn -> userGoal = "자기개발"
                            binding.interviewBtn -> userGoal = "면접준비"
                            binding.portfolioBtn -> userGoal = "포트폴리오"
                            binding.contentBtn -> userGoal = "컨텐츠 제작"
                            binding.homeworkBtn -> userGoal = "학업과제"
                        }
                    } else {
                        goalViews.setTextColor(unselect)   // 원래대로
                        goalViews.setBackgroundResource(R.drawable.button_round)
                    }
                }
            }
        }

        /** 당신의 경험 선택 */
        experienceView.forEach { view ->
            view.setOnClickListener {
                experienceView.forEach { experienceView ->
                    if (experienceView == view) {
                        experienceView.setTextColor(select) // 선택된 뷰의 색상 변경
                        experienceView.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.button_round_se)
                        when (experienceView) {
                            binding.noneBtn -> userExperience = "전무"
                            binding.introductionBtn -> userExperience = "입문자"
                            binding.intermediateBtn -> userExperience = "중급자"
                            binding.majorBtn -> userExperience = "전공자"
                            binding.practicalBtn -> userExperience = "실무경험"
                        }
                    } else {
                        experienceView.setTextColor(unselect)   // 원래대로
                        experienceView.setBackgroundResource(R.drawable.button_round)
                    }
                }
            }
        }
        
        // 왼쪽 화살표 버튼 클릭 이벤트 처리, 이전 페이지로 이동
        binding.nextArrow.setOnClickListener {
            val userJobFragment = UserJobFragment()
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.loginFragmentFrame, userJobFragment)
                addToBackStack(null)
                commit()
            }
        }
        
        // 오른쪽 화살표 버튼 클릭 이벤트 처리
        binding.nextArrow.setOnClickListener {
            val userDetailFragment = UserDetailFragment()
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.loginFragmentFrame, userDetailFragment)
                addToBackStack(null)
                commit()
            }
            saveUserInfoCategoryDTO(userCategory, userGoal, userExperience)
        }
    }

    /** 유저 정보 저장하는 함수 */
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