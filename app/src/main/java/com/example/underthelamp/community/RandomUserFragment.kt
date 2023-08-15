package com.example.underthelamp.community

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentRandomUserBinding
import com.google.firebase.firestore.FirebaseFirestore
import androidx.palette.graphics.Palette

class RandomUserFragment: Fragment() {

    lateinit var binding : FragmentRandomUserBinding
    var firestore : FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        firestore = FirebaseFirestore.getInstance()
        binding = FragmentRandomUserBinding.inflate(inflater, container, false)

        val userPostImage = binding.userPostImage
        val randomUserFrame = binding.randomUserFrame

        val bitmap = (userPostImage.drawable as? BitmapDrawable)?.bitmap

        bitmap?.let {
            // Palette 라이브러리를 사용하여 이미지의 주요 색상을 추출
            Palette.from(it).generate { palette ->
                val dominantColor = palette?.getDominantColor(Color.WHITE) // 기본값

                // 추출한 주요 색상을 기반으로 그림자 색상을 설정 0(투명) ~ 255(불투명)
                val shadowColors = arrayOf(
                    ColorUtils.setAlphaComponent(dominantColor!!, 10),
                    ColorUtils.setAlphaComponent(dominantColor, 9),
                    ColorUtils.setAlphaComponent(dominantColor, 8),
                    ColorUtils.setAlphaComponent(dominantColor, 7),
                    ColorUtils.setAlphaComponent(dominantColor, 6),
                    ColorUtils.setAlphaComponent(dominantColor, 5),
                    ColorUtils.setAlphaComponent(dominantColor, 4),
                    ColorUtils.setAlphaComponent(dominantColor, 3),
                    ColorUtils.setAlphaComponent(dominantColor, 2),
                    ColorUtils.setAlphaComponent(dominantColor, 1)

                )

                // 그림자의 색상을 변경
                val layerDrawable = randomUserFrame.background as? LayerDrawable
                if (layerDrawable != null && layerDrawable.numberOfLayers >= 10) {
                    for (i in 0 until 10) {
                        val shapeDrawable = layerDrawable.getDrawable(i) as? GradientDrawable
                        shapeDrawable?.setColor(shadowColors[i])
                    }
                }
            }
        }

        return binding.root
    }
}