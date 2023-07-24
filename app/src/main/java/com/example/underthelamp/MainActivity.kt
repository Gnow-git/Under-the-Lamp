package com.example.underthelamp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.underthelamp.navigation.*
import com.example.underthelamp.navigation.util.FcmPush
import com.example.underthelamp.search.SearchActivity
import com.example.underthelamp.search.SearchFragment
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    NavigationBarView.OnItemSelectedListener {
    // Navigation 기능 설정
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        //setToolbarDefault()
        when(item.itemId){
            R.id.action_home ->{
                var detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, detailViewFragment).commit()
                return true
            }
            R.id.action_search ->{
//                startActivity(Intent(this, SearchActivity::class.java))
//                return true
                var searchFragment = SearchFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, searchFragment).commit()
                return true
            }
            /*R.id.action_search ->{
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, gridFragment).commit()
                return true
            }*/
            R.id.action_community-> {
                // 커뮤니티
            }
            R.id.action_board -> {
                // 게시글
            }
/*            R.id.action_add_photo ->{


            }
            R.id.action_favorite_alarm ->{
                var alarmFragment = AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, alarmFragment).commit()
                return true
            }*/
            R.id.action_account ->{
                var userFragment = UserFragment()
                var bundle = Bundle()
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                bundle.putString("destinationUid",uid)
                userFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.main_content, userFragment).commit()
                return true
            }
        }
        return false

    }
//    fun setToolbarDefault(){
//        toolbar_username.visibility = View.GONE
//        toolbar_btn_back.visibility = View.GONE
//    }
    fun registerPushToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            task ->
            if (task.isSuccessful) {
                val token = task.result
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                val map = mutableMapOf<String,Any>()
                map["pushToken"] = token!!

              FirebaseFirestore.getInstance().collection("pushtokens").document(uid!!).set(map)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnItemSelectedListener(this)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)

        // Set default screen
        bottom_navigation.selectedItemId = R.id.action_home
        registerPushToken()

        // floating button 지정
        var isOpen = false
        val fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        val fabRClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise)
        val fabRAntiClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_anticlockwise)

        default_upload.setOnClickListener{

            if(isOpen){ // floating button이 열려 있을 경우 실행
                image_upload.startAnimation(fabClose)
                camera_upload.startAnimation(fabClose)
                file_upload.startAnimation(fabClose)
                default_upload.startAnimation(fabRClockwise)

                isOpen = false
            }

            else{   // floating button이 닫혀 있을 경우 실행
                image_upload.startAnimation(fabOpen)
                camera_upload.startAnimation(fabOpen)
                file_upload.startAnimation(fabOpen)
                default_upload.startAnimation(fabRAntiClockwise)

                image_upload.isClickable
                camera_upload.isClickable
                file_upload.isClickable

                isOpen = true

                // floating 의 image(첫번째) 버튼을 눌렀을 경우
                image_upload.setOnClickListener{
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        startActivity(Intent(this,AddPhotoActivity::class.java))
                    }
                }
            }
        }
    }
    
 // Profile
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == UserFragment.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            // 사진 선택할 경우 처리
            var imageUri = data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            storageRef.putFile(imageUri!!).continueWithTask{ task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener { uri ->
                var map = HashMap<String,Any>()
                map["image"] = uri.toString()
                FirebaseFirestore.getInstance().collection("profileImage").document(uid).set(map)
            }
        }
    }

}