package com.example.underthelamp.information

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentWritingBinding
import com.example.underthelamp.model.ContestDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.fragment_writing.addPhotoImage
import kotlinx.android.synthetic.main.fragment_writing.contestContentEdit
import kotlinx.android.synthetic.main.fragment_writing.contestTitleEdit
import java.text.SimpleDateFormat
import java.util.Date

class WritingFragment : Fragment() {

    lateinit var binding : FragmentWritingBinding
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWritingBinding.inflate(inflater, container, false)
        val view = binding.root

        // firebaseStorage 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.uploadBtn.setOnClickListener{
            // 게시글 업로드
            openImagePicker()   // 사진을 불러 오는 함수
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
            if(resultCode == Activity.RESULT_OK) {
                photoUri = data?.data
                addPhotoImage.setImageURI(photoUri)

                contestUpload()
            } else {
                // finish
                activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()

                // 업로드 안될 경우 이동
                // 업로드 후 fragment 변경
                var informationFragment = InformationFragment()
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, informationFragment)?.commit()
            }
        }
    }

    fun contestUpload() {
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "ContestImage" + timestamp + "_.png"    // 이미지 파일의 중복 방지

        var storageRef = storage?.reference?.child("contestImages")?.child(imageFileName)

        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contestDTO = ContestDTO()

            // 이미지 url
            contestDTO.imageUrl = uri.toString()

            // 작성자 uid
            contestDTO.uid = auth?.currentUser?.uid

            // 게시물 제목
            contestDTO.contestTitle = contestTitleEdit.text.toString()

            // 게시물 내용
            contestDTO.contestContent = contestContentEdit.text.toString()

            // timestamp 추가
            contestDTO.timestamp = System.currentTimeMillis()

            // 저장할 위치 지정
            firestore?.collection("contest")?.document()?.set(contestDTO)
            
            // 현재 fragment 종료
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
            
            // 업로드 후 fragment 변경
            var informationFragment = InformationFragment()
            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, informationFragment)?.commit()
        }
    }
}