package com.example.underthelamp.intro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.underthelamp.R
import com.example.underthelamp.user.LoginActivity
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {

    private var vpAdapter : FragmentStatePagerAdapter? = null
    private lateinit var intro_btn: Button  // 시작하기 버튼
    private lateinit var front_arrow: ImageView // 다음 버튼
    private lateinit var back_arrow: ImageView  // 이전 버튼
    private lateinit var viewPager: ViewPager   // 화면 전환 위젯
    private var position : Int = 0  // 페이지 위치

    override fun onCreate(savedInstanceState: Bundle?) {
        var intent = Intent(this, LoginActivity::class.java)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        intro_btn = findViewById(R.id.intro_btn)
        front_arrow = findViewById(R.id.front_arrow)
        back_arrow = findViewById(R.id.back_arrow)
        viewPager = findViewById(R.id.viewpager)

        vpAdapter = CustomPagerAdapter(supportFragmentManager)
        viewpager.adapter = vpAdapter

        indicator.setViewPager(viewpager)

        /** 페이지의 position을 파악하기 위한 함수 */
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            /** 페이지 변할 때 마다 함수 호출 */
            override fun onPageSelected(position: Int) {
                this@StartActivity.position = position
                /** 버튼 표시 함수 실행 */
                updateButtonVisibility()

                /** 화살표 버튼 표시 함수 실행 */
                updateArrowVisibility()
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        /** 오른쪽 화살표를 눌렀을 경우 */
        front_arrow.setOnClickListener {
            if (position < 4) { // 마지막 페이지가 아닐 경우
                viewPager.currentItem = position + 1    // 다음 페이지로 이동
            }
        }

        /** 왼쪽 화살표를 눌렀을 경우 */
        back_arrow.setOnClickListener {
            if (position > 0) { // 첫번째 페이지가 아닐 경우
                viewPager.currentItem = position - 1    // 이전 페이지로 이동
            }
        }

        /** 시작하기 버튼을 누를 경우 로그인 화면으로 이동 */
        intro_btn.setOnClickListener {
            startActivity(intent)
            finish()
        }

        updateButtonVisibility()    // 버튼 표시 초기화
        updateArrowVisibility() // 화살표 표시 초기화
    }
    /** 버튼 표시 함수 */
    private fun updateButtonVisibility() {
        if (position == 4) {
            intro_btn.visibility = View.VISIBLE
        } else {
            intro_btn.visibility = View.INVISIBLE
        }
    }

    /** 화살표 표시 함수 */
    private fun updateArrowVisibility() {
        when (position) {
            0 -> {
                front_arrow.visibility = View.VISIBLE
                back_arrow.visibility = View.GONE
            }
            4 -> {
                front_arrow.visibility = View.GONE
                back_arrow.visibility = View.VISIBLE
            }
            else -> {
                front_arrow.visibility = View.VISIBLE
                back_arrow.visibility = View.VISIBLE
            }
        }
    }

    /** 시작화면 Adapter 설정 */
    inner class CustomPagerAdapter(fm: FragmentManager): FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val PAGENUMBER = 5  // 페이지 개수 지정

        override fun getCount(): Int {
            return PAGENUMBER
        }

        override fun getItem(position: Int): Fragment {

            /** 각 페이지에서의 image, text, position을 IntroFragment로 전달 */
            return when (position) {    // 빈 이미지 == 0
                0 -> {
                    IntroFragment.newInstance(R.raw.intro_0, "당신이 당신답게\n빛날 수 있도록", position)
                }
                1 -> IntroFragment.newInstance(R.raw.intro_1, "예술가-기획자\n중개 서비스" , position)
                2 -> {
                    IntroFragment.newInstance(R.raw.intro_2, "자신을 표현하고," , position)
                }
                3 -> IntroFragment.newInstance(R.raw.intro_3, "다양한 분야의 사람을 \n만나보세요" , position)
                4 -> IntroFragment.newInstance(R.raw.intro_4, "함께 협업하며\n서로의 빛을 켜주세요" , position)
                else -> IntroFragment.newInstance(0, "page00" , position)
            }
        }
    }

}