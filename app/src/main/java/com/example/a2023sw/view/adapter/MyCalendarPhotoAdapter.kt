package com.example.a2023sw

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.a2023sw.MyApplication.Companion.storage
import com.example.a2023sw.databinding.ItemCalendarPhotoBinding
import com.example.a2023sw.databinding.ItemPhotoBinding
import com.example.a2023sw.model.ItemPhotoModel
import com.example.a2023sw.view.activity.PhotoDetailActivity

class MyCalendarPhotoViewHolder(val binding: ItemCalendarPhotoBinding) : RecyclerView.ViewHolder(binding.root)

class MyCalendarPhotoAdapter(val context: Context, val itemList: MutableList<ItemPhotoModel>): RecyclerView.Adapter<MyCalendarPhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCalendarPhotoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyCalendarPhotoViewHolder(ItemCalendarPhotoBinding.inflate(layoutInflater))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
    override fun onBindViewHolder(holder: MyCalendarPhotoViewHolder, position: Int) {
//        val position = holder.getAdapterPosition()
        val data = itemList.get(position)

        holder.binding.run {
            itemEmailView.text = data.email
            itemTitleView.text = data.title
            itemDateView.text = data.date
            itemFoodTimeView.text = data.foodTime
            itemFoodView.text = data.food
            itemCompanyView.text = data.company
            itemWhereView.text = data.where
            itemMemoView.text = data.memo
            itemNickNameView.text = data.nickName

            itemTitleView.setOnClickListener {
                val bundle: Bundle = Bundle()
                bundle.putString("docId", data.docId)
                bundle.putString("email", data.email)
                bundle.putString("title", data.title)
                bundle.putString("date", data.date)
                bundle.putString("foodTime", data.foodTime)
                bundle.putString("food", data.food)
                bundle.putString("company", data.company)
                bundle.putString("userEmail", data.email)
                bundle.putString("where", data.where)
                bundle.putString("foodImage", data.foodImage)
                bundle.putString("memo", data.memo)
                bundle.putString("nickName", data.nickName)
                bundle.putStringArrayList("uriList", data.uriList)
                bundle.putString("bookmark", data.bookmark)

                Intent(context, PhotoDetailActivity::class.java).apply {
                    putExtras(bundle)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { context.startActivity(this) }
            }

            //스토리지 이미지 다운로드........................
            val imageRef = storage.reference.child("images/${data.docId}_0.jpg")
            imageRef.downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val multiOption = MultiTransformation(
                        CenterCrop(),
                        RoundedCorners(30)
                    );
                    Glide.with(context)
                        .load(task.result)
                        .apply(RequestOptions.bitmapTransform(multiOption))
                        .into(holder.binding.itemFoodImageView)

//                    Glide.with(context)
//                        .load(task.result)
//                        .apply(RequestOptions().override(120, 140).centerCrop())
//                        .into(holder.binding.itemFoodImageView)
                }

            }
        }
    }
}
