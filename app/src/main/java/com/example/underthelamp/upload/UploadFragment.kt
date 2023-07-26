package com.example.underthelamp.upload

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentUploadBinding
import com.example.underthelamp.navigation.DetailViewFragment
import com.example.underthelamp.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.addphoto_btn_upload
import kotlinx.android.synthetic.main.activity_add_photo.addphoto_edit_explain
import kotlinx.android.synthetic.main.activity_add_photo.addphoto_image
import java.text.SimpleDateFormat
import java.util.Date

class UploadFragment : Fragment() {

    lateinit var binding : FragmentUploadBinding
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUploadBinding.inflate(inflater, container, false)
        val view = binding.root

        // firebaseStorage 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // fragment에서는 튕기는 오류 발생
//        var photoPickerIntent = Intent(Intent.ACTION_PICK)
//        photoPickerIntent.type = "image/*"
//        getActivity()?.startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        binding.addphotoBtnUpload.setOnClickListener() {
            //contentUpload()
            openImagePicker()   // 사진 불러오는 함수 호출
        }
        return view
    }

    private fun openImagePicker() {
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM) {
            if(resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri)

                contentUpload()
            } else {
                // finish
                getActivity()?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
            }
        }
    }

    fun contentUpload() {
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"  // 중복 방지를 위한 파일명

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            // url 추가
            contentDTO.imageUrl = uri.toString()

            // user uid 추가
            contentDTO.uid = auth?.currentUser?.uid

            // content에 대한 내용 추가
            contentDTO.explain = addphoto_edit_explain.text.toString()

            // timestamp 추가
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            getActivity()?.setResult(Activity.RESULT_OK)

            // finish
            getActivity()?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()

            // 프래그먼트 변경
            var detailViewFragment = DetailViewFragment()
            getActivity()?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, detailViewFragment)?.commit()
        }
    }

}