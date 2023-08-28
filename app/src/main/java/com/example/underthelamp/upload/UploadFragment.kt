package com.example.underthelamp.upload

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentUploadBinding
import com.example.underthelamp.home.DetailViewFragment
import com.example.underthelamp.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
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

        binding.loadImageForm.setOnClickListener {
            openImagePicker()   // 사진 불러 오는 함수 호출
        }

        binding.uploadBtn.setOnClickListener {
            contentUpload()
        }

        binding.clearTextBtn.setOnClickListener {
            binding.contentEdit.text = null // 작성한 글 초기화
        }
        return view
    }

    /** 저장된 이미지를 찾도록 해주는 함수 */
    private fun openImagePicker() {
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM) {
            if(resultCode == Activity.RESULT_OK){   // 이미지를 정상적으로 불러 왔을 경우
                photoUri = data?.data

                binding.loadPhotoBtn.visibility = View.INVISIBLE

                binding.previewImage.setImageURI(photoUri) // 이미지뷰에 적용시켜 미리보기

            } else {    // 이미지를 불러오지 못하였거나 선택하지 않고 나갔을 경우

                var uploadFragment = UploadFragment()    // 다시 업로드 화면으로 이동
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, uploadFragment)?.commit()
            }
        }
    }

    private fun contentUpload() {
        val timestamp = com.google.firebase.Timestamp.now()
        val imageFileName = "IMAGE_" + timestamp + "_.png"  // 중복 방지를 위한 파일명

        val storageRef = storage?.reference?.child("images")?.child(imageFileName)

        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            val contentDTO = ContentDTO()

            // images 라는 collection 에 랜덤한 document 를 가진 주소 생성
            val documentRef = firestore?.collection("images")?.document()

            // url 추가
            contentDTO.imageUrl = uri.toString()

            // 게시글 uid 추가(랜덤 생성한 document 값)
            contentDTO.contentUid = documentRef?.id
            
            // 작성한 사람의 id 추가
            contentDTO.userId = auth?.currentUser?.uid

            // content 에 대한 내용 추가
            contentDTO.explain = binding.contentEdit.text.toString()

            // firebase 용 timestamp 추가
            contentDTO.timestamp = timestamp

            // 추가한 내용들 fireStore 에 저장
            documentRef?.set(contentDTO)

            // 정상 업로드 결과 전달
            activity?.setResult(Activity.RESULT_OK)

            // 프래그먼트 종료 후 메인 화면 으로 이동
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()

            var detailViewFragment = DetailViewFragment()
            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, detailViewFragment)?.commit()
        }
    }

}