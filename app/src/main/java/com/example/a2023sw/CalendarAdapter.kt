package com.example.a2023sw

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.a2023sw.databinding.ItemCalendarBinding
import com.example.a2023sw.databinding.ItemPhotoBinding
import kotlinx.coroutines.NonDisposableHandle.parent

class CalendarViewHolder(val binding: ItemCalendarBinding) : RecyclerView.ViewHolder(binding.root)

class CalendarAdapter(val context: Context, val itemList: MutableList<ItemPhotoModel>) : RecyclerView.Adapter<CalendarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, ViewGroup: Int): CalendarViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CalendarViewHolder(ItemCalendarBinding.inflate(layoutInflater))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val data = itemList.get(position)

        // 여기서 이미지를 로드하여 ImageView에 표시
        // Glide, Picasso 등의 이미지 로딩 라이브러리를 사용할 수 있습니다.
        // 예: Glide.with(context).load(event.imagePath).into(holder.imageView)
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
            val imageRef = MyApplication.storage.reference.child("images/${data.docId}_0.jpg")
            imageRef.downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Glide.with(context)
                        .load(task.result)
                        .apply(RequestOptions().override(140, 140).centerCrop())
                        .into(holder.binding.itemFoodImageView)
                }

            }
        }
    }
}