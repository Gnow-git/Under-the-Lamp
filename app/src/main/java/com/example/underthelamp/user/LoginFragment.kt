package com.example.underthelamp.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.underthelamp.MainActivity
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentLoginBinding
import com.example.underthelamp.navigation.model.UserDTO
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserInfo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    lateinit var binding : FragmentLoginBinding
    lateinit var mAuth : FirebaseAuth

    private val db = Firebase.firestore
    private val userinfo = db.collection("userinfo")

    lateinit var emailEdit: EditText
    lateinit var passwordEdit: EditText

    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        emailEdit = binding.emailEdit
        passwordEdit = binding.passwordEdit


        // 인증 초기화
        mAuth = Firebase.auth

        binding.loginBtn.setOnClickListener{

            // trim 을 통해 공백 제거
            var email = emailEdit.text.toString().trim()
            var password = passwordEdit.text.toString().trim()

            login(email, password)
        }

        // google 로그인
        binding.googleBtn.setOnClickListener {
            googleLogin()
        }

        // google 로그인
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)


        return view

    }

    override fun onStart() { // 이전에 로그인한 적이 있다면 자동 로그인
        super.onStart()
        moveMainPage(mAuth?.currentUser)
    }

    private fun login(email: String, password: String) {

        if (email.isEmpty() || password.isEmpty()) {    // 로그인 시 null 방지
            Toast.makeText(activity, "이메일과 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 로그인 성공시
                val intent: Intent = Intent(activity, MainActivity::class.java)

                startActivity(intent)
                Toast.makeText(activity, "로그인 성공", Toast.LENGTH_SHORT).show()
                activity?.finish()

            } else if(task.exception?.message?.contains("no user record") == true){
                // 계정이 없는 경우 회원가입 수행
                signUp(email, password)

            } else if (task.exception?.message.isNullOrEmpty()) {
                Toast.makeText(activity, task.exception?.message, Toast.LENGTH_LONG).show()
            } else {
                // 로그인 실패시
                Toast.makeText(activity, "로그인 실패", Toast.LENGTH_SHORT).show()
                Log.d("Login", "Error: ${task.exception}")
            }
        }
    }

    private fun signUp(email: String, password: String) {

        if (email.isEmpty() || password.isEmpty()) {    // 회원가입 시 null 방지
            Toast.makeText(activity, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 회원가입 성공시 직업 선택 화면으로 이동

                    val userJobFragment = UserJobFragment()
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.loginFragmentFrame, userJobFragment)
                        addToBackStack(null)
                        commit()
                    }
                    addUserToDatabase(email, mAuth.currentUser?.uid!!)
                    
                    Toast.makeText(activity, "회원가입 및 로그인 성공", Toast.LENGTH_SHORT).show()

                } else {
                    // 회원가입 실패시
                    Toast.makeText(activity, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    Log.d("SignUp", "Error: ${task.exception}")
                }
            }
    }

    private fun addUserToDatabase(email: String, uId: String){
        userinfo.document(uId).set(UserDTO(uId, email))
    }

    fun googleLogin() {
        var signInIntent: Intent = googleSignInClient!!.signInIntent
        startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            if(result!!.isSuccess){
                var account = result!!.signInAccount
                // Second step
                firebaseAuthWithGoogle(account)
                Toast.makeText(activity, "구글 로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        mAuth.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login
                    moveMainPage(task.result?.user)
                } else {
                    // Show the error message
                    Toast.makeText(activity, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun moveMainPage(user: FirebaseUser?){  // 자동 로그인
        if(user!= null){
            startActivity(Intent(activity, MainActivity::class.java))
            activity?.finish()
        }
    }

}