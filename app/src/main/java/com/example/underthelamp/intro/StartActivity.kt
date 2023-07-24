package com.example.underthelamp.intro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.underthelamp.R
import com.example.underthelamp.user.LoginActivity
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {

    private var vpAdapter : FragmentStatePagerAdapter? = null
    private lateinit var intro_btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        var intent = Intent(this, LoginActivity::class.java)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        intro_btn = findViewById(R.id.intro_btn)

        vpAdapter = CustomPagerAdapter(supportFragmentManager)
        viewpager.adapter = vpAdapter

        indicator.setViewPager(viewpager)

        intro_btn.setOnClickListener {
            startActivity(intent)
            finish()
        }
    }

    inner class CustomPagerAdapter(fm: FragmentManager): FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val PAGENUMBER = 5  // 페이지 개수 지정

        override fun getCount(): Int {
            return PAGENUMBER
        }

        override fun getItem(position: Int): Fragment {

            return when (position) {
                0 -> {
                    IntroFragment.newInstance(0, "당신이 당신답게\n 빛날 수 있도록")
                }
                1 -> IntroFragment.newInstance(0, "예술가-기획자\n 중개 서비스")  // 빈 이미지로 0
                2 -> IntroFragment.newInstance(0, "자신을 표현하고,")
                3 -> IntroFragment.newInstance(0, "다양한 분야의\n 사람을 만나보세요")
                4 -> {
                    intro_btn.visibility = View.VISIBLE
                    IntroFragment.newInstance(0, "함께 협업하며\n 서로의 빛을 켜주세요")
                }
                else -> IntroFragment.newInstance(0, "page00")
            }
        }
    }

}