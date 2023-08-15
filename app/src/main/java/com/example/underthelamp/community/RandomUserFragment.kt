package com.example.underthelamp.community

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentRandomUserBinding
import com.google.firebase.firestore.FirebaseFirestore

class RandomUserFragment: Fragment() {

    lateinit var binding : FragmentRandomUserBinding
    var firestore : FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_random_user, container, false)
        firestore = FirebaseFirestore.getInstance()
        binding = FragmentRandomUserBinding.inflate(inflater, container, false)

        // userPostImage의 Drawbale을 가져옴
        val drawable = binding.userPostImage.drawable as? BitmapDrawable

        // Drawable이 Bitmap 형태인지 확인
        if (drawable != null) {
            val bitmap = drawable.bitmap

            // Bitmap에서 중심 색상 추출
            val centerColor = getCenterColor(bitmap)

            // 그림자 색상 계산
            val shadowColor = calculateShadowColor(centerColor)

            // userPostImage의 그림자 색상 설정
            binding.userPostImage.setBackgroundColor(shadowColor)
        }
        return view
    }

    private fun getCenterColor(bitmap: Bitmap): Int {
        val centerX = bitmap.width / 2
        val centerY = bitmap.height / 2
        return bitmap.getPixel(centerX, centerY)
    }

    private fun calculateShadowColor(baseColor: Int): Int {
        val luminance = ColorUtils.calculateLuminance(baseColor)
        val adjustedLuminance = if (luminance > 0.5) luminance - 0.2 else luminance + 0.2

        return ColorUtils.setAlphaComponent(ColorUtils.blendARGB(baseColor, Color.BLACK,
            adjustedLuminance.toFloat()
        ), 100)
    }
}