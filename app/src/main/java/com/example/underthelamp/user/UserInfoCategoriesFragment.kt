package com.example.underthelamp.user

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.underthelamp.MainActivity
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentUserInfoCatergoriesBinding
import com.example.underthelamp.model.UserCategoryDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_search.category

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
        val categoryText = listOf(binding.artText,binding.publicMusicText, binding.musicText, binding.theaterText, binding.literatureText)
        val goalViews = listOf(binding.contestBtn, binding.practicalUseBtn, binding.selfDevelopmentBtn, binding.interviewBtn, binding.portfolioBtn, binding.contentBtn, binding.homeworkBtn)
        val experienceView = listOf(binding.noneBtn, binding.introductionBtn, binding.intermediateBtn, binding.majorBtn, binding.practicalBtn,)

        /** 카테고리 선택 */
        categoryViews.forEachIndexed { index, view ->
            view.setOnClickListener {
                categoryViews.forEachIndexed { categoryIndex, categoryViews ->
                    if (categoryViews == view) {
                        // 뷰가 선택될 경우
                        categoryViews.setColorFilter(resources.getColor(R.color.selectColor))
                        categoryText[categoryIndex].setTextColor(ContextCompat.getColor(requireContext(), R.color.selectColor))
                        when (categoryViews) {
                            binding.categoriesArt -> userCategory = "미술"
                            binding.categoriesPublicMusic -> userCategory = "대중음악"
                            binding.categoriesMusic -> userCategory = "음악"
                            binding.categoriesTheater -> userCategory = "연극"
                            binding.categoriesLiterature -> userCategory = "문학"
                        }
                    } else {
                        // 선택한 뷰 제외한 나머지는 그대로
                        categoryViews.setColorFilter(resources.getColor(R.color.white))
                        categoryText[categoryIndex].setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                }
            }
        }

        /** 목적 선택 */
        goalViews.forEach { view ->
            view.setOnClickListener {
                goalViews.forEach { goalViews ->
                    if (goalViews == view) {
                        // 선택된 뷰의 색상 변경
                        goalViews.setBackgroundResource(R.drawable.button_round_se)
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
                        experienceView.setBackgroundResource(R.drawable.button_round_se)

                        when (experienceView) {
                            binding.noneBtn -> userExperience = "전무"
                            binding.introductionBtn -> userExperience = "입문자"
                            binding.intermediateBtn -> userExperience = "중급자"
                            binding.majorBtn -> userExperience = "전공자"
                            binding.practicalBtn -> userExperience = "실무경험"
                        }
                    } else {
                        experienceView.setBackgroundResource(R.drawable.button_round)
                    }
                }
            }
        }
        
        // 왼쪽 화살표 버튼 클릭 이벤트 처리, 이전 페이지로 이동
        binding.backArrow.setOnClickListener {
            val userJobFragment = UserJobFragment()
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.loginFragmentFrame, userJobFragment)
                addToBackStack(null)
                commit()
            }
        }
        
        // 오른쪽 화살표 버튼 클릭 이벤트 처리, 메인 페이지로 이동
        binding.nextArrow.setOnClickListener {

            saveUserInfoCategoryDTO(userCategory, userGoal, userExperience)
        }
    }

    /** 유저 정보 저장하는 함수 */
    private fun saveUserInfoCategoryDTO(userCategory : String, userGoal : String, userExperience : String) {

        userCategoryDTO = UserCategoryDTO(userCategory, userGoal, userExperience)

        // Firestore에 DTO 저장
        userinfo.document(getCurrentUserUid()).collection("information").document("category").set(userCategoryDTO)
            .addOnSuccessListener {
                // 성공적으로 저장된 경우 처리할 로직 작성
                Toast.makeText(activity, "유저 정보 저장이 완료되었습니다.", Toast.LENGTH_SHORT).show()
//                val intent: Intent = Intent(activity, MainActivity::class.java)
//
//                startActivity(intent)
                val userDetailFragment = UserDetailFragment()
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.loginFragmentFrame, userDetailFragment)
                    addToBackStack(null)
                    commit()
                }
            }
            .addOnFailureListener {
                // 저장 실패 시 처리할 로직 작성
                Toast.makeText(activity, "직업 저장에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                val userInfoCategoriesFragment = UserInfoCategoriesFragment()
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.loginFragmentFrame, userInfoCategoriesFragment)
                    addToBackStack(null)
                    commit()
                }
            }
    }

    private fun getCurrentUserUid(): String {
        // 현재 사용자의 UID 반환 (Firebase Auth 사용)
        return mAuth.currentUser?.uid ?: ""
    }
}