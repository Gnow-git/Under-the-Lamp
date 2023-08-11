package com.example.underthelamp.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentUserJobBinding
import com.example.underthelamp.navigation.model.UserJobDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserInfo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserJobFragment : Fragment() {

    private lateinit var binding : FragmentUserJobBinding
    private lateinit var mAuth : FirebaseAuth
    private val db = Firebase.firestore
    private val userinfo = db.collection("userinfo")
    private lateinit var userJobDTO : UserJobDTO
    private var userJob : String = "직업"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserJobBinding.inflate(inflater, container, false)

        // 인증 초기화
        mAuth = Firebase.auth


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val jobViews = listOf(binding.artist, binding.promoter)

        jobViews.forEach { view ->
            view.setOnClickListener {
                jobViews.forEach { jobViews ->
                    jobViews.setTextColor(resources.getColor(R.color.selectColor))
                    if (jobViews == view) {
                        userJob = if (jobViews == binding.artist) "예술가" else "기획자"
                        saveUserJobDTO(userJob) // 직업 저장
                    } else {
                        // 선택 안된 항목에 대해 처리
                        jobViews.setTextColor(resources.getColor(R.color.white))
                    }
                }
            }
        }
    }

    /** 유저의 직업을 저장 하는 함수 */
    private fun saveUserJobDTO(userJob : String) {

        userJobDTO = UserJobDTO(userJob)

        // Firestore 에 DTO 저장
        userinfo.document(getCurrentUserUid()).collection("information").document("job").set(userJobDTO)
            .addOnSuccessListener {
                // 성공적으로 저장된 경우 처리할 로직 작성
                Toast.makeText(activity, "직업 저장이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                val userInfoCategoriesFragment = UserInfoCategoriesFragment()
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.loginFragmentFrame, userInfoCategoriesFragment)
                    addToBackStack(null)
                    commit()
                }

            }
            .addOnFailureListener {
                // 저장 실패 시 처리할 로직 작성
                Toast.makeText(activity, "직업 저장에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentUserUid(): String {
        // 현재 사용자의 UID 반환 (Firebase Auth 사용)
        return mAuth.currentUser?.uid ?: ""
    }
}