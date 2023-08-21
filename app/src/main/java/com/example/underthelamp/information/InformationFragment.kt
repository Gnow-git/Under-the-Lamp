package com.example.underthelamp.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.underthelamp.R
import com.example.underthelamp.model.ContestDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_information.view.contest_recyclerview
import kotlinx.android.synthetic.main.item_contest.view.contestForm
import kotlinx.android.synthetic.main.item_contest.view.contestImage
import kotlinx.android.synthetic.main.item_contest.view.contestTitle

class InformationFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    var uid : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_information, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.contest_recyclerview.adapter = ContestAdapter()
        view.contest_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

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
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contestDTOS.size
        }

        /** 유저의 게시글을 불러와 보여주는 ViewHolder */
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            var viewHolder = (holder as CustomViewHolder).itemView

            // 작성된 공모전 제목 불러오기
            viewHolder.contestTitle.text = contestDTOS!![position].contestTitle
            
            // 공모전 Image 불러오기
            Glide.with(holder.itemView.context).load(contestDTOS!![position].imageUrl).into(viewHolder.contestImage)

            // 커뮤니티의 게시글을 누를 경우
            viewHolder.contestForm.setOnClickListener { v ->

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