package com.example.underthelamp.information

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentInformationBinding
import com.example.underthelamp.model.ContestDTO
import com.example.underthelamp.model.RankDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_information.view.contestRecyclerView
import kotlinx.android.synthetic.main.fragment_information.view.informationRank
import kotlinx.android.synthetic.main.item_contest.like_heart_icon
import kotlinx.android.synthetic.main.item_contest.view.contestForm
import kotlinx.android.synthetic.main.item_contest.view.contestHashTag
import kotlinx.android.synthetic.main.item_contest.view.contestImage
import kotlinx.android.synthetic.main.item_contest.view.contestTitle
import kotlinx.android.synthetic.main.item_contest.view.likeView
import kotlinx.android.synthetic.main.item_hashtag.view.tagText
import kotlinx.android.synthetic.main.item_like.view.likeUserProfile
import kotlinx.android.synthetic.main.item_rank.view.enterBtn
import kotlinx.android.synthetic.main.item_rank.view.rankImage
import kotlinx.android.synthetic.main.item_rank.view.rankLikeCount
import kotlinx.android.synthetic.main.item_rank.view.rankTitle

class InformationFragment : Fragment() {
    lateinit var binding : FragmentInformationBinding
    var firestore : FirebaseFirestore? = null
    var uid : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid
        binding = FragmentInformationBinding.inflate(inflater, container, false)
        val view = binding.root

        /** informationRank 에 대한 adapter 와 layoutManager 지정 */
        view.informationRank.adapter = RankAdapter()
        // 가로로 설정
        view.informationRank.layoutManager = LinearLayoutManager(activity , RecyclerView.HORIZONTAL, false)

        /** contestRecyclerView 에 대한 adapter 와 layoutManager 지정 */
        view.contestRecyclerView.adapter = ContestAdapter()
        view.contestRecyclerView.layoutManager = LinearLayoutManager(activity)
        return view
    }

    /** informationRank 에 대한 adapter */
    inner class RankAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var rankDTOS : ArrayList<RankDTO> = arrayListOf()
        var rankUidList : ArrayList<String> = arrayListOf()
        init{
            firestore?.collection("rank")?.orderBy("orderNum")?.addSnapshotListener {
                querySnapshot, firebaseFirestoreException ->
                rankDTOS.clear()
                rankUidList.clear()
                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(RankDTO::class.java)
                    rankDTOS.add(item!!)
                    rankUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_rank, parent, false)
            return CustomRankViewHolder(view)
        }

        inner class CustomRankViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return rankDTOS.size
        }

        /** 랭킹 주제를 불러와 보여 주는 ViewHolder */
        @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var rankViewHolder = (holder as CustomRankViewHolder).itemView

            if (rankDTOS!![position].imageUrl != null) {
                // 저장된 Rank 주제에 해당 하는 공모전 이미지 불러 오기
                Glide.with(holder.itemView.context)
                    .load(rankDTOS!![position].imageUrl).into(rankViewHolder.rankImage)

            } else {
               // 만약 저장된 이미지가 없다면
                Glide.with(holder.itemView.context)
                    .load(R.drawable.rank_image_default).into(rankViewHolder.rankImage)
            }

            // 저장된 Rank 주제 불러 오기
            rankViewHolder.rankTitle.text = rankDTOS[position].rankTitle.toString().replace("\\n", "\n")

            // Rank 주제에 해당 하는 공모전 의 like 불러 오기
            rankViewHolder.rankLikeCount.text = "Like " + rankDTOS[position].rankLikeCount

            /** 불러온 Image 의 모서리 부분을 원하는 형태로 조정 하기 위한 코드 */
            rankViewHolder.rankImage.background = rankViewHolder.resources.getDrawable(R.drawable.layout_round, null)
            rankViewHolder.rankImage.clipToOutline = true

            /** 랭킹의 화살표 버튼을 누를 경우 */
            rankViewHolder.enterBtn.setOnClickListener {

                val rankDetailFragment = RankDetailFragment()
                val args = Bundle()
                args.putString("rankTitle", rankDTOS[position].rankTitle)
                args.putString("rankType", rankDTOS[position].rankType)
                args.putString("rankStandard", rankDTOS[position].rankStandard)
                rankDetailFragment.arguments = args

                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_content, rankDetailFragment)
                    .addToBackStack(null)
                    .commit()

            }
        }
    }

    /** contestRecyclerView(하단) 에 대한 adapter */
    @SuppressLint("NotifyDataSetChanged")
    inner class ContestAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contestDTOS : ArrayList<ContestDTO> = arrayListOf() // DTO 지정
        var contestUidList : ArrayList<String> = arrayListOf()
        init{
            firestore?.collection("contest")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contestDTOS.clear()
                contestUidList.clear()
                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot.documents) {
                    var item = snapshot.toObject(ContestDTO::class.java)
                    contestDTOS.add(item!!)
                    contestUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var contestView = LayoutInflater.from(parent.context).inflate(R.layout.item_contest,parent, false)
            return CustomContestViewHolder(contestView)
        }

        inner class CustomContestViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contestDTOS.size
        }

        /** 공모전 을 불러와 보여 주는 ViewHolder */
        @SuppressLint("UseCompatLoadingForDrawables")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            var contestViewHolder = (holder as CustomContestViewHolder).itemView

            // 공모전 Image 불러 오기
            Glide.with(holder.itemView.context).load(contestDTOS!![position].imageUrl).into(contestViewHolder.contestImage)

            /** 불러온 Image 의 모서리 부분을 원하는 형태로 조정 하기 위한 코드 */
            contestViewHolder.contestImage.background = contestViewHolder.resources.getDrawable(R.drawable.contest_image, null)
            contestViewHolder.contestImage.clipToOutline = true

            // 작성된 공모전 제목 불러 오기
            contestViewHolder.contestTitle.text = contestDTOS!![position].contestTitle

            // 작성된 공모전 의 해시태그 불러 오기
            val hashTagAdapter = HashTagAdapter(contestDTOS[position].hashTag)
            contestViewHolder.contestHashTag.adapter = hashTagAdapter
            contestViewHolder.contestHashTag.layoutManager = LinearLayoutManager(
                contestViewHolder.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            // 좋아요 누른 사람들의 프로필 이미지 불러 오기
            val contestLikeAdapter = ContestLikeAdapter(contestDTOS[position].likes)
            contestViewHolder.likeView.adapter = contestLikeAdapter
            contestViewHolder.likeView.layoutManager = LinearLayoutManager(
                contestViewHolder.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            // 커뮤니티 의 게시글 을 누를 경우
            contestViewHolder.contestForm.setOnClickListener { v ->

                val contestDetailFragment = ContestDetailFragment()
                val args = Bundle()
                args.putString("contestUid", contestUidList[position])
                args.putString("destinationUid", contestDTOS[position].uid)
                contestDetailFragment.arguments = args

                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_content, contestDetailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    /** 해시태그를 표시하기 위한 어댑터 */
    inner class HashTagAdapter(private val hashTags: List<String>?) :
            RecyclerView.Adapter<HashTagAdapter.HashTagViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashTagViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hashtag, parent, false)
            return HashTagViewHolder(view)
        }

        override fun onBindViewHolder(holder: HashTagViewHolder, position: Int) {
            hashTags?.get(position)?.let { hashTag ->
                holder.itemView.tagText.text = "#$hashTag"
            }
        }

        override fun getItemCount(): Int {
            return hashTags?.size ?: 0
        }

        inner class HashTagViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tagText: TextView = view.findViewById(R.id.tagText)
        }
    }

    /** 좋아요 누른 유저의 프로필 사진을 표시하기 위한 어댑터 */
    inner class ContestLikeAdapter(private val likes: MutableMap<String, Boolean>?) :
            RecyclerView.Adapter<ContestLikeAdapter.ContestLikeViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContestLikeViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_like, parent, false)
            return ContestLikeViewHolder(view)
        }

        override fun onBindViewHolder(holder: ContestLikeAdapter.ContestLikeViewHolder, position: Int) {
            val likeViewHolder = holder.itemView

            // likes 맵에서 uid 가져오기
            val uidList = likes?.keys?.toList()
            val uid = uidList?.get(position)

            /** ImageView 의 round 적용이 안되어 적용 하기 위한 코드 */
            val likeUserProfile = likeViewHolder.findViewById<ImageView>(R.id.likeUserProfile)
            likeUserProfile.background = resources.getDrawable(R.drawable.like_background, null)
            likeUserProfile.clipToOutline = true

            val likeUserProfileForm = likeViewHolder.findViewById<ConstraintLayout>(R.id.likeUserProfileForm)

            if (uid != null) {
                firestore?.collection("profileImage")?.document(uid)?.get()
                    ?.addOnSuccessListener { documentSnapshot ->
                        val imageUrl = documentSnapshot.getString("image")

                        // 프로필 이미지를 가져와 likeView 에 표시
                        if (!imageUrl.isNullOrEmpty()) {

                            Glide.with(holder.itemView.context)
                                .load(imageUrl)
                                .into(likeViewHolder.likeUserProfile)

                            like_heart_icon.visibility = View.VISIBLE

                            val layoutParams = likeViewHolder.likeUserProfile.layoutParams as ViewGroup.MarginLayoutParams

                            layoutParams.width = dpToPx(if (position == 0) 24 else 20)
                            layoutParams.height = dpToPx(if (position == 0) 24 else 20)
                            layoutParams.topMargin = dpToPx(if (position == 0) 0 else 2)

                            val maxZValue = 10F
                            val minZValue = 4F

                            val zValue = maxZValue - (position * ((maxZValue - minZValue) / itemCount))

                            likeUserProfileForm.translationZ = dpToPx(zValue.toInt()).toFloat()

                        likeViewHolder.likeUserProfile.layoutParams = layoutParams

                        } else
                            // 좋아요 를 누른 유저가 없을 경우
                            like_heart_icon.visibility = View.INVISIBLE

                    }
            }
        }

        override fun getItemCount(): Int {
            return likes?.size ?: 0
        }

        inner class ContestLikeViewHolder(view: View) : RecyclerView.ViewHolder(view)

        fun dpToPx(dp: Int): Int {
            val scale = Resources.getSystem().displayMetrics.density
            return (dp * scale).toInt()
        }
    }
}