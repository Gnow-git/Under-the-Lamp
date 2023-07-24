package com.example.underthelamp.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentSignUpBinding
import com.example.underthelamp.navigation.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpFragment : Fragment() {
    lateinit var binding : FragmentSignUpBinding
    lateinit var mAuth : FirebaseAuth

    private val db = Firebase.firestore
    private val userinfo = db.collection("userinfo")

    lateinit var emailEdit: EditText
    lateinit var passwordEdit: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val view = binding.root
        emailEdit = binding.emailEdit
        passwordEdit = binding.passwordEdit

        // 인증 초기화
        mAuth = Firebase.auth

        binding.signUpBtn.setOnClickListener{

            var emailEdit = emailEdit.text.toString().trim()
            var passwordEdit = passwordEdit.text.toString().trim()

            signUp(emailEdit, passwordEdit)
        }


        return view
    }

    private fun signUp(email: String, password: String){

        if (email.isEmpty() || password.isEmpty()) {    // 회원가입 시 null 방지
            Toast.makeText(activity, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 성공했을 때

                    val userJobFragment = UserJobFragment()
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.fragmentFrame, userJobFragment)
                        addToBackStack(null)
                        commit()
                    }
                    addUserToDatabase(email, mAuth.currentUser?.uid!!)

                } else {
                    // 실패했을 때
                    Toast.makeText(activity, "회원가입 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * firestore에 userinfo 저장하는 함수
     */
    private fun addUserToDatabase(email: String, uId: String){
        userinfo.document(uId).set(UserDTO(uId, email))
    }

}