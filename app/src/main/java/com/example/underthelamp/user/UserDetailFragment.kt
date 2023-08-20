package com.example.underthelamp.user

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.underthelamp.MainActivity
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentUserDetailBinding
import com.example.underthelamp.model.UserDetailDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserDetailFragment : Fragment() {

    private lateinit var binding: FragmentUserDetailBinding
    private lateinit var mAuth: FirebaseAuth
    private val db = Firebase.firestore
    private val userinfo = db.collection("userinfo")
    private lateinit var userDetailDTO: UserDetailDTO
    private var userAge : String = "연령"
    private var userLocation : String = "지역"
    private var userName : String = "이름"
    private var userGender: String = "성별"
    private var userSubCategory: String = "부카테고리"
    private lateinit var nameEdit: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUserDetailBinding.inflate(inflater, container, false)

        val Spinner_age = binding.spinnerAge
        val Spinner_location = binding.spinnerLocation

        val data_age = listOf("선택해주세요","10","20","30","40","50","60","70","80","90","100")
        val adapter_age = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, data_age)

        val data_location = listOf("선택해주세요","서울","부산","인천","대구","대전","광주","울산","세종","경기도","강원도","충북","충남","전북","전남","경북","경남","제주")
        val adapter_location = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, data_location)

        // 인증 초기화
        mAuth = Firebase.auth

        Spinner_age.adapter = adapter_age
        Spinner_age.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 나이 선택 시
                userAge = parent?.getItemAtPosition(position).toString()

                // 선택된 항목의 글자 색상을 변경
                if (position > 0) {
                    (view as? TextView)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.selectColor))
                } else {    // 선택해주세요 일 경우
                    (view as? TextView)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                userAge = "미선택"
            } // 선택 안할 시
        }

        Spinner_location.adapter = adapter_location
        Spinner_location.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 지역 선택 시
                userLocation = parent?.getItemAtPosition(position).toString()
                // 선택된 항목의 글자 색상을 변경
                if (position > 0) {
                    (view as? TextView)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.selectColor))
                } else {    // 선택해주세요 일 경우
                    (view as? TextView)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                userLocation = "미선택"
            } // 선택 안할 시

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nameEdit = binding.editName
        userName = "이름"
        userGender = "성별"
        userSubCategory = "부카테고리"

        val genderViews = listOf(binding.man, binding.women)
        val subCategoryViews = listOf(binding.noneBtn, binding.introductionBtn, binding.intermediateBtn, binding.majorBtn, binding.practicalBtn)

        genderViews.forEach { view ->
            view.setOnClickListener {
                genderViews.forEach { genderView ->
                    if (genderView == view) {
                        //genderView.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_se)
                        genderView.setBackgroundResource(R.drawable.button_round_se)
                        userGender = if (genderView == binding.man) "남자" else "여자"
                    } else {
                        genderView.setBackgroundResource(R.drawable.button_round)
                    }
                }
            }
        }

        /** 저장된 카테고리 값에 따른 이미지 변경 */
        userinfo.document(getCurrentUserUid()).collection("information").document("category").get().addOnSuccessListener { querySnapshot ->
            val userCategory = querySnapshot.getString("user_category")

            when (userCategory) {
                "미술" -> binding.detailCategory.setImageResource(R.drawable.art_icon)
                "대중음악" -> binding.detailCategory.setImageResource(R.drawable.public_music_icon)
                "음악" -> binding.detailCategory.setImageResource(R.drawable.music_icon)
                "연극" -> binding.detailCategory.setImageResource(R.drawable.theater_icon)
                "문학" -> binding.detailCategory.setImageResource(R.drawable.literature_icon)
            }
        }

        subCategoryViews.forEach { view ->
            view.setOnClickListener {
                subCategoryViews.forEach { subCategoryView ->
                    if (subCategoryView == view) {
                        subCategoryView.setBackgroundResource(R.drawable.button_round_se)
                        when (subCategoryView) {
                            binding.majorBtn -> userSubCategory = "전무"
                            binding.introductionBtn -> userSubCategory = "입문자"
                            binding.intermediateBtn -> userSubCategory = "중급자"
                            binding.majorBtn -> userSubCategory = "전공자"
                            binding.practicalBtn -> userSubCategory = "실무경험"
                        }
                    } else {
                        subCategoryView.setBackgroundResource(R.drawable.button_round)

                    }
                }
            }
        }

        // 왼쪽 화살표 버튼 클릭 이벤트 처리
        binding.back.setOnClickListener {
            val userInfoCategoriesFragment = UserInfoCategoriesFragment()
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.loginFragmentFrame, userInfoCategoriesFragment)
                addToBackStack(null)
                commit()
            }
        }

        // 오른쪽 화살표 버튼 클릭 이벤트 처리
        binding.signupNext.setOnClickListener {
            var userName = nameEdit.text.toString().trim()
            saveUserDetailDTO(userName, userGender, userAge, userLocation, userSubCategory)
        }
    }

    private fun saveUserDetailDTO(userName : String, userGender : String, userAge : String, userLocation : String, userSubCategory : String) {

        userDetailDTO = UserDetailDTO(userName, userGender, userAge, userLocation, userSubCategory)

        // Firestore 에 DTO 저장
        userinfo.document(getCurrentUserUid()).collection("userinfo").document("detail").set(userDetailDTO)
            .addOnSuccessListener {
                // 성공적으로 저장된 경우 처리할 로직 작성
                Toast.makeText(activity, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                
                // 회원가입 완료 시 MainActivity로 이동
                val intent = Intent(activity, MainActivity::class.java)
                
                startActivity(intent)
            }
            .addOnFailureListener {
                // 저장 실패 시 처리할 로직 작성
                Toast.makeText(activity, "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentUserUid(): String {
        // 현재 사용자의 UID 반환 (Firebase Auth 사용)
        return mAuth.currentUser?.uid ?: ""
    }

}