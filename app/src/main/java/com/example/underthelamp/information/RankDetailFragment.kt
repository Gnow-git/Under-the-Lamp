package com.example.underthelamp.information

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.underthelamp.R
import com.example.underthelamp.databinding.FragmentRankDetailBinding
import com.example.underthelamp.model.ContestDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_rank_detail.view.rankDetailBanner
import kotlinx.android.synthetic.main.fragment_rank_detail.view.rankDetailRecyclerView
import kotlinx.android.synthetic.main.fragment_rank_detail.view.rankTitle
import kotlinx.android.synthetic.main.item_contest.view.contestImage
import kotlinx.android.synthetic.main.item_rank.view.rankImage
import kotlinx.android.synthetic.main.item_rank_detail.view.rankDetailContent
import kotlinx.android.synthetic.main.item_rank_detail.view.rankDetailForm
import kotlinx.android.synthetic.main.item_rank_detail.view.rankDetailTitle

class RankDetailFragment : Fragment() {
    lateinit var binding: FragmentRankDetailBinding
    var firestore : FirebaseFirestore? = null
    var rankTitle : String? = null
    var rankType : String? = null
    var rankStandard : String? = null
    private var typeOrder: String? = null /** 정렬할 종류 (공모전 & 협업 등) */
    private var standardOrder: String? = null /** 정렬할 기준 (좋아요 & 댓글 등) */

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRankDetailBinding.inflate(inflater, container, false)
        var view = binding.root

        firestore = FirebaseFirestore.getInstance()

        /** 랭킹 테마 제목 */
        rankTitle = arguments?.getString("rankTitle")?.replace("\\n", "\n")
        view.rankTitle.text = rankTitle

        /** 랭킹 으로 정할 주제(공모전, 협업) */
        rankType = arguments?.getString("rankType")

        /** 랭킹의 기준 (좋아요 수, 댓글 수 등) */
        rankStandard = arguments?.getString("rankStandard")

        makeRank(rankType!!, rankStandard!!)

        /** 랭킹에 대한 adapter 와 layoutManager 지정 */
        view.rankDetailRecyclerView.adapter = RankDetailAdapter(typeOrder!!, standardOrder!!)
        view.rankDetailRecyclerView.layoutManager = LinearLayoutManager(activity)
        return view
    }

    /** rankDetailRecyclerView 에 대한 adapter */
    inner class RankDetailAdapter(typeOrder: String, standardOrder: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contestDTOS : ArrayList<ContestDTO> = arrayListOf()
        var contestUidList : ArrayList<String> = arrayListOf()

        init{
            firestore
                ?.collection(typeOrder)  /** 정렬할 종류 (공모전 & 협업 등) */
                ?.orderBy(standardOrder) /** 정렬할 기준 (좋아요 & 댓글 등) */
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contestDTOS.clear()
                contestUidList.clear()
                if (querySnapshot == null)  return@addSnapshotListener


                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContestDTO::class.java)
                    contestDTOS.add(item!!)
                    contestUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_rank_detail, parent, false)
            return CustomRankViewHolder(view)
        }

        inner class CustomRankViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contestDTOS.size
        }

        /** 랭킹에 해당 하는 게시글 을 불러와 보여 주는 ViewHolder*/
        @SuppressLint("UseCompatLoadingForDrawables")   // rank_detail_banner_round 를 적용할 때 충돌 방지 지정
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var rankDetailViewHolder = (holder as CustomRankViewHolder).itemView

            /** 불러온 Banner 의 모서리 부분을 원하는 형태로 조정 하기 위한 코드 */
            rankDetailViewHolder.rankDetailBanner.background = rankDetailViewHolder.resources.getDrawable(R.drawable.rank_detail_banner_round, null)
            rankDetailViewHolder.rankImage.clipToOutline = true

            // 랭킹에 속한 게시글 제목 불러 오기
            rankDetailViewHolder.rankDetailTitle.text = contestDTOS!![position].contestTitle

            // 랭킹에 속한 게시글 내용 불러 오기
            rankDetailViewHolder.rankDetailContent.text = contestDTOS!![position].contestContent?.replace("\\n", "\n")

            // 랭킹에 속한 게시글 이미지 불러 오기
            Glide.with(holder.itemView.context).load(contestDTOS!![position].imageUrl).into(rankDetailViewHolder.contestImage)

            // 랭킹에 속한 게시글 을 누를 경우 원본 게시물 로 이동
            rankDetailViewHolder.rankDetailForm.setOnClickListener { v ->
                val contestDetailFragment = ContestDetailFragment()
                val args = Bundle()
                args.putString("contestUid", contestUidList[position])
                args.putString("destinationUid", contestDTOS[position].uid)
                contestDetailFragment.arguments = args

                /** 게시글 을 누를 경우 contestDetailFragment 로 변경 */
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_content, contestDetailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    /** InformationFragment 에서 넘어온 값을 가지고 랭킹 시스템 을 만드는 함수 */
     private fun makeRank(rankType: String, rankStandard: String){

        typeOrder = when (rankType) {
            "공모전" -> {
                "contest"
            }

            "협업" -> {
                "project"
            }

            else -> "none"
        }

        standardOrder = when (rankStandard) {
            "좋아요" -> {
                "like"
            }
            "댓글" -> {
                "comment"
            }

            else -> "none"
        }
     }
}