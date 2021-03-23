package com.example.centicbids.adapter

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.centicbids.R
import com.example.centicbids.listener.OnItemClickListener
import com.example.centicbids.model.Item
import com.example.centicbids.util.getTimeDifference
import com.example.centicbids.util.getTimerText
import com.example.centicbids.util.isValid
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item_item.view.*

open class ItemAdapter(query: Query, private val mListener: OnItemClickListener) :
    FirestoreAdapter<ItemAdapter.ItemViewHolder?>(query) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(inflater.inflate(R.layout.list_item_item, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getSnapshot(position), mListener)
    }


    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(snapshot: DocumentSnapshot, listener: OnItemClickListener?) {
            snapshot.toObject(Item::class.java)?.let { item ->
                if (!item.images.isNullOrEmpty()) {
                    Picasso.get()
                        .load(item.images[0])
                        .placeholder(R.drawable.vector_loading)
                        .error(R.drawable.vector_loading)
                        .into(itemView.img_item_main)
                }
                if (item.title != null) {
                    itemView.txt_item_title.text = item.title
                }
                if (item.description != null) {
                    itemView.txt_item_description.text = item.description
                }
                if (item.basePrice != null) {
                    itemView.txt_item_base_price.text = item.basePrice.toString()
                }
                if (item.highestBid != null) {
                    itemView.txt_item_highest_bid.text = item.highestBid.toString()
                }
                itemView.setOnClickListener { listener?.onItemClicked(snapshot) }
                itemView.btn_bid_now.setOnClickListener { listener?.onItemClicked(snapshot) }
                if (isValid(item.expiryDate)) {
                    //setBtnEnabled(true)
                    itemView.btn_bid_now.setOnClickListener { listener?.onItemClicked(snapshot) }
                    val timeDifference = getTimeDifference(item.expiryDate!!)
                    itemView.txt_item_expires_in.text = getTimerText(timeDifference)
                    val countDownTimer =
                        object : CountDownTimer(timeDifference, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                itemView.txt_item_expires_in.text =
                                    getTimerText(millisUntilFinished)
                            }

                            override fun onFinish() {
                                itemView.txt_item_expires_in.text = "00 h : 00 m : 00 s"
                            }
                        }
                    countDownTimer.start()
                } else {
                    itemView.txt_item_expires_in.text = "N/A"
                }
            }
        }
    }
}