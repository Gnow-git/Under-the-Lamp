package com.example.underthelamp.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.underthelamp.MainActivity
import com.example.underthelamp.R
import com.example.underthelamp.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_grid.view.gridfragment_recyclerview

/** 카테고리를 검색한 유저의 게시물 정보를 보여 주는 Fragment */
class SearchGridFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    var fragmentView : View? = null
    var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
    var explain : String? = null

     companion object {
        private const val ARG_CATEGORY = "category"
        private const val ARG_USER_UIDS = "userUids"

        fun newInstance(category: String, userUids: ArrayList<String>): SearchGridFragment {
            if (userUids.isNotEmpty()){
                val fragment = SearchGridFragment()
                val args = Bundle()
                args.putString(ARG_CATEGORY, category)
                args.putStringArrayList(ARG_USER_UIDS, userUids)
                fragment.arguments = args
                return fragment
            } else {
                // UIDs가 없는 경우 동작
                val fragment = SearchGridFragment()
                return fragment
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_search_grid, container, false)
        firestore = FirebaseFirestore.getInstance()
        fragmentView?.gridfragment_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.gridfragment_recyclerview?.layoutManager = GridLayoutManager(activity,3)

        explain = arguments?.getString("explain")


        return fragmentView
    }

    // 뒤로가기(백 버튼)를 처리하기 위한 메서드
    fun onBackPressed() {
        // MainActivity로 돌아가도록 인텐트를 생성
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
    }


    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val category = arguments?.getString(ARG_CATEGORY)
        val userId = arguments?.getStringArrayList(ARG_USER_UIDS)

        init {
            if (userId != null) {
                firestore?.collection("images")
                    ?.whereIn("userId", userId)
                    ?.addSnapshotListener { querySnapshots, firebaseFirestoreException ->


                        if (querySnapshots == null) return@addSnapshotListener

                        for (snapshot in querySnapshots.documents) {
                            contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                        }
                        notifyDataSetChanged()
                    }
            }else{
                firestore?.collection("images")
                    ?.whereEqualTo("explain", explain)
                    ?.addSnapshotListener { querySnapshots, firebaseFirestoreException ->

                        if (querySnapshots == null) return@addSnapshotListener

                        for (snapshot in querySnapshots.documents) {
                            contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                        }
                        notifyDataSetChanged()
                    }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            val dpValue = 310 // 설정하려는 DP 값
            val density = resources.displayMetrics.density
            val pixelValue = (dpValue * density).toInt()

            var width = pixelValue / 3

            var imageview = ImageView(parent.context)

            val marginDp = 5 // 설정하려는 DP 마진 값
            val marginPixel = (marginDp * density).toInt()

            val layoutParams = MarginLayoutParams(width, width)
            layoutParams.setMargins(marginPixel, 0, marginPixel, marginPixel*2)
            imageview.layoutParams = layoutParams
            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(
                RequestOptions().centerCrop()).into(imageview)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }

}