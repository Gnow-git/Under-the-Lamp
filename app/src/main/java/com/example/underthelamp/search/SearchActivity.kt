package com.example.underthelamp.search

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.underthelamp.R
import com.example.underthelamp.databinding.ActivitySearchBinding
import com.example.underthelamp.databinding.FragmentSearchRecyclerBinding
import com.example.underthelamp.navigation.DetailViewFragment
import com.example.underthelamp.navigation.GridFragment
import com.example.underthelamp.navigation.UserFragment
import com.example.underthelamp.navigation.model.ContentDTO
import com.example.underthelamp.navigation.model.UserCategoryDTO
import com.example.underthelamp.navigation.model.UserDetailDTO
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    NavigationBarView.OnItemSelectedListener {

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        //setToolbarDefault()
        when(item.itemId){
            R.id.action_home ->{
                var detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, detailViewFragment).commit()
                return true
            }
            R.id.action_search ->{
                startActivity(Intent(this, SearchActivity::class.java))
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

                            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                                startActivity(Intent(this,AddPhotoActivity::class.java))
                            }
                            return true
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
    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        searchFragment()
        binding.searchBtn.setOnClickListener { // 검색창을 누를 경우
            showSearchRecyclerFragment()
        }
    }

    private fun searchFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = SearchCategoryFragment()
        fragmentTransaction.replace(R.id.framelayout, fragment)
        fragmentTransaction.commit()
    }

    private fun showSearchRecyclerFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        //val fragment = SearchRecyclerFragment()
        val fragment = GridFragment()
        fragmentTransaction.replace(R.id.framelayout, fragment)
        fragmentTransaction.commit()
    }
}