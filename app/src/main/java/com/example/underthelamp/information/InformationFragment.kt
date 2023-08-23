package com.example.underthelamp.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.bumptech.glide.Glide
import com.example.underthelamp.R
import com.example.underthelamp.model.ContestDTO
import com.example.underthelamp.model.RankDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_information.view.contestRecyclerView
import kotlinx.android.synthetic.main.fragment_information.view.informationRank
import kotlinx.android.synthetic.main.item_contest.view.contestForm
import kotlinx.android.synthetic.main.item_contest.view.contestImage
import kotlinx.android.synthetic.main.item_contest.view.contestTitle
import kotlinx.android.synthetic.main.item_rank.rankImage
import kotlinx.android.synthetic.main.item_rank.view.enterBtn
import kotlinx.android.synthetic.main.item_rank.view.rankImage
import kotlinx.android.synthetic.main.item_rank.view.rankLikeCount
import kotlinx.android.synthetic.main.item_rank.view.rankTitle

class InformationFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    var uid : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_information, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

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
            rankViewHolder.rankTitle.text = rankDTOS!![position].rankTitle.toString().replace("\\n", "\n")

            // Rank 주제에 해당 하는 공모전 의 like 불러 오기
            rankViewHolder.rankLikeCount.text = "Like " + rankDTOS!![position].rankLikeCount

            /** 불러온 Image 의 모서리 부분을 원하는 형태로 조정 하기 위한 코드 */
            rankViewHolder.rankImage.background = rankViewHolder.resources.getDrawable(R.drawable.layout_round, null)
            rankViewHolder.rankImage.clipToOutline = true

            /** 랭킹의 화살표 버튼을 누를 경우 */
            rankViewHolder.enterBtn.setOnClickListener {

                val rankDetailFragment = RankDetailFragment()
                val args = Bundle()
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

    /** contestRecyclerView 에 대한 adapter */
    inner class ContestAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contestDTOS : ArrayList<ContestDTO> = arrayListOf() // DTO 지정
        var contestUidList : ArrayList<String> = arrayListOf()
        init{
            firestore?.collection("contest")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contestDTOS.clear()
                contestUidList.clear()
                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContestDTO::class.java)
                    contestDTOS.add(item!!)
                    contestUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_contest,parent, false)
            return CustomContestViewHolder(view)
        }

        inner class CustomContestViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contestDTOS.size
        }

        /** 유저의 게시글 을 불러와 보여 주는 ViewHolder */
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            var contestViewHolder = (holder as CustomContestViewHolder).itemView

            // 작성된 공모전 제목 불러 오기
            contestViewHolder.contestTitle.text = contestDTOS!![position].contestTitle
            
            // 공모전 Image 불러 오기
            Glide.with(holder.itemView.context).load(contestDTOS!![position].imageUrl).into(contestViewHolder.contestImage)

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
}