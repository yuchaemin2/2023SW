package com.example.a2023sw

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.MyApplication.Companion.db
import com.example.a2023sw.MyApplication.Companion.storage
import com.example.a2023sw.databinding.ItemPhotoBinding
import com.google.firebase.firestore.FirebaseFirestore

class MyPhotoViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root)

class MyPhotoAdapter(val context: Context, val itemList: MutableList<ItemPhotoModel>): RecyclerView.Adapter<MyPhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPhotoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyPhotoViewHolder(ItemPhotoBinding.inflate(layoutInflater))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
    override fun onBindViewHolder(holder: MyPhotoViewHolder, position: Int) {
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

            itemFoodImageView.setOnClickListener {
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

                Intent(context, PhotoDetailActivity::class.java).apply {
                    putExtras(bundle)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { context.startActivity(this) }
            }

            //스토리지 이미지 다운로드........................
            val imageRef = storage.reference.child("images/${data.docId}_0.jpg")
            imageRef.downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Glide.with(context)
                        .load(task.result)
                        .apply(RequestOptions().override(120, 120).centerCrop())
                        .into(holder.binding.itemFoodImageView)
                }

            }
        }
    }
}
