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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.underthelamp.community.CommunityFragment
import com.example.underthelamp.community.WritingFragment
import com.example.underthelamp.navigation.*
import com.example.underthelamp.search.SearchFragment
import com.example.underthelamp.upload.UploadFragment
import com.example.underthelamp.user.UserJobFragment
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

    var fragment_position = 0;  // 현재 fragment 위치 파악
    private var isOpen = false

    // Navigation 기능 설정
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        //setToolbarDefault()
        when(item.itemId){
            R.id.action_home ->{
                fragment_position = 0;
                var detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, detailViewFragment).commit()

                default_upload.visibility = View.VISIBLE   // floating 버튼 보이게
                if (isOpen) {
                    closeFab()
                }
                return true
            }
            R.id.action_search ->{
                fragment_position = 1;
                var searchFragment = SearchFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, searchFragment).commit()
                if (isOpen) {
                    closeFab()
                }
                default_upload.visibility = View.GONE   // floating 버튼 안 보이게

                return true
            }
            R.id.action_community-> {

            }
            R.id.action_board -> {
                // 정보 게시글
                fragment_position = 3;
                var communityFragment = CommunityFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, communityFragment).commit()

                default_upload.visibility = View.VISIBLE   // floating 버튼 보이게
                if (isOpen) {
                    closeFab()
                }
                return true

            }

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
        val fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        val fabRAntiClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_anticlockwise)

        default_upload.setOnClickListener{

            if(isOpen){ // floating button 이 열려 있을 경우
                closeFab()  // floating button 닫는 함수
            }

            else{   // floating button 이 닫혀 있을 경우
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
                        closeFab()
                        if(fragment_position == 0){ // 홈에서 image 버튼을 눌렀을 경우
                            var uploadFragment = UploadFragment()
                            supportFragmentManager.beginTransaction().replace(R.id.main_content, uploadFragment).commit()
                        } else if(fragment_position == 3){  // 정보 게시글 화면에서 image 버튼을 눌렀을 경우
                            var writingFragment = WritingFragment()
                            supportFragmentManager.beginTransaction().replace(R.id.main_content, writingFragment).commit()
                        }
                    }
                }
            }
        }
    }

    private fun closeFab() {
        val fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        val fabRClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise)

        image_upload.startAnimation(fabClose)
        camera_upload.startAnimation(fabClose)
        file_upload.startAnimation(fabClose)
        default_upload.startAnimation(fabRClockwise)

        if(fragment_position ==1){
            default_upload.visibility = View.GONE
        }   // floating 버튼 안 보이게
        else default_upload.visibility = View.VISIBLE
        isOpen = false
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