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
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentUserDetailBinding
import com.example.underthelamp.navigation.model.UserDetailDTO
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
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                userLocation = "미선택"
            } // 선택 안할 시

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nameEdit = binding.editTextTextPersonName
        userName = "이름"
        userGender = "성별"
        userSubCategory = "부카테고리"

        val genderViews = listOf(binding.man, binding.women)
        val subCategoryViews = listOf(binding.button, binding.button2, binding.button3, binding.button4, binding.button5)
        val color_se = ContextCompat.getColor(requireContext(), R.color.defaultColor)
        val color_un = ContextCompat.getColor(requireContext(), R.color.selectColor)

        genderViews.forEach { view ->
            view.setOnClickListener {
                genderViews.forEach { genderView ->
                    if (genderView == view) {
                        genderView.setTextColor(color_se) // 선택된 뷰의 색상 변경
                        genderView.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_se)
                        userGender = if (genderView == binding.man) "남자" else "여자"
                    } else {
                        genderView.setTextColor(color_un)   // 원래대로
                        genderView.setBackgroundResource(R.drawable.button_round)
                    }
                }
            }
        }

        subCategoryViews.forEach { view ->
            view.setOnClickListener {
                subCategoryViews.forEach { subCategoryView ->
                    if (subCategoryView == view) {
                        subCategoryView.setTextColor(color_se) // 선택된 뷰의 색상 변경
                        subCategoryView.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_se)
                        when (subCategoryView) {
                            binding.button -> userSubCategory = "전무"
                            binding.button2 -> userSubCategory = "입문자"
                            binding.button3 -> userSubCategory = "중급자"
                            binding.button4 -> userSubCategory = "전공자"
                            binding.button5 -> userSubCategory = "실무경험"
                        }
                    } else {
                        subCategoryView.setTextColor(color_un)   // 원래대로
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

                startActivity(Intent(activity, LoginActivity::class.java))
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